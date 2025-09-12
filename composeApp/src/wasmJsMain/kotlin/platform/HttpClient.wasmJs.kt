package platform

import io.ktor.client.HttpClient

actual fun httpClientPlatform(): HttpClient = HttpClient(Js).config
