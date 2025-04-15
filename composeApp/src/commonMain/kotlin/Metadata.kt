import data.FileInfoHelper
import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpHeaders
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.withTimeout
import misc.ZipFileUtil.locateCentralDirectory
import misc.ZipFileUtil.locateLocalFileHeader
import misc.ZipFileUtil.locateLocalFileOffset

private const val METADATA_PATH = "META-INF/com/android/metadata"
private const val CHUNK_SIZE = 1024
private const val END_BYTES_SIZE = 4096
private const val LOCAL_HEADER_SIZE = 256
private const val TIMEOUT_MS = 20000L

class Metadata private constructor() {

    companion object {
        private val instance by lazy { Metadata() }

        suspend fun getMetadata(url: String): String = instance.getMetadata(url)

        fun getMetadataValue(metadata: String, prefix: String): String =
            metadata.lineSequence()
                .firstOrNull { it.startsWith(prefix) }
                ?.substringAfter(prefix)
                .orEmpty()
    }

    private var position = 0L
    private val client = httpClientPlatform()

    suspend fun getMetadata(url: String): String = try {
        withTimeout(TIMEOUT_MS) {
            extractMetadata(url)
        }
    } catch (_: Exception) {
        ""
    }

    private suspend fun extractMetadata(url: String): String {
        val fileLength = getFileLength(url) ?: return ""
        val endBytes = readEndBytes(url, fileLength) ?: return ""

        val centralDirectoryInfo = locateCentralDirectory(endBytes, fileLength)
        val centralDirectory = readCentralDirectory(url, centralDirectoryInfo) ?: return ""

        val localHeaderOffset = locateLocalFileHeader(centralDirectory, METADATA_PATH)
        if (localHeaderOffset == -1L) return ""

        val localHeaderBytes = readLocalHeader(url, localHeaderOffset) ?: return ""
        val metadataOffset = locateLocalFileOffset(localHeaderBytes) + localHeaderOffset
        val fileSize = localHeaderBytes.getFileSize()

        return readContent(url, metadataOffset, fileSize) ?: ""
    }

    private suspend fun getFileLength(url: String): Long? = try {
        client.head(url) {
            header(HttpHeaders.Range, "bytes=0-0")
        }.headers[HttpHeaders.ContentRange]?.split("/")?.get(1)?.toLong()
    } catch (_: Exception) {
        null
    }

    private suspend fun readRangeBytes(url: String, start: Long, size: Int): ByteArray? = try {
        ByteArray(size).also { bytes ->
            position = start
            executeRangeRequest(url, bytes)
        }
    } catch (_: Exception) {
        null
    }

    private suspend fun readEndBytes(url: String, fileLength: Long): ByteArray? =
        readRangeBytes(url, fileLength - END_BYTES_SIZE, END_BYTES_SIZE)

    private suspend fun readCentralDirectory(url: String, info: FileInfoHelper.FileInfo): ByteArray? =
        readRangeBytes(url, info.offset, info.size.toInt())

    private suspend fun readLocalHeader(url: String, offset: Long): ByteArray? =
        readRangeBytes(url, offset, LOCAL_HEADER_SIZE)

    private suspend fun readContent(url: String, offset: Long, size: Int): String? = try {
        ByteArray(size).also { content ->
            position = offset
            var bytesRead = 0
            while (bytesRead < size) {
                val remaining = size - bytesRead
                val currentChunkSize = minOf(CHUNK_SIZE, remaining)
                val chunk = ByteArray(currentChunkSize)
                executeRangeRequest(url, chunk)
                chunk.copyInto(content, bytesRead)
                bytesRead += currentChunkSize
            }
        }.decodeToString()
    } catch (_: Exception) {
        null
    }

    private suspend fun executeRangeRequest(url: String, bytes: ByteArray) {
        client.get(url) {
            header(HttpHeaders.Range, "bytes=$position-${position + bytes.size - 1}")
        }.let { response ->
            response.bodyAsChannel().readAvailable(bytes).also {
                position += it
            }
        }
    }
}

private fun ByteArray.getFileSize(): Int =
    (this[22].toInt() and 0xff) or
            ((this[23].toInt() and 0xff) shl 8) or
            ((this[24].toInt() and 0xff) shl 16) or
            ((this[25].toInt() and 0xff) shl 24)