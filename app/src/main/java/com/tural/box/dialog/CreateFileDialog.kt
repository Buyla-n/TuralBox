package com.tural.box.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import com.tural.box.util.createFile
import com.tural.box.util.createFolder
import com.tural.box.util.invalidChars
import com.tural.box.util.refresh
import java.nio.file.Path

@Composable
fun CreateFileDialog(
    dialogManager: DialogManager,
    targetPath: Path,
    onRefresh: (String) -> Unit
) {
    var fileName by remember { mutableStateOf("") }
    var createFail by remember { mutableStateOf(false) }

    fun createFile(isFolder: Boolean, name: String) {
        val creator = if (isFolder) createFolder(targetPath, name) else createFile(targetPath, name)
        if (!creator) createFail = true else {
            dialogManager.showCreateFile = false
            onRefresh(name)
        }
    }

    AlertDialog(
        onDismissRequest = { dialogManager.showCreateFile = false },
        title = { Text("新建") },
        text = {
            val hasInvalidChar = remember(fileName) {
                fileName.any { it in invalidChars }
            }
            val isEmpty = fileName.isBlank()
            val isValid = !hasInvalidChar && !isEmpty && !createFail
            val focusRequester = remember { FocusRequester() }

            TextField(
                value = fileName,
                onValueChange = {
                    fileName = it
                    createFail = false
                },
                shape = MaterialTheme.shapes.small,
                isError = !isValid,
                supportingText = {
                    when {
                        isEmpty -> Text(
                            "文件名不能为空",
                            color = MaterialTheme.colorScheme.error
                        )

                        hasInvalidChar -> Text(
                            "不能包含: ${invalidChars.joinToString("")}",
                            color = MaterialTheme.colorScheme.error
                        )

                        createFail -> Text(
                            "创建失败",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                singleLine = true,
                modifier = Modifier.focusRequester(focusRequester)
            )

            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    createFile(false, fileName)
                }
            ) {
                Text("文件")
            }
            Button(
                onClick = {
                    createFile(true, fileName)
                }
            ) {
                Text("文件夹")
            }
        },
        dismissButton = {
            FilledTonalButton(
                onClick = { dialogManager.showCreateFile = false }
            ) {
                Text(" 取消 ")
            }
        }
    )
}