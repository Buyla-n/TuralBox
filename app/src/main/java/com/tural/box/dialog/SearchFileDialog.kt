package com.tural.box.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import com.tural.box.ui.screen.main.FileRow
import com.tural.box.util.getFileType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

@Composable
fun SearchFileDialog(
    dialogManager: DialogManager,
    targetPath: Path,
    onFileClick: (File) -> Unit
) {
    var searchFileName by remember { mutableStateOf("") }
    val found = remember { mutableStateListOf<File>() }
    var searchProgress by remember { mutableFloatStateOf(0f) }
    var processedFiles by remember { mutableIntStateOf(0) }
    var isSearching by remember { mutableStateOf(false) }
    var includeSubdirectories by remember { mutableStateOf(true) }

    LaunchedEffect(isSearching) {
        withContext(Dispatchers.IO) {
            if (isSearching) {
                found.clear()
                var totalFiles = 0L

                if (includeSubdirectories) {
                    Files.walkFileTree(targetPath, object : SimpleFileVisitor<Path>() {
                        override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                            return try {
                                FileVisitResult.CONTINUE
                            } catch (_: AccessDeniedException) {
                                FileVisitResult.SKIP_SUBTREE
                            } catch (_: SecurityException) {
                                FileVisitResult.SKIP_SUBTREE
                            }
                        }

                        override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                            totalFiles++
                            return FileVisitResult.CONTINUE
                        }

                        override fun visitFileFailed(file: Path, exc: IOException): FileVisitResult {
                            return FileVisitResult.SKIP_SUBTREE
                        }
                    })
                } else {
                    Files.list(targetPath).use { stream ->
                        totalFiles = stream.count()
                    }
                }

                if (includeSubdirectories) {
                    Files.walkFileTree(targetPath, object : SimpleFileVisitor<Path>() {
                        override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                            return try {
                                FileVisitResult.CONTINUE
                            } catch (_: AccessDeniedException) {
                                FileVisitResult.SKIP_SUBTREE
                            } catch (_: SecurityException) {
                                FileVisitResult.SKIP_SUBTREE
                            }
                        }

                        override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                            try {
                                processedFiles++
                                searchProgress = if (totalFiles > 0) {
                                    (processedFiles.toFloat() / totalFiles.toFloat())
                                } else {
                                    0f
                                }

                                if (file.fileName.toString().contains(searchFileName, true)) {
                                    found.add(file.toFile())
                                }
                            } catch (_: Exception) {
                                // 忽略单个文件的错误
                            }
                            return FileVisitResult.CONTINUE
                        }

                        override fun visitFileFailed(file: Path, exc: IOException): FileVisitResult {
                            return FileVisitResult.SKIP_SUBTREE
                        }
                    })
                } else {
                    Files.list(targetPath).use { stream ->
                        stream.forEach { path ->
                            try {
                                processedFiles++
                                searchProgress = if (totalFiles > 0) {
                                    (processedFiles.toFloat() / totalFiles.toFloat())
                                } else {
                                    0f
                                }

                                if (path.fileName.toString().contains(searchFileName, true)) {
                                    found.add(path.toFile())
                                }
                            } catch (_: Exception) {

                            }
                        }
                    }
                }

                isSearching = false
            }
        }
    }

    AlertDialog(
        onDismissRequest = { dialogManager.showSearch = false },
        title = { Text("搜索") },
        text = {
            Column {
                val focusRequester = remember { FocusRequester() }
                TextField(
                    value = searchFileName,
                    onValueChange = { searchFileName = it },
                    modifier = Modifier.focusRequester(focusRequester),
                    shape = MaterialTheme.shapes.small,
                )

                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .offset(x = (-12).dp)
                ) {
                    Checkbox(
                        checked = includeSubdirectories,
                        onCheckedChange = { includeSubdirectories = it }
                    )
                    Text("包含子目录")
                }

                if (isSearching) {
                    LinearProgressIndicator(
                        progress = { searchProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    )
                }

                if (found.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                    ) {
                        items(found) { file ->
                            FileRow(
                                file = file,
                                type = getFileType(file),
                                onFileClick = {
                                    onFileClick(file)
                                },
                                onFileLongClick = {
                                    //onFileClick(file, null)
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    processedFiles = 0
                    searchProgress = 0f
                    isSearching = true
                },
                enabled = !isSearching && searchFileName.isNotEmpty()
            ) {
                Text("搜索")
            }
        },
        dismissButton = {
            FilledTonalButton(
                onClick = { dialogManager.showSearch = false },
                enabled = !isSearching
            ) {
                Text("取消")
            }
        }
    )
}