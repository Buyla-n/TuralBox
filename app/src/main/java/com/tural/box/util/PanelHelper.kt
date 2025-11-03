package com.tural.box.util

import com.tural.box.ui.screen.main.PanelStates
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun refresh(scope: CoroutineScope, state: PanelStates) {
    scope.launch(Dispatchers.IO) {
        state.files = accessFiles(state.path, state.sortOrder)
    }
}