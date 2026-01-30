package platform

import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard

actual suspend fun Clipboard.copyToClipboard(string: String) {
    this.setClipEntry(ClipEntry.withPlainText(string))
}