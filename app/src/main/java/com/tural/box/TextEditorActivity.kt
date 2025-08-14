package com.tural.box

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.tural.box.ui.TextEditor
import com.tural.box.ui.theme.TuralBoxTheme

class TextEditorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.isNavigationBarContrastEnforced = false

        val filePath = intent.getStringExtra("filePath") ?: ""

        if (filePath.isEmpty()) {
            Toast.makeText(this, "文件不存在", Toast.LENGTH_LONG).show()
            this.finish()
        }
        setContent {
            TuralBoxTheme {
                TextEditor(this, filePath) { this.finish() }
            }
        }
    }
}