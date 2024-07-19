import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js

actual fun httpClientPlatform(): HttpClient {
    return HttpClient(Js)
}

actual fun md5Hash(input: String): String {
    TODO("Not yet implemented")
}