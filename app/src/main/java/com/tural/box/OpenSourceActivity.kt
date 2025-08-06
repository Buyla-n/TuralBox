package com.tural.box

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.tural.box.ui.OpenSourceScreen
import com.tural.box.ui.theme.TuralBoxTheme

class OpenSourceActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.isNavigationBarContrastEnforced = false
        setContent {
            TuralBoxTheme {
                OpenSourceScreen(onBackPressed = { finish() })
            }
        }
    }
}