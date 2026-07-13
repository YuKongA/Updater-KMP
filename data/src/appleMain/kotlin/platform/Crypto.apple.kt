package platform

import platform.crypto.AesCbc
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

// AES-CBC with a Keychain-held key; only ciphertext + IV land in NSUserDefaults,
// so clearing them on logout invalidates stored credentials.
private const val KEY_SIZE = 32
private const val IV_SIZE = 16

actual suspend fun generateKey() {
    if (KeychainKeyStore.load() == null) {
        KeychainKeyStore.save(KeychainKeyStore.randomBytes(KEY_SIZE))
    }
}

@OptIn(ExperimentalEncodingApi::class)
actual suspend fun ownEncrypt(string: String): Pair<String, String> {
    val key = KeychainKeyStore.load() ?: return Pair("", "")
    val iv = KeychainKeyStore.randomBytes(IV_SIZE)
    val encrypted = AesCbc.encrypt(key, iv, string.encodeToByteArray())
    return Pair(Base64.encode(encrypted), Base64.encode(iv))
}

@OptIn(ExperimentalEncodingApi::class)
actual suspend fun ownDecrypt(encryptedText: String, encodedIv: String): String {
    val key = KeychainKeyStore.load() ?: return ""
    val decrypted = AesCbc.decrypt(key, Base64.decode(encodedIv), Base64.decode(encryptedText))
    return decrypted.decodeToString()
}
