package platform

import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.DelicateCryptographyApi
import dev.whyoleg.cryptography.algorithms.AES
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

private val iv = "0102030405060708".encodeToByteArray()

expect suspend fun provider(): CryptographyProvider

expect fun generateKey()

/**
 * Generate a Cipher to be used by the xiaomi server.
 */
suspend fun miuiCipher(securityKey: ByteArray): AES.IvCipher {
    val provider = provider()
    val aesCBC = provider.get(AES.CBC) // AES CBC
    val key = aesCBC.keyDecoder().decodeFromByteArray(AES.Key.Format.RAW, securityKey)
    return key.cipher(true) // PKCS5Padding
}

/**
 * Encrypt the JSON used for the request using AES.
 *
 * @param jsonRequest: JSON used for the request
 * @param securityKey: Security key
 *
 * @return Encrypted JSON text
 */
@OptIn(ExperimentalEncodingApi::class, DelicateCryptographyApi::class)
suspend fun miuiEncrypt(jsonRequest: String, securityKey: ByteArray): String {
    val cipher = miuiCipher(securityKey)
    val encrypted = cipher.encryptWithIv(iv, jsonRequest.encodeToByteArray())
    return Base64.UrlSafe.encode(encrypted)
}

/**
 * Decrypt the returned content using AES.
 *
 * @param encryptedText: Returned content
 * @param securityKey: Security key
 *
 * @return Decrypted return content text
 */
@OptIn(DelicateCryptographyApi::class, ExperimentalEncodingApi::class)
suspend fun miuiDecrypt(encryptedText: String, securityKey: ByteArray): String {
    val cipher = miuiCipher(securityKey)
    val encryptedTextBytes = Base64.Mime.decode(encryptedText)
    val decryptedTextBytes = cipher.decryptWithIv(iv, encryptedTextBytes)
    return decryptedTextBytes.decodeToString()
}

expect fun ownEncrypt(string: String): Pair<String, String>

expect fun ownDecrypt(encryptedText: String, encodedIv: String): String
