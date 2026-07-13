package platform

import io.ktor.client.HttpClient
import io.ktor.client.engine.curl.Curl
import io.ktor.client.plugins.HttpTimeout

// Ktor's CIO engine has no TLS on Kotlin/Native; Curl (libcurl) is the only
// Linux-native engine that can reach the HTTPS Xiaomi endpoints.
private val httpClient = HttpClient(Curl) {
    install(HttpTimeout) {
        requestTimeoutMillis = 30000
        connectTimeoutMillis = 15000
        socketTimeoutMillis = 30000
    }
}

actual fun httpClientPlatform(): HttpClient = httpClient
