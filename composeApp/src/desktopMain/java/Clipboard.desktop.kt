import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import java.awt.datatransfer.StringSelection

internal actual suspend fun Clipboard.copyToClipboard(string: String) {
    setClipEntry(ClipEntry(StringSelection(string)))
}