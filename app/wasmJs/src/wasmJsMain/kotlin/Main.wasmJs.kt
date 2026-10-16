import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.InternalComposeApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.window.ComposeViewport
import di.initKoin
import top.yukonga.miuix.kmp.theme.MiuixTheme
import withFontFamily
import webfont.installOnDemandWebFonts
import webfont.queryParam

private const val DEFAULT_CSS_URL =
    "https://i02.appmifile.com/i18n/fonts/MiSans/vf/index.css"

@OptIn(ExperimentalComposeUiApi::class, InternalComposeApi::class)
fun main() {
    initKoin()
    val cssUrl = queryParam("cssUrl").ifBlank { DEFAULT_CSS_URL }
    ComposeViewport(
        viewportContainerId = "composeApplication"
    ) {
        // Hook on-demand font subset downloads into Compose's WebUnresolvedSymbolsRegistry so
        // missing CJK glyphs fetch their required MiSans chunks as they appear on screen.
        // Preloads core Latin subsets into a primary FontFamily so English and numbers also use MiSans.
        val fontFamilyResolver = LocalFontFamilyResolver.current
        val scope = rememberCoroutineScope()
        var miSansFamily by remember { mutableStateOf<FontFamily?>(null) }
        DisposableEffect(cssUrl, fontFamilyResolver, scope) {
            val handle = installOnDemandWebFonts(
                cssUrl = cssUrl,
                resolver = fontFamilyResolver,
                scope = scope,
                onLatinFamilyLoaded = { family ->
                    miSansFamily = family
                },
            )
            onDispose {
                handle.close()
            }
        }

        val baseTextStyles = MiuixTheme.textStyles
        val textStyles = remember(miSansFamily, baseTextStyles) {
            miSansFamily?.let { baseTextStyles.withFontFamily(it) } ?: baseTextStyles
        }

        LaunchedEffect(Unit) {
            withFrameNanos {} // let the first frame paint behind the overlay
            platformHideLoading()
        }

        App(textStyles = textStyles)
    }
}
