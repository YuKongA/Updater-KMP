import ZipFileUtil.locateCentralDirectory
import ZipFileUtil.locateLocalFileHeader
import ZipFileUtil.locateLocalFileOffset
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class Metadata {
    suspend fun getMetadata(pathOrUrl: String): String {
        val endBytes = ByteArray(4096)
        val fileName = "META-INF/com/android/metadata"
        if (pathOrUrl.startsWith("https://")) {
            HttpClient.seek(HttpClient.length() - 4096)
            HttpClient.read(endBytes)
            val centralDirectoryInfo = locateCentralDirectory(endBytes, HttpClient.length())
            HttpClient.seek(centralDirectoryInfo.offset)
            val centralDirectory = ByteArray(centralDirectoryInfo.size.toInt())
            HttpClient.read(centralDirectory)
            val localHeaderOffset = locateLocalFileHeader(centralDirectory, fileName)

            if (localHeaderOffset != -1L) {
                val localHeaderBytes = ByteArray(256)
                HttpClient.seek(localHeaderOffset)
                HttpClient.read(localHeaderBytes)
                val metadataOffset = locateLocalFileOffset(localHeaderBytes) + localHeaderOffset

                val fileSize = (localHeaderBytes[22].toInt() and 0xff) or
                        ((localHeaderBytes[23].toInt() and 0xff) shl 8) or
                        ((localHeaderBytes[24].toInt() and 0xff) shl 16) or
                        ((localHeaderBytes[25].toInt() and 0xff) shl 24)

                val chunkSize = 8192
                val metadataContent = ByteArray(fileSize)
                var bytesRead = 0

                HttpClient.seek(metadataOffset)
                while (bytesRead < fileSize) {
                    val remaining = fileSize - bytesRead
                    val currentChunkSize = minOf(chunkSize, remaining)
                    val chunk = ByteArray(currentChunkSize)
                    HttpClient.read(chunk)
                    chunk.forEachIndexed { index, byte ->
                        metadataContent[bytesRead + index] = byte
                    }
                    bytesRead += currentChunkSize
                }
                return metadataContent.decodeToString()
            }
        }
        return ""
    }

    fun getPostSecurityPatchLevel(metadata: String): String {
        return metadata.lineSequence()
            .find { it.startsWith("post-security-patch-level=") }
            ?.substringAfter("post-security-patch-level=")
            ?: ""
    }

    fun getPostTimestamp(metadata: String): String {
        return metadata.lineSequence()
            .find { it.startsWith("post-timestamp=") }
            ?.substringAfter("post-timestamp=")
            ?: ""
    }

    fun convertTimestampToDateTime(timestamp: String): String {
        val epochSeconds = timestamp.toLongOrNull() ?: return ""
        val instant = Instant.fromEpochSeconds(epochSeconds)
        val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return "${dateTime.year}-${dateTime.monthNumber.toString().padStart(2, '0')}-${dateTime.dayOfMonth.toString().padStart(2, '0')} " +
                "${dateTime.hour.toString().padStart(2, '0')}:${dateTime.minute.toString().padStart(2, '0')}:${dateTime.second.toString().padStart(2, '0')}"
    }
}