import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.symmetric.AES
import dev.whyoleg.cryptography.providers.jdk.JDK

actual suspend fun miuiCipher(securityKey: ByteArray): AES.CBC.Cipher {
    val provider = CryptographyProvider.JDK
    val aesCBC = provider.get(AES.CBC) // AES CBC
    val key = aesCBC.keyDecoder().decodeFrom(AES.Key.Format.RAW, securityKey)
    return key.cipher(true) // PKCS5Padding
}