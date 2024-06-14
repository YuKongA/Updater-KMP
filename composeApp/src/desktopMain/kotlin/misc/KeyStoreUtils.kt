package misc

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object KeyStoreUtils {
    private const val KEY_STORE_TYPE = "JCEKS"
    private const val KEY_ALIAS = "updater_key_alias"
    private const val AES_MODE = "AES/GCM/NoPadding"
    private const val JVM_KEY_STORE = "JvmKeyStore"
    private const val KEY_STORE_FILE = "keystore.jks"

    fun generateKey() {
        val keyGenerator = KeyGenerator.getInstance("AES")
        val secretKey = keyGenerator.generateKey()
        val secretKeyEntry = KeyStore.SecretKeyEntry(secretKey)

        val keyStore = KeyStore.getInstance(KEY_STORE_TYPE)
        keyStore.load(null, null)
        val password = KeyStore.PasswordProtection(JVM_KEY_STORE.toCharArray())
        keyStore.setEntry(KEY_ALIAS, secretKeyEntry, password)

        FileOutputStream(KEY_STORE_FILE).use { keyStore.store(it, JVM_KEY_STORE.toCharArray()) }
    }

    private val secretKey: SecretKey?
        get() {
            val keyStore = KeyStore.getInstance(KEY_STORE_TYPE)

            val file = File(KEY_STORE_FILE)
            if (!file.exists()) return null

            FileInputStream(file).use { keyStore.load(it, JVM_KEY_STORE.toCharArray()) }

            val password = KeyStore.PasswordProtection(JVM_KEY_STORE.toCharArray())
            val secretKeyEntry = keyStore.getEntry(KEY_ALIAS, password) as KeyStore.SecretKeyEntry
            return secretKeyEntry.secretKey
        }

    @Throws(Exception::class)
    fun getEncryptionCipher(): Cipher {
        val cipher = Cipher.getInstance(AES_MODE)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        return cipher
    }

    @Throws(Exception::class)
    fun getDecryptionCipher(iv: ByteArray): Cipher? {
        if (secretKey == null) return null
        val cipher = Cipher.getInstance(AES_MODE)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
        return cipher
    }
}