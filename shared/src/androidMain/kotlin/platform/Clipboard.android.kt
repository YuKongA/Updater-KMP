package platform

import android.content.ClipData
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard

actual suspend fun Clipboard.copyToClipboard(string: String) {
    val clipData = ClipData.newPlainText("Clipboard", string)
    setClipEntry(ClipEntry(clipData))
}