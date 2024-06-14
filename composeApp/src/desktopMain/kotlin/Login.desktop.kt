import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import java.security.MessageDigest

actual fun httpClientPlatform(): HttpClient {
    return HttpClient(CIO)
}

actual fun md5Hash(input: String): String {
    val md = MessageDigest.getInstance("MD5")
    md.update(input.toByteArray())
    return md.digest().joinToString("") { "%02x".format(it) }.uppercase()
}