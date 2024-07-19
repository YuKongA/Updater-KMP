actual fun copyToClipboard(text: String) {
    writeToClipboard(text)
}

@JsFun("text => navigator.clipboard.writeText(text)")
external fun writeToClipboard(text: String)