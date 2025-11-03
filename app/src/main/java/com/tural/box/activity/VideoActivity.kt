package com.tural.box.activity

import android.os.Bundle
import android.view.WindowInsetsController
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.tural.box.ui.screen.videoviewer.VideoViewer
import com.tural.box.ui.theme.TuralBoxTheme

class VideoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.isNavigationBarContrastEnforced = false
        window.insetsController?.setSystemBarsAppearance(
            0,
            WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
        )

        val filePath = intent.getStringExtra("filePath") ?: ""

        if (filePath.isEmpty()) {
            Toast.makeText(this, "视频不存在", Toast.LENGTH_LONG).show()
            this.finish()
        }

        setContent {
            TuralBoxTheme {
                VideoViewer(this, filePath)
            }
        }
    }
}