package platform

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO

actual fun httpClientPlatform(): HttpClient = HttpClient(CIO)