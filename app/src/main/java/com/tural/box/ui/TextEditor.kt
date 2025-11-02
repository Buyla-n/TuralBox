package com.tural.box.ui

import android.content.Context
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tural.box.R
import com.tural.box.decoder.axml.AXMLPrinter
import com.tural.box.util.isAXMLFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TextEditor(
    context: Context,
    filePath: String,
    onBack: () -> Unit // 返回回调
) {
    val scope = rememberCoroutineScope()
    val file = remember { File(filePath) }
    val state = rememberTextFieldState()
    var showSaveDialog by remember { mutableStateOf(false) }

    fun saveFile() {
        scope.launch(Dispatchers.IO) {
            try {
                file.writeText(state.text.toString())
                scope.launch(Dispatchers.Main) {
                    Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show()
                }
            } catch (_: Exception) {
                false
            }
        }
    }

    LaunchedEffect(filePath) {
        scope.launch(Dispatchers.IO) {
            state.setTextAndPlaceCursorAtEnd(
                if (file.exists()) {
                    if (file.extension != "xml") {
                        file.readText()
                    } else {
                        if (isAXMLFile(file)) {
                            AXMLPrinter.print(file.path)
                        } else {
                            file.readText()
                        }
                    }
                } else {
                    "文件消失了, 这是一个警示标语, 如果你看见他请不要保存"
                }
            )
        }
    }

    BackHandler(
        enabled = state.undoState.canUndo
    ) {
        showSaveDialog = true
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(file.name, maxLines = 1, softWrap = false, overflow = TextOverflow.StartEllipsis) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (state.undoState.canUndo) {
                            showSaveDialog = true
                        } else {
                            (context as ComponentActivity).finish()
                        }
                    }) {
                        Icon(painterResource(R.drawable.outline_arrow_back_24), "返回")
                    }
                },
                actions = {
                    IconButton(enabled = state.undoState.canUndo, onClick = { state.undoState.undo() }) {
                        Icon(painter = painterResource(R.drawable.outline_undo_24), null)
                    }
                    IconButton(enabled = state.undoState.canRedo, onClick = { state.undoState.redo() }) {
                        Icon(painter = painterResource(R.drawable.outline_redo_24), null)
                    }
                    IconButton(
                        enabled = state.undoState.canUndo,
                        onClick = {
                        saveFile()
                        state.undoState.clearHistory()
                    }) {
                        Icon(painter = painterResource(R.drawable.outline_save_24), null)
                    }
                    IconButton(onClick = {  }) {
                        Icon(painter = painterResource(R.drawable.outline_more_vert_24), null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                )
            )
        },
        contentWindowInsets = WindowInsets(0,0,0,0)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn {
                item(key = 0) {
                    LazyRow {
                        item {
                            BasicTextField(
                                state = state,
                                modifier = Modifier
                                    .padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("未保存的更改") },
            text = { Text("是否要保存对文件的更改？") },
            confirmButton = {
                Button(
                    onClick = {
                        saveFile()
                        showSaveDialog = false
                        onBack()
                    }
                ) {
                    Text("保存")
                }
            },
            dismissButton = {
                FilledTonalButton(
                    onClick = {
                        showSaveDialog = false
                    }
                ) {
                    Text("取消")
                }

                FilledTonalButton(
                    onClick = {
                        showSaveDialog = false
                        onBack()
                    }
                ) {
                    Text("退出")
                }
            }
        )
    }
}

enum class TextType {
    TEXT,
    AXML
}