package platform

import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.DelicateCryptographyApi
import dev.whyoleg.cryptography.algorithms.AES
import dev.whyoleg.cryptography.operations.IvCipher
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

// AES-CBC with a Keychain-held key; only the ciphertext + IV reach
// NSUserDefaults, so clearing them on logout is enough to invalidate credentials.
private const val KEY_SIZE = 32
private const val IV_SIZE = 16

actual suspend fun generateKey() {
    if (KeychainKeyStore.load() == null) {
        KeychainKeyStore.save(KeychainKeyStore.randomBytes(KEY_SIZE))
    }
}

@OptIn(ExperimentalEncodingApi::class, DelicateCryptographyApi::class)
actual suspend fun ownEncrypt(string: String): Pair<String, String> {
    val key = KeychainKeyStore.load() ?: return Pair("", "")
    val iv = KeychainKeyStore.randomBytes(IV_SIZE)
    val encrypted = aesCipher(key).encryptWithIv(iv, string.encodeToByteArray())
    return Pair(Base64.encode(encrypted), Base64.encode(iv))
}

@OptIn(ExperimentalEncodingApi::class, DelicateCryptographyApi::class)
actual suspend fun ownDecrypt(encryptedText: String, encodedIv: String): String {
    val key = KeychainKeyStore.load() ?: return ""
    val decrypted = aesCipher(key).decryptWithIv(Base64.decode(encodedIv), Base64.decode(encryptedText))
    return decrypted.decodeToString()
}

@OptIn(DelicateCryptographyApi::class)
private suspend fun aesCipher(keyBytes: ByteArray): IvCipher {
    val aesCbc = CryptographyProvider.Default.get(AES.CBC)
    val key = aesCbc.keyDecoder().decodeFromByteArray(AES.Key.Format.RAW, keyBytes)
    return key.cipher(true) // PKCS5Padding, matching the other platforms
}
