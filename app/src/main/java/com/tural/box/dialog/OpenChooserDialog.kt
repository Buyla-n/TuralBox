package com.tural.box.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.tural.box.R
import com.tural.box.model.FileType
import java.io.File

@Composable
fun OpenChooserDialog(
    dialogManager: DialogManager,
    onFileClick: (FileType) -> Unit
) {
    AlertDialog(
        onDismissRequest = { dialogManager.showOpenChooser = false },
        confirmButton = {
            Button(
                onClick = { dialogManager.showOpenChooser = false }
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
                            onFileClick(type)
                            dialogManager.showOpenChooser = false
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