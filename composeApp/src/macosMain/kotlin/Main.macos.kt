import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import platform.AppKit.NSApp
import platform.AppKit.NSApplication
import platform.AppKit.NSApplicationDelegateProtocol
import platform.Foundation.NSNotification
import platform.darwin.NSObject
import kotlin.system.exitProcess

class AppDelegate : NSObject(), NSApplicationDelegateProtocol {
    override fun applicationShouldTerminateAfterLastWindowClosed(sender: NSApplication): Boolean {
        return true
    }

    override fun applicationWillTerminate(notification: NSNotification) {
        exitProcess(0)
    }
}

fun main() {
    NSApplication.sharedApplication()
    val delegate = AppDelegate()
    NSApp?.setDelegate(delegate)
    Window(
        title = "Updater",
        size = DpSize(420.dp, 820.dp)
    ) {
        App()
    }
    NSApp?.run()
}