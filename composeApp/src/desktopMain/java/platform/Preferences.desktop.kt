package platform

import java.util.prefs.Preferences

private val preferences = Preferences.userRoot().node("UpdaterKMP")

actual fun prefSet(key: String, value: String) {
    preferences.put(key, value)
}

actual fun prefGet(key: String): String? {
    return preferences.get(key, null)
}

actual fun prefRemove(key: String) {
    preferences.remove(key)
}