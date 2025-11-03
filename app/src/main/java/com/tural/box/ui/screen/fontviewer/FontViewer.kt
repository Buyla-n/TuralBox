package com.tural.box.ui.screen.fontviewer

import android.content.Context
import android.graphics.Typeface
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tural.box.R
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FontViewer(
    context: Context,
    filePath: String
) {
    val file = File(filePath)

    val fontFamily = remember { mutableStateOf<FontFamily?>(null) }
    val isLoading = remember { mutableStateOf(true) }
    val error = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(filePath) {
        val typeface = try {
            Typeface.createFromFile(File(filePath))
        } catch (_: Exception) {
            null
        }

        if (typeface != null) {
            fontFamily.value = FontFamily(typeface)
            isLoading.value = false
        } else {
            error.value = "无法加载字体"
            isLoading.value = false
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(file.name, maxLines = 1, softWrap = false, overflow = TextOverflow.StartEllipsis) },
                navigationIcon = {
                    IconButton(onClick = {
                        (context as ComponentActivity).finish()
                    }) {
                        Icon(painterResource(R.drawable.outline_arrow_back_24), "返回")
                    }
                },
                actions = {
                    IconButton(onClick = {  }) {
                        Icon(painter = painterResource(R.drawable.outline_edit_24), null)
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
        Box(
            modifier = Modifier.padding(contentPadding),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading.value -> {
                    CircularProgressIndicator()
                }
                error.value != null -> {
                    Text(text = error.value!!, color = MaterialTheme.colorScheme.error)
                }
                else -> {
                    val fontFamily = fontFamily.value
                    val textStyles = listOf(
                        TextStyle(fontFamily = fontFamily, fontSize = 12.sp),
                        TextStyle(fontFamily = fontFamily),
                        TextStyle(fontFamily = fontFamily, fontSize = 16.sp),
                        TextStyle(fontFamily = fontFamily, fontSize = 20.sp),
                        TextStyle(fontFamily = fontFamily, fontSize = 24.sp),
                    )

                    val sampleText = "A Community, Built by All.\n" +
                            "世界上的每一个人，构成了世界。"

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        item {
                            Text(
                                text = "abcdefghijklmnopqrstuvwxyz\nABCDEFGHIJKLMNOPQRSTUVWXYZ",
                                style = TextStyle(fontFamily = fontFamily, fontSize = 16.sp)
                            )
                        }

                        item {
                            Text(
                                text = "1234567890\n . : , ; ' \" ( ! ? ) + - * / = < \n> { } $ [ ] | \\ ~ ` @ # % ^ & _",
                                style = TextStyle(fontFamily = fontFamily, fontSize = 16.sp)
                            )
                        }

                        item {
                            Column {
                                textStyles.forEach { style ->
                                    Text(
                                        text = sampleText,
                                        style = style,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 2.dp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}