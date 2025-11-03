package com.tural.box.ui.screen.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.tural.box.model.SortOrder
import com.tural.box.util.RootPath
import java.io.File
import kotlin.io.path.Path

class PanelStates {
    var path by mutableStateOf(Path(RootPath))
    var highLightFiles by mutableStateOf(emptySet<String>())

    var files by mutableStateOf(emptyList<File>())
    var sortOrder by mutableStateOf(SortOrder.NAME)
    var isInZip: Boolean = false
    var previousPath = Path(RootPath)
    var zipFile: File? = null
}