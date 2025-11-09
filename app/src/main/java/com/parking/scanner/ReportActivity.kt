package com.parking.scanner

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

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
