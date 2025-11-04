package com.tural.box.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.tural.box.R
import com.tural.box.model.FileType
import com.tural.box.model.SortOrder
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.io.path.Path
import kotlin.io.path.pathString
import kotlin.math.log10
import kotlin.math.pow

const val RootPath = "/storage/emulated/0"
const val ExtractPath = "/storage/emulated/0/TuralBox/Extract"
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
        val files = path.toFile().listFiles()?.toList()!!

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

fun getFileSize(file: File): String {
    return if (file.isDirectory) {
        "文件夹"
    } else {
        formatSizeDetail(file.length())
    }
}

fun formatSizeDetail(size: Long): String {
    if (size <= 0) return "0 B"

    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (log10(size.toDouble()) / log10(1024.0)).toInt()

    return String.format(
        Locale.US,
        "%.1f %s (%d 字节)",
        size / 1024.0.pow(digitGroups.toDouble()),
        units[digitGroups],
        size
    )
}

fun formatFileDate(file: File): String {
    val date = Date(file.lastModified())
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return formatter.format(date)
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
    emit(FileChangeProgress.Completed(failedCount == 0))
}.flowOn(Dispatchers.IO)

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

fun isAXMLFile(file: File): Boolean {
    FileInputStream(file).use { fis ->
        val expectedHeader = byteArrayOf(0x03, 0x00, 0x08, 0x00)
        val actualHeader = ByteArray(4)
        fis.read(actualHeader)
        return actualHeader.contentEquals(expectedHeader)
    }
}

fun Path.isRootPath(): Boolean {
    return pathString == "/storage/emulated/0"
}