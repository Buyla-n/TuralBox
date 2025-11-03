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
import com.tural.box.util.copyFile
import com.tural.box.util.copyFolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.io.File
import java.nio.file.Path

@Composable
fun CopyFileDialog(
    dialogManager: DialogManager,
    targetFile: File,
    scope: CoroutineScope,
    targetPath: Path,
    onRefresh: () -> Unit
) {
    var progress by remember { mutableStateOf<FileChangeProgress?>(null) }
    var loadingType by remember { mutableStateOf(FileProcessType.NONE) }

    fun onCopy() {
        scope.launch(Dispatchers.IO) {
            if (targetFile.isDirectory) {
                loadingType = FileProcessType.DIRECTORY
                copyFolder(
                    targetFile,
                    File("$targetPath/${targetFile.name}")
                )
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
                val result = copyFile(
                    targetFile,
                    File("$targetPath/${targetFile.name}")
                )
                if (result) {
                    dialogManager.showCopy = false
                    onRefresh()
                } else {
                    loadingType = FileProcessType.FAIL
                }
            }
        }
    }

    AlertDialog(
        modifier = Modifier.width(560.dp),
        onDismissRequest = { dialogManager.showCopy = false },
        title = { Text("确认复制") },
        text = {
            Column {
                Text("是否复制 ${targetFile.name} 到 $targetPath ?")

                Spacer(Modifier.height(8.dp))

                when (loadingType) {
                    FileProcessType.FAIL -> Text(
                        "复制失败",
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
                                    dialogManager.showCopy = false
                                    onRefresh()
                                } else {
                                    Text(
                                        "部分复制失败",
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
                    onCopy()
                }
            ) {
                Text("复制")
            }
        },
        dismissButton = {
            FilledTonalButton(
                onClick = {
                    dialogManager.showCopy = false
                }
            ) {
                Text("取消")
            }
        }
    )
}