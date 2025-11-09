package com.parking.scanner

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.parking.scanner.utils.CsvManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class SettingsActivity : AppCompatActivity() {
    
    private lateinit var csvManager: CsvManager
    private lateinit var statusText: TextView
    
    // Launcher per selezionare file CSV
    private val pickCsvLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                loadCsvFromUri(uri)
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        csvManager = CsvManager(this)
        
        // Crea layout programmaticamente
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
        }
        
        val titleText = TextView(this).apply {
            text = "Impostazioni"
            textSize = 24f
            setPadding(0, 0, 0, 32)
        }
        
        val importButton = Button(this).apply {
            text = "Carica CSV Parcheggi"
            textSize = 16f
            setPadding(24, 24, 24, 24)
            setOnClickListener {
                openFilePicker()
            }
        }
        
        statusText = TextView(this).apply {
            text = "Nessun file caricato"
            textSize = 14f
            setPadding(0, 24, 0, 24)
        }
        
        val infoText = TextView(this).apply {
            text = "Il file CSV deve contenere le colonne:\n" +
                   "- ID\n" +
                   "- COMPANY_NAME\n" +
                   "- COLOR\n" +
                   "- TIMESTAMP\n\n" +
                   "Altre funzionalità in sviluppo."
            textSize = 14f
        }
        
        layout.addView(titleText)
        layout.addView(importButton)
        layout.addView(statusText)
        layout.addView(infoText)
        
        setContentView(layout)
        
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Impostazioni"
        
        // Mostra statistiche attuali
        loadStats()
    }
    
    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("text/csv", "text/comma-separated-values"))
        }
        pickCsvLauncher.launch(intent)
    }
    
    private fun loadCsvFromUri(uri: android.net.Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Copia il file in una directory temporanea
                val inputStream = contentResolver.openInputStream(uri)
                val tempFile = File(cacheDir, "temp_import.csv")
                
                inputStream?.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }
                
                // Carica il CSV nel database
                val result = csvManager.loadCsvFromFile(tempFile)
                
                withContext(Dispatchers.Main) {
                    result.onSuccess { count ->
                        statusText.text = "Caricati $count record con successo!"
                        Toast.makeText(
                            this@SettingsActivity,
                            "CSV caricato: $count parcheggi",
                            Toast.LENGTH_LONG
                        ).show()
                    }.onFailure { error ->
                        statusText.text = "Errore nel caricamento: ${error.message}"
                        Toast.makeText(
                            this@SettingsActivity,
                            "Errore: ${error.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                
                // Pulisci il file temporaneo
                tempFile.delete()
                
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    statusText.text = "Errore: ${e.message}"
                    Toast.makeText(
                        this@SettingsActivity,
                        "Errore lettura file: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
    
    private fun loadStats() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val tickets = csvManager.getTickets()
                val companies = csvManager.getCompanies()
                
                withContext(Dispatchers.Main) {
                    statusText.text = "Database: ${tickets.size} parcheggi, ${companies.size} aziende"
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
