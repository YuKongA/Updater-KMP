package platform

import androidx.compose.ui.platform.Clipboard
import platform.AppKit.NSPasteboard
import platform.AppKit.NSPasteboardTypeString

actual suspend fun Clipboard.copyToClipboard(string: String) {
    val pasteboard = NSPasteboard.generalPasteboard
    pasteboard.clearContents()
    pasteboard.setString(string, forType = NSPasteboardTypeString)
}