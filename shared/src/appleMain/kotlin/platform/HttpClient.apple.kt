package platform

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.HttpTimeout

private val httpClient = HttpClient(Darwin) {
    install(HttpTimeout) {
        requestTimeoutMillis = 30000
        connectTimeoutMillis = 15000
        socketTimeoutMillis = 30000
    }
}

actual fun httpClientPlatform(): HttpClient = httpClient
