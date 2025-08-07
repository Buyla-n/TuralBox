package com.tural.box.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileItem(
    itemData: FileItemData,
    onFileClick: (File) -> Unit,
    onFileLongClick: (File) -> Unit,
    onCheck: (File) -> Unit = {}
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.StartToEnd || it == SwipeToDismissBoxValue.EndToStart) {
                onCheck(itemData.file)
            }
            false
        }
    )
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
        }
    ) {
        Row(
            modifier = Modifier
                .background(
                    color = if (!itemData.selected) Color.Transparent else MaterialTheme.colorScheme.primaryContainer
                )
                .combinedClickable(
                    onClick = {
                        onFileClick(itemData.file)
                    },
                    onLongClick = {
                        onFileLongClick(itemData.file)
                    }
                )
                .fillMaxWidth()
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = getFileIcon(itemData.type)

            Icon(
                imageVector = icon,
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
                    text = itemData.file.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (!itemData.highLight) Color.Unspecified else MaterialTheme.colorScheme.primary
                )
                if (itemData.file.isFile)
                    Text(
                        text = formatFileSize(itemData.file.length()),
                        style = MaterialTheme.typography.bodySmall
                    )
            }
        }
    }
}

@Composable
fun UpwardItem(
    onFileClick: () -> Unit
) {
    Surface(
        onClick = {
            onFileClick()
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
                imageVector = Icons.Default.ArrowUpward,
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

data class FileItemData(
    val file: File,
    val type: FileType,
    val highLight: Boolean = false,
    val selected: Boolean = false
)

enum class FileType {
    FOLDER,
    FILE,
    TEXT,
    AUDIO,
    IMAGE,
    VIDEO,
    PACKAGE,
    INSTALL,
    XML,
    SHELL
}