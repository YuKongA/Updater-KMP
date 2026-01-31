import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

@OptIn(ExperimentalComposeUiApi::class)
@Suppress("unused")
fun main(): UIViewController = ComposeUIViewController(
    configure = {
        parallelRendering = true
    }
) {
    ResourceEnvironmentFix {
        App()
    }
}