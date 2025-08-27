package platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.Foundation.NSDownloadsDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSHomeDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fwrite

actual object FileSystem {
    actual suspend fun getDownloadsDirectory(): String {
        val urls = NSFileManager.defaultManager.URLsForDirectory(NSDownloadsDirectory, NSUserDomainMask)
        val downloadsUrl = urls.firstOrNull() as? NSURL
        return downloadsUrl?.path ?: (NSHomeDirectory() + "/Downloads")
    }
}

@OptIn(ExperimentalForeignApi::class)
actual suspend fun saveFileWithProgress(
    data: ByteArray,
    fileName: String,
    folder: String?,
    onProgress: (FileSaveProgress) -> Unit
): Result<String> {
    try {
        val downloadsDir = FileSystem.getDownloadsDirectory()
        val targetDir = if (folder.isNullOrBlank()) downloadsDir else "$downloadsDir/$folder"
        val filePath = "$targetDir/$fileName"

        val fileManager = NSFileManager.defaultManager
        val exists = fileManager.fileExistsAtPath(targetDir, null)
        if (!exists) {
            val success = fileManager.createDirectoryAtPath(targetDir, true, null, null)
            if (!success) throw Exception("Error: $targetDir")
        }

        val file = fopen(filePath, "wb") ?: throw Exception("Error: $filePath")
        val totalBytes = data.size.toLong()
        var bytesWritten = 0L
        val bufferSize = 8192
        while (bytesWritten < totalBytes) {
            val remaining = (totalBytes - bytesWritten).toInt()
            val writeSize = if (remaining < bufferSize) remaining else bufferSize
            val written = data.usePinned {
                fwrite(it.addressOf(bytesWritten.toInt()), 1.convert(), writeSize.convert(), file)
            }
            if (written <= 0u) throw Exception("Error: $filePath")
            bytesWritten += written.toLong()
            onProgress(
                FileSaveProgress(
                    bytesWritten = bytesWritten,
                    totalBytes = totalBytes,
                    progress = bytesWritten.toFloat() / totalBytes,
                    isCompleted = false,
                    filePath = filePath
                )
            )
        }
        fclose(file)
        onProgress(
            FileSaveProgress(
                bytesWritten = bytesWritten,
                totalBytes = totalBytes,
                progress = 1f,
                isCompleted = true,
                filePath = filePath
            )
        )
        return Result.success(filePath)
    } catch (e: Exception) {
        onProgress(
            FileSaveProgress(
                bytesWritten = 0,
                totalBytes = data.size.toLong(),
                progress = 0f,
                isCompleted = true,
                error = e.message
            )
        )
        return Result.failure(e)
    }
}