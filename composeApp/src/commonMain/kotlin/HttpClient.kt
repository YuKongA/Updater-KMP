import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentDisposition
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path

expect fun httpClientPlatform(): HttpClient

object HttpClient {
    private lateinit var url: String
    private lateinit var fileName: String
    private var fileLength: Long = 0
    private var position: Long = 0

    private val client = httpClientPlatform()

    suspend fun init(link: String) = withContext(Dispatchers.Default) {
        url = link
        runCatching {
            val response = client.head(link) {
                header(HttpHeaders.Range, "bytes=0-0")
            }

            if (response.status.isSuccess()) {
                val contentRange = response.headers[HttpHeaders.ContentRange]
                fileLength = contentRange?.split("/")?.get(1)?.toLong() ?: 0L
                fileName = getFileNameFromHeaders(response.headers)
            } else {
                error("Failed to initialize HTTP request: ${response.status.description}")
            }
        }.onFailure { exception ->
            error("Failed to initialize HTTP request: ${exception.message}")
        }
    }

    fun length() = fileLength

    suspend fun read(byteArray: ByteArray): Int = withContext(Dispatchers.Default) {
        var totalBytesRead = 0
        val response = client.get(url) {
            header(HttpHeaders.Range, "bytes=$position-${position + byteArray.size - 1}")
        }

        if (!response.status.isSuccess()) {
            error("Unexpected response ${response.status}")
        }

        response.bodyAsChannel().readAvailable(byteArray, 0, byteArray.size).also {
            totalBytesRead = it
            position += totalBytesRead
        }
        totalBytesRead
    }

    fun seek(bytePosition: Long) {
        if (bytePosition in 0 until fileLength) {
            position = bytePosition
        } else {
            error("Invalid seek position")
        }
    }

    private fun getFileNameFromHeaders(headers: Headers): String {
        headers[HttpHeaders.ContentDisposition]?.let { disposition ->
            ContentDisposition.parse(disposition).parameter(ContentDisposition.Parameters.FileName)
                ?.let { return it }
        }
        return Path(url).name
    }
}