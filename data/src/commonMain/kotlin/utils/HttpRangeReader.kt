package utils

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.CancellationException

/** Reads byte ranges of a remote file over HTTP Range requests. */
class HttpRangeReader(private val client: HttpClient) {

    suspend fun fileLength(url: String): Long? {
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

    suspend fun read(url: String, start: Long, size: Int): ByteArray? {
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
