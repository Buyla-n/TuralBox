package com.tural.box.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.io.File
import kotlin.io.path.Path

class PanelStates {
    var path by mutableStateOf(Path(RootPath))
    var highLightFiles by mutableStateOf(emptyList<String>())
    var files by mutableStateOf(emptyList<File>())
    var sortOrder by mutableStateOf(SortOrder.NAME)
}