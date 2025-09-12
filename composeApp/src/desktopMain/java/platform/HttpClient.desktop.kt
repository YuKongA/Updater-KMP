package platform

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpRedirect
import io.ktor.client.plugins.compression.ContentEncoding
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies

actual fun httpClientPlatform(): HttpClient {
    return HttpClient(CIO) {
        install(ContentEncoding) {
            gzip()
        }
        install(HttpCookies) {
            storage = AcceptAllCookiesStorage()
        }
        install(HttpRedirect) {
            checkHttpMethod = false
        }
    }
}