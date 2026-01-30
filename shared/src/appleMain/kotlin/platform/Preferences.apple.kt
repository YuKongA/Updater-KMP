package platform

import platform.Foundation.NSUserDefaults

private val preferences = NSUserDefaults.standardUserDefaults()

actual fun prefSet(key: String, value: String) {
    preferences.setObject(value, key)
}

actual fun prefGet(key: String): String? {
    return preferences.stringForKey(key)
}

actual fun prefRemove(key: String) {
    preferences.removeObjectForKey(key)
}