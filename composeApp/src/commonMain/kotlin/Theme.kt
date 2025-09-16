import androidx.compose.foundation.ComposeFoundationFlags
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.darkColorScheme
import top.yukonga.miuix.kmp.theme.lightColorScheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppTheme(
    isDarkTheme: Boolean,
    content: @Composable () -> Unit
) {
    ComposeFoundationFlags.isNewContextMenuEnabled = true
    MiuixTheme(
        colors = if (isDarkTheme) {
            darkColorScheme()
        } else {
            lightColorScheme()
        }
    ) {
        content()
    }
}
