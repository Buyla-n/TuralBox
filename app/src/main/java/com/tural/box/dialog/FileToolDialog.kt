package com.tural.box.dialog

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.tural.box.R
import com.tural.box.model.PanelPosition
import com.tural.box.util.shareFile
import java.io.File

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun FileToolDialog(
    dialogManager: DialogManager,
    currentPanel: PanelPosition,
    targetFile: File,
    context: Context
) {
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
                                dialogManager.showOpenChooser = true
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
                                context.shareFile(targetFile)
                            },
                            enabled = !targetFile.isDirectory
                        )
                    }
                }
            }
        }
    )
}