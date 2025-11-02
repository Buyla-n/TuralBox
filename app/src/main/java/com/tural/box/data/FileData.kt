package com.tural.box.data

import com.tural.box.util.VirtualFile
import java.io.File
import java.util.zip.ZipEntry

data class LocalFile(val file: File) : VirtualFile {
    override val name get() = file.name
    override val path get() = file.path
    override val isDirectory get() = file.isDirectory
    override val size get() = file.length()
    override val lastModified get() = file.lastModified()
    override val children: MutableList<VirtualFile>? get() = null
}

data class ZipVirtualFile(
    val entry: ZipEntry,
    val zipPath: String,
    override val children: MutableList<VirtualFile>? = mutableListOf()
) : VirtualFile {
    override val name get() = entry.name.substringAfterLast("/")
    override val path get() = "$zipPath!/${entry.name}"
    override val isDirectory get() = entry.isDirectory
    override val size get() = entry.size
    override val lastModified get() = entry.time
}
