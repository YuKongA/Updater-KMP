import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import top.yukonga.miuix.kmp.theme.LocalColors
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.darkColorScheme
import top.yukonga.miuix.kmp.theme.lightColorScheme
import top.yukonga.miuix.kmp.utils.MiuixIndication

@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    MiuixTheme(
        colors = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
    ) {
        val colors = LocalColors.current
        val miuixIndication = remember(colors.onBackground) {
            MiuixIndication(backgroundColor = colors.onBackground)
        }
        CompositionLocalProvider(
            LocalIndication provides miuixIndication
        ) {
            content()
        }
    }
}