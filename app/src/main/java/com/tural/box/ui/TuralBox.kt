package com.tural.box.ui

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Environment
import android.os.StatFs
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import coil.compose.AsyncImage
import com.tural.box.AppExtractActivity
import com.tural.box.FontActivity
import com.tural.box.ImageActivity
import com.tural.box.OpenSourceActivity
import com.tural.box.R
import com.tural.box.TerminalActivity
import com.tural.box.TextEditorActivity
import com.tural.box.VideoActivity
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class,
    ExperimentalLayoutApi::class
)
@Composable
fun TuralApp(
    context: Context
) {
    val scope = rememberCoroutineScope()
    var currentPanel by remember { mutableStateOf(PanelPosition.LEFT) }
    var currentPath by remember { mutableStateOf(Path(RootPath)) }
    var negativePath by remember { mutableStateOf(Path(RootPath)) }
    var currentFile by remember { mutableStateOf<File?>(null) }
    val colorWhite = if (isSystemInDarkTheme()) Color.Black else Color.White
    val leftLazyState = rememberLazyListState()
    val rightLazyState = rememberLazyListState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val dialogManager = remember { DialogManager() }
    val leftPanelState = remember { PanelStates() }
    val rightPanelState = remember { PanelStates() }

    fun currentPanelState(): PanelStates {
        return if (currentPanel == PanelPosition.LEFT) {
            leftPanelState
        } else {
            rightPanelState
        }
    }

    fun uncurrentPanelState(): PanelStates {
        return if (currentPanel == PanelPosition.RIGHT) {
            leftPanelState
        } else {
            rightPanelState
        }
    }

    fun handleBack() {
        val cps = currentPanelState()
//        if (parseZipPath(cps.path.pathString).isEmpty() && cps.isInZip) {
//            cps.isInZip = false
//            cps.zipFile = null
//        }
        if (!currentPath.isRootPath()) {
            cps.path = cps.path.parent
        }
    }

    fun refresh() {
        val cps = currentPanelState()
        scope.launch(Dispatchers.IO) {
            cps.files = accessFiles(cps.path, cps.sortOrder)
        }
    }

    fun negativeRefresh() {
        val cps = uncurrentPanelState()
        scope.launch(Dispatchers.IO) {
            cps.files = accessFiles(cps.path, cps.sortOrder)
        }
    }

    fun handleFileClick(file: File, type: FileType? = null) {
        if (file.isDirectory) {
            currentPanelState().path = file.toPath()
        } else {
            currentFile = file
            when(type ?: getFileType(file)) {
                FileType.TEXT -> context.startActivity(Intent(context, TextEditorActivity::class.java).putExtra("filePath", file.path))
                FileType.IMAGE -> context.startActivity(Intent(context, ImageActivity::class.java).putExtra("filePath", file.path))
                FileType.INSTALLABLE -> { dialogManager.showAppDetail = true }
                FileType.XML -> context.startActivity(Intent(context, TextEditorActivity::class.java).putExtra("filePath", file.path))
                FileType.FONT -> context.startActivity(Intent(context, FontActivity::class.java).putExtra("filePath", file.path))
                FileType.VIDEO -> context.startActivity(Intent(context, VideoActivity::class.java).putExtra("filePath", file.path))
                FileType.AUDIO -> { dialogManager.showAudio = true }
                FileType.ARCHIVE -> {
                    val cps = currentPanelState()
                    cps.zipFile = file
                    cps.previousPath = cps.path
                    cps.isInZip = true
                    cps.path = Path("${file.path}")
                }
                else -> { dialogManager.showOpenMode = true }
            }
        }
    }

    BackHandler(
        enabled = !currentPanelState().path.isRootPath()
    ) {
        handleBack()
    }

    BackHandler(
        enabled = !drawerState.isClosed
    ) {
        scope.launch {
            drawerState.close()
        }
    }

    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.fillMaxWidth(0.8f),
                drawerShape = MaterialTheme.shapes.extraLarge.copy(topStart = CornerSize(0.dp), bottomStart = CornerSize(0.dp))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Spacer(Modifier.height(12.dp))

                    NavigationDrawerItem(
                        label = {
                            Text(
                                "TuralBox",
                                style = MaterialTheme.typography.titleLarge
                            )
                        },
                        selected = false,
                        badge = {
                            Icon(
                                painter = painterResource(R.drawable.outline_settings_24),
                                contentDescription = null
                            )
                        },
                        onClick = {
                            dialogManager.showAbout = true
                        }
                    )
                    HorizontalDivider()

                    Text("存储", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
                    NavigationDrawerItem(
                        label = {
                            var storageProgress by remember { mutableFloatStateOf(0f) }

                            LaunchedEffect(Unit) {
                                val progress = withContext(Dispatchers.IO) {
                                    try {
                                        val stat = StatFs(Environment.getExternalStorageDirectory().path)
                                        val totalBytes = stat.totalBytes
                                        val availableBytes = stat.availableBytes
                                        if (totalBytes > 0) (totalBytes - availableBytes).toFloat() / totalBytes else 0f
                                    } catch (_: Exception) {
                                        0f
                                    }
                                }
                                storageProgress = progress
                            }
                            Column {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("内部存储")
                                    Text("${(storageProgress * 100).toInt()}%")
                                }
                                Spacer(Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { storageProgress },
                                    drawStopIndicator = {},
                                    gapSize = (-2).dp,
                                    trackColor = MaterialTheme.colorScheme.surface
                                )
                            }
                        },
                        selected = true,
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.outline_storage_24),
                                contentDescription = null
                            )
                        },
                        onClick = {
                            scope.launch {
                                currentPanelState().path = Path(RootPath)
                                drawerState.close()
                            }
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Text("工具", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
                    NavigationDrawerItem(
                        label = { Text("软件提取") },
                        selected = false,
                        icon = { Icon(painterResource(R.drawable.outline_unarchive_24), contentDescription = null) },
                        onClick = {
                            context.startActivity(Intent(context, AppExtractActivity::class.java))
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text("终端模拟") },
                        selected = false,
                        icon = { Icon(painterResource(R.drawable.outline_terminal_24), contentDescription = null) },
                        onClick = {
                            context.startActivity(Intent(context, TerminalActivity::class.java))
                        },
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
        },
        drawerState = drawerState
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = currentPath.pathString,
                                maxLines = 1,
                                overflow = TextOverflow.StartEllipsis,
                                softWrap = false,
                                modifier = Modifier.clickable(
                                    onClick = {
                                        dialogManager.showPath = true
                                    }
                                )
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    if (drawerState.isClosed) {
                                        drawerState.open()
                                    } else {
                                        drawerState.close()
                                    }
                                }
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.outline_menu_24),
                                contentDescription = null
                            )
                        }
                    },
                    actions = {
                        Box {
                            var expanded by remember { mutableStateOf(false) }
                            IconButton(
                                onClick = {
                                    expanded = true
                                }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.outline_more_vert_24),
                                    contentDescription = null
                                )
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                shape = MaterialTheme.shapes.small
                            ) {
                                DropdownMenuItem(
                                    text = { Text("刷新") },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(R.drawable.outline_refresh_24),
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        refresh()
                                        expanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("搜索") },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(R.drawable.outline_search_24),
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        dialogManager.showSearch = true
                                        expanded = false
                                    }
                                )

                                HorizontalDivider()

                                DropdownMenuItem(
                                    text = { Text("排序方式") },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(R.drawable.outline_sort_24),
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        dialogManager.showSort = true
                                        expanded = false
                                    }
                                )

                                HorizontalDivider()

                                DropdownMenuItem(
                                    text = { Text("设置") },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(R.drawable.outline_settings_24),
                                            contentDescription = null
                                        )
                                    },
                                    onClick = { /* Do something... */ }
                                )
                                DropdownMenuItem(
                                    text = { Text("退出") },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(R.drawable.outline_exit_to_app_24),
                                            contentDescription = null
                                        )
                                    },
                                    onClick = { (context as ComponentActivity).finishAffinity() }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    )
                )
            },
            bottomBar = {
                BottomAppBar(
                    actions = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            IconButton(onClick = { /* do something */ }) {
                                Icon(
                                    painter = painterResource(R.drawable.outline_keyboard_arrow_left_24),
                                    contentDescription = null
                                )
                            }
                            IconButton(onClick = { refresh() }) {
                                Icon(
                                    painter = painterResource(R.drawable.outline_refresh_24),
                                    contentDescription = null,
                                )
                            }
                            IconButton(onClick = { dialogManager.showCreateFile = true }) {
                                Icon(
                                    painter = painterResource(R.drawable.outline_add_24),
                                    contentDescription = null,
                                )
                            }
                            IconButton(onClick = {
                                scope.launch {
                                    if (currentPanel == PanelPosition.LEFT) {
                                        rightPanelState.path = leftPanelState.path
                                    } else {
                                        leftPanelState.path = rightPanelState.path
                                    }
                                }
                            }) {
                                Icon(
                                    painterResource(R.drawable.outline_arrow_range_24),
                                    contentDescription = null,
                                )
                            }
                            IconButton(onClick = { handleBack() }) {
                                Icon(
                                    painter = painterResource(R.drawable.outline_arrow_upward_24),
                                    contentDescription = null,
                                )
                            }
                        }
                    }
                )
            }
        ) { contentPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
            ) {

                LaunchedEffect(leftPanelState.path,Unit) {
                    val cps = leftPanelState
                    if (cps.highLightFiles.any { cps.path.endsWith(it) }) {
                        cps.path = cps.path.parent
                    }
                    scope.launch(Dispatchers.IO) {
                        //if (!cps.isInZip) {
                            cps.files = accessFiles(cps.path, cps.sortOrder)
//                        } else {
//                            cps.files = accessArchiveEntry(cps.zipFile!!, cps.path, cps.sortOrder)
//                        }
                    }.join()

                    scope.launch(Dispatchers.Main) {
                        if (cps.highLightFiles.isNotEmpty()) {
                            delay(50)
                            val index = cps.files.indexOfFirst { file ->
                                file.name.equals(cps.highLightFiles.first(), ignoreCase = true)
                            }
                            if (index != -1) {
                                leftLazyState.scrollToItem(index)
                            }
                        }
                    }
                }

                LaunchedEffect(rightPanelState.path,Unit) {
                    val cps = rightPanelState
                    if (cps.highLightFiles.any { cps.path.endsWith(it) }) {
                        cps.path = cps.path.parent
                    }
                    scope.launch(Dispatchers.IO) {
                        //if (!cps.isInZip) {
                            cps.files = accessFiles(cps.path, cps.sortOrder)
//                        } else {
//                            cps.files = accessArchiveEntry(cps.zipFile!!, cps.path, cps.sortOrder)
//                        }
                    }.join()

                    scope.launch(Dispatchers.Main) {
                        if (cps.highLightFiles.isNotEmpty()) {
                            delay(50)
                            val index = cps.files.indexOfFirst { file ->
                                file.name.equals(cps.highLightFiles.first(), ignoreCase = true)
                            }
                            if (index != -1) {
                                rightLazyState.scrollToItem(index)
                            }
                        }
                    }
                }

                LaunchedEffect(currentPanelState().path, currentPanel) {
                    scope.launch(Dispatchers.Default) {
                        currentPath = currentPanelState().path
                        negativePath = uncurrentPanelState().path
                    }
                }

                fun handleFileLongClick(file: File) {
                    currentFile = file
                    dialogManager.showTool = true
                }

                val animatedColorLeft by animateColorAsState(
                    targetValue = if (currentPanel == PanelPosition.LEFT) MaterialTheme.colorScheme.surface else colorWhite,
                    animationSpec = tween(150)
                )

                AnimatedContent(
                    targetState = leftPanelState.files,
                    modifier = Modifier.weight(1f),
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(220, delayMillis = 0)) +
                                scaleIn(
                                    initialScale = 0.98f,
                                    animationSpec = tween(220, delayMillis = 0)
                                ))
                            .togetherWith(fadeOut(animationSpec = tween(0)))
                    }
                ) { files ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f)
                            .background(color = animatedColorLeft)
                            .pointerInteropFilter { event ->
                                if (event.action == MotionEvent.ACTION_DOWN) {
                                    currentPanel = PanelPosition.LEFT
                                }
                                false
                            },
                        state = leftLazyState
                    ) {
                        item {
                            UpwardItem(leftPanelState)
                        }
                        items(files) { file ->
                            FileItem(
                                file = file,
                                type = getFileType(file),
                                highLight = file.name in leftPanelState.highLightFiles,
                                onFileClick = { handleFileClick(file) },
                                onFileLongClick = { handleFileLongClick(file) },
                            )
                        }
                    }
                }

                val animatedColorRight by animateColorAsState(
                    targetValue = if (currentPanel == PanelPosition.RIGHT) MaterialTheme.colorScheme.surface else colorWhite,
                    animationSpec = tween(150)
                )

                AnimatedContent(
                    targetState = rightPanelState.files,
                    modifier = Modifier.weight(1f),
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(220, delayMillis = 0)) +
                                scaleIn(
                                    initialScale = 0.98f,
                                    animationSpec = tween(220, delayMillis = 0)
                                ))
                            .togetherWith(fadeOut(animationSpec = tween(0)))
                    }
                ) { files ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(1f)
                            .background(color = animatedColorRight)
                            .pointerInteropFilter { event ->
                                if (event.action == MotionEvent.ACTION_DOWN) {
                                    currentPanel = PanelPosition.RIGHT
                                }
                                false
                            },
                        state = rightLazyState
                    ) {
                        item {
                            UpwardItem(rightPanelState)
                        }
                        items(files) { file ->
                            FileItem(
                                file = file,
                                type = getFileType(file),
                                highLight = file.name in rightPanelState.highLightFiles,
                                onFileClick = { handleFileClick(file) },
                                onFileLongClick = { handleFileLongClick(file) }
                            )
                        }
                    }
                }
            }



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
                                else -> {}
                            }

                            Spacer(Modifier.height(8.dp))

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
                                        dialogManager.showDelete = false
                                        refresh()
                                    } else {
                                        Text(
                                            "部分删除失败",
                                            color = MaterialTheme.colorScheme.error
                                        )
                                        println(loadingType)
                                    }
                                }

                                null -> {}
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
                                                refresh()
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
                            Text("是否复制 ${currentFile!!.name} 到 $negativePath ?")

                            Spacer(Modifier.height(8.dp))

                            when (loadingType) {
                                LoadingType.FAIL -> Text(
                                    "复制失败",
                                    color = MaterialTheme.colorScheme.error
                                )

                                LoadingType.FILE -> LinearProgressIndicator(modifier = Modifier)
                                else -> {}
                            }

                            Spacer(Modifier.height(8.dp))

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
                                        negativeRefresh()
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
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (currentFile != null) {
                                    scope.launch(Dispatchers.IO) {
                                        if (currentFile!!.isDirectory) {
                                            loadingType = LoadingType.DIRECTORY
                                            copyFolder(currentFile!!, File("$negativePath/${currentFile!!.name}"))
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
                                            val result = copyFile(currentFile!!, File("$negativePath/${currentFile!!.name}"))
                                            if (result) {
                                                dialogManager.showCopy = false
                                                negativeRefresh()
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
                            Text("是否移动 ${currentFile!!.name} 到 $negativePath ?")

                            Spacer(Modifier.height(8.dp))

                            when (loadingType) {
                                LoadingType.FAIL -> Text(
                                    "移动失败",
                                    color = MaterialTheme.colorScheme.error
                                )

                                LoadingType.FILE -> LinearProgressIndicator(modifier = Modifier)
                                else -> {}
                            }

                            Spacer(Modifier.height(8.dp))

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
                                        negativeRefresh()
                                        refresh()
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
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (currentFile != null) {
                                    scope.launch(Dispatchers.IO) {

                                        loadingType = LoadingType.FILE
                                        val result = moveFile(
                                            currentFile!!,
                                            File("$negativePath/${currentFile!!.name}")
                                        )
                                        if (result) {
                                            dialogManager.showMove = false
                                            negativeRefresh()
                                            refresh()
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
                                val creator = createFile(currentPath, fileName)
                                if (!creator) createFail = true else {
                                    dialogManager.showCreateFile = false
                                    refresh()
                                }
                            }
                        ) {
                            Text("文件")
                        }
                        Button(
                            onClick = {
                                val creator = createFolder(currentPath, fileName)
                                if (!creator) createFail = true else {
                                    dialogManager.showCreateFile = false
                                    refresh()
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
                selectedSortOption = currentPanelState().sortOrder

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
                                currentPanelState().sortOrder = selectedSortOption
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
                var path by remember { mutableStateOf(currentPath.pathString) }
                AlertDialog(
                    onDismissRequest = { dialogManager.showPath = false },
                    title = { Text("排序 ${if (currentPanel == PanelPosition.LEFT) "左" else "右"}") },
                    text = {
                        val focusRequester = remember { FocusRequester() }

                        TextField(
                            value = path,
                            onValueChange = { path = it },
                            shape = MaterialTheme.shapes.small,
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
                                currentPanelState().path = Path(path)
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
                var fileName by remember { mutableStateOf(currentFile!!.name) }
                var renameFail by remember { mutableStateOf(false) }
                AlertDialog(
                    onDismissRequest = { dialogManager.showRename = false },
                    title = { Text("重命名") },
                    text = {
                        val hasInvalidChar = remember(fileName) {
                            fileName.any { it in invalidChars }
                        }
                        val isEmpty = fileName.isBlank()
                        val isValid = !hasInvalidChar && !isEmpty && !renameFail
                        val focusRequester = remember { FocusRequester() }

                        TextField(
                            value = fileName,
                            onValueChange = {
                                fileName = it
                                renameFail = false
                            },
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
                                val renamer = renameFile(
                                    currentFile!!,
                                    File("${currentFile!!.parent}/$fileName")
                                )
                                if (!renamer) renameFail = true else {
                                    dialogManager.showRename = false
                                    refresh()
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
                val finded = remember { mutableStateListOf<File>() }
                var searchProgress by remember { mutableFloatStateOf(0f) }
                var processedFiles by remember { mutableIntStateOf(0) }
                var isSearching by remember { mutableStateOf(false) }
                var includeSubdirectories by remember { mutableStateOf(true) } // 新增：搜索子目录开关

                LaunchedEffect(isSearching) {
                    withContext(Dispatchers.IO) {
                        if (isSearching) {
                            finded.clear()
                            val searchPath = currentPath
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
                                                finded.add(file.toFile())
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
                                                finded.add(path.toFile())
                                            }
                                        } catch (_: Exception) {
                                            // 忽略单个文件的错误
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

                            if (finded.isNotEmpty()) {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 400.dp)
                                ) {
                                    items(finded) { file ->
                                        FileItem(
                                            file = file,
                                            type = getFileType(file),
                                            onFileClick = { handleFileClick(file) },
                                            onFileLongClick = {
                                                val cps = currentPanelState()
                                                cps.highLightFiles = setOf(file.name)
                                                cps.path = Path(file.path)
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
                                            text = "0.0.3 (SnapShot)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                                Row(Modifier.fillMaxWidth()) {
                                    OutlinedButton(
                                        onClick = {
                                            val intent =
                                                Intent(context, OpenSourceActivity::class.java)
                                            context.startActivity(intent)
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(" 开源库 ")
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    OutlinedButton(
                                        onClick = {
                                            val intent = Intent(
                                                Intent.ACTION_VIEW,
                                                context.getString(R.string.qq_group_link).toUri()
                                            )
                                            context.startActivity(intent)
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
                                        handleFileClick(currentFile!!, type)
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

            }
        }
    }
}

data class PackageInfo(
    val name: String,
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
    val uid: Int? = null,
    val sourceDir: String,
    val dataDir: String? = null,
    val icon: Drawable
)

enum class PanelPosition {
    LEFT,
    RIGHT
}

enum class LoadingType {
    NONE,
    FILE,
    DIRECTORY,
    FAIL,
    PART_FAIL
}

enum class SortOrder {
    NAME,
    SIZE,
    TIME,
    TYPE
}