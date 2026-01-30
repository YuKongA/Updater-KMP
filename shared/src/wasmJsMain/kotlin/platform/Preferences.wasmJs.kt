package platform

import kotlinx.browser.window

actual fun prefSet(key: String, value: String) {
    window.localStorage.setItem(key, value)
}

actual fun prefGet(key: String): String? {
    return window.localStorage.getItem(key)
}

actual fun prefRemove(key: String) {
    window.localStorage.removeItem(key)
}