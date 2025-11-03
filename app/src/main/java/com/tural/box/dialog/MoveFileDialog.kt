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
import com.tural.box.util.moveFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.nio.file.Path

@Composable
fun MoveFileDialog(
    dialogManager: DialogManager,
    targetFile: File,
    scope: CoroutineScope,
    targetPath: Path,
    onRefresh: () -> Unit
) {
    var progress by remember { mutableStateOf<FileChangeProgress?>(null) }
    var loadingType by remember { mutableStateOf(FileProcessType.NONE) }
    AlertDialog(
        modifier = Modifier.width(560.dp),
        onDismissRequest = { dialogManager.showMove = false },
        title = { Text("确认移动") },
        text = {
            Column {
                Text("是否移动 ${targetFile.name} 到 $targetPath ?")

                Spacer(Modifier.height(8.dp))

                when (loadingType) {
                    FileProcessType.FAIL -> Text(
                        "移动失败",
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
                                    dialogManager.showMove = false
                                    onRefresh()
                                } else {
                                    Text(
                                        "部分移动失败",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    println(loadingType)
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

                        loadingType = FileProcessType.FILE
                        val result = moveFile(
                            targetFile,
                            File("$targetPath/${targetFile.name}")
                        )
                        if (result) {
                            dialogManager.showMove = false
                            onRefresh()
                        } else {
                            loadingType = FileProcessType.FAIL
                        }

                    }
                }
            ) {
                Text("移动")
            }
        },
        dismissButton = {
            FilledTonalButton(
                onClick = {
                    dialogManager.showMove = false
                }
            ) {
                Text("取消")
            }
        }
    )
}