package data.storage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.generateKey
import platform.ownDecrypt
import platform.ownEncrypt

class CredentialsStorage(private val prefs: PreferencesStorage) {

    suspend fun save(account: String, password: String) = withContext(Dispatchers.Default) {
        generateKey()
        val encryptedAccount = ownEncrypt(account)
        val encryptedPassword = ownEncrypt(password)
        prefs.set(KEY_SAVE, "1")
        prefs.set(KEY_ACCOUNT, encryptedAccount.first)
        prefs.set(KEY_ACCOUNT_IV, encryptedAccount.second)
        prefs.set(KEY_PASSWORD, encryptedPassword.first)
        prefs.set(KEY_PASSWORD_IV, encryptedPassword.second)
    }

    suspend fun delete() {
        prefs.remove(KEY_SAVE)
        prefs.remove(KEY_ACCOUNT)
        prefs.remove(KEY_ACCOUNT_IV)
        prefs.remove(KEY_PASSWORD)
        prefs.remove(KEY_PASSWORD_IV)
    }

    suspend fun deleteOnlyPassword() {
        prefs.remove(KEY_PASSWORD)
        prefs.remove(KEY_PASSWORD_IV)
    }

    suspend fun load(): Credentials = withContext(Dispatchers.Default) {
        val account = prefs.get(KEY_ACCOUNT)
        val accountIv = prefs.get(KEY_ACCOUNT_IV)
        val password = prefs.get(KEY_PASSWORD)
        val passwordIv = prefs.get(KEY_PASSWORD_IV)
        if (account != null && accountIv != null && password != null && passwordIv != null) {
            Credentials(
                account = ownDecrypt(account, accountIv),
                password = ownDecrypt(password, passwordIv),
            )
        } else {
            Credentials.Empty
        }
    }

    suspend fun isSaveEnabled(): Boolean = prefs.get(KEY_SAVE) == "1"

    data class Credentials(val account: String, val password: String) {
        companion object {
            val Empty = Credentials("", "")
        }
    }

    companion object {
        private const val KEY_SAVE = "savePassword"
        private const val KEY_ACCOUNT = "account"
        private const val KEY_ACCOUNT_IV = "accountIv"
        private const val KEY_PASSWORD = "password"
        private const val KEY_PASSWORD_IV = "passwordIv"
    }
}
