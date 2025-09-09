package platform

import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies

actual fun httpClientPlatform(): HttpClient {
    return HttpClient(Js).config {
        install(HttpCookies) {
            storage = AcceptAllCookiesStorage()
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 60000
            connectTimeoutMillis = 10000
            socketTimeoutMillis = 60000
        }
    }
}