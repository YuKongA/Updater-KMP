package platform

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

actual object FileSystem {

    actual suspend fun getDownloadsDirectory(): String {
        val userHome = System.getProperty("user.home")
        val os = System.getProperty("os.name").lowercase()

        return when {
            os.contains("win") -> {
                File(userHome, "Downloads").absolutePath
            }

            os.contains("mac") -> {
                File(userHome, "Downloads").absolutePath
            }

            else -> {
                File(userHome, "Downloads").absolutePath
            }
        }
    }

    actual suspend fun saveFile(
        data: ByteArray,
        fileName: String,
        directory: String?
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val targetDirectory = directory ?: getDownloadsDirectory()
            val dir = File(targetDirectory)

            // 确保目录存在
            if (!dir.exists()) {
                dir.mkdirs()
            }

            val file = File(dir, fileName)
            FileOutputStream(file).use { output ->
                output.write(data)
                output.flush()
            }

            Result.success(file.absolutePath)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    actual suspend fun fileExists(filePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            File(filePath).exists()
        } catch (e: Exception) {
            false
        }
    }

    actual suspend fun getFileSize(filePath: String): Long? = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (file.exists()) file.length() else null
        } catch (e: Exception) {
            null
        }
    }

    actual suspend fun deleteFile(filePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            File(filePath).delete()
        } catch (e: Exception) {
            false
        }
    }

    actual suspend fun createDirectory(directoryPath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            File(directoryPath).mkdirs()
        } catch (e: Exception) {
            false
        }
    }
}

actual suspend fun saveFileWithProgress(
    data: ByteArray,
    fileName: String,
    directory: String?,
    onProgress: (FileSaveProgress) -> Unit
): Result<String> = withContext(Dispatchers.IO) {
    try {
        val targetDirectory = directory ?: FileSystem.getDownloadsDirectory()
        val dir = File(targetDirectory)

        // 确保目录存在
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
