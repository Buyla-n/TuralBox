package com.tural.box.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tural.box.model.FileProcessType
import com.tural.box.util.FileChangeProgress
import com.tural.box.util.deleteFile
import com.tural.box.util.deleteFolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun DeleteFileDialog(
    dialogManager: DialogManager,
    targetFile: File,
    scope: CoroutineScope,
    onRefresh: () -> Unit
) {
    var progress by remember { mutableStateOf<FileChangeProgress?>(null) }
    var loadingType by remember { mutableStateOf(FileProcessType.NONE) }
    AlertDialog(
        modifier = Modifier.width(560.dp),
        onDismissRequest = { dialogManager.showDelete = false },
        title = { Text("确认删除") },
        text = {
            Column {
                Text("是否删除 ${targetFile.name} ?")

                Spacer(Modifier.height(8.dp))

                when (loadingType) {
                    FileProcessType.FAIL -> Text(
                        "删除失败",
                        color = MaterialTheme.colorScheme.error
                    )
                    FileProcessType.NONE -> Text(
                        "文件将永久丢失",
                        color = MaterialTheme.colorScheme.error
                    )
                    FileProcessType.FILE -> LinearProgressIndicator(modifier = Modifier)
                    FileProcessType.DIRECTORY -> {
                        when (val current = progress) {
                            is FileChangeProgress.InProgress -> {

                                LinearProgressIndicator(
                                    progress = { current.percentage / 100f },
                                    modifier = Modifier
                                )

                                Spacer(Modifier.height(8.dp))
                                Text("进度: ${current.percentage}%")
                                Text("已处理: ${current.current}/${current.total}")
                                if (current.failedCount > 0) {
                                    loadingType = FileProcessType.PART_FAIL
                                    Text("失败: ${current.failedCount}", color = Color.Red)
                                }
                            }

                            is FileChangeProgress.Error -> {
                                loadingType = FileProcessType.FAIL
                            }

                            is FileChangeProgress.Completed -> {
                                if (current.isAllSuccess) {
                                    dialogManager.showDelete = false
                                    onRefresh()
                                } else {
                                    Text(
                                        "部分删除失败",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                            null -> {}
                        }
                    }
                    else -> {}
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    scope.launch(Dispatchers.IO) {
                        if (targetFile.isDirectory) {
                            loadingType = FileProcessType.DIRECTORY
                            deleteFolder(targetFile)
                                .catch { e ->
                                    loadingType = FileProcessType.FAIL
                                    progress = FileChangeProgress.Error(
                                        e as Exception,
                                        targetFile
                                    )
                                }
                                .collect { update ->
                                    progress = update
                                }
                        } else {
                            loadingType = FileProcessType.FILE
                            val result = deleteFile(targetFile)
                            if (result) {
                                dialogManager.showDelete = false
                                onRefresh()
                            } else {
                                loadingType = FileProcessType.FAIL
                            }
                        }
                    }
                }
            ) {
                Text("删除")
            }
        },
        dismissButton = {
            FilledTonalButton(
                onClick = {
                    dialogManager.showDelete = false
                }
            ) {
                Text("取消")
            }
        }
    )
}