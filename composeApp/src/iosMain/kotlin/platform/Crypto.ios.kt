package platform

import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.providers.apple.Apple

actual suspend fun provider() = CryptographyProvider.Apple

actual fun ownEncrypt(string: String): Pair<String, String> = Pair(string, "")

actual fun ownDecrypt(encryptedText: String, encodedIv: String): String = encryptedText

actual fun generateKey() = Unit