import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.providers.apple.Apple

actual suspend fun provider() = CryptographyProvider.Apple

actual fun ownEncrypt(string: String): Pair<String, String>{
    TODO("Not yet implemented")
}

actual fun ownDecrypt(encryptedText: String, encodedIv: String): String {
    TODO("Not yet implemented")
}

actual fun generateKey() {
    TODO("Not yet implemented")
}