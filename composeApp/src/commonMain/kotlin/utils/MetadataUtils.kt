package utils

import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpHeaders
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.withTimeout
import platform.httpClientPlatform
import utils.ZipFileUtils.locateCentralDirectory
import utils.ZipFileUtils.locateLocalFileHeader
import utils.ZipFileUtils.locateLocalFileOffset
import kotlin.math.min


class MetadataUtils private constructor() {
    companion object {

        private const val METADATA_PATH = "META-INF/com/android/metadata"
        private const val CHUNK_SIZE = 1024
        private const val END_BYTES_SIZE = 4096
        private const val LOCAL_HEADER_SIZE = 256
        private const val TIMEOUT_MS = 20000L
        private val instance by lazy { MetadataUtils() }

        suspend fun getMetadata(url: String): String = instance.fetchMetadata(url)

        fun getMetadataValue(metadata: String, prefix: String): String =
            metadata.lineSequence().firstOrNull { it.startsWith(prefix) }?.substringAfter(prefix).orEmpty()
    }

    private val client = httpClientPlatform()

    private suspend fun fetchMetadata(url: String): String {
        return withTimeout(TIMEOUT_MS) {
            try {
                extractMetadata(url)
            } catch (_: Exception) {
                ""
            }
        }
    }

    private suspend fun extractMetadata(url: String): String {
        val fileLength = getFileLength(url) ?: return ""
        if (fileLength == 0L) return ""

        val actualEndBytesSize = min(fileLength, END_BYTES_SIZE.toLong()).toInt()
        val endBytes = readRange(url, fileLength - actualEndBytesSize, actualEndBytesSize) ?: return ""

        val centralDirectoryInfo = locateCentralDirectory(endBytes, fileLength)
        if (centralDirectoryInfo.offset == -1L || centralDirectoryInfo.size == -1L ||
            centralDirectoryInfo.offset < 0 || centralDirectoryInfo.size <= 0 ||
            centralDirectoryInfo.offset + centralDirectoryInfo.size > fileLength
        ) return ""

        val centralDirectory = readRange(url, centralDirectoryInfo.offset, centralDirectoryInfo.size.toInt()) ?: return ""

        val localHeaderOffset = locateLocalFileHeader(centralDirectory, METADATA_PATH)
        if (localHeaderOffset == -1L || localHeaderOffset < 0 || localHeaderOffset >= fileLength) return ""

        val maxBytesForLocalHeader = min(fileLength - localHeaderOffset, LOCAL_HEADER_SIZE.toLong()).toInt()

        if (maxBytesForLocalHeader < 30) return ""
        val localHeaderBytes = readRange(url, localHeaderOffset, maxBytesForLocalHeader) ?: return ""

        val metadataInternalOffset = locateLocalFileOffset(localHeaderBytes)
        if (metadataInternalOffset == -1L || metadataInternalOffset > maxBytesForLocalHeader) return ""

        val metadataActualOffset = localHeaderOffset + metadataInternalOffset
        val metadataSize = localHeaderBytes.getUncompressedSize()

        if (metadataSize < 0 || metadataActualOffset + metadataSize > fileLength) return ""

        return readContent(url, metadataActualOffset, metadataSize) ?: return ""
    }

    private suspend fun getFileLength(url: String): Long? {
        return try {
            val response = client.head(url) {
                header(HttpHeaders.Range, "bytes=0-0")
            }

            response.headers[HttpHeaders.ContentRange]?.let { contentRange ->
                val parts = contentRange.split("/")
                if (parts.size > 1) {
                    parts[1].toLongOrNull()?.let { if (it > 0) return it }
                }
            }
            response.headers[HttpHeaders.ContentLength]?.toLongOrNull()?.let { if (it > 0) return it }

            null
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun readRange(url: String, start: Long, size: Int): ByteArray? {
        if (size == 0) return ByteArray(0)
        if (size < 0 || start < 0) return null

        val bytes = ByteArray(size)
        return try {
            val response = client.get(url) {
                header(HttpHeaders.Range, "bytes=$start-${start + size - 1}")
            }

            val channel = response.bodyAsChannel()
            var totalBytesRead = 0
            while (totalBytesRead < size) {
                val bytesReadThisTurn = channel.readAvailable(bytes, totalBytesRead, size - totalBytesRead)
                if (bytesReadThisTurn == -1) return null

                totalBytesRead += bytesReadThisTurn
            }
            bytes
        } catch (_: Exception) {
            null
        }

    }

    private suspend fun readContent(url: String, offset: Long, size: Int): String? {
        if (size == 0) return ""
        if (size < 0 || offset < 0) return null

        val contentBytes = ByteArray(size)
        var totalBytesRead = 0
        return try {
            while (totalBytesRead < size) {
                val remaining = size - totalBytesRead
                val currentChunkSize = min(CHUNK_SIZE, remaining)

                val bytesReadInChunk = executeStreamedRangeRequest(
                    url,
                    offset + totalBytesRead,
                    contentBytes,
                    totalBytesRead,
                    currentChunkSize
                )

                if (bytesReadInChunk <= 0) return null
                totalBytesRead += bytesReadInChunk
            }
            contentBytes.decodeToString()
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun executeStreamedRangeRequest(
        url: String,
        fileOffset: Long,
        buffer: ByteArray,
        bufferOffset: Int,
        length: Int
    ): Int {
        if (length == 0) return 0
        if (length < 0 || fileOffset < 0 || bufferOffset < 0 || bufferOffset + length > buffer.size) return -1 // Invalid params

        return try {
            val response = client.get(url) {
                header(HttpHeaders.Range, "bytes=$fileOffset-${fileOffset + length - 1}")
            }
            response.bodyAsChannel().readAvailable(buffer, bufferOffset, length)
        } catch (_: Exception) {
            -1
        }
    }

    private fun ByteArray.getUncompressedSize(): Int {
        if (this.size < 22 + 4) return -1
        return (this[22].toInt() and 0xff) or
                ((this[23].toInt() and 0xff) shl 8) or
                ((this[24].toInt() and 0xff) shl 16) or
                ((this[25].toInt() and 0xff) shl 24)
    }
}
