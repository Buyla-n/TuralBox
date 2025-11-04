package com.tural.box.util

import com.tural.box.model.SortOrder
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipFile
import java.io.File

data class ZipNode(
    val name: String,
    val isDirectory: Boolean,
    val path: String,
    val children: MutableList<ZipNode> = mutableListOf()
)

fun buildZipTree(zipFile: ZipFile): ZipNode {
    val root = ZipNode("", true, "")
    val pathMap = mutableMapOf("" to root)

    for (entry in zipFile.entries) {
        val path = entry.name.trimEnd('/')
        val parts = path.split("/").filter { it.isNotEmpty() }

        var currentPath = ""
        var parent = root

        for ((i, part) in parts.withIndex()) {
            currentPath = if (currentPath.isEmpty()) part else "$currentPath/$part"
            val isDir = i < parts.size - 1 || entry.isDirectory

            val node = pathMap.getOrPut(currentPath) {
                val newNode = ZipNode(part, isDir, currentPath)
                parent.children.add(newNode)
                newNode
            }

            parent = node
        }
    }
    return root
}

fun findNode(root: ZipNode, path: String): ZipNode? {
    val start = System.nanoTime()
    if (path.isEmpty()) return root
    val parts = path.split("/").filter { it.isNotEmpty() }
    var node: ZipNode? = root
    for (part in parts) {
        node = node?.children?.find { it.name == part}
    }
    val end = System.nanoTime()
    println(end - start)
    return node
}

fun accessCompressFile(file: String, path: String, sortOrder: SortOrder): List<CompressFile> {
    ZipFile.builder().setFile(File(file)).get().use { zipFile ->
        val root = buildZipTree(zipFile)
        val targetNode = findNode(root, path.removePrefix(file)) ?: root
        val entryMap = zipFile.entries.asSequence().associateBy { it.name }

        val result = targetNode.children.map { child ->
            val entry = entryMap[child.path]
            CompressFile(file, child.isDirectory, entry ?: ZipArchiveEntry(child.path))
        }

        return result.sortedWith(
            compareBy<File> { !it.isDirectory }
                .then(
                    when (sortOrder) {
                        SortOrder.NAME -> compareBy { it.name.lowercase() }
                        SortOrder.TYPE -> compareBy { it.extension.lowercase() }
                        SortOrder.SIZE -> compareBy { it.length() }
                        SortOrder.TIME -> compareByDescending { it.lastModified() }
                    }
                )
        )

    }
}

data class CompressFile(
    val zipPath: String,
    val isDir: Boolean,
    val entry: ZipArchiveEntry
): File(zipPath + "/" + entry.name) {
    override fun getName(): String = entry.name.substringAfterLast("/")
    override fun getPath(): String = "$zipPath!/${entry.name}"
    override fun isDirectory(): Boolean = isDir
    override fun isFile(): Boolean = !isDir
    override fun length(): Long = entry.size
    override fun lastModified(): Long = entry.time
}

