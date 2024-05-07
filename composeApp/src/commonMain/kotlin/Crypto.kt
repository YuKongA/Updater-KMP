import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.DelicateCryptographyApi
import dev.whyoleg.cryptography.algorithms.symmetric.AES
import okio.internal.commonAsUtf8ToByteArray
import okio.internal.commonToUtf8String
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

val iv = "0102030405060708".commonAsUtf8ToByteArray()

suspend fun miuiCipher(securityKey: ByteArray): AES.CBC.Cipher {
    val provider = CryptographyProvider.Default
    val aesCBC = provider.get(AES.CBC) // AES CBC
    val key = aesCBC.keyDecoder().decodeFrom(AES.Key.Format.RAW, securityKey)
    return key.cipher(true) // PKCS5Padding
}

@OptIn(DelicateCryptographyApi::class, ExperimentalEncodingApi::class)
suspend fun miuiEncrypt(jsonRequest: String, securityKey: ByteArray): String {
    val cipher = miuiCipher(securityKey)
    val encrypted = cipher.encrypt(iv, jsonRequest.commonAsUtf8ToByteArray())
    return Base64.UrlSafe.encode(encrypted)
}

@OptIn(DelicateCryptographyApi::class, ExperimentalEncodingApi::class)
suspend fun miuiDecrypt(encryptedText: String, securityKey: ByteArray): String {
    val cipher = miuiCipher(securityKey)
    val encryptedTextBytes = Base64.Mime.decode(encryptedText)
    val decryptedTextBytes = cipher.decrypt(iv, encryptedTextBytes)
    return decryptedTextBytes.commonToUtf8String()
}