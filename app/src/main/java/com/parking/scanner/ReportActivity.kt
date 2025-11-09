package com.parking.scanner

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.parking.scanner.db.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReportActivity : AppCompatActivity() {
    
    private lateinit var database: AppDatabase
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Per ora mostra solo un messaggio
        // TODO: implementare il layout activity_report.xml e RecyclerView
        setContentView(android.R.layout.simple_list_item_1)
        
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Report Scansioni"
        
        database = AppDatabase.getDatabase(this)
        
        loadReports()
    }
    
    private fun loadReports() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val scans = database.scanDao().getAllScans()
                withContext(Dispatchers.Main) {
                    // TODO: mostrare i dati in una RecyclerView
                    // Per ora logga il numero di scansioni
                    println("Scansioni trovate: ${scans.size}")
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
