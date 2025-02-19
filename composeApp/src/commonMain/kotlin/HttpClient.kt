import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

expect fun httpClientPlatform(): HttpClient

object HttpClient {
    private var url = ""
    private var fileLength = 0L
    private var position = 0L
    private val client = httpClientPlatform()

    suspend fun init(link: String) = withContext(Dispatchers.Default) {
        url = link
        fileLength = runCatching {
            client.head(link) {
                header(HttpHeaders.Range, "bytes=0-0")
            }.let { response ->
                if (!response.status.isSuccess()) {
                    error("Failed to initialize: ${response.status.description}")
                }
                response.headers[HttpHeaders.ContentRange]?.split("/")?.get(1)?.toLong() ?: 0L
            }
        }.getOrElse { error("Failed to initialize: ${it.message}") }
    }

    fun length() = fileLength

    suspend fun read(byteArray: ByteArray) = withContext(Dispatchers.Default) {
        run {
            client.get(url) {
                header(HttpHeaders.Range, "bytes=$position-${position + byteArray.size - 1}")
            }.let { response ->
                if (!response.status.isSuccess()) {
                    error("Unexpected response ${response.status}")
                }
                response.bodyAsChannel().readAvailable(byteArray).also {
                    position += it
                }
            }
        }
    }

    fun seek(bytePosition: Long): Result<Unit> = runCatching {
        if (bytePosition !in 0 until fileLength) {
            error("Invalid seek position: $bytePosition, fileLength: $fileLength")
        }
        position = bytePosition
    }
}