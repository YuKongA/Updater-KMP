import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.window.ComposeViewport
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import webfont.preloadWebFonts
import webfont.queryParam
import kotlin.time.Duration.Companion.milliseconds

private const val DEFAULT_CSS_URL =
    "https://cdn-font.hyperos.mi.com/font/css?family=MiSans_VF:VF:Chinese_Simplify&display=swap"

// Safety cap on how long the loading overlay waits for fonts; afterwards it hides and any remaining
// subsets keep downloading in the background.
private const val LOADING_FONT_TIMEOUT_MS = 10_000L

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val cssUrl = queryParam("cssUrl").ifBlank { DEFAULT_CSS_URL }
    ComposeViewport(
        viewportContainerId = "composeApplication"
    ) {
        // Register each CJK subset with the shared resolver so the whole app picks the glyphs up
        // through the global font fallback (no bundled font, no per-text wrappers), while feeding
        // download progress into the overlay's font bar. Launched in an outer scope so it survives
        // the overlay's timeout.
        val fontFamilyResolver = LocalFontFamilyResolver.current
        val scope = rememberCoroutineScope()
        LaunchedEffect(cssUrl, fontFamilyResolver) {
            withFrameNanos {} // let the first frame paint behind the overlay
            val preload = scope.launch {
                preloadWebFonts(cssUrl, fontFamilyResolver) { done, total ->
                    platformSetFontProgress(done, total)
                }
            }
            withTimeoutOrNull(LOADING_FONT_TIMEOUT_MS.milliseconds) { preload.join() }
            platformHideLoading()
        }

        App()
    }
}
