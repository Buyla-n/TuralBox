package com.tural.box.ui

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.tural.box.AppExtractActivity
import com.tural.box.ImageActivity
import com.tural.box.OpenSourceActivity
import com.tural.box.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.UncheckedIOException
import java.nio.file.Files
import java.nio.file.Path
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
    var paneType by remember { mutableStateOf(PaneType.LEFT) }
    var checkedPath by remember { mutableStateOf(Path(RootPath)) }
    var showToolDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    var showCreateFileDialog by remember { mutableStateOf(false) }
    var showSearchDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var checkedFile by remember { mutableStateOf<File?>(null) }
    var leftHighLightFiles by remember { mutableStateOf(emptyList<String>()) }
    var rightHighLightFiles by remember { mutableStateOf(emptyList<String>()) }
    var leftSelectedFiles by remember { mutableStateOf(emptyList<String>()) }
    var rightSelectedFiles by remember { mutableStateOf(emptyList<String>()) }
    var leftPath by remember { mutableStateOf(Path(RootPath)) }
    var rightPath by remember { mutableStateOf(Path(RootPath)) }
    var leftFiles by remember { mutableStateOf(emptyList<File>()) }
    var rightFiles by remember { mutableStateOf(emptyList<File>()) }
    val colorWhite = if (isSystemInDarkTheme()) Color.Black else Color.White
    val leftLazyState = rememberLazyListState()
    val rightLazyState = rememberLazyListState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var leftSortOrder by remember { mutableStateOf(SortOrder.NAME) }
    var rightSortOrder by remember { mutableStateOf(SortOrder.NAME) }
    var showAppDetailDialog by remember { mutableStateOf(false) }

    val panes = remember {
        mutableStateMapOf(
            PaneType.LEFT to PaneState(),
            PaneType.RIGHT to PaneState()
        )
    }

    fun handleBack() {
        if (paneType == PaneType.LEFT) {
            if (!leftPath.isRootPath()) leftPath = leftPath.parent
        } else {
            if (!rightPath.isRootPath()) rightPath = rightPath.parent
        }
    }

    fun handleRefresh() {
        scope.launch(Dispatchers.IO) {
            if (paneType == PaneType.LEFT) {
                leftFiles = accessFiles(leftPath, leftSortOrder)
            } else {
                rightFiles = accessFiles(rightPath, rightSortOrder)
            }
        }
    }

    fun handleFileClick(file: File, isLeftPane: Boolean) {
        if (file.isDirectory) {
            val newPath = file.toPath()
            if (isLeftPane) {
                leftPath = newPath
            } else {
                rightPath = newPath
            }
        } else {
            when(getFileType(file)) {
                FileType.IMAGE -> context.startActivity(Intent(context, ImageActivity::class.java).putExtra("filePath", file.path))
                FileType.INSTALL -> {
                    checkedFile = file
                    showAppDetailDialog = true
                }
                else -> {}
            }
        }
    }

    fun setPath(
        path: Path
    ) {
        if (paneType == PaneType.LEFT) {
            leftPath = path
        } else {
            rightPath = path
        }
    }

    BackHandler(
        enabled = if (paneType == PaneType.LEFT) {
            !leftPath.isRootPath()
        } else {
            !rightPath.isRootPath()
        }
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
                drawerShape = MaterialTheme.shapes.extraLarge
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
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = null
                            )
                        },
                        onClick = {
                            showAboutDialog = true
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
                                imageVector = Icons.Default.Storage,
                                contentDescription = null
                            )
                        },
                        onClick = {
                            scope.launch {
                                setPath(Path(RootPath))
                                drawerState.close()
                            }
                        }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    Text("工具", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
                    NavigationDrawerItem(
                        label = { Text("软件提取") },
                        selected = false,
                        icon = { Icon(Icons.Outlined.Archive, contentDescription = null) },
                        onClick = {
                            context.startActivity(Intent(context, AppExtractActivity::class.java))
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text("终端模拟") },
                        selected = false,
                        icon = { Icon(Icons.Outlined.Terminal, contentDescription = null) },
                        onClick = { /* Handle click */ },
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
                CenterAlignedTopAppBar(
                    title = {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = checkedPath.pathString,
                                maxLines = 1,
                                overflow = TextOverflow.StartEllipsis,
                                softWrap = false
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
                                imageVector = Icons.Default.Menu,
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
                                    imageVector = Icons.Default.MoreVert,
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
                                            Icons.Outlined.Refresh,
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        handleRefresh()
                                        expanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("搜索") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Outlined.Search,
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        showSearchDialog = true
                                        expanded = false
                                    }
                                )

                                HorizontalDivider()

                                DropdownMenuItem(
                                    text = { Text("排序方式") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.AutoMirrored.Outlined.Sort,
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        showSortDialog = true
                                        expanded = false
                                    }
                                )

                                HorizontalDivider()

                                DropdownMenuItem(
                                    text = { Text("设置") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Outlined.Settings,
                                            contentDescription = null
                                        )
                                    },
                                    onClick = { /* Do something... */ }
                                )
                                DropdownMenuItem(
                                    text = { Text("退出") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.AutoMirrored.Outlined.ExitToApp,
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
                                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = null
                                )
                            }
                            IconButton(onClick = { /* do something */ }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                )
                            }
                            IconButton(onClick = { showCreateFileDialog = true }) {
                                Icon(
                                    Icons.Filled.Add,
                                    contentDescription = null,
                                )
                            }
                            IconButton(onClick = {
                                scope.launch {
                                    if (paneType == PaneType.LEFT) {
                                        rightPath = leftPath
                                    } else {
                                        leftPath = rightPath
                                    }
                                }
                            }) {
                                Icon(
                                    if (paneType == PaneType.LEFT) Icons.AutoMirrored.Filled.Redo else Icons.AutoMirrored.Filled.Undo,
                                    contentDescription = null,
                                )
                            }
                            IconButton(onClick = { handleBack() }) {
                                Icon(
                                    Icons.Filled.ArrowUpward,
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

                LaunchedEffect(leftPath, leftSortOrder,Unit) {
                    if (leftHighLightFiles.any { leftPath.endsWith(it) }) {
                        leftPath = leftPath.parent
                    }
                    scope.launch(Dispatchers.IO) {
                        leftFiles = accessFiles(leftPath, leftSortOrder)
                    }.join()

                    scope.launch(Dispatchers.Main) {
                        if (leftHighLightFiles.isNotEmpty()) {
                            delay(50)
                            val index = leftFiles.indexOfFirst { file ->
                                file.name.equals(leftHighLightFiles.first(), ignoreCase = true)
                            }
                            if (index != -1) {
                                leftLazyState.scrollToItem(index)
                            }
                        }
                    }
                }

                LaunchedEffect(rightPath, rightSortOrder,Unit) {
                    if (rightHighLightFiles.any { rightPath.endsWith(it) }) {
                        rightPath = rightPath.parent
                    }

                    scope.launch(Dispatchers.IO) {
                        rightFiles = accessFiles(rightPath, rightSortOrder)
                    }.join()

                    scope.launch(Dispatchers.Main) {
                        if (rightHighLightFiles.isNotEmpty()) {
                            delay(50)
                            val index = rightFiles.indexOfFirst { file ->
                                file.name.equals(rightHighLightFiles.first(), ignoreCase = true)
                            }
                            if (index != -1) {
                                rightLazyState.scrollToItem(index)
                            }
                        }
                    }
                }

                LaunchedEffect(leftPath, rightPath, paneType) {
                    scope.launch(Dispatchers.Default) {
                        checkedPath = if (paneType == PaneType.LEFT) {
                            leftPath
                        } else {
                            rightPath
                        }
                    }
                }

                fun handleFileLongClick(file: File) {
                    checkedFile = file
                    showToolDialog = true
                }

                val animatedColorLeft by animateColorAsState(
                    targetValue = if (paneType == PaneType.LEFT) MaterialTheme.colorScheme.surface else colorWhite,
                    animationSpec = tween(150)
                )

                AnimatedContent(
                    targetState = leftFiles,
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
                                    paneType = PaneType.LEFT
                                }
                                false
                            },
                        state = leftLazyState
                    ) {
                        item {
                            UpwardItem {
                                if (leftPath.pathString != "/storage/emulated/0")
                                    leftPath = leftPath.parent
                            }
                        }
                        items(files) { file ->
                            FileItem(
                                itemData = FileItemData(
                                    file = file,
                                    type = getFileType(file),
                                    highLight = leftHighLightFiles.any { it == file.name },
                                    selected = leftSelectedFiles.any { it == file.name }
                                ),
                                onFileClick = { file ->
                                    if (leftSelectedFiles.isEmpty()) {
                                        handleFileClick(file = file, isLeftPane = true)
                                    } else {
                                        leftSelectedFiles = if (leftSelectedFiles.any { it == file.name }) {
                                            leftSelectedFiles - file.name
                                        } else {
                                            leftSelectedFiles + file.name
                                        }
                                    }
                                },
                                onFileLongClick = { handleFileLongClick(it) },
                                onCheck = { checkFile ->
                                    if (!leftSelectedFiles.any { it == checkFile.name }) {
                                        leftSelectedFiles += checkFile.name
                                    }
                                }
                            )
                        }
                    }
                }

                val animatedColorRight by animateColorAsState(
                    targetValue = if (paneType == PaneType.RIGHT) MaterialTheme.colorScheme.surface else colorWhite,
                    animationSpec = tween(150)
                )

                AnimatedContent(
                    targetState = rightFiles,
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
                                    paneType = PaneType.RIGHT
                                }
                                false
                            },
                        state = rightLazyState
                    ) {
                        item {
                            UpwardItem {
                                if (rightPath.pathString != "/storage/emulated/0")
                                    rightPath = rightPath.parent
                            }
                        }
                        items(files) { file ->
                            FileItem(
                                itemData = FileItemData(
                                    file = file,
                                    type = getFileType(file),
                                    highLight = rightHighLightFiles.any { it == file.name }),
                                onFileClick = { handleFileClick(file = it, isLeftPane = false) },
                                onFileLongClick = { handleFileLongClick(it) }
                            )
                        }
                    }
                }
            }

            var showDeleteDialog by remember { mutableStateOf(false) }
            var showRenameDialog by remember { mutableStateOf(false) }

            if (showToolDialog)
                BasicAlertDialog(
                    onDismissRequest = { showToolDialog = false },
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
                                        icon: ImageVector,
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
                                                    imageVector = icon,
                                                    contentDescription = null
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(text)
                                            }
                                        }
                                    }

                                    ToolItem(
                                        text = if (paneType == PaneType.RIGHT) "<-复制" else "复制->",
                                        icon = Icons.Default.ContentCopy,
                                        onClick = { /* 复制操作 */ }
                                    )

                                    ToolItem(
                                        text = if (paneType == PaneType.RIGHT) "<-移动" else "移动->",
                                        icon = Icons.AutoMirrored.Filled.DriveFileMove,
                                        onClick = { /* 移动操作 */ }
                                    )

                                    ToolItem(
                                        text = "打开方式",
                                        icon = Icons.Default.OpenInBrowser,
                                        onClick = { /* 打开方式操作 */ }
                                    )

                                    ToolItem(
                                        text = "重命名",
                                        icon = Icons.Default.Edit,
                                        onClick = {
                                            showRenameDialog = true
                                            showToolDialog = false
                                        }
                                    )

                                    ToolItem(
                                        text = "删除",
                                        icon = Icons.Default.Delete,
                                        onClick = {
                                            showDeleteDialog = true
                                            showToolDialog = false
                                        }
                                    )

                                    ToolItem(
                                        text = "压缩",
                                        icon = Icons.Default.Download,
                                        onClick = { /* 压缩操作 */ }
                                    )

                                    ToolItem(
                                        text = "属性",
                                        icon = Icons.Default.Info,
                                        onClick = { /* 属性操作 */ }
                                    )

                                    ToolItem(
                                        text = "分享",
                                        icon = Icons.Default.Share,
                                        onClick = {
                                            context.shareFile(checkedFile!!)
                                            showToolDialog = false
                                        },
                                        enabled = !checkedFile!!.isDirectory
                                    )
                                }
                            }
                        }
                    }
                )

            if (showDeleteDialog) {
                var progress by remember { mutableStateOf<DeleteProgress?>(null) }
                var loadingType by remember { mutableStateOf(LoadingType.NONE) }
                AlertDialog(
                    modifier = Modifier.width(560.dp),
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("确认删除") },
                    text = {
                        Column {
                            Text("是否删除 ${checkedFile!!.name} ?")

                            Spacer(Modifier.height(8.dp))

                            when (loadingType) {
                                LoadingType.DIRECTORY_FAIL -> Text(
                                    "删除失败",
                                    color = MaterialTheme.colorScheme.error
                                )

                                LoadingType.FILE_FAIL -> Text(
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
                                is DeleteProgress.InProgress -> {

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

                                is DeleteProgress.Error -> {
                                    loadingType == LoadingType.DIRECTORY_FAIL
                                }

                                is DeleteProgress.Completed -> {
                                    if (current.isAllSuccess) {
                                        showDeleteDialog = false
                                        handleRefresh()
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
                                if (checkedFile != null) {
                                    scope.launch(Dispatchers.IO) {
                                        if (checkedFile!!.isDirectory) {
                                            loadingType = LoadingType.DIRECTORY
                                            deleteFolder(checkedFile!!)
                                                .catch { e ->
                                                    loadingType = LoadingType.DIRECTORY_FAIL
                                                    progress = DeleteProgress.Error(
                                                        e as Exception,
                                                        checkedFile!!
                                                    )
                                                }
                                                .collect { update ->
                                                    progress = update
                                                }
                                        } else {
                                            loadingType = LoadingType.FILE
                                            val result = deleteFile(checkedFile!!)
                                            if (result) {
                                                showDeleteDialog = false
                                                handleRefresh()
                                            } else {
                                                loadingType = LoadingType.FILE_FAIL
                                            }
                                        }
                                    }
                                } else {
                                    loadingType = LoadingType.FILE_FAIL
                                }
                            }
                        ) {
                            Text("删除")
                        }
                    },
                    dismissButton = {
                        FilledTonalButton(
                            onClick = {
                                showDeleteDialog = false
                            }
                        ) {
                            Text("取消")
                        }
                    }
                )
            }

            if (showCreateFileDialog) {
                var fileName by remember { mutableStateOf("") }
                val invalidChars = remember { listOf('/', '\\', ':', '*', '?', '"', '<', '>', '|') }
                var createFail by remember { mutableStateOf(false) }
                AlertDialog(
                    onDismissRequest = { showCreateFileDialog = false },
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
                                val creator = createFile(checkedPath, fileName)
                                if (!creator) createFail = true else {
                                    showCreateFileDialog = false
                                    handleRefresh()
                                }
                            }
                        ) {
                            Text("文件")
                        }
                        Button(
                            onClick = {
                                val creator = createFolder(checkedPath, fileName)
                                if (!creator) createFail = true else {
                                    showCreateFileDialog = false
                                    handleRefresh()
                                }
                            }
                        ) {
                            Text("文件夹")
                        }
                    },
                    dismissButton = {
                        FilledTonalButton(
                            onClick = { showCreateFileDialog = false }
                        ) {
                            Text(" 取消 ")
                        }
                    }
                )
            }

            if (showSortDialog) {
                val isLeft = paneType == PaneType.LEFT
                var selectedSortOption by remember { mutableStateOf(SortOrder.NAME) } // 0=名称, 1=大小, 2=时间, 3=类型
                selectedSortOption = if (isLeft) leftSortOrder else rightSortOrder

                AlertDialog(
                    onDismissRequest = { showSortDialog = false },
                    title = { Text("排序 ${if (isLeft) "左" else "右"}") },
                    text = {
                        Column {
                            // 选项1：按名称排序
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedSortOption = SortOrder.NAME }
                                    .padding(horizontal = 8.dp)
                            ) {
                                RadioButton(
                                    selected = selectedSortOption == SortOrder.NAME,
                                    onClick = { selectedSortOption = SortOrder.NAME }
                                )
                                Text("名称排序", modifier = Modifier.padding(start = 8.dp))
                            }

                            // 选项2：按大小排序
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedSortOption = SortOrder.SIZE }
                                    .padding(horizontal = 8.dp)
                            ) {
                                RadioButton(
                                    selected = selectedSortOption == SortOrder.SIZE,
                                    onClick = { selectedSortOption = SortOrder.SIZE }
                                )
                                Text("大小排序", modifier = Modifier.padding(start = 8.dp))
                            }

                            // 选项3：按时间排序
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedSortOption = SortOrder.TIME }
                                    .padding(horizontal = 8.dp)
                            ) {
                                RadioButton(
                                    selected = selectedSortOption == SortOrder.TIME,
                                    onClick = { selectedSortOption = SortOrder.TIME }
                                )
                                Text("时间排序", modifier = Modifier.padding(start = 8.dp))
                            }

                            // 选项4：按类型排序
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedSortOption = SortOrder.TYPE }
                                    .padding(horizontal = 8.dp)
                            ) {
                                RadioButton(
                                    selected = selectedSortOption == SortOrder.TYPE,
                                    onClick = { selectedSortOption = SortOrder.TYPE }
                                )
                                Text("类型排序", modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (isLeft) {
                                    leftSortOrder = selectedSortOption
                                } else {
                                    rightSortOrder = selectedSortOption
                                }
                                showSortDialog = false
                            }
                        ) {
                            Text("确定")
                        }
                    },
                    dismissButton = {
                        FilledTonalButton(
                            onClick = { showSortDialog = false }
                        ) {
                            Text("取消")
                        }
                    }
                )
            }

            if (showRenameDialog) {
                var fileName by remember { mutableStateOf(checkedFile!!.name) }
                val invalidChars = remember { listOf('/', '\\', ':', '*', '?', '"', '<', '>', '|') }
                var renameFail by remember { mutableStateOf(false) }
                AlertDialog(
                    onDismissRequest = { showRenameDialog = false },
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
                                    checkedFile!!,
                                    File("${checkedFile!!.parent}/$fileName")
                                )
                                if (!renamer) renameFail = true else {
                                    showRenameDialog = false
                                    handleRefresh()
                                }
                            }
                        ) {
                            Text("重命名")
                        }
                    },
                    dismissButton = {
                        FilledTonalButton(
                            onClick = { showRenameDialog = false }
                        ) {
                            Text(" 取消 ")
                        }
                    }
                )
            }

            if (showSearchDialog) {
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
                            val searchPath = checkedPath
                            val totalFiles: Long = try {
                                if (includeSubdirectories) {
                                    Files.walk(searchPath).use { it.count() }
                                } else {
                                    Files.list(searchPath).use { it.count() }
                                }
                            } catch (_: UncheckedIOException) {
                                0
                            }

                            try {
                                val stream = if (includeSubdirectories) {
                                    Files.walk(searchPath)
                                } else {
                                    Files.list(searchPath).onClose { }
                                }

                                stream.use { paths ->
                                    paths.forEach { path ->
                                        try {
                                            processedFiles++
                                            searchProgress = if (totalFiles > 0) {
                                                processedFiles.toFloat() / totalFiles
                                            } else {
                                                0f
                                            }

                                            if (path.fileName.toString()
                                                    .contains(searchFileName, true)
                                            ) {
                                                finded.add(File(path.pathString))
                                            }
                                        } catch (_: AccessDeniedException) { } catch (_: SecurityException) { }
                                    }
                                }
                            } catch (_: UncheckedIOException) { } catch (_: SecurityException) {
                            } finally {
                                isSearching = false
                            }
                        }
                    }
                }

                AlertDialog(
                    onDismissRequest = { showSearchDialog = false },
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
                                            itemData = FileItemData(
                                                file = file,
                                                type = getFileType(file)
                                            ),
                                            onFileClick = {
                                                handleFileClick(
                                                    file = file,
                                                    isLeftPane = paneType == PaneType.LEFT
                                                )
                                            },
                                            onFileLongClick = {
                                                if (paneType == PaneType.LEFT) {
                                                    leftHighLightFiles = listOf(file.name)
                                                    leftPath = Path(file.path)
                                                } else {
                                                    rightHighLightFiles = listOf(file.name)
                                                    rightPath = Path(file.path)
                                                }
                                                showSearchDialog = false
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
                            onClick = { showSearchDialog = false },
                            enabled = !isSearching
                        ) {
                            Text("取消")
                        }
                    }
                )
            }

            if (showAppDetailDialog) {
                val app = checkedFile
                val pm = context.packageManager
                var loadType by remember { mutableStateOf(ExtractProcess.NONE) }
                AlertDialog(
                    onDismissRequest = { showAppDetailDialog = false },
                    text = {
                        Column {
                            val apkInfo =
                                try {
                                    pm.getPackageArchiveInfo(app!!.path, 0)
                                } catch (_: Exception) {
                                    null
                                }

                            if (apkInfo != null) {

                                val isInstalled = try {
                                    pm.getApplicationInfo(apkInfo.packageName, 0)
                                    true
                                } catch (_: PackageManager.NameNotFoundException) {
                                    false
                                }

                                val packageInfo = if (isInstalled) {
                                    val pkgInfo = pm.getPackageInfo(apkInfo.packageName, 0)
                                    PackageInfo(
                                        name = pm.getApplicationLabel(pkgInfo.applicationInfo!!).toString(),
                                        uid = pkgInfo.applicationInfo!!.uid,
                                        versionName = pkgInfo.versionName ?: "未知",
                                        versionCode = pkgInfo.longVersionCode,
                                        packageName = pkgInfo.packageName,
                                        icon = pm.getApplicationIcon(pkgInfo.packageName),
                                        sourceDir = pkgInfo.applicationInfo!!.sourceDir,
                                        dataDir = pkgInfo.applicationInfo!!.dataDir
                                    )
                                } else {
                                    apkInfo.applicationInfo!!.apply {
                                        sourceDir = app!!.absolutePath
                                        publicSourceDir = app.absolutePath
                                    }
                                    PackageInfo(
                                        name = apkInfo.applicationInfo!!.loadLabel(pm).toString(),
                                        uid = null,
                                        packageName = apkInfo.packageName,
                                        versionName = apkInfo.versionName ?: "未知",
                                        versionCode = apkInfo.longVersionCode,
                                        sourceDir = app!!.path,
                                        icon = apkInfo.applicationInfo!!.loadIcon(pm),
                                    )
                                }

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

                                HorizontalDivider(modifier = Modifier.width(230.dp))

                                Spacer(Modifier.height(8.dp))

                                Column {
                                    InfoItem("包名", packageInfo.packageName)
                                    InfoItem("版本号", packageInfo.versionCode.toString())
                                    InfoItem("安装状态", if (isInstalled) "已安装" else "未安装")
                                    InfoItem("大小", formatFileSize(File(packageInfo.sourceDir).length()))
                                    if (isInstalled) {
                                        InfoItem("数据目录", packageInfo.dataDir!!)
                                        InfoItem("安装目录", packageInfo.sourceDir)
                                        InfoItem("UID", packageInfo.uid.toString())
                                    }
                                }

                                when (loadType) {
                                    ExtractProcess.LOADING -> {
                                        LinearProgressIndicator(Modifier.width(230.dp))
                                    }

                                    ExtractProcess.NONE -> {}
                                    ExtractProcess.ERROR -> {
                                        Spacer(Modifier.height(16.dp))
                                        Text("提取失败", color = MaterialTheme.colorScheme.error)
                                    }

                                    ExtractProcess.DONE -> {
                                        Spacer(Modifier.height(16.dp))
                                        Text(
                                            "提取完成, 文件被保存在 $ExtractPath",
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.widthIn(max = 230.dp)
                                        )
                                    }
                                }
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

                            }
                        ) {
                            Text("安装")
                        }
                    },
                    dismissButton = {
                        Row(Modifier.width(60.dp)) {
                            OutlinedIconButton(
                                onClick = {

                                }
                            ) {
                                Icon(imageVector = Icons.Default.MoreVert, null)
                            }
                        }
                    }
                )
            }

            if (showAboutDialog)
                BasicAlertDialog(
                    onDismissRequest = { showAboutDialog = false },
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

                                    Image(
                                        painter = painterResource(R.drawable.ic_launcher_static),
                                        contentDescription = "App icon",
                                        modifier = Modifier.size(48.dp)
                                    )

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
                                            text = "0.0.1 (SnapShot)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                                LinkText(
                                    "软件所用的",
                                    {
                                        val intent = Intent(context, OpenSourceActivity::class.java)
                                        context.startActivity(intent)
                                    },
                                    "开源库",
                                )
                                LinkText(
                                    "加入我们的",
                                    {
                                        val intent = Intent(Intent.ACTION_VIEW, context.getString(R.string.qq_group_link).toUri())
                                        context.startActivity(intent)
                                    },
                                    "QQ 群聊",
                                )

                            }
                        }
                    }
                )
        }
    }
}

data class PaneState(
    val path: Path = Path(RootPath),
    val files: List<File> = emptyList(),
    val highlightFiles: List<String> = emptyList(),
    val selectedFiles: List<String> = emptyList(),
    val sortOrder: SortOrder = SortOrder.NAME
)

@Composable
fun LinkText(
    start: String,
    onClick: () -> Unit,
    linkName: String
) {
    Text(
        buildAnnotatedString {
            append(start)

            withLink(
                LinkAnnotation.Clickable(
                    tag = "1",
                    styles = TextLinkStyles(
                        SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline
                        )
                    ),
                    linkInteractionListener = {
                        onClick()
                    },
                )

            ) {
                append(linkName)
            }
        }
    )
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

enum class PaneType {
    LEFT,
    RIGHT
}

enum class LoadingType {
    NONE,
    FILE,
    DIRECTORY,
    FILE_FAIL,
    DIRECTORY_FAIL,
    PART_FAIL
}

enum class SortOrder {
    NAME,
    SIZE,
    TIME,
    TYPE
} //1623!!!