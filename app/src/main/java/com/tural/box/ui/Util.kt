package com.tural.box.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.tural.box.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.net.URLConnection
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.pathString

const val RootPath = "/storage/emulated/0"
const val ExtractPath = "/storage/emulated/0/TuralBox/Extract"
var rootMode = false
val invalidChars = listOf('/', '\\', ':', '*', '?', '"', '<', '>', '|')

fun getFileIcon(type: FileType) : Int {
    return when(type) {
        FileType.FOLDER -> R.drawable.baseline_folder_24
        FileType.TEXT -> R.drawable.outline_description_24
        FileType.AUDIO -> R.drawable.outline_audio_file_24
        FileType.IMAGE -> R.drawable.outline_image_24
        FileType.VIDEO -> R.drawable.outline_video_file_24
        FileType.ARCHIVE -> R.drawable.outline_folder_zip_24
        FileType.INSTALLABLE -> R.drawable.outline_android_24
        FileType.XML -> R.drawable.outline_description_24
        FileType.SCRIPT -> R.drawable.outline_description_24
        FileType.FONT -> R.drawable.outline_font_download_24
        else -> R.drawable.outline_insert_drive_file_24
    }
}

fun accessFiles(path: Path, sortOrder: SortOrder): List<File> {
    try {
        val files: List<File> = if (!rootMode) {
            path.toFile().listFiles()?.toList()!!
        } else {
            ProcessBuilder("su", "-c", "ls", "-1", path.pathString) // -1 每行一个文件
                .redirectErrorStream(true)
                .start()
                .inputStream.bufferedReader()
                .use { reader ->
                    reader.readLines().map { name -> File(File(path.pathString), name) }
                }
        }

        return files.sortedWith(
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
    } catch (e: Exception) {
        e.printStackTrace()
        return emptyList()
    }
}

//fun accessArchiveEntry(zipFile: File, path: Path, sortOrder: SortOrder): List<File> {
//    try {
//        SevenZip.initSevenZipFromPlatformJAR()
//        val innerPath = parseZipPath(path.pathString)
//        val archiveEntries = mutableListOf<File>()
//
//        RandomAccessFile(zipFile.path, "r").use { raf ->
//            SevenZip.openInArchive(null, RandomAccessFileInStream(raf)).use { inArchive ->
//                for (i in 0 until inArchive.numberOfItems) {
//                    archiveEntries.add(createVirtualFile(i, zipFile, inArchive, "$innerPath/"))
//                }
//            }
//        }
//
////        ZipFile(zipFile).use { zip ->
////            val entries: Enumeration<ZipArchiveEntry> = zip.entries
////
////            while (entries.hasMoreElements()) {
////                val entry = entries.nextElement()
////                val name = entry.name.removePrefix(innerPath + "/")
////                val name2 = if (!entry.isDirectory) name + "/" else name
////
////                archiveEntries.add(createVirtualFile(zipFile, entry, "$innerPath/"))
////            }
////        }
//
//
//        return archiveEntries.sortedWith(
//            compareBy<File> { !it.isDirectory }
//                .then(
//                    when (sortOrder) {
//                        SortOrder.NAME -> compareBy { it.name.lowercase() }
//                        SortOrder.TYPE -> compareBy { it.extension.lowercase() }
//                        SortOrder.SIZE -> compareBy { it.length() }
//                        SortOrder.TIME -> compareByDescending { it.lastModified() }
//                    }
//                )
//        )
//    } catch (e: Exception) {
//        e.printStackTrace()
//        return emptyList()
//    }
//}
//
//private fun createVirtualFile(i: Int, zipFile: File, entry: IInArchive, innerPath: String): File {
//    val name = entry.getProperty(i, PropID.NAME) as String
//    return object : File("${zipFile.path}${separator}${name}") {
//        override fun exists(): Boolean = true
//        override fun isDirectory(): Boolean = entry.getProperty(i, PropID.IS_FOLDER) as Boolean
//        override fun isFile(): Boolean = !(entry.getProperty(i, PropID.IS_FOLDER) as Boolean)
//        override fun length(): Long = entry.getProperty(i, PropID.SIZE) as Long
//        override fun getName(): String = name.removePrefix(innerPath).removeSuffix("/")
//        override fun getPath(): String = zipFile.path + "/" + name
//
//        override fun toString(): String {
//            return "ZipEntryFile[name=${getName()}, path=${zipFile.path + "/" + name}, " +
//                    "size=${length()}, isDir=$isDirectory]"
//        }
//    }
//}
//
//fun parseZipPath(zipPath: String): String {
//    val zipFileEndIndex = zipPath.indexOf(".zip") + 4
//    val innerPath = zipPath.substring(zipFileEndIndex).removePrefix("/")
//    return innerPath
//}


fun getFileType(file: File): FileType {
    return if (file.isDirectory) FileType.FOLDER else when (file.extension.lowercase()) {
        "txt", "prop", "conf", "json" -> FileType.TEXT
        "jpg", "jpeg", "png", "gif", "webp" -> FileType.IMAGE
        "mp3", "wav", "ogg" -> FileType.AUDIO
        "mp4" -> FileType.VIDEO
        "sh", "rc" -> FileType.SCRIPT
        "ttf", "otf" -> FileType.FONT
        "apk" -> FileType.INSTALLABLE
        "xml" -> FileType.XML
        "zip", "rar", "7z" -> FileType.ARCHIVE
        else -> FileType.FILE
    }
}

fun formatFileSize(sizeInBytes: Long): String {
    return when {
        sizeInBytes < 0x400 -> "$sizeInBytes B"
        sizeInBytes < 0x100000 -> "%.1f KB".format(sizeInBytes / 1024.0)
        sizeInBytes < 0x40000000 -> "%.1f MB".format(sizeInBytes / (1024.0 * 1024.0))
        else -> "%.1f GB".format(sizeInBytes / (1024.0 * 1024.0 * 1024.0))
    }
}

fun createFile(directory: Path, fileName: String): Boolean {
    try {
        val file = directory.resolve(fileName).toFile()
        return file.createNewFile()
    } catch (_: IOException) {
        return false
    }
}

fun renameFile(file: File, targetFile: File): Boolean {
    return try {
        file.renameTo(targetFile)
    } catch (_: IOException) {
        false
    }
}

fun copyFile(file: File, targetFile: File): Boolean {
    try {
        file.copyTo(targetFile)
        return true
    } catch (_: IOException) {
        return false
    }
}

fun moveFile(file: File, targetFile: File): Boolean {
    try {
        Files.move(Path(file.path), Path(targetFile.path))
        return true
    } catch (_: IOException) {
        return false
    }
}

fun deleteFile(file: File): Boolean {
    return try {
        file.delete()
    } catch (_: IOException) {
        false
    }
}

fun deleteFolder(folder: File): Flow<FileChangeProgress> = flow {
    require(folder.isDirectory) { "Path must be a directory" }

    val allFiles = folder.walkBottomUp().toList()
    val totalCount = allFiles.size
    if (totalCount == 0) {
        emit(FileChangeProgress.Completed( true))
        return@flow
    }

    var failedCount = 0

    allFiles.forEachIndexed { index, file ->
        try {
            val isDeleted = if (file.isDirectory) {
                file.delete().also { success ->
                    if (!success && file.list()?.isEmpty() == true) {
                        file.delete()
                    }
                }
            } else {
                file.delete()
            }

            if (!isDeleted) failedCount++

            emit(FileChangeProgress.InProgress(
                current = index + 1,
                total = totalCount,
                percentage = ((index + 1) * 100 / totalCount).coerceAtMost(100),
                failedCount = failedCount
            ))
        } catch (e: Exception) {
            failedCount++
            emit(FileChangeProgress.Error(e, file))
        }
    }
    // 最终结果
    emit(FileChangeProgress.Completed(failedCount == 0))
}.flowOn(Dispatchers.IO) // 在IO线程执行

fun copyFolder(sourceFolder: File, targetFolder: File): Flow<FileChangeProgress> = flow {
    require(sourceFolder.isDirectory) { "Source path must be a directory" }
    if (!targetFolder.exists()) {
        targetFolder.mkdirs()
    }

    val allFiles = sourceFolder.walkTopDown().toList()
    val totalCount = allFiles.size
    if (totalCount == 0) {
        emit(FileChangeProgress.Completed(true))
        return@flow
    }

    var failedCount = 0

    allFiles.forEachIndexed { index, sourceFile ->
        try {
            val relativePath = sourceFile.relativeTo(sourceFolder).path
            val targetFile = File(targetFolder, relativePath)

            var isCopied: Boolean

            if (sourceFile.isDirectory) {
                targetFile.mkdirs()
                isCopied = true
            } else {
                isCopied = sourceFile.copyTo(targetFile, overwrite = false).exists()
            }

            if (!isCopied) failedCount++

            emit(FileChangeProgress.InProgress(
                current = index + 1,
                total = totalCount,
                percentage = ((index + 1) * 100 / totalCount).coerceAtMost(100),
                failedCount = failedCount
            ))
        } catch (e: Exception) {
            failedCount++
            emit(FileChangeProgress.Error(e, sourceFile))
        }
    }
    // Final result
    emit(FileChangeProgress.Completed( failedCount == 0))
}.flowOn(Dispatchers.IO)

sealed class FileChangeProgress {
    data class InProgress(
        val current: Int,
        val total: Int,
        val percentage: Int,
        val failedCount: Int,
    ) : FileChangeProgress()

    data class Completed(
        val isAllSuccess: Boolean
    ) : FileChangeProgress()

    data class Error(
        val exception: Exception,
        val failedFile: File
    ) : FileChangeProgress()
}

fun createFolder(directory: Path, folderName: String): Boolean {
    try {
        val folder = directory.resolve(folderName).toFile()
        return folder.mkdir()
    } catch (_: IOException) {
        return false
    }
}

fun getMimeType(file: File): String {
    val extension = MimeTypeMap.getFileExtensionFromUrl(file.path)
    val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    return mimeType ?: URLConnection.guessContentTypeFromName(file.name) ?: "*/*"
}

fun Context.shareFile(file: File) {
    val mimeType = getMimeType(file)
    val uri = FileProvider.getUriForFile(
        this,
        "${applicationContext.packageName}.fileprovider",
        file
    )

    Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    }.also { intent ->
        startActivity(Intent.createChooser(intent, "分享文件"))
    }
}

fun install(context: Context, file: File) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        setDataAndType(uri, "application/vnd.android.package-archive")
    }
    if (!context.packageManager.canRequestPackageInstalls()) {
        context.startActivity(
            Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = "package:${context.packageName}".toUri()
            }
        )
        return
    }

    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, "未找到安装程序", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "安装失败: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

/**
 * 使用 root 权限读取文件（通过 su 命令）
 */
fun readFileWithRoot(filePath: String): String {
    return try {
        // 执行 su 命令读取文件（适用于 Android 设备）
        val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "cat '$filePath'"))
        val inputStream = process.inputStream

        val content = inputStream.bufferedReader().use { it.readText() }
        process.waitFor()
        content
    } catch (e: Exception) {
        "访问出错: ${e.message}"
    }
}

fun isAXMLFile(file: File): Boolean {
    FileInputStream(file).use { fis ->
        val expectedHeader = byteArrayOf(0x03, 0x00, 0x08, 0x00)
        val actualHeader = ByteArray(4)
        fis.read(actualHeader)
        return actualHeader.contentEquals(expectedHeader)
    }
}

fun Path.isRootPath(): Boolean {
    return if (!rootMode) pathString == "/storage/emulated/0" else pathString == "/"
}