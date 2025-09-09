package platform

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.endpoint
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies

actual fun httpClientPlatform(): HttpClient {
    return HttpClient(CIO) {
        engine {
            maxConnectionsCount = 16
            endpoint {
                maxConnectionsPerRoute = 6
                pipelineMaxSize = 16
                keepAliveTime = 30000
                connectTimeout = 10000
                connectAttempts = 3
            }
        }
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
