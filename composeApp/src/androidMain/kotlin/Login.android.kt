import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import java.security.MessageDigest

actual fun md5Hash(input: String): String {
    val md = MessageDigest.getInstance("MD5")
    md.update(input.toByteArray())
    return md.digest().joinToString("") { "%02x".format(it) }.uppercase()
}

actual fun sha1Hash(input: String): ByteArray {
    val md = MessageDigest.getInstance("SHA-1")
    md.update(input.toByteArray())
    return md.digest()
}

actual suspend fun httpClientPlatform(): HttpClient {
    return HttpClient(OkHttp)
}