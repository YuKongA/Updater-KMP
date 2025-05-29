import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.darkColorScheme
import top.yukonga.miuix.kmp.theme.lightColorScheme

@Composable
fun AppTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
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