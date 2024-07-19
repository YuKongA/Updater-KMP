import io.ktor.client.HttpClient

actual fun httpClientPlatform(): HttpClient {
    return HttpClient()
}

actual fun md5Hash(input: String): String {
    TODO("Not yet implemented")
}