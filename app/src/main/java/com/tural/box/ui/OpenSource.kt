package com.tural.box.ui

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.tural.box.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenSourceScreen(onBackPressed: () -> Unit) {
    val libraries = listOf(
        Library(
            name = "AndroidX Core KTX",
            license = "Apache 2.0",
            website = "https://developer.android.com/jetpack/androidx"
        ),
        Library(
            name = "AndroidX Lifecycle Runtime KTX",
            license = "Apache 2.0",
            website = "https://developer.android.com/jetpack/androidx"
        ),
        Library(
            name = "AndroidX Activity Compose",
            license = "Apache 2.0",
            website = "https://developer.android.com/jetpack/androidx"
        ),
        Library(
            name = "AndroidX Compose BOM",
            license = "Apache 2.0",
            website = "https://developer.android.com/jetpack/compose"
        ),
        Library(
            name = "AndroidX Compose UI",
            license = "Apache 2.0",
            website = "https://developer.android.com/jetpack/compose"
        ),
        Library(
            name = "AndroidX Compose Material 3",
            license = "Apache 2.0",
            website = "https://developer.android.com/jetpack/compose"
        ),
        Library(
            name = "AndroidX Navigation Compose",
            license = "Apache 2.0",
            website = "https://developer.android.com/jetpack/compose/navigation"
        ),
        Library(
            name = "AndroidX Material Icons Extended",
            license = "Apache 2.0",
            website = "https://developer.android.com/jetpack/androidx"
        ),
        Library(
            name = "Coil Compose",
            license = "Apache 2.0",
            website = "https://coil-kt.github.io/coil/compose/"
        ),
        Library(
            name = "Scale",
            license = "Apache 2.0",
            website = "https://github.com/jvziyaoyao/scale"
        ),
        Library(
            name = "ExoPlayer",
            license = "Apache 2.0",
            website = "https://github.com/google/ExoPlayer"
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("开源库声明") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(painter = painterResource(R.drawable.outline_arrow_back_24), contentDescription = "返回")
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
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(Modifier.height(16.dp))
            }
            items(libraries) { library ->
                LibraryItem(library = library)
            }
            item {
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun LibraryItem(library: Library) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = library.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "许可证: ${library.license}",
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = library.website,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary,
                style = TextStyle(textDecoration = TextDecoration.Underline),
                modifier = Modifier.clickable(onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, library.website.toUri())
                    context.startActivity(intent)
                })
            )
        }
    }
}

data class Library(
    val name: String,
    val license: String,
    val website: String
)