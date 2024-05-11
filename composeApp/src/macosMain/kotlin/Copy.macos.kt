import platform.AppKit.NSPasteboard.Companion.generalPasteboard
import platform.AppKit.NSPasteboardTypeString

actual fun copyToClipboard(text: String) {
    val pasteboard = generalPasteboard()
    pasteboard.clearContents()
    pasteboard.setString(text, forType = NSPasteboardTypeString)
}