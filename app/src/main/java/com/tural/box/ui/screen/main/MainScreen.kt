package com.tural.box.ui.screen.main

import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tural.box.R
import com.tural.box.activity.AppListActivity
import com.tural.box.activity.FontActivity
import com.tural.box.activity.ImageActivity
import com.tural.box.activity.SettingsActivity
import com.tural.box.activity.TerminalActivity
import com.tural.box.activity.TextEditorActivity
import com.tural.box.activity.VideoActivity
import com.tural.box.dialog.DialogContainer
import com.tural.box.dialog.DialogManager
import com.tural.box.model.FileType
import com.tural.box.model.PanelPosition
import com.tural.box.util.RootPath
import com.tural.box.util.accessFiles
import com.tural.box.util.getFileType
import com.tural.box.util.isRootPath
import com.tural.box.util.refresh
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.pathString

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class,
    ExperimentalLayoutApi::class
)
@Composable
fun MainScreen(
    context: Context
) {
    val scope = rememberCoroutineScope()
    var currentPanel by remember { mutableStateOf(PanelPosition.LEFT) }
    var currentPath by remember { mutableStateOf(Path(RootPath)) }
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

    fun handleFileClick(file: File, type: FileType? = null) {
        if (file.isDirectory) {
            currentPanelState().path = file.toPath()
        } else {
            dialogManager.currentFile = file
            when (type ?: getFileType(file)) {
                FileType.TEXT -> context.startActivity(
                    Intent(
                        context,
                        TextEditorActivity::class.java
                    ).putExtra("filePath", file.path)
                )

                FileType.IMAGE -> context.startActivity(
                    Intent(
                        context,
                        ImageActivity::class.java
                    ).putExtra("filePath", file.path)
                )

                FileType.INSTALLABLE -> {
                    dialogManager.showAppDetail = true
                }

                FileType.XML -> context.startActivity(
                    Intent(
                        context,
                        TextEditorActivity::class.java
                    ).putExtra("filePath", file.path)
                )

                FileType.FONT -> context.startActivity(
                    Intent(
                        context,
                        FontActivity::class.java
                    ).putExtra("filePath", file.path)
                )

                FileType.VIDEO -> context.startActivity(
                    Intent(
                        context,
                        VideoActivity::class.java
                    ).putExtra("filePath", file.path)
                )

                FileType.AUDIO -> {
                    dialogManager.showAudio = true
                }

                FileType.ARCHIVE -> {
                    val cps = currentPanelState()
                    cps.zipFile = file
                    cps.previousPath = cps.path
                    cps.isInZip = true
                    cps.path = Path("${file.path}")
                }

                else -> {
                    dialogManager.showOpenChooser = true
                }
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
                drawerShape = MaterialTheme.shapes.extraLarge.copy(
                    topStart = CornerSize(0.dp),
                    bottomStart = CornerSize(0.dp)
                )
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
                                painter = painterResource(R.drawable.outline_info_24),
                                contentDescription = null
                            )
                        },
                        onClick = {
                            dialogManager.showAbout = true
                        }
                    )
                    HorizontalDivider()

                    Text(
                        "存储",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                    NavigationDrawerItem(
                        label = {
                            var storageProgress by remember { mutableFloatStateOf(0f) }

                            LaunchedEffect(Unit) {
                                val progress = withContext(Dispatchers.IO) {
                                    try {
                                        val stat =
                                            StatFs(Environment.getExternalStorageDirectory().path)
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
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
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

                    Text(
                        "工具",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                    NavigationDrawerItem(
                        label = { Text("已安装的应用") },
                        selected = false,
                        icon = {
                            Icon(
                                painterResource(R.drawable.outline_unarchive_24),
                                contentDescription = null
                            )
                        },
                        onClick = {
                            context.startActivity(Intent(context, AppListActivity::class.java))
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text("终端") },
                        selected = false,
                        icon = {
                            Icon(
                                painterResource(R.drawable.outline_terminal_24),
                                contentDescription = null
                            )
                        },
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
                                        refresh(scope, currentPanelState())
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
                                    onClick = {
                                        context.startActivity(
                                            Intent(
                                                context,
                                                SettingsActivity::class.java
                                            )
                                        )
                                    }
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
                            IconButton(onClick = { refresh(scope, currentPanelState()) }) {
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

                LaunchedEffect(leftPanelState.path, Unit) {
                    if (leftPanelState.highLightFiles.any { leftPanelState.path.endsWith(it) }) {
                        leftPanelState.path = leftPanelState.path.parent
                    }

                    leftPanelState.files = withContext(Dispatchers.IO) {
                        accessFiles(leftPanelState.path, leftPanelState.sortOrder)
                    }

                    scope.launch(Dispatchers.Main) {
                        if (leftPanelState.highLightFiles.isNotEmpty()) {
                            delay(50)
                            val index = leftPanelState.files.indexOfFirst { file ->
                                file.name.equals(
                                    leftPanelState.highLightFiles.first(),
                                    ignoreCase = true
                                )
                            }
                            if (index != -1) {
                                leftLazyState.scrollToItem(index)
                            }
                        }
                    }
                }

                LaunchedEffect(rightPanelState.path, Unit) {
                    if (rightPanelState.highLightFiles.any { rightPanelState.path.endsWith(it) }) {
                        rightPanelState.path = rightPanelState.path.parent
                    }

                    rightPanelState.files = withContext(Dispatchers.IO) {
                        accessFiles(rightPanelState.path, rightPanelState.sortOrder)
                    }

                    scope.launch(Dispatchers.Main) {
                        if (rightPanelState.highLightFiles.isNotEmpty()) {
                            delay(50)
                            val index = rightPanelState.files.indexOfFirst { file ->
                                file.name.equals(
                                    rightPanelState.highLightFiles.first(),
                                    ignoreCase = true
                                )
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
                    }
                }

                fun handleFileLongClick(file: File) {
                    dialogManager.currentFile = file
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
                            FileRow(
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
                            FileRow(
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

            DialogContainer(
                context = context,
                scope = scope,
                dialogManager = dialogManager,
                panelStates = currentPanelState(),
                negativePanelStates = uncurrentPanelState(),
                currentPanel = currentPanel,
                onFileClick = { file, type ->
                    handleFileClick(file, type)
                }
            )
        }
    }
}