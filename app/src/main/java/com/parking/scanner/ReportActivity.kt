// app/src/main/java/com/parking/scanner/ReportActivity.kt

package com.parking.scanner

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
        val reportFiles = dir?.listFiles { file -> file.name.startsWith("report_") && file.name.endsWith(".txt") }
            ?.sortedByDescending { it.lastModified() } ?: emptyList()

        recyclerView.adapter = ReportFileAdapter(reportFiles)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

class ReportFileAdapter(private val files: List<File>) : RecyclerView.Adapter<ReportFileViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ReportFileViewHolder(LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false))

    override fun getItemCount() = files.size

    override fun onBindViewHolder(holder: ReportFileViewHolder, position: Int) {
        holder.bind(files[position])
    }
}

class ReportFileViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val title = view.findViewById<TextView>(android.R.id.text1)
    private val subtitle = view.findViewById<TextView>(android.R.id.text2)
    private var expanded = false
    private val contentView = TextView(view.context).apply {
        setPadding(42, 0, 16, 16)
        visibility = View.GONE
        textSize = 14f
        typeface = Typeface.MONOSPACE
    }

    init {
        (view as ViewGroup).addView(contentView)
        view.setOnClickListener {
            expanded = !expanded
            contentView.visibility = if (expanded) View.VISIBLE else View.GONE
        }
    }

    fun bind(file: File) {
        title.text = file.name
        subtitle.text = "Ultima modifica: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(file.lastModified()))}"
        contentView.text = file.readText()
        contentView.visibility = if (expanded) View.VISIBLE else View.GONE
    }
}
