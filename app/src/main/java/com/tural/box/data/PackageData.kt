package com.tural.box.data

import android.graphics.drawable.Drawable

data class PackageData(
    val name: String,
    val packageName: String,
    val versionName: String,
    val versionCode: Long,
    val uid: Int? = null,
    val sourceDir: String,
    val dataDir: String? = null,
    val icon: Drawable
)