import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import platform.AppKit.NSApplication

fun main() {
    val app = NSApplication.sharedApplication()
    Window(
        title = "UpdaterKMM",
        size = DpSize(420.dp, 820.dp)
    ) {
        App()
    }
    app.run()
}