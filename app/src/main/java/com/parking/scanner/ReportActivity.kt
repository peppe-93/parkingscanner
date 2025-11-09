package com.parking.scanner

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.parking.scanner.db.ParkingDatabase
import com.parking.scanner.db.ParkingTicketEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.widget.Button
import java.io.File
import java.io.FileWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ReportActivity : AppCompatActivity() {
    private lateinit var database: ParkingDatabase
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CompanyReportAdapter
    private lateinit var btnExport: Button
    private var reportData: List<CompanyReport> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Report Scansioni"

        database = ParkingDatabase.getDatabase(this)
        recyclerView = findViewById(R.id.recyclerViewReports)
        btnExport = findViewById(R.id.btnExportReport)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = CompanyReportAdapter()
        recyclerView.adapter = adapter

        loadReports()
        btnExport.setOnClickListener {
            exportReportToCsv()
        }
    }

    private fun loadReports() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val tickets = database.parkingTicketDao().getAllTickets()
                // Aggrega per azienda
                val companyGroups = tickets.groupBy { it.companyName }
                val reports = companyGroups.map { (company, list) ->
                    val doubleCount = list.count { it.isDouble }
                    val normalCount = list.size - doubleCount
                    val totalCount = normalCount + (doubleCount * 2)
                    CompanyReport(company, normalCount, doubleCount, totalCount)
                }
                withContext(Dispatchers.Main) {
                    reportData = reports
                    adapter.submitList(reports)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ReportActivity, "Errore caricamento report", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun exportReportToCsv() {
        try {
            val fileName = "parking_report_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv"
            val file = File(getExternalFilesDir(null), fileName)
            FileWriter(file).use { writer ->
                writer.append("Azienda,Posti Occupati,Doppi Posti,Totale\n")
                for (report in reportData) {
                    writer.append("${report.company},${report.normalCount},${report.doubleCount},${report.totalCount}\n")
                }
            }
            Toast.makeText(this, "Report esportato in ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Errore esportazione report", Toast.LENGTH_LONG).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

// Dati per la visualizzazione report
data class CompanyReport(val company: String, val normalCount: Int, val doubleCount: Int, val totalCount: Int)

// Adapter minimal per la lista delle aziende
class CompanyReportAdapter : RecyclerView.Adapter<CompanyReportViewHolder>() {
    private var items: List<CompanyReport> = emptyList()
    fun submitList(data: List<CompanyReport>) {
        items = data
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): CompanyReportViewHolder {
        val view = android.view.LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false)
        return CompanyReportViewHolder(view)
    }
    override fun getItemCount() = items.size
    override fun onBindViewHolder(holder: CompanyReportViewHolder, position: Int) {
        holder.bind(items[position])
    }
}
class CompanyReportViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
    private val title = view.findViewById<android.widget.TextView>(android.R.id.text1)
    private val subtitle = view.findViewById<android.widget.TextView>(android.R.id.text2)
    fun bind(report: CompanyReport) {
        title.text = "${report.company} - Totale ${report.totalCount}"
        subtitle.text = "Normali: ${report.normalCount} | Doppi: ${report.doubleCount}"
    }
}
