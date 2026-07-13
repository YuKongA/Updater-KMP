import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeUIViewController
import di.initKoin
import platform.UIKit.UIViewController

@OptIn(ExperimentalComposeUiApi::class)
@Suppress("unused")
fun main(): UIViewController {
    initKoin()
    return ComposeUIViewController(
        configure = {
            parallelRendering = true
        }
    ) {
        ResourceEnvironmentFix {
            App()
        }
    }
}