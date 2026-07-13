package platform

import platform.crypto.AesCbc
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

private val iv = "0102030405060708".encodeToByteArray()

expect suspend fun generateKey()

/** AES-CBC encrypt the request JSON, returned as URL-safe Base64. */
@OptIn(ExperimentalEncodingApi::class)
fun miuiEncrypt(jsonRequest: String, securityKey: ByteArray): String {
    val encrypted = AesCbc.encrypt(securityKey, iv, jsonRequest.encodeToByteArray())
    return Base64.UrlSafe.encode(encrypted)
}

/** AES-CBC decrypt a Base64 server response back to plaintext JSON. */
@OptIn(ExperimentalEncodingApi::class)
fun miuiDecrypt(encryptedText: String, securityKey: ByteArray): String {
    val encryptedTextBytes = Base64.Mime.decode(encryptedText)
    val decryptedTextBytes = AesCbc.decrypt(securityKey, iv, encryptedTextBytes)
    return decryptedTextBytes.decodeToString()
}

expect suspend fun ownEncrypt(string: String): Pair<String, String>

expect suspend fun ownDecrypt(encryptedText: String, encodedIv: String): String
