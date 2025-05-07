import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard

@OptIn(ExperimentalComposeUiApi::class)
actual suspend fun Clipboard.copyToClipboard(string: String) {
    this.setClipEntry(ClipEntry.withPlainText(string))
}