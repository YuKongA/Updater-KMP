import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

actual fun httpClientPlatform(): HttpClient {
    return HttpClient(OkHttp)
}

actual fun isWeb(): Boolean = false