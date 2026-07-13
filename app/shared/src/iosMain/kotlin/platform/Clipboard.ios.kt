package platform

import androidx.compose.ui.platform.Clipboard
import platform.UIKit.UIPasteboard

actual suspend fun Clipboard.copyToClipboard(string: String) {
    UIPasteboard.generalPasteboard.string = string
}