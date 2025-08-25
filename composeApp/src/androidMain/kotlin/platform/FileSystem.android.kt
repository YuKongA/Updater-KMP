package platform

import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.FileSystem.getDownloadsDirectory
import java.io.File
import java.io.FileOutputStream

actual object FileSystem {

    actual suspend fun getDownloadsDirectory(): String {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
    }
}

actual suspend fun saveFileWithProgress(
    data: ByteArray,
    fileName: String,
    folder: String?,
    onProgress: (FileSaveProgress) -> Unit
): Result<String> = withContext(Dispatchers.IO) {
    try {
        val downloadsDirectory = getDownloadsDirectory()
        val targetDirectory = if (!folder.isNullOrBlank()) {
            File(downloadsDirectory, folder).absolutePath
        } else {
            downloadsDirectory
        }
        val dir = File(targetDirectory)

        if (!dir.exists()) {
            dir.mkdirs()
        }

        val file = File(dir, fileName)
        val totalBytes = data.size.toLong()
        var bytesWritten = 0L

        onProgress(
            FileSaveProgress(
                bytesWritten = 0,
                totalBytes = totalBytes,
                progress = 0f,
                isCompleted = false
            )
        )

        FileOutputStream(file).use { output ->
            val chunkSize = 8192
            var offset = 0

            while (offset < data.size) {
                val remainingBytes = data.size - offset
                val currentChunkSize = minOf(chunkSize, remainingBytes)

                output.write(data, offset, currentChunkSize)

                offset += currentChunkSize
                bytesWritten += currentChunkSize

                val progress = if (totalBytes > 0) {
                    (bytesWritten.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
                } else 1f

                onProgress(
                    FileSaveProgress(
                        bytesWritten = bytesWritten,
                        totalBytes = totalBytes,
                        progress = progress,
                        isCompleted = false
                    )
                )
            }

            output.flush()
        }

        onProgress(
            FileSaveProgress(
                bytesWritten = bytesWritten,
                totalBytes = totalBytes,
                progress = 1f,
                isCompleted = true,
                filePath = file.absolutePath
            )
        )

        Result.success(file.absolutePath)
    } catch (e: Exception) {
        onProgress(
            FileSaveProgress(
                bytesWritten = 0,
                totalBytes = data.size.toLong(),
                progress = 0f,
                isCompleted = false,
                error = e.message
            )
        )
        Result.failure(e)
    }
}
