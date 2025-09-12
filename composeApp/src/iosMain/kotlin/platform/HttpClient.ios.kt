package platform

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.HttpRedirect
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header

actual fun httpClientPlatform(): HttpClient {
    return HttpClient(Darwin).config {
        defaultRequest {
            header("Accept-Encoding", "gzip, deflate, br")
        }
        install(HttpCookies) {
            storage = AcceptAllCookiesStorage()
        }
        install(HttpRedirect) {
            checkHttpMethod = false
        }
    }
}
