package platform

// No user-scoped key store to lean on without cinterop (libsecret needs a desktop
// daemon); credentials are stored as-is in the 0600-protected config file.
actual suspend fun ownEncrypt(string: String): Pair<String, String> = Pair(string, "")

actual suspend fun ownDecrypt(encryptedText: String, encodedIv: String): String = encryptedText

actual suspend fun generateKey() = Unit
