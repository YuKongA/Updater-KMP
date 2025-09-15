import platform.generateKey
import platform.ownDecrypt
import platform.ownEncrypt
import platform.prefGet
import platform.prefRemove
import platform.prefSet

class Password {
    /**
     * Save Xiaomi's account & password.
     *
     * @param account: Xiaomi account
     * @param password: Password
     */
    fun savePassword(account: String, password: String) {
        generateKey()
        val encryptedAccount = ownEncrypt(account)
        val encryptedPassword = ownEncrypt(password)
        prefSet("account", encryptedAccount.first)
        prefSet("accountIv", encryptedAccount.second)
        prefSet("password", encryptedPassword.first)
        prefSet("passwordIv", encryptedPassword.second)
    }

    /**
     * Delete Xiaomi's account & password.
     */
    fun deletePassword() {
        prefRemove("account")
        prefRemove("accountIv")
        prefRemove("password")
        prefRemove("passwordIv")
    }

    /**
     * Get Xiaomi's account & password.
     *
     * @return Pair of Xiaomi's account & password
     */
    fun getPassword(): Pair<String, String> {
        if (prefGet("account") != null && prefGet("password") != null && prefGet("accountIv") != null && prefGet("passwordIv") != null) {
            val encryptedAccount = prefGet("account").toString()
            val encodedAccountKey = prefGet("accountIv").toString()
            val encryptedPassword = prefGet("password").toString()
            val encodedPasswordKey = prefGet("passwordIv").toString()
            val account = ownDecrypt(encryptedAccount, encodedAccountKey)
            val password = ownDecrypt(encryptedPassword, encodedPasswordKey)
            return Pair(account, password)
        } else return Pair("", "")
    }
}
