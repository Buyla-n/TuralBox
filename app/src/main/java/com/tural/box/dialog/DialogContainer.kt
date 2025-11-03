package com.tural.box.dialog

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import coil.compose.AsyncImage
import com.tural.box.BuildConfig
import com.tural.box.R
import com.tural.box.activity.LicensesActivity
import com.tural.box.icons.AppIcon
import com.tural.box.model.FileType
import com.tural.box.ui.screen.main.FileRow
import com.tural.box.ui.screen.main.LoadingType
import com.tural.box.ui.screen.main.PackageInfo
import com.tural.box.ui.screen.main.PanelPosition
import com.tural.box.ui.screen.main.PanelStates
import com.tural.box.ui.screen.main.SortOrder
import com.tural.box.util.FileChangeProgress
import com.tural.box.util.copyFile
import com.tural.box.util.copyFolder
import com.tural.box.util.createFile
import com.tural.box.util.createFolder
import com.tural.box.util.deleteFile
import com.tural.box.util.deleteFolder
import com.tural.box.util.formatFileDate
import com.tural.box.util.formatFileSize
import com.tural.box.util.getFileSize
import com.tural.box.util.getFileType
import com.tural.box.util.install
import com.tural.box.util.invalidChars
import com.tural.box.util.moveFile
import com.tural.box.util.refresh
import com.tural.box.util.renameFile
import com.tural.box.util.shareFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.Path
import kotlin.io.path.pathString
import kotlin.use

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogContainer(
    context: Context,
    scope: CoroutineScope,
    dialogManager: DialogManager,
    panelStates: PanelStates,
    negativePanelStates: PanelStates,
    currentPanel: PanelPosition,
    onFileClick: (File, FileType?) -> Unit
) {
    val currentFile = dialogManager.currentFile
    if (dialogManager.showTool) {
        BasicAlertDialog(
            onDismissRequest = { dialogManager.showTool = false },
            content = {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier.width(300.dp)
                ) {
                    Column {
//                            Text(
//                                checkedFile!!.name,
//                                style = MaterialTheme.typography.titleMedium,
//                                modifier = Modifier.padding(start = 32.dp, top = 24.dp, end = 32.dp)
//                            )
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = 20.dp,
                                    end = 20.dp,
                                    bottom = 24.dp,
                                    top = 16.dp
                                ),
                            maxItemsInEachRow = 2,
                        ) {
                            @Composable
                            fun ToolItem(
                                text: String,
                                icon: Int,
                                onClick: () -> Unit,
                                enabled: Boolean = true
                            ) {
                                Surface(
                                    enabled = enabled,
                                    modifier = Modifier.width(128.dp),
                                    onClick = onClick,
                                    shape = ButtonDefaults.shape,
                                    color = Color.Transparent
                                ) {
                                    Row(Modifier.padding(8.dp)) {
                                        Icon(
                                            painter = painterResource(icon),
                                            contentDescription = null
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text)
                                    }
                                }
                            }

                            fun cancel() {
                                dialogManager.showTool = false
                            }

                            ToolItem(
                                text = if (currentPanel == PanelPosition.RIGHT) "<-复制" else "复制->",
                                icon = R.drawable.outline_file_copy_24,
                                onClick = {
                                    dialogManager.showCopy = true
                                    cancel()
                                }
                            )

                            ToolItem(
                                text = if (currentPanel == PanelPosition.RIGHT) "<-移动" else "移动->",
                                icon = R.drawable.outline_drive_file_move_24,
                                onClick = {
                                    dialogManager.showMove = true
                                    cancel()
                                }
                            )

                            ToolItem(
                                text = "打开方式",
                                icon = R.drawable.outline_file_open_24,
                                onClick = {
                                    dialogManager.showOpenMode = true
                                    cancel()
                                }
                            )

                            ToolItem(
                                text = "重命名",
                                icon = R.drawable.outline_edit_24,
                                onClick = {
                                    dialogManager.showRename = true
                                    cancel()
                                }
                            )

                            ToolItem(
                                text = "删除",
                                icon = R.drawable.outline_delete_24,
                                onClick = {
                                    dialogManager.showDelete = true
                                    cancel()
                                }
                            )

                            ToolItem(
                                text = "压缩",
                                icon = R.drawable.outline_archive_24,
                                onClick = { /* 压缩操作 */ }
                            )

                            ToolItem(
                                text = "属性",
                                icon = R.drawable.outline_info_24,
                                onClick = {
                                    dialogManager.showProperties = true
                                    cancel()
                                }
                            )

                            ToolItem(
                                text = "分享",
                                icon = R.drawable.outline_share_24,
                                onClick = {
                                    context.shareFile(currentFile!!)
                                },
                                enabled = !currentFile!!.isDirectory
                            )
                        }
                    }
                }
            }
        )
    }
    if (dialogManager.showDelete) {
        var progress by remember { mutableStateOf<FileChangeProgress?>(null) }
        var loadingType by remember { mutableStateOf(LoadingType.NONE) }
        AlertDialog(
            modifier = Modifier.width(560.dp),
            onDismissRequest = { dialogManager.showDelete = false },
            title = { Text("确认删除") },
            text = {
                Column {
                    Text("是否删除 ${currentFile!!.name} ?")

                    Spacer(Modifier.height(8.dp))

                    when (loadingType) {
                        LoadingType.FAIL -> Text(
                            "删除失败",
                            color = MaterialTheme.colorScheme.error
                        )
                        LoadingType.NONE -> Text(
                            "文件将永久丢失",
                            color = MaterialTheme.colorScheme.error
                        )
                        LoadingType.FILE -> LinearProgressIndicator(modifier = Modifier)
                        LoadingType.DIRECTORY -> {
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
                                        loadingType = LoadingType.PART_FAIL
                                        Text("失败: ${current.failedCount}", color = Color.Red)
                                    }
                                }

                                is FileChangeProgress.Error -> {
                                    loadingType = LoadingType.FAIL
                                }

                                is FileChangeProgress.Completed -> {
                                    if (current.isAllSuccess) {
                                        dialogManager.showDelete = false
                                        refresh(scope, panelStates)
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
                        if (currentFile != null) {
                            scope.launch(Dispatchers.IO) {
                                if (currentFile!!.isDirectory) {
                                    loadingType = LoadingType.DIRECTORY
                                    deleteFolder(currentFile!!)
                                        .catch { e ->
                                            loadingType = LoadingType.FAIL
                                            progress = FileChangeProgress.Error(
                                                e as Exception,
                                                currentFile!!
                                            )
                                        }
                                        .collect { update ->
                                            progress = update
                                        }
                                } else {
                                    loadingType = LoadingType.FILE
                                    val result = deleteFile(currentFile!!)
                                    if (result) {
                                        dialogManager.showDelete = false
                                        refresh(scope, panelStates)
                                    } else {
                                        loadingType = LoadingType.FAIL
                                    }
                                }
                            }
                        } else {
                            loadingType = LoadingType.FAIL
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
    if (dialogManager.showCopy) {
        var progress by remember { mutableStateOf<FileChangeProgress?>(null) }
        var loadingType by remember { mutableStateOf(LoadingType.NONE) }
        AlertDialog(
            modifier = Modifier.width(560.dp),
            onDismissRequest = { dialogManager.showCopy = false },
            title = { Text("确认复制") },
            text = {
                Column {
                    Text("是否复制 ${currentFile!!.name} 到 ${negativePanelStates.path} ?")

                    Spacer(Modifier.height(8.dp))

                    when (loadingType) {
                        LoadingType.FAIL -> Text(
                            "复制失败",
                            color = MaterialTheme.colorScheme.error
                        )

                        LoadingType.FILE -> LinearProgressIndicator(modifier = Modifier)
                        LoadingType.DIRECTORY -> {
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
                                        loadingType = LoadingType.PART_FAIL
                                        Text("失败: ${current.failedCount}", color = Color.Red)
                                    }
                                }

                                is FileChangeProgress.Error -> {
                                    loadingType == LoadingType.FAIL
                                }

                                is FileChangeProgress.Completed -> {
                                    if (current.isAllSuccess) {
                                        dialogManager.showCopy = false
                                        refresh(scope, negativePanelStates)
                                    } else {
                                        Text(
                                            "部分复制失败",
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
                        if (currentFile != null) {
                            scope.launch(Dispatchers.IO) {
                                if (currentFile!!.isDirectory) {
                                    loadingType = LoadingType.DIRECTORY
                                    copyFolder(
                                        currentFile!!,
                                        File("${negativePanelStates.path}/${currentFile!!.name}")
                                    )
                                        .catch { e ->
                                            loadingType = LoadingType.FAIL
                                            progress = FileChangeProgress.Error(
                                                e as Exception,
                                                currentFile!!
                                            )
                                        }
                                        .collect { update ->
                                            progress = update
                                        }
                                } else {
                                    loadingType = LoadingType.FILE
                                    val result = copyFile(
                                        currentFile!!,
                                        File("${negativePanelStates.path}/${currentFile!!.name}")
                                    )
                                    if (result) {
                                        dialogManager.showCopy = false
                                        refresh(scope, negativePanelStates)
                                    } else {
                                        loadingType = LoadingType.FAIL
                                    }
                                }
                            }
                        } else {
                            loadingType = LoadingType.FAIL
                        }
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
    if (dialogManager.showMove) {
        var progress by remember { mutableStateOf<FileChangeProgress?>(null) }
        var loadingType by remember { mutableStateOf(LoadingType.NONE) }
        AlertDialog(
            modifier = Modifier.width(560.dp),
            onDismissRequest = { dialogManager.showMove = false },
            title = { Text("确认移动") },
            text = {
                Column {
                    Text("是否移动 ${currentFile!!.name} 到 ${negativePanelStates.path} ?")

                    Spacer(Modifier.height(8.dp))

                    when (loadingType) {
                        LoadingType.FAIL -> Text(
                            "移动失败",
                            color = MaterialTheme.colorScheme.error
                        )

                        LoadingType.FILE -> LinearProgressIndicator(modifier = Modifier)
                        LoadingType.DIRECTORY -> {
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
                                        loadingType = LoadingType.PART_FAIL
                                        Text("失败: ${current.failedCount}", color = Color.Red)
                                    }
                                }

                                is FileChangeProgress.Error -> {
                                    loadingType == LoadingType.FAIL
                                }

                                is FileChangeProgress.Completed -> {
                                    if (current.isAllSuccess) {
                                        dialogManager.showMove = false
                                        refresh(scope, panelStates)
                                        refresh(scope, negativePanelStates)
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
                        if (currentFile != null) {
                            scope.launch(Dispatchers.IO) {

                                loadingType = LoadingType.FILE
                                val result = moveFile(
                                    currentFile!!,
                                    File("${negativePanelStates.path}/${currentFile!!.name}")
                                )
                                if (result) {
                                    dialogManager.showMove = false
                                    refresh(scope, panelStates)
                                    refresh(scope, negativePanelStates)
                                } else {
                                    loadingType = LoadingType.FAIL
                                }

                            }
                        } else {
                            loadingType = LoadingType.FAIL
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
    if (dialogManager.showCreateFile) {
        var fileName by remember { mutableStateOf("") }
        var createFail by remember { mutableStateOf(false) }
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
                        val creator = createFile(panelStates.path, fileName)
                        if (!creator) createFail = true else {
                            dialogManager.showCreateFile = false
                            panelStates.highLightFiles = setOf(fileName)
                            refresh(scope, panelStates)
                        }
                    }
                ) {
                    Text("文件")
                }
                Button(
                    onClick = {
                        val creator = createFolder(panelStates.path, fileName)
                        if (!creator) createFail = true else {
                            dialogManager.showCreateFile = false
                            panelStates.highLightFiles = setOf(fileName)
                            refresh(scope, panelStates)
                        }
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
    if (dialogManager.showSort) {
        var selectedSortOption by remember { mutableStateOf(SortOrder.NAME) } // 0=名称, 1=大小, 2=时间, 3=类型
        selectedSortOption = panelStates.sortOrder

        AlertDialog(
            onDismissRequest = { dialogManager.showSort = false },
            title = { Text("排序 ${if (currentPanel == PanelPosition.LEFT) "左" else "右"}") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    listOf(
                        SortOrder.NAME to "名称排序",
                        SortOrder.SIZE to "大小排序",
                        SortOrder.TIME to "时间排序",
                        SortOrder.TYPE to "类型排序"
                    ).forEach { (order, text) ->
                        ListItem(
                            headlineContent = { Text(text) },
                            leadingContent = {
                                RadioButton(
                                    selected = selectedSortOption == order,
                                    onClick = { selectedSortOption = order }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedSortOption = order }
                                .padding(),
                            colors = ListItemDefaults.colors(
                                containerColor = Color.Transparent
                            )
                        )
                        HorizontalDivider()
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        panelStates.sortOrder = selectedSortOption
                        dialogManager.showSort = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                FilledTonalButton(
                    onClick = { dialogManager.showSort = false }
                ) {
                    Text("取消")
                }
            }
        )
    }
    if (dialogManager.showPath) {
        val textFieldState = rememberTextFieldState(initialText = panelStates.path.pathString)
        AlertDialog(
            onDismissRequest = { dialogManager.showPath = false },
            title = { Text("路径 ${if (currentPanel == PanelPosition.LEFT) "左" else "右"}") },
            text = {
                val focusRequester = remember { FocusRequester() }

                TextField(
                    state = textFieldState,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.focusRequester(focusRequester)
                )

                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        panelStates.path = Path(textFieldState.text.toString())
                        dialogManager.showPath = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                FilledTonalButton(
                    onClick = { dialogManager.showPath = false }
                ) {
                    Text("取消")
                }
            }
        )
    }
    if (dialogManager.showRename) {
        val textFieldState = rememberTextFieldState(initialText = currentFile!!.name)
        var renameFail by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { dialogManager.showRename = false },
            title = { Text("重命名") },
            text = {
                val currentText = textFieldState.text
                val hasInvalidChar = remember(currentText) {
                    currentText.any { it in invalidChars }
                }
                val isEmpty = currentText.isBlank()
                val isValid = !hasInvalidChar && !isEmpty && !renameFail
                val focusRequester = remember { FocusRequester() }

                TextField(
                    state = textFieldState,
                    shape = MaterialTheme.shapes.small,
                    isError = !isValid,
                    supportingText = {
                        when {
                            isEmpty -> Text(
                                "命名不能为空",
                                color = MaterialTheme.colorScheme.error
                            )

                            hasInvalidChar -> Text(
                                "不能包含: ${invalidChars.joinToString("")}",
                                color = MaterialTheme.colorScheme.error
                            )

                            renameFail -> Text(
                                "命名失败",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    modifier = Modifier.focusRequester(focusRequester)
                )

                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val renamer = renameFile(
                            currentFile!!,
                            File("${currentFile!!.parent}/${textFieldState.text}")
                        )
                        if (!renamer) renameFail = true else {
                            dialogManager.showRename = false
                            refresh(scope, panelStates)
                        }
                    }
                ) {
                    Text("重命名")
                }
            },
            dismissButton = {
                FilledTonalButton(
                    onClick = { dialogManager.showRename = false }
                ) {
                    Text(" 取消 ")
                }
            }
        )
    }
    if (dialogManager.showSearch) {
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
                    val searchPath = panelStates.path
                    var totalFiles = 0L

                    if (includeSubdirectories) {
                        Files.walkFileTree(searchPath, object : SimpleFileVisitor<Path>() {
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
                        Files.list(searchPath).use { stream ->
                            totalFiles = stream.count()
                        }
                    }

                    if (includeSubdirectories) {
                        Files.walkFileTree(searchPath, object : SimpleFileVisitor<Path>() {
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
                        Files.list(searchPath).use { stream ->
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
                                    onFileClick = { onFileClick(file, null) },
                                    onFileLongClick = {
                                        panelStates.highLightFiles = setOf(file.name)
                                        panelStates.path = Path(file.path)
                                        dialogManager.showSearch = false
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
    if (dialogManager.showAppDetail) {
        val app = currentFile
        val pm = context.packageManager
        AlertDialog(
            onDismissRequest = { dialogManager.showAppDetail = false },
            text = {
                Column {
                    val apkInfo = try { pm.getPackageArchiveInfo(app!!.path, 0) } catch (_: Exception) { null }

                    if (apkInfo != null) {

                        apkInfo.applicationInfo!!.apply {
                            sourceDir = app!!.absolutePath
                            publicSourceDir = app.absolutePath
                        }

                        val isIn = try {
                            pm.getApplicationInfo(apkInfo.packageName, 0)
                            true
                        } catch (_: PackageManager.NameNotFoundException) {
                            false
                        }

                        val pkgInfo = if (isIn) pm.getPackageInfo(apkInfo.packageName, 0) else null

                        val packageInfo =
                            PackageInfo(
                                name = apkInfo.applicationInfo!!.loadLabel(pm).toString(),
                                uid = if (isIn) pkgInfo!!.applicationInfo!!.uid else null,
                                versionName = apkInfo.versionName ?: "未知",
                                versionCode = apkInfo.longVersionCode,
                                packageName = apkInfo.packageName,
                                icon = apkInfo.applicationInfo!!.loadIcon(pm),
                                sourceDir = if (isIn) pkgInfo!!.applicationInfo!!.sourceDir else app!!.path,
                                dataDir = if (isIn) pkgInfo!!.applicationInfo!!.dataDir else null
                            )

                        Row(
                            modifier = Modifier.padding(bottom = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            AsyncImage(
                                model = packageInfo.icon,
                                contentDescription = "App icon",
                                modifier = Modifier.size(48.dp),
                                contentScale = ContentScale.Fit,
                                placeholder = ColorPainter(Color.LightGray)
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text(
                                    text = packageInfo.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.widthIn(max = 160.dp)
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = packageInfo.versionName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.width(245.dp))

                        Spacer(Modifier.height(8.dp))

                        Column {
                            @Composable
                            fun InfoItem(title: String, summary: String) {
                                Row(modifier = Modifier.width(245.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(title)
                                    Text(
                                        text = summary,
                                        softWrap = false,
                                        modifier = Modifier
                                            .widthIn(max = 160.dp)
                                            .combinedClickable(
                                                onClick = {

                                                }
                                            ),
                                        overflow = TextOverflow.MiddleEllipsis,
                                        textAlign = TextAlign.End
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                            }
                            InfoItem("包名", packageInfo.packageName)
                            InfoItem("版本号", packageInfo.versionCode.toString())
                            InfoItem("安装状态", if (isIn) "已安装" else "未安装")
                            InfoItem("大小", formatFileSize(File(app!!.path).length()))
                            if (isIn) {
                                InfoItem("数据目录", packageInfo.dataDir!!)
                                InfoItem("安装目录", packageInfo.sourceDir)
                                InfoItem("UID", packageInfo.uid.toString())
                            }
                        }
                    } else {
                        Text("无法获取安装包信息", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.titleMedium)
                    }
                }
            },
            confirmButton = {
                OutlinedButton(
                    onClick = {

                    }
                ) {
                    Text("查看")
                }
                Button(
                    onClick = {
                        install(context, currentFile!!)
                    }
                ) {
                    Text("安装")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {

                    }
                ) {
                    Text("功能")
                }
            }
        )
    }
    if (dialogManager.showAbout) {
        BasicAlertDialog(
            onDismissRequest = { dialogManager.showAbout = false },
            content = {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier.width(280.dp)
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.padding(bottom = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            AppIcon()

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text(
                                    text = "TuralBox",
                                    style = MaterialTheme.typography.bodyLarge,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.widthIn(max = 160.dp)
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "${BuildConfig.versionName} (SnapShot)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                        Row(Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(context, LicensesActivity::class.java)
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(" 开源库 ")
                            }
                            Spacer(Modifier.width(8.dp))
                            OutlinedButton(
                                onClick = {
                                    Toast.makeText(context, "没建", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("QQ 群聊")
                            }
                        }
                    }
                }
            }
        )
    }
    if (dialogManager.showOpenMode) {
        AlertDialog(
            onDismissRequest = { dialogManager.showOpenMode = false },
            confirmButton = {
                Button(
                    onClick = { dialogManager.showOpenMode = false }
                ) {
                    Text("取消")
                }
            },
            text = {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    maxItemsInEachRow = 4
                ) {
                    @Composable
                    fun OpenModeItem(
                        name: String,
                        icon: Int,
                        type: FileType
                    ) {
                        Surface(
                            onClick = {
                                onFileClick(currentFile!!, type)
                                dialogManager.showOpenMode = false
                            },
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.width(60.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Icon(
                                    painter = painterResource(icon),
                                    contentDescription = null
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(name)
                            }
                        }
                    }
                    OpenModeItem(name = "文本", icon = R.drawable.outline_description_24, FileType.TEXT)
                    OpenModeItem(name = "图片", icon = R.drawable.outline_image_24, FileType.IMAGE)
                    OpenModeItem(name = "视频", icon = R.drawable.outline_video_file_24, FileType.VIDEO)
                    OpenModeItem(name = "音频", icon = R.drawable.outline_audio_file_24, FileType.AUDIO)

                    OpenModeItem(name = "安装包", icon = R.drawable.outline_android_24, FileType.INSTALLABLE)
                    OpenModeItem(name = "脚本", icon = R.drawable.outline_terminal_24, FileType.SCRIPT)
                    OpenModeItem(name = "字体", icon = R.drawable.outline_font_download_24, FileType.FONT)
                    OpenModeItem(name = "压缩包", icon = R.drawable.outline_archive_24, FileType.ARCHIVE)
                }
            }
        )
    }
    if (dialogManager.showAudio) {
        BasicAlertDialog(
            onDismissRequest = { dialogManager.showAudio = false },
            content = {
                val context = LocalContext.current
                val exoPlayer = remember {
                    ExoPlayer.Builder(context).build().apply {
                        val mediaItem = MediaItem.fromUri(Uri.fromFile(currentFile))
                        setMediaItem(mediaItem)
                        prepare()
                    }
                }

                var isPlaying by remember { mutableStateOf(true) }
                var isLooping by remember { mutableStateOf(false) }
                var playbackSpeed by remember { mutableFloatStateOf(1f) }
                var currentPosition by remember { mutableLongStateOf(0L) }
                var totalDuration by remember { mutableLongStateOf(0L) }

                DisposableEffect(Unit) {
                    onDispose {
                        exoPlayer.release()
                    }
                }

                LaunchedEffect(exoPlayer) {
                    exoPlayer.play()
                    while (true) {
                        currentPosition = exoPlayer.currentPosition
                        totalDuration = exoPlayer.duration
                        if (totalDuration in 1..currentPosition && !isLooping) {
                            exoPlayer.pause()
                            isPlaying = false
                        }
                        delay(1)
                    }
                }
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceContainer,
                            MaterialTheme.shapes.extraLarge
                        ),
                    verticalArrangement = Arrangement.Center
                ) {
                    Spacer(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        currentFile!!.name,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )

                    Column(
                        modifier = Modifier.padding(top = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Slider(
                            value = if (totalDuration > 0) currentPosition.toFloat().coerceIn(0f, totalDuration.toFloat()) else 0f,
                            onValueChange = {exoPlayer.seekTo(it.toLong()) },
                            valueRange = if (totalDuration > 0) 0f..totalDuration.toFloat() else 0f .. 0f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 8.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            TextButton(onClick = {
                                playbackSpeed = when (playbackSpeed) {
                                    0.5f -> 1f
                                    1f -> 1.5f
                                    1.5f -> 2f
                                    else -> 0.5f
                                }
                                exoPlayer.setPlaybackSpeed(playbackSpeed)
                            }) {
                                Text("$playbackSpeed x")
                            }

                            IconButton(onClick = {
                                isLooping = !isLooping
                                exoPlayer.repeatMode =
                                    if (isLooping) ExoPlayer.REPEAT_MODE_ONE else ExoPlayer.REPEAT_MODE_OFF
                            }) {
                                Icon(
                                    painter = painterResource(if (isLooping) {
                                        R.drawable.outline_repeat_on_24
                                    } else {
                                        R.drawable.baseline_repeat_24
                                    }),
                                    contentDescription = if (isLooping) "关闭循环" else "开启循环"
                                )
                            }

                            IconButton(
                                onClick = {
                                    dialogManager.showAudio = false
                                }
                            ) {
                                Icon(
                                    painterResource(R.drawable.outline_close_24),
                                    contentDescription = null,
                                )
                            }

                            IconButton(
                                onClick = {
                                    if (isPlaying) {
                                        exoPlayer.pause()
                                    } else {
                                        exoPlayer.play()
                                    }
                                    isPlaying = !isPlaying
                                    if (totalDuration in 1..currentPosition) {
                                        exoPlayer.seekTo(0)
                                    }
                                }
                            ) {
                                Icon(
                                    painter = painterResource(if (isPlaying) {
                                        R.drawable.outline_pause_24
                                    } else {
                                        R.drawable.outline_play_arrow_24
                                    }),
                                    contentDescription = if (isPlaying) "暂停" else "播放",
                                )
                            }
                        }
                    }
                }
            }
        )
    }
    if (dialogManager.showProperties) {
        val file = currentFile!!
        AlertDialog(
            onDismissRequest = { dialogManager.showProperties = false },
            text = {
                val fileSize = remember(file) { getFileSize(file) }
                val formattedDate = remember(file) { formatFileDate(file) }
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    @Composable
                    fun PropertyRow(
                        label: String,
                        value: String
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = value,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    // 文件名
                    PropertyRow(
                        label = "文件名",
                        value = file.name
                    )

                    // 目录
                    PropertyRow(
                        label = "目录",
                        value = file.parent ?: "无"
                    )

                    // 类型（由你补充）
                    PropertyRow(
                        label = "类型",
                        value = getFileType(file).name // 你需要实现这个函数
                    )

                    // 大小
                    PropertyRow(
                        label = "大小",
                        value = fileSize
                    )

                    // 修改时间
                    PropertyRow(
                        label = "修改时间",
                        value = formattedDate
                    )

                    // 文件路径（额外信息）
                    PropertyRow(
                        label = "路径",
                        value = file.absolutePath
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { dialogManager.showProperties = false }
                ) {
                    Text("关闭")
                }
            }
        )
    }
}