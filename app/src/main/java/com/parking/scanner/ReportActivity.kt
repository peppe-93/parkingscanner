package com.parking.scanner

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import android.widget.Toast
import android.widget.TextView

class ReportActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReportFileAdapter
    private lateinit var emptyView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Report sessioni"

        recyclerView = findViewById(R.id.recyclerViewReports)
        emptyView = TextView(this).apply {
            text = "Nessun report di sessione trovato.\nEffettua una scansione e termina la sessione per generare un report."
            textSize = 16f
            setPadding(24, 80, 24, 24)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)

        val dir = getExternalFilesDir(null)
        val reportFiles = dir?.listFiles { file -> file.name.startsWith("report_") && file.name.endsWith(".txt") }?.sortedByDescending { it.lastModified() } ?: emptyList()

        if (reportFiles.isEmpty()) {
            setContentView(emptyView)
        } else {
            adapter = ReportFileAdapter(reportFiles) { file ->
                openReport(file)
            }
            recyclerView.adapter = adapter
        }
    }

    private fun openReport(file: File) {
        try {
            val intent = Intent(this, ReportFileViewerActivity::class.java)
            intent.putExtra("reportFilePath", file.absolutePath)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Impossibile aprire il report", Toast.LENGTH_LONG).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

class ReportFileAdapter(private val data: List<File>, private val onClick: (File) -> Unit) : RecyclerView.Adapter<ReportFileViewHolder>() {
    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ReportFileViewHolder {
        val view = android.view.LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false)
        return ReportFileViewHolder(view, onClick)
    }
    override fun getItemCount() = data.size
    override fun onBindViewHolder(holder: ReportFileViewHolder, position: Int) {
        holder.bind(data[position])
    }
}
class ReportFileViewHolder(view: android.view.View, private val onClick: (File) -> Unit) : RecyclerView.ViewHolder(view) {
    private val title = view.findViewById<android.widget.TextView>(android.R.id.text1)
    private val subtitle = view.findViewById<android.widget.TextView>(android.R.id.text2)
    private var reportFile: File? = null
    init {
        view.setOnClickListener { reportFile?.let(onClick) }
    }
    fun bind(file: File) {
        reportFile = file
        title.text = file.name
        subtitle.text = "Ultima modifica: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(java.util.Date(file.lastModified()))}"
    }
}
