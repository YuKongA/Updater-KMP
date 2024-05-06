import kotlinx.browser.window

actual fun copyToClipboard(text: String) {
    window.navigator.clipboard.writeText(text)
}