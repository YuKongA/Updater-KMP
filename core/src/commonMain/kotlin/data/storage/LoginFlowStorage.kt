package data.storage

class LoginFlowStorage(private val prefs: PreferencesStorage) {

    suspend fun saveIdentitySession(value: String) = prefs.set(KEY_IDENTITY_SESSION, value)
    suspend fun identitySession(): String = prefs.get(KEY_IDENTITY_SESSION) ?: ""

    suspend fun saveTwoFactorContext(value: String) = prefs.set(KEY_2FA_CONTEXT, value)
    suspend fun saveNotificationUrl(value: String) = prefs.set(KEY_NOTIFICATION_URL, value)

    suspend fun clearTransient() {
        prefs.remove(KEY_CAPTCHA_URL)
        prefs.remove(KEY_NOTIFICATION_URL)
        prefs.remove(KEY_IDENTITY_SESSION)
        prefs.remove(KEY_2FA_CONTEXT)
    }

    companion object {
        private const val KEY_IDENTITY_SESSION = "identity_session"
        private const val KEY_2FA_CONTEXT = "2FAContext"
        private const val KEY_NOTIFICATION_URL = "notificationUrl"
        private const val KEY_CAPTCHA_URL = "captchaUrl"
    }
}
