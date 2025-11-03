package com.tural.box.dialog

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tural.box.data.PackageData
import com.tural.box.util.formatFileSize
import com.tural.box.util.install
import java.io.File

@Composable
fun ApkDetailDialog(
    dialogManager: DialogManager,
    context: Context,
    targetFile: File
) {
    val pm = context.packageManager
    AlertDialog(
        onDismissRequest = { dialogManager.showAppDetail = false },
        text = {
            Column {
                val apkInfo = try { pm.getPackageArchiveInfo(targetFile.path, 0) } catch (_: Exception) { null }

                if (apkInfo != null) {

                    apkInfo.applicationInfo!!.apply {
                        sourceDir = targetFile.absolutePath
                        publicSourceDir = targetFile.absolutePath
                    }

                    val isIn = try {
                        pm.getApplicationInfo(apkInfo.packageName, 0)
                        true
                    } catch (_: PackageManager.NameNotFoundException) {
                        false
                    }

                    val pkgInfo = if (isIn) pm.getPackageInfo(apkInfo.packageName, 0) else null

                    val packageInfo =
                        PackageData(
                            name = apkInfo.applicationInfo!!.loadLabel(pm).toString(),
                            uid = if (isIn) pkgInfo!!.applicationInfo!!.uid else null,
                            versionName = apkInfo.versionName ?: "未知",
                            versionCode = apkInfo.longVersionCode,
                            packageName = apkInfo.packageName,
                            icon = apkInfo.applicationInfo!!.loadIcon(pm),
                            sourceDir = if (isIn) pkgInfo!!.applicationInfo!!.sourceDir else targetFile.path,
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
                        InfoItem("大小", formatFileSize(File(targetFile.path).length()))
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
                    install(context, targetFile)
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