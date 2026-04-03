package platform

actual fun ownEncrypt(string: String): Pair<String, String> = Pair(string, "")

actual fun ownDecrypt(encryptedText: String, encodedIv: String): String = encryptedText

actual fun generateKey() = Unit