package platform

import io.ktor.client.HttpClient

expect fun httpClientPlatform(): HttpClient
