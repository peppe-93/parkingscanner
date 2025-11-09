package com.parking.scanner

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.parking.scanner.db.ParkingDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReportActivity : AppCompatActivity() {
    
    private lateinit var database: ParkingDatabase
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Per ora mostra solo un messaggio
        // TODO: implementare il layout activity_report.xml e RecyclerView
        setContentView(android.R.layout.simple_list_item_1)
        
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Report Scansioni"
        
        database = ParkingDatabase.getDatabase(this)
        
        loadReports()
    }
    
    private fun loadReports() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val tickets = database.parkingTicketDao().getAllTickets()
                withContext(Dispatchers.Main) {
                    // TODO: mostrare i dati in una RecyclerView
                    // Per ora logga il numero di ticket
                    println("Ticket trovati: ${tickets.size}")
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
