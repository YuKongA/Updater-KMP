import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.providers.jdk.JDK
import top.yukonga.updater.kmp.misc.KeyStoreUtils
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

actual suspend fun provider() = CryptographyProvider.JDK

@OptIn(ExperimentalEncodingApi::class)
actual fun ownEncrypt(string: String): Pair<String, String> {
    val cipher = KeyStoreUtils.getEncryptionCipher()
    val encrypted = cipher.doFinal(string.toByteArray())
    val iv = cipher.iv
    return Pair(Base64.encode(encrypted), Base64.encode(iv))
}

@OptIn(ExperimentalEncodingApi::class)
actual fun ownDecrypt(encryptedText: String, encodedIv: String): String {
    val encrypted = Base64.decode(encryptedText)
    val iv = Base64.decode(encodedIv)
    val cipher = KeyStoreUtils.getDecryptionCipher(iv)
    return String(cipher.doFinal(encrypted))
}

actual fun generateKey() {
    KeyStoreUtils.generateKey()
}