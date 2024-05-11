import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import platform.AppKit.NSApp
import platform.AppKit.NSApplication

fun main() {
    NSApplication.sharedApplication()
    Window(
        title = "UpdaterKMM",
        size = DpSize(420.dp, 820.dp)
    ) {
        App()
    }
    NSApp?.run()
}