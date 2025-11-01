package com.tural.box.ui

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.tural.box.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@SuppressLint("CoroutineCreationDuringComposition", "SdCardPath")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Terminal(filePath: String) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberLazyListState()
    var commandInput by remember { mutableStateOf("") }
    val history = remember { mutableStateListOf<TerminalItem>() }
    var isAutoScrollEnabled by remember { mutableStateOf(true) }
    var lastCommand by remember { mutableStateOf("") }

    LaunchedEffect(scrollState.isScrollInProgress) {
            val atBottom = scrollState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ==
                    scrollState.layoutInfo.totalItemsCount - 1
            isAutoScrollEnabled = atBottom
    }

    LaunchedEffect(history.size) {
        if (isAutoScrollEnabled) {
            scrollState.scrollToItem(history.size)
        }
    }
    val process = remember {
        ProcessBuilder("sh").start()
    }
    val inputStream = remember { process.outputStream }
    val outputStream = remember { process.inputStream.bufferedReader() }
    val errorStream = remember { process.errorStream.bufferedReader() }

    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            while (true) {
                val line = outputStream.readLine() ?: break
                withContext(Dispatchers.Main) {
                    history.add(TerminalItem.Output(line))

                }
            }
        }

        coroutineScope.launch(Dispatchers.IO) {
            while (true) {
                val line = errorStream.readLine() ?: break
                withContext(Dispatchers.Main) {
                    history.add(TerminalItem.Error(line))
                }
            }
        }
    }
    // 执行
    fun executeCommand(command: String) {
        coroutineScope.launch(Dispatchers.IO) {
            history.add(TerminalItem.Command(command))
            inputStream.write("$command\n".toByteArray())
            inputStream.flush()
        }
        if (command == "clear") {
            history.clear()
        }
        if (command == "exit") {
            history.add(TerminalItem.Command("Shell 进程退出"))
            process.destroy()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("模拟终端") },
                navigationIcon = {
                    IconButton(onClick = { (context as? ComponentActivity)?.finish() }) {
                        Icon(
                            painter = painterResource(R.drawable.outline_arrow_back_24),
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        contentWindowInsets = WindowInsets(0,0,0,0)
    ) { innerPadding ->
        val focusRequester = remember { FocusRequester() }

        if (filePath.isNotEmpty()) {
            executeCommand(filePath)
        }

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {

            val imeInsets = WindowInsets.ime.getBottom(LocalDensity.current)
            val bottomPadding = with(LocalDensity.current) { imeInsets.toDp() }
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(bottom = bottomPadding),
                state = scrollState,
            ) {
                item {
                    Spacer(Modifier.padding(top = 2.dp))
                }
                itemsIndexed(history) { _, item ->
                    when (item) {
                        is TerminalItem.Command -> CommandText(item.text)
                        is TerminalItem.Output -> OutputText(item.text)
                        is TerminalItem.Error -> ErrorText(item.text)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
                item("input") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 添加路径提示
                        Text(
                            text = "> ",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        BasicTextField(
                            value = commandInput,
                            onValueChange = { commandInput = it },
                            keyboardActions = KeyboardActions(
                                onAny = {
                                    lastCommand = commandInput
                                    executeCommand(commandInput)
                                    commandInput = ""
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
                            singleLine = true,
                            textStyle = TextStyle.Default.copy(color = MaterialTheme.colorScheme.onSurface)
                        )
                        LaunchedEffect(lastCommand) {
                            focusRequester.requestFocus()
                        }
                    }
                }
                item {
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

sealed class TerminalItem {
    data class Command(val text: String) : TerminalItem()
    data class Output(val text: String) : TerminalItem()
    data class Error(val text: String) : TerminalItem()
}

@Composable
private fun CommandText(text: String) {
    Text(
        text = "> $text",
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}

@Composable
private fun OutputText(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}

@Composable
private fun ErrorText(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}