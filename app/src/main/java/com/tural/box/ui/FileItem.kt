package com.tural.box.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.tural.box.R
import java.io.File
import java.nio.file.Files
import kotlin.io.path.Path

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileItem(
    file: File,
    type: FileType,
    highLight: Boolean = false,
    selected: Boolean = false,
    onFileClick: () -> Unit,
    onFileLongClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .background(
                color = if (!selected) Color.Transparent else MaterialTheme.colorScheme.primaryContainer
            )
            .combinedClickable(
                onClick = {
                    onFileClick()
                },
                onLongClick = {
                    onFileLongClick()
                }
            )
            .fillMaxWidth()
            .padding(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val icon = getFileIcon(type)

        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .border(
                    width = 1.dp,
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primaryContainer
                )
                .padding(4.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(
                text = file.name,
                style = MaterialTheme.typography.titleSmall,
                color = if (!highLight) Color.Unspecified else MaterialTheme.colorScheme.primary
            )
            if (file.isFile) {
                Text(
                    text = formatFileSize(file.length()),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(Modifier.weight(1f))

        if (Files.isSymbolicLink(Path(file.path))) {
            Icon(
                painter = painterResource(R.drawable.baseline_insert_link_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun UpwardItem(
    cps: PanelStates
) {
    Surface(
        onClick = {
//            if (parseZipPath(cps.path.pathString).isEmpty() && cps.isInZip) {
//                cps.isInZip = false
//                cps.zipFile = null
//            }
            if (!cps.path.isRootPath()) cps.path = cps.path.parent
        },
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.outline_arrow_upward_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primaryContainer
                    )
                    .padding(4.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(
                    text = "..",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = " ",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

enum class FileType {
    FOLDER,
    FILE,
    TEXT,
    AUDIO,
    IMAGE,
    VIDEO,
    ARCHIVE,
    INSTALLABLE,
    XML,
    SCRIPT,
    FONT
}