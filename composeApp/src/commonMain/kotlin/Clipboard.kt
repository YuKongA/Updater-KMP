import androidx.compose.ui.platform.Clipboard

internal expect suspend fun Clipboard.copyToClipboard(string: String)