import java.util.prefs.Preferences

private val preferences = Preferences.userRoot().node("UpdaterKMM")

actual fun perfSet(key: String, value: String) {
    preferences.put(key, value)
}

actual fun perfGet(key: String): String? {
    return preferences.get(key, null)
}

actual fun perfRemove(key: String) {
    preferences.remove(key)
}