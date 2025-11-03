package com.tural.box.dialog

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tural.box.BuildConfig
import com.tural.box.activity.LicensesActivity
import com.tural.box.icons.AppIcon
import com.tural.box.model.FileType
import com.tural.box.model.PanelPosition
import com.tural.box.model.SortOrder
import com.tural.box.ui.screen.main.PanelStates
import com.tural.box.util.formatFileDate
import com.tural.box.util.getFileSize
import com.tural.box.util.getFileType
import com.tural.box.util.invalidChars
import com.tural.box.util.refresh
import com.tural.box.util.renameFile
import kotlinx.coroutines.CoroutineScope
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.pathString

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
        FileToolDialog(dialogManager, currentPanel, currentFile!!, context)
    }
    if (dialogManager.showDelete) {
        DeleteFileDialog(dialogManager, currentFile!!, scope) { refresh(scope, panelStates) }
    }
    if (dialogManager.showCopy) {
        CopyFileDialog(dialogManager, currentFile!!, scope, negativePanelStates.path) {
            refresh(scope, negativePanelStates)
        }
    }
    if (dialogManager.showMove) {
        MoveFileDialog(dialogManager, currentFile!!, scope, negativePanelStates.path) {
            refresh(scope, panelStates)
            refresh(scope, negativePanelStates)
        }
    }
    if (dialogManager.showCreateFile) {
        CreateFileDialog(dialogManager, panelStates.path) {
            panelStates.highLightFiles = setOf(it)
            refresh(scope, panelStates)
        }
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
                            currentFile,
                            File("${currentFile.parent}/${textFieldState.text}")
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
        SearchFileDialog(dialogManager, panelStates.path) {
            panelStates.highLightFiles = setOf(it.name)
            panelStates.path = Path(it.path)
            dialogManager.showSearch = false
        }
    }
    if (dialogManager.showAppDetail) {
        ApkDetailDialog(dialogManager, context, currentFile!!)
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
    if (dialogManager.showOpenChooser) {
        OpenChooserDialog(dialogManager) { onFileClick(currentFile!!, it) }
    }
    if (dialogManager.showAudio) {
        AudioPlayerDialog(dialogManager, currentFile!!)
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