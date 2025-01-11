import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO

actual fun httpClientPlatform(): HttpClient {
    return HttpClient(CIO)
}

actual fun isWeb(): Boolean = false