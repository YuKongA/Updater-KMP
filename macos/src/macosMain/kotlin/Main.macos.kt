import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import kotlinx.coroutines.flow.MutableStateFlow
import platform.AppKit.NSApp
import platform.AppKit.NSApplication
import platform.AppKit.NSApplicationDelegateProtocol
import platform.Foundation.NSDistributedNotificationCenter
import platform.Foundation.NSNotification
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSUserDefaults
import platform.darwin.NSObject
import kotlin.system.exitProcess

val isDarkThemeState = MutableStateFlow(false)

class AppDelegate : NSObject(), NSApplicationDelegateProtocol {
    override fun applicationShouldTerminateAfterLastWindowClosed(sender: NSApplication): Boolean {
        return true
    }

    override fun applicationWillTerminate(notification: NSNotification) {
        exitProcess(0)
    }

    override fun applicationDidFinishLaunching(notification: NSNotification) {
        updateThemeMode()

        NSDistributedNotificationCenter.defaultCenter().addObserverForName(
            name = "AppleInterfaceThemeChangedNotification",
            `object` = null,
            queue = NSOperationQueue.mainQueue,
            usingBlock = { _: NSNotification? ->
                this.updateThemeMode()
            }
        )
    }

    private fun updateThemeMode() {
        val defaults = NSUserDefaults.standardUserDefaults
        val interfaceStyle = defaults.stringForKey("AppleInterfaceStyle")
        val isCurrentlyDark = interfaceStyle != null && interfaceStyle.equals("Dark", ignoreCase = true)
        if (isDarkThemeState.value != isCurrentlyDark) isDarkThemeState.value = isCurrentlyDark
    }
}

fun main() {
    NSApplication.sharedApplication()
    val delegate = AppDelegate()
    NSApp?.setDelegate(delegate)

    Window(
        title = "Updater",
        size = DpSize(1200.dp, 800.dp),
    ) {
        ResourceEnvironmentFix {
            val isDarkTheme by isDarkThemeState.collectAsState()
            App(isDarkTheme)
        }
    }

    NSApp?.run()
}