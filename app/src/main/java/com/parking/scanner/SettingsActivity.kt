package com.parking.scanner

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Crea un layout semplice programmaticamente
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
        }
        
        val titleText = TextView(this).apply {
            text = "Impostazioni"
            textSize = 24f
            setPadding(0, 0, 0, 32)
        }
        
        val infoText = TextView(this).apply {
            text = "Funzionalità impostazioni in sviluppo.\n\nQui potrai configurare:\n- Server URL\n- Autorizzazioni aree\n- Preferenze scanner\n- Altro"
            textSize = 16f
        }
        
        layout.addView(titleText)
        layout.addView(infoText)
        
        setContentView(layout)
        
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Impostazioni"
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
