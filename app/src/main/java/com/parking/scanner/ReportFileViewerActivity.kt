package com.parking.scanner

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import java.io.File
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider

class ReportFileViewerActivity : AppCompatActivity() {
    private lateinit var shareButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = android.widget.LinearLayout(this)
        layout.orientation = android.widget.LinearLayout.VERTICAL
        val textView = TextView(this)
        textView.setPadding(16, 16, 16, 16)
        shareButton = Button(this)
        shareButton.text = "Condividi/Salva Report"
        layout.addView(textView)
        layout.addView(shareButton)
        setContentView(layout)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Visualizza report"
        val path = intent.getStringExtra("reportFilePath")
        if (path != null) {
            try {
                val file = File(path)
                val contents = file.readText()
                textView.text = contents
                shareButton.setOnClickListener {
                    val uri: Uri = FileProvider.getUriForFile(
                        this,
                        "com.parking.scanner.provider",
                        file
                    )
                    val shareIntent = Intent(Intent.ACTION_SEND)
                    shareIntent.type = "text/plain"
                    shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    shareIntent.putExtra(Intent.EXTRA_TEXT, contents)
                    startActivity(Intent.createChooser(shareIntent, "Condividi report"))
                }
            } catch (e: Exception) {
                textView.text = "Errore lettura report."
                shareButton.visibility = android.view.View.GONE
            }
        } else {
            textView.text = "File report non trovato."
            shareButton.visibility = android.view.View.GONE
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
