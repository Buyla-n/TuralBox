package com.tural.box.ui.screen.applist

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tural.box.R
import com.tural.box.util.ExtractPath
import com.tural.box.util.copyFile
import com.tural.box.util.formatFileSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppExtract(){
    val context = LocalContext.current
    var selectedDestination by rememberSaveable { mutableStateOf(Destination.USER) }
    var userApps by remember { mutableStateOf(emptyList<PackageInfo>()) }
    var systemApps by remember { mutableStateOf(emptyList<PackageInfo>()) }
    var checkedApp by remember { mutableStateOf<PackageInfo?>(null) }
    var showAppDetailDialog by remember { mutableStateOf(false) }
    var searchActive by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    val pm = context.packageManager
    val scope = rememberCoroutineScope()

    val filteredUserApps = remember(userApps,searchText) {
        if (!searchActive) {
            userApps
        } else {
            userApps.filter { app ->
                app.applicationInfo!!.loadLabel(context.packageManager)
                    .toString()
                    .contains(searchText, ignoreCase = true) || app.packageName.contains(searchText)
            }
        }
    }

    val filteredSystemApps = remember(systemApps,searchText) {
        if (!searchActive) {
            systemApps
        } else {
            systemApps.filter { app ->
                app.applicationInfo!!.loadLabel(context.packageManager)
                    .toString()
                    .contains(searchText, ignoreCase = true)|| app.packageName.contains(searchText)
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    if (!searchActive) {
                        Text("软件提取", modifier = Modifier.fillMaxWidth())
                    } else {
                        val focusRequester = remember { FocusRequester() }
                        val focusManager = LocalFocusManager.current
                        Surface(
                            shape = RoundedCornerShape(32.dp),
                            modifier = Modifier.height(46.dp)
                        ) {
                            BasicTextField(
                                value = searchText,
                                onValueChange = { searchText = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                                    .padding(horizontal = 16.dp)
                                    .focusRequester(focusRequester)
                                    .wrapContentHeight(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(
                                    onSearch = {
                                        focusManager.clearFocus()
                                    }
                                ),
                                textStyle = MaterialTheme.typography.titleMedium.copy(
                                    textAlign = TextAlign.Unspecified
                                ),

                            )

                            LaunchedEffect(Unit) {
                                focusRequester.requestFocus()
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { (context as ComponentActivity).finish() }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.outline_arrow_back_24),
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (searchText.isEmpty()) {
                                searchActive = !searchActive
                            } else {
                                searchText = ""
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(if (searchActive) R.drawable.outline_close_24 else R.drawable.outline_search_24),
                            contentDescription = null
                        )
                    }

                    IconButton(
                        onClick = {  }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.outline_more_vert_24),
                            contentDescription = null
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                )
            )
        },
        contentWindowInsets = WindowInsets(0,0,0,0)
    ) { contentPadding ->
        Column(modifier = Modifier.padding(contentPadding)) {
            PrimaryTabRow(
                selectedTabIndex = selectedDestination.ordinal,
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Destination.entries.forEachIndexed { index, destination ->
                    Tab(
                        selected = selectedDestination == Destination.entries[index],
                        onClick = {
                            Destination.entries
                            selectedDestination = Destination.entries[index]
                        },
                        text = {
                            Text(
                                text = destination.label,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }
            }

            LaunchedEffect(Unit, searchText) {
                val allApps = withContext(Dispatchers.IO) {
                    pm.getInstalledPackages(PackageManager.GET_META_DATA)
                }
                userApps = allApps.filter {
                    (pm.getApplicationInfo(
                        it.packageName,
                        0
                    ).flags and ApplicationInfo.FLAG_SYSTEM) == 0
                }
                systemApps = allApps.filter {
                    (pm.getApplicationInfo(
                        it.packageName,
                        0
                    ).flags and ApplicationInfo.FLAG_SYSTEM) != 0
                }
                isLoading = false
            }

            fun handleAppClick(
                app: PackageInfo
            ) {
                checkedApp = app
                showAppDetailDialog = true
            }

            if (!isLoading) {
                AnimatedContent(
                    targetState = selectedDestination
                ) { it ->
                    when (it) {
                        Destination.USER -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(filteredUserApps) { app ->
                                    AppItem(app, pm) { handleAppClick(it) }
                                }
                                item {
                                    Spacer(Modifier.height(16.dp))
                                }
                            }
                        }

                        Destination.SYSTEM -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(filteredSystemApps) { app ->
                                    AppItem(app, pm) { handleAppClick(it) }
                                }
                                item {
                                    Spacer(Modifier.height(16.dp))
                                }
                            }
                        }
                    }
                }
            } else {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) { CircularProgressIndicator() }
            }

            if (showAppDetailDialog) {
                val app = checkedApp!!
                var loadType by remember { mutableStateOf(ExtractProcess.NONE) }
                AlertDialog(
                    onDismissRequest = { showAppDetailDialog = false },
                    text = {
                        Column {
                            Row(
                                modifier = Modifier.padding(bottom = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val appIcon = remember(app.packageName) {
                                    try {
                                        pm.getApplicationIcon(app.packageName)
                                    } catch (_: Exception) {
                                        null
                                    }
                                }

                                appIcon?.let { icon ->
                                    AsyncImage(
                                        model = icon,
                                        contentDescription = "App icon",
                                        modifier = Modifier.size(48.dp),
                                        contentScale = ContentScale.Fit,
                                        placeholder = ColorPainter(Color.LightGray)
                                    )
                                } ?: Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(Color.LightGray)
                                )

                                Spacer(modifier = Modifier.width(16.dp))

                                Column {
                                    Text(
                                        text = pm.getApplicationLabel(app.applicationInfo!!).toString(),
                                        style = MaterialTheme.typography.bodyLarge,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.widthIn(max = 160.dp)
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = app.versionName.toString(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }

                            HorizontalDivider(modifier = Modifier.width(230.dp))

                            Spacer(Modifier.height(8.dp))

                            Column {
                                InfoItem("包名", app.packageName)
                                InfoItem("版本号", app.longVersionCode.toString())
                                InfoItem("大小",
                                    formatFileSize(File(app.applicationInfo!!.sourceDir).length())
                                )
                                InfoItem("数据目录", app.applicationInfo!!.dataDir)
                                InfoItem("安装目录", app.applicationInfo!!.sourceDir)
                                InfoItem("UID", app.applicationInfo!!.uid.toString())
                            }

                            when(loadType) {
                                ExtractProcess.LOADING -> { LinearProgressIndicator(Modifier.width(230.dp)) }
                                ExtractProcess.NONE -> { }
                                ExtractProcess.ERROR -> {
                                    Spacer(Modifier.height(16.dp))
                                    Text("提取失败", color = MaterialTheme.colorScheme.error)
                                }
                                ExtractProcess.DONE -> {
                                    Spacer(Modifier.height(16.dp))
                                    Text("提取完成, 文件被保存在 ${ExtractPath}", color = MaterialTheme.colorScheme.primary, modifier = Modifier.widthIn(max = 230.dp))
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                scope.launch(Dispatchers.IO) {
                                    loadType = ExtractProcess.LOADING
                                    val target = File(ExtractPath)
                                    if (!target.exists()) {
                                        target.mkdir()
                                    }
                                    val process = copyFile(
                                        file = File(app.applicationInfo!!.sourceDir),
                                        targetFile = File(
                                            ExtractPath + "/${
                                                pm.getApplicationLabel(
                                                    app.applicationInfo!!
                                                )
                                            }.apk"
                                        )
                                    )
                                    loadType =
                                        if (process) ExtractProcess.DONE else ExtractProcess.ERROR
                                }
                            }
                        ) {
                            Text("提取")
                        }
                    },
                    dismissButton = {
                        Row(Modifier.width(150.dp)) {
                            OutlinedIconButton(
                                onClick = {
                                    context.startActivity(Intent(Intent.ACTION_DELETE)
                                        .apply {
                                            data = Uri.fromParts("package", app.packageName, null)
                                        }
                                    )
                                }
                            ) {
                                Icon(painter = painterResource(R.drawable.outline_delete_24), contentDescription = null)
                            }
                            OutlinedIconButton(
                                onClick = {
                                    context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                        .apply {
                                            data = Uri.fromParts("package", app.packageName, null)
                                        }
                                    )
                                }
                            ) {
                                Icon(painter = painterResource(R.drawable.outline_info_24), contentDescription = null)
                            }
                            OutlinedIconButton(
                                onClick = {
                                    try {
                                        context.startActivity(pm.getLaunchIntentForPackage(app.packageName))
                                    } catch (_: NullPointerException) {
                                        Toast.makeText(context, "应用没有主活动", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Icon(painter = painterResource(R.drawable.outline_open_in_browser_24), contentDescription = null)
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun InfoItem(title: String, summary: String) {
    Row(modifier = Modifier.width(230.dp), horizontalArrangement = Arrangement.SpaceBetween) {
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
@Composable
fun AppItem(
    app: PackageInfo,
    packageManager: PackageManager,
    onClick: (PackageInfo) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onClick(app) }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val appIcon = remember(app.packageName) {
                try {
                    packageManager.getApplicationIcon(app.packageName)
                } catch (_: Exception) {
                    null
                }
            }

            appIcon?.let { icon ->
                AsyncImage(
                    model = icon,
                    contentDescription = "App icon",
                    modifier = Modifier.size(48.dp),
                    contentScale = ContentScale.Fit,
                    placeholder = ColorPainter(Color.LightGray)
                )
            } ?: Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.LightGray)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = packageManager.getApplicationLabel(app.applicationInfo!!).toString(),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

enum class Destination(
    val label: String
) {
    USER("用户"),
    SYSTEM("系统"),
}

enum class ExtractProcess{
    NONE,
    LOADING,
    ERROR,
    DONE
}