import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.CoreCrypto.CC_MD5
import platform.CoreCrypto.CC_MD5_DIGEST_LENGTH

actual fun httpClientPlatform(): HttpClient {
    return HttpClient(Darwin)
}

@OptIn(ExperimentalForeignApi::class)
actual fun md5Hash(input: String): String {
    val data = input.encodeToByteArray().toUByteArray()
    val result = UByteArray(CC_MD5_DIGEST_LENGTH)
    data.usePinned { pinned ->
        result.usePinned { resultPinned ->
            CC_MD5(
                pinned.addressOf(0), data.size.convert(), resultPinned.addressOf(0)
            )
        }
    }
    return result.joinToString("") { it.toInt().toString(16).padStart(2, '0') }.uppercase()
}

actual fun isWasm(): Boolean = false