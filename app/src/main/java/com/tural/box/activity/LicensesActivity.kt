package com.tural.box.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.tural.box.ui.screen.about.AboutLibrariesScreen
import com.tural.box.ui.theme.TuralBoxTheme

class LicensesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.isNavigationBarContrastEnforced = false
        setContent {
            TuralBoxTheme {
                AboutLibrariesScreen(onBackPressed = { finish() })
            }
        }
    }
}