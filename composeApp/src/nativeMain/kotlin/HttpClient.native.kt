import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin

actual fun httpClientPlatform(): HttpClient {
    return HttpClient(Darwin)
}
