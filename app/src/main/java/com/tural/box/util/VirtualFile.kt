package com.tural.box.util

interface VirtualFile {
    val name: String
    val path: String
    val isDirectory: Boolean
    val size: Long
    val lastModified: Long
    val children: MutableList<VirtualFile>?
}