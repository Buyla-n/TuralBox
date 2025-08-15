package com.tural.box.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.compose.runtime.remember
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.tural.box.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.io.IOException
import java.net.URLConnection
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.pathString

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
        FileType.PACKAGE -> R.drawable.outline_folder_zip_24
        FileType.INSTALL -> R.drawable.outline_android_24
        FileType.XML -> R.drawable.outline_description_24
        FileType.SHELL -> R.drawable.outline_description_24
        FileType.FONT -> R.drawable.outline_font_download_24
        else -> R.drawable.outline_insert_drive_file_24
    }
}

fun accessFiles(path: Path, sortOrder: SortOrder): List<File> {
    try {
        val files = path.toFile().listFiles()?.toList()
        return files!!.sortedWith(
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
        "txt" -> FileType.TEXT
        "jpg", "jpeg", "png", "gif", "webp" -> FileType.IMAGE
        "mp3", "wav", "ogg" -> FileType.AUDIO
        "mp4" -> FileType.VIDEO
        "sh" -> FileType.SHELL
        "ttf", "otf" -> FileType.FONT
        "apk" -> FileType.INSTALL
        "xml" -> FileType.XML
        "zip", "rar", "7z" -> FileType.PACKAGE
        else -> FileType.FILE
    }
}

fun formatFileSize(sizeInBytes: Long): String {
    return when {
        sizeInBytes < 1024 -> "$sizeInBytes B"
        sizeInBytes < 1024 * 1024 -> "%.1f KB".format(sizeInBytes / 1024.0)
        sizeInBytes < 1024 * 1024 * 1024 -> "%.1f MB".format(sizeInBytes / (1024.0 * 1024.0))
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

fun deleteFolder(folder: File): Flow<DeleteProgress> = flow {
    require(folder.isDirectory) { "Path must be a directory" }

    val allFiles = folder.walkBottomUp().toList()
    val totalCount = allFiles.size
    if (totalCount == 0) {
        emit(DeleteProgress.Completed(0, true))
        return@flow
    }

    var successCount = 0
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

            when {
                isDeleted -> successCount++
                else -> failedCount++
            }

            emit(DeleteProgress.InProgress(
                current = index + 1,
                total = totalCount,
                percentage = ((index + 1) * 100 / totalCount).coerceAtMost(100),
                deletedCount = successCount,
                failedCount = failedCount
            ))
        } catch (e: Exception) {
            failedCount++
            emit(DeleteProgress.Error(e, file))
        }
    }
    // 最终结果
    emit(DeleteProgress.Completed(successCount, failedCount == 0))
}.flowOn(Dispatchers.IO) // 在IO线程执行

fun copyFolder(sourceFolder: File, targetFolder: File): Flow<DeleteProgress> = flow {
    require(sourceFolder.isDirectory) { "Source path must be a directory" }
    if (!targetFolder.exists()) {
        targetFolder.mkdirs()
    }

    val allFiles = sourceFolder.walkTopDown().toList()
    val totalCount = allFiles.size
    if (totalCount == 0) {
        emit(DeleteProgress.Completed(0, true))
        return@flow
    }

    var successCount = 0
    var failedCount = 0

    allFiles.forEachIndexed { index, sourceFile ->
        try {
            val relativePath = sourceFile.relativeTo(sourceFolder).path
            val targetFile = File(targetFolder, relativePath)

            var isCopied: Boolean

            if (sourceFile.isDirectory) {
                // Create directory if it doesn't exist
                targetFile.mkdirs()
                isCopied = true
            } else {
                // Copy file
                isCopied = sourceFile.copyTo(targetFile, overwrite = false).exists()
            }

            when {
                isCopied -> successCount++
                else -> failedCount++
            }

            emit(DeleteProgress.InProgress(
                current = index + 1,
                total = totalCount,
                percentage = ((index + 1) * 100 / totalCount).coerceAtMost(100),
                deletedCount = successCount,
                failedCount = failedCount
            ))
        } catch (e: Exception) {
            failedCount++
            emit(DeleteProgress.Error(e, sourceFile))
        }
    }
    // Final result
    emit(DeleteProgress.Completed(successCount, failedCount == 0))
}.flowOn(Dispatchers.IO)

sealed class DeleteProgress {
    data class InProgress(
        val current: Int,
        val total: Int,
        val percentage: Int,
        val deletedCount: Int,
        val failedCount: Int,
    ) : DeleteProgress()

    data class Completed(
        val deletedCount: Int,
        val isAllSuccess: Boolean
    ) : DeleteProgress()

    data class Error(
        val exception: Exception,
        val failedFile: File
    ) : DeleteProgress()
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

fun Path.isRootPath(): Boolean = pathString == "/storage/emulated/0"