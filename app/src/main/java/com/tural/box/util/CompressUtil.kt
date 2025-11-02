package com.tural.box.util

import com.tural.box.data.ZipVirtualFile
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipFile

fun buildZipTree(zipPath: String): ZipVirtualFile {
    val zipFile = ZipFile(zipPath)
    val root = ZipVirtualFile(ZipArchiveEntry(""), zipPath)
    val pathMap = mutableMapOf("" to root)

    for (entry in zipFile.entries) {
        val path = entry.name
        val parts = path.split("/").filter { it.isNotEmpty() }

        var currentPath = ""
        var parent = root

        for ((i, part) in parts.withIndex()) {
            currentPath = if (currentPath.isEmpty()) part else "$currentPath/$part"
            val node = pathMap.getOrPut(currentPath) {
                val newNode = ZipVirtualFile(ZipArchiveEntry(part), zipPath)
                parent.children?.add(newNode)
                newNode
            }

            parent = node
        }
    }

    zipFile.close()
    return root
}

fun findNode(root: ZipVirtualFile, path: String): ZipVirtualFile? {
    if (path.isEmpty()) return root
    val parts = path.split("/").filter { it.isNotEmpty() }
    var node: ZipVirtualFile? = root
    for (part in parts) {
        node = node?.children?.find { it.name == part && it.isDirectory } as ZipVirtualFile?
    }
    return node
}

fun getCompressFiles(zipPath: String, path: String): List<VirtualFile>? {
    return findNode(buildZipTree(zipPath), path)?.children
}