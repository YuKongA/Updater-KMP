package platform

import kotlinx.browser.window

actual fun perfSet(key: String, value: String) {
    window.localStorage.setItem(key, value)
}

actual fun perfGet(key: String): String? {
    return window.localStorage.getItem(key)
}

actual fun perfRemove(key: String) {
    window.localStorage.removeItem(key)
}