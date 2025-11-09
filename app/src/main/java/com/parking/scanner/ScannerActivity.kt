package com.parking.scanner

import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraProvider
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.parking.scanner.databinding.ActivityScannerBinding
import com.parking.scanner.db.ParkingDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

class ScannerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScannerBinding
    private lateinit var cameraProvider: ProcessCameraProvider
    private val barcodeScanner = BarcodeScanning.getClient()
    private val db by lazy { ParkingDatabase.getDatabase(this) }
    private val dao by lazy { db.parkingTicketDao() }

    private val scannedCodes = mutableMapOf<String, Int>()
    private var isDoubleSpotMode = false
    private var lastScanTime = 0L
    private val SCAN_COOLDOWN = 2000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupCamera()
    }

    private fun setupUI() {
        binding.btnDoubleSpot.setOnClickListener {
            toggleDoubleSpot()
        }

        binding.btnEndSession.setOnClickListener {
            endSession()
        }

        updateStats()
    }

    private fun toggleDoubleSpot() {
        isDoubleSpotMode = !isDoubleSpotMode
        binding.btnDoubleSpot.isSelected = isDoubleSpotMode
        vibrate(100)
    }

    @OptIn(ExperimentalGetImage::class)
    private fun setupCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                        processImage(imageProxy)
                    }
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer
                )
            } catch (exc: Exception) {
                Toast.makeText(this, "Errore camera", Toast.LENGTH_SHORT).show()
            }

        }, ContextCompat.getMainExecutor(this))
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImage(imageProxy: androidx.camera.core.ImageProxy) {
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastScanTime < SCAN_COOLDOWN) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image ?: run {
            imageProxy.close()
            return
        }

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        barcodeScanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    barcode.rawValue?.let { qrCode ->
                        lastScanTime = currentTime
                        handleQrCodeScanned(qrCode)
                    }
                }
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    private fun handleQrCodeScanned(qrCode: String) {
        lifecycleScope.launch {
            val ticket = dao.findById(qrCode)

            when {
                ticket == null -> {
                    showError("QR Code non valido")
                    //playSound(R.raw.error_beep)
                    vibrate(500)
                }

                scannedCodes.containsKey(qrCode) -> {
                    showWarning("Già scansionato: ${ticket.companyName}")
                    //playSound(R.raw.warning_beep)
                    vibrate(200)
                }

                else -> {
                    val spots = if (isDoubleSpotMode) 2 else 1
                    scannedCodes[qrCode] = spots
                    showSuccess("${ticket.companyName} - $spots posto/i")
                    //playSound(R.raw.success_beep)
                    vibrate(100)
                    updateStats()
                    resetDoubleSpot()
                }
            }
        }
    }

    private fun showSuccess(message: String) {
        binding.tvStatus.apply {
            text = "✓ VALIDO"
            setTextColor(getColor(android.R.color.holo_green_dark))
        }
        binding.tvInfo.text = message
    }

    private fun showWarning(message: String) {
        binding.tvStatus.apply {
            text = "⚠ DUPLICATO"
            setTextColor(getColor(android.R.color.holo_orange_dark))
        }
        binding.tvInfo.text = message
    }

    private fun showError(message: String) {
        binding.tvStatus.apply {
            text = "✗ ERRORE"
            setTextColor(getColor(android.R.color.holo_red_dark))
        }
        binding.tvInfo.text = message
    }

    private fun updateStats() {
        var totalSpots = 0
        scannedCodes.forEach { (_, spots) ->
            totalSpots += spots
        }
        binding.tvScansCount.text = scannedCodes.size.toString()
        binding.tvSpotsCount.text = totalSpots.toString()
    }

    private fun resetDoubleSpot() {
        isDoubleSpotMode = false
        binding.btnDoubleSpot.isSelected = false
    }

    private fun endSession() {
        if (scannedCodes.isEmpty()) {
            Toast.makeText(this, "Nessun cartellino scansionato", Toast.LENGTH_SHORT).show()
            return
        }

        val totalSpots = scannedCodes.values.sum()
        val message = "Scansionati: ${scannedCodes.size} QR\nPosti totali: $totalSpots\n\nTerminare sessione?"

        android.app.AlertDialog.Builder(this)
            .setTitle("Fine Sessione")
            .setMessage(message)
            .setPositiveButton("Sì") { _, _ ->
                generateReport()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun generateReport() {
    lifecycleScope.launch {
        // Raggruppa per azienda
        val companiesData = mutableMapOf<String, MutableList<Pair<String, Int>>>()
        
        scannedCodes.forEach { (qrCode, spots) ->
            val ticket = dao.findById(qrCode)
            val companyName = ticket?.companyName ?: "Sconosciuto"
            if (!companiesData.containsKey(companyName)) {
                companiesData[companyName] = mutableListOf()
            }
            companiesData[companyName]?.add(Pair(qrCode, spots))
        }

        val report = buildString {
            appendLine("REPORT SESSIONE PARCHEGGIO")
            appendLine("Data: ${java.time.LocalDateTime.now()}")
            appendLine("=" .repeat(40))
            appendLine()
            
            // Riepilogo generale
            val totalScanned = scannedCodes.size
            val totalSpots = scannedCodes.values.sum()
            val totalDouble = scannedCodes.values.count { it == 2 }
            
            appendLine("RIEPILOGO GENERALE:")
            appendLine("QR Code Scansionati: $totalScanned")
            appendLine("Doppi Posti: $totalDouble")
            appendLine("Posti Totali Occupati: $totalSpots")
            appendLine()
            appendLine("=" .repeat(40))
            appendLine()
            
            // Dettaglio per azienda
            appendLine("DETTAGLIO PER AZIENDA:")
            appendLine()
            
            companiesData.forEach { (company, tickets) ->
                val companyScanned = tickets.size
                val companySpots = tickets.sumOf { it.second }
                val companyDouble = tickets.count { it.second == 2 }
                
                appendLine("▶ $company")
                appendLine("  QR Scansionati: $companyScanned")
                appendLine("  Doppi Posti: $companyDouble")
                appendLine("  Posti Occupati: $companySpots")
                appendLine()
            }
            
            appendLine("=" .repeat(40))
            appendLine()
            appendLine("DETTAGLIO COMPLETO:")
            scannedCodes.forEach { (qrCode, spots) ->
                val ticket = dao.findById(qrCode)
                val companyName = ticket?.companyName ?: "Sconosciuto"
                val spotType = if (spots == 2) "DOPPIO" else "NORMALE"
                appendLine("$qrCode - $companyName - $spots posto/i ($spotType)")
            }
        }

        val fileName = "report_${System.currentTimeMillis()}.txt"
        val file = java.io.File(getExternalFilesDir(null), fileName)
        file.writeText(report)

        withContext(Dispatchers.Main) {
            Toast.makeText(this@ScannerActivity, "Report salvato: ${file.name}", Toast.LENGTH_LONG).show()
            scannedCodes.clear()
            updateStats()
            finish()
        }
    }
}
