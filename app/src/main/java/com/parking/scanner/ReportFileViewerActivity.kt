package com.parking.scanner

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class ReportFileViewerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val textView = TextView(this)
        textView.setPadding(16, 16, 16, 16)
        setContentView(textView)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Visualizza report"
        val path = intent.getStringExtra("reportFilePath")
        if (path != null) {
            try {
                val contents = File(path).readText()
                textView.text = contents
            } catch (e: Exception) {
                textView.text = "Errore lettura report."
            }
        } else {
            textView.text = "File report non trovato."
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
