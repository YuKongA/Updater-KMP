import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.providers.webcrypto.WebCrypto

actual suspend fun provider() = CryptographyProvider.WebCrypto

actual fun ownEncrypt(string: String): Pair<String, String> = Pair(string, "")

actual fun ownDecrypt(encryptedText: String, encodedIv: String): String = encryptedText

actual fun generateKey() = Unit