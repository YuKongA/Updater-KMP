package platform

actual suspend fun ownEncrypt(string: String): Pair<String, String> = Pair(string, "")

actual suspend fun ownDecrypt(encryptedText: String, encodedIv: String): String = encryptedText

actual suspend fun generateKey() = Unit