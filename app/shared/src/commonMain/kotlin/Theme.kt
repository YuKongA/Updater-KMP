import androidx.compose.foundation.ComposeFoundationFlags
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.TextStyles
import top.yukonga.miuix.kmp.theme.darkColorScheme
import top.yukonga.miuix.kmp.theme.lightColorScheme
import top.yukonga.miuix.kmp.utils.Platform
import top.yukonga.miuix.kmp.utils.platform

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppTheme(
    isDarkTheme: Boolean,
    textStyles: TextStyles = MiuixTheme.textStyles,
    content: @Composable () -> Unit,
) {
    if (platform() != Platform.MacOS) ComposeFoundationFlags.isNewContextMenuEnabled = true
    MiuixTheme(
        colors = if (isDarkTheme) darkColorScheme() else lightColorScheme(),
        textStyles = textStyles,
    ) {
        content()
    }
}

fun TextStyles.withFontFamily(fontFamily: FontFamily): TextStyles = copy(
    main = main.copy(fontFamily = fontFamily),
    paragraph = paragraph.copy(fontFamily = fontFamily),
    body1 = body1.copy(fontFamily = fontFamily),
    body2 = body2.copy(fontFamily = fontFamily),
    button = button.copy(fontFamily = fontFamily),
    footnote1 = footnote1.copy(fontFamily = fontFamily),
    footnote2 = footnote2.copy(fontFamily = fontFamily),
    headline1 = headline1.copy(fontFamily = fontFamily),
    headline2 = headline2.copy(fontFamily = fontFamily),
    subtitle = subtitle.copy(fontFamily = fontFamily),
    title1 = title1.copy(fontFamily = fontFamily),
    title2 = title2.copy(fontFamily = fontFamily),
    title3 = title3.copy(fontFamily = fontFamily),
    title4 = title4.copy(fontFamily = fontFamily),
)
