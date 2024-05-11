import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UByteVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.usePinned
import platform.CoreCrypto.CC_MD5
import platform.CoreCrypto.CC_MD5_DIGEST_LENGTH
import platform.CoreCrypto.CC_SHA1
import platform.CoreCrypto.CC_SHA1_DIGEST_LENGTH

actual suspend fun httpClientPlatform(): HttpClient {
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

@OptIn(ExperimentalForeignApi::class)
actual fun sha1Hash(input: String): ByteArray {
    val digest = memScoped { allocArray<UByteVar>(CC_SHA1_DIGEST_LENGTH) }
    input.usePinned { bytes ->
        CC_SHA1(
            bytes.addressOf(0), input.length.toUInt(), digest
        )
    }
    return digest.readBytes(CC_SHA1_DIGEST_LENGTH)
}