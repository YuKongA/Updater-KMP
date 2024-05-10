import platform.Foundation.NSUserDefaults

private val preferences = NSUserDefaults.standardUserDefaults()

actual fun perfSet(key: String, value: String) {
    preferences.setObject(value, key)
}

actual fun perfGet(key: String): String? {
    return preferences.stringForKey(key)
}

actual fun perfRemove(key: String) {
    preferences.removeObjectForKey(key)
}