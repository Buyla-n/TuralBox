package com.tural.box.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tural.box.model.PanelPosition
import com.tural.box.model.SortOrder
import com.tural.box.ui.screen.main.PanelStates

@Composable
fun SortOrderDialog(
    dialogManager: DialogManager,
    panelStates: PanelStates,
    currentPanel: PanelPosition
) {
    var selectedSortOption by remember { mutableStateOf(SortOrder.NAME) }
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