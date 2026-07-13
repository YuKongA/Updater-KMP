package data.repository

import data.DeviceState
import data.OtaMetadataPb
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf
import platform.httpClientPlatform
import utils.ZipFileUtils.CdEntry
import utils.ZipFileUtils.locateCentralDirectory
import utils.ZipFileUtils.locateEntries
import utils.ZipFileUtils.locateLocalFileOffset
import kotlin.math.min
import kotlin.time.Duration.Companion.milliseconds

class OtaMetadataFetcherImpl(
    private val client: HttpClient = httpClientPlatform(),
) : OtaMetadataFetcher {
    private companion object {
        const val METADATA_PATH = "META-INF/com/android/metadata"
        const val METADATA_PB_PATH = "META-INF/com/android/metadata.pb"
        const val END_BYTES_SIZE = 4096
        const val LOCAL_HEADER_SIZE = 256
        const val TIMEOUT_MS = 20000L
    }

    override suspend fun getOtaMetadata(url: String): OtaMetadataPb? = fetchOtaMetadata(url)

    private suspend fun fetchOtaMetadata(url: String): OtaMetadataPb? {
        return withTimeout(TIMEOUT_MS.milliseconds) {
            try {
                extractOtaMetadata(url)
            } catch (_: Exception) {
                null
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private suspend fun extractOtaMetadata(url: String): OtaMetadataPb? {
        val fileLength = getFileLength(url) ?: return null
        if (fileLength <= 0L) return null

        val actualEndBytesSize = min(fileLength, END_BYTES_SIZE.toLong()).toInt()
        val endBytes = readRange(url, fileLength - actualEndBytesSize, actualEndBytesSize) ?: return null

        val cd = locateCentralDirectory(endBytes, fileLength)
        if (cd.offset < 0 || cd.size <= 0 || cd.offset + cd.size > fileLength) return null

        val centralDirectory = readRange(url, cd.offset, cd.size.toInt()) ?: return null

        val entries = locateEntries(centralDirectory, setOf(METADATA_PB_PATH, METADATA_PATH))

        entries[METADATA_PB_PATH]?.takeIf { it.method == 0 }?.let { entry ->
            val pbBytes = readEntryBytes(url, entry, fileLength)
            if (pbBytes != null) {
                try {
                    return ProtoBuf.decodeFromByteArray(OtaMetadataPb.serializer(), pbBytes)
                } catch (_: Exception) {
                    // fall through to text fallback
                }
            }
        }

        entries[METADATA_PATH]?.takeIf { it.method == 0 }?.let { entry ->
            val textBytes = readEntryBytes(url, entry, fileLength) ?: return null
            val text = textBytes.decodeToString()
            if (text.isNotEmpty()) return parseTextMetadata(text)
        }

        return null
    }

    private suspend fun readEntryBytes(url: String, entry: CdEntry, fileLength: Long): ByteArray? {
        val headerOffset = entry.localHeaderOffset
        if (headerOffset !in 0..<fileLength) return null

        val maxLocalHeaderRead = min(fileLength - headerOffset, LOCAL_HEADER_SIZE.toLong()).toInt()
        if (maxLocalHeaderRead < 30) return null

        val localHeaderBytes = readRange(url, headerOffset, maxLocalHeaderRead) ?: return null
        val internalOffset = locateLocalFileOffset(localHeaderBytes)
        if (internalOffset !in 0..maxLocalHeaderRead) return null

        val dataOffset = headerOffset + internalOffset
        val size = entry.uncompressedSize
        if (size < 0 || size > Int.MAX_VALUE.toLong() || dataOffset + size > fileLength) return null

        return readRange(url, dataOffset, size.toInt())
    }

    private fun parseTextMetadata(text: String): OtaMetadataPb {
        val map = HashMap<String, String>()
        text.lineSequence().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("#")) return@forEach
            val idx = trimmed.indexOf('=')
            if (idx > 0) map[trimmed.substring(0, idx)] = trimmed.substring(idx + 1)
        }

        val type = when (map["ota-type"]) {
            "AB" -> 1
            "BLOCK" -> 2
            "BRICK" -> 3
            else -> 0
        }

        val pre = if (map.keys.any { it.startsWith("pre-") }) {
            DeviceState(
                device = map["pre-device"]?.let { listOf(it) }.orEmpty(),
                build = map["pre-build"]?.let { listOf(it) }.orEmpty(),
                buildIncremental = map["pre-build-incremental"].orEmpty(),
            )
        } else null

        val post = DeviceState(
            device = map["post-device"]?.let { listOf(it) }.orEmpty(),
            build = map["post-build"]?.let { listOf(it) }.orEmpty(),
            buildIncremental = map["post-build-incremental"].orEmpty(),
            timestamp = map["post-timestamp"]?.toLongOrNull() ?: 0L,
            sdkLevel = map["post-sdk-level"].orEmpty(),
            securityPatchLevel = map["post-security-patch-level"].orEmpty(),
        )

        return OtaMetadataPb(type = type, precondition = pre, postcondition = post)
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
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun readRange(url: String, start: Long, size: Int): ByteArray? {
        if (size == 0) return ByteArray(0)
        if (size < 0 || start < 0) return null

        return try {
            val response = client.get(url) {
                header(HttpHeaders.Range, "bytes=$start-${start + size - 1}")
            }
            val bytes = response.body<ByteArray>()
            when {
                bytes.size < size -> null
                bytes.size == size -> bytes
                else -> bytes.copyOf(size)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
    }
}
