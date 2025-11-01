package com.tural.box

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.tural.box.ui.Terminal
import com.tural.box.ui.theme.TuralBoxTheme

class TerminalActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.isNavigationBarContrastEnforced = false

        val filePath = intent.getStringExtra("filePath") ?: ""

        setContent {
            TuralBoxTheme {
                Terminal(filePath)
            }
        }
    }
}