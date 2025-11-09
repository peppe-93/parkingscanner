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

class ReportActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Report sessioni"

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewReports)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val dir = getExternalFilesDir(null)
        val reportFiles = dir?.listFiles { file -> 
            file.name.startsWith("report_") && file.name.endsWith(".txt") 
        }?.sortedByDescending { it.lastModified() } ?: emptyList()

        recyclerView.adapter = ReportFileAdapter(reportFiles)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

class ReportFileAdapter(private val files: List<File>) : RecyclerView.Adapter<ReportFileViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ReportFileViewHolder(
            LayoutInflater.from(parent.context).inflate(
                android.R.layout.simple_list_item_2, 
                parent, 
                false
            )
        )

    override fun getItemCount() = files.size

    override fun onBindViewHolder(holder: ReportFileViewHolder, position: Int) {
        holder.bind(files[position])
    }
}

class ReportFileViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val title = view.findViewById<TextView>(android.R.id.text1)
    private val subtitle = view.findViewById<TextView>(android.R.id.text2)

    init {
        itemView.setOnClickListener {  // ← usa itemView invece di view
            val file = itemView.tag as? File ?: return@setOnClickListener  // ← usa itemView
            showReportDialog(file)
        }
    }

    fun bind(file: File) {
        itemView.tag = file  // ← usa itemView invece di view
        title.text = file.name
        subtitle.text = "Ultima modifica: ${
            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                .format(Date(file.lastModified()))
        }"
    }

    private fun showReportDialog(file: File) {
    val context = itemView.context
    val content = try {
        file.readText()
    } catch (e: Exception) {
        "Errore lettura file: ${e.message}"
    }

    val textView = TextView(context).apply {
        text = content
        setPadding(32, 32, 32, 32)
        textSize = 14f
        typeface = Typeface.MONOSPACE
    }

    // Avvolgi il TextView in uno ScrollView
    val scrollView = android.widget.ScrollView(context).apply {
        addView(textView)
    }

    AlertDialog.Builder(context)
        .setTitle("Report Dettagliato")
        .setView(scrollView)  // ← Usa scrollView invece di textView
        .setPositiveButton("Chiudi") { dialog, _ -> dialog.dismiss() }
        .show()
}
}
