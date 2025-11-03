package com.tural.box.dialog

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.io.File

class DialogManager {
    //convenient
    var showTool by mutableStateOf(false)
    var showSort by mutableStateOf(false)
    var showAppDetail by mutableStateOf(false)
    var showOpenChooser by mutableStateOf(false)
    var showCopy by mutableStateOf(false)
    var showMove by mutableStateOf(false)
    var showDelete by mutableStateOf(false)
    var showRename by mutableStateOf(false)
    var showPath by mutableStateOf(false)
    var showCreateFile by mutableStateOf(false)
    var showSearch by mutableStateOf(false)
    var showAbout by mutableStateOf(false)
    var showAudio by mutableStateOf(false)
    var showProperties by mutableStateOf(false)

    var currentFile: File? by mutableStateOf(null)
}