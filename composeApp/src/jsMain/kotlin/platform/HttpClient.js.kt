package platform

import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js

actual fun httpClientPlatform(): HttpClient = HttpClient(Js)