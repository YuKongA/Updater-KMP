import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.sun.jna.Platform.isMac
import com.sun.jna.Platform.isWindows
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import theme.MacOSThemeManager
import theme.WindowsThemeManager
import updater.composeapp.generated.resources.Res
import updater.composeapp.generated.resources.app_name
import updater.composeapp.generated.resources.icon
import javax.swing.SwingUtilities

fun main() = application {
    val state = rememberWindowState(
        size = DpSize(1200.dp, 800.dp),
        position = WindowPosition.Aligned(Alignment.Center)
    )

    Window(
        state = state,
        onCloseRequest = ::exitApplication,
        title = stringResource(Res.string.app_name),
        icon = painterResource(Res.drawable.icon),
    ) {
        when {
            isWindows() -> {
                var isDarkTheme by remember { mutableStateOf(WindowsThemeManager.isWindowsDarkTheme()) }
                LaunchedEffect(Unit) {
                    withContext(Dispatchers.IO) {
                        WindowsThemeManager.listenWindowsThemeChanges { newSystemThemeIsDark ->
                            if (isDarkTheme != newSystemThemeIsDark) isDarkTheme = newSystemThemeIsDark
                        }
                    }
                }
                LaunchedEffect(isDarkTheme, window) {
                    SwingUtilities.invokeLater {
                        WindowsThemeManager.setWindowsTitleBarTheme(window, isDarkTheme)
                    }
                }
                App(isDarkTheme)
            }

            isMac() -> {
                var isDarkTheme by remember { mutableStateOf(MacOSThemeManager.isMacOSDarkTheme()) }
                LaunchedEffect(Unit) {
                    withContext(Dispatchers.IO) {
                        MacOSThemeManager.listenMacOSThemeChanges { newSystemThemeIsDark ->
                            if (isDarkTheme != newSystemThemeIsDark) isDarkTheme = newSystemThemeIsDark
                        }
                    }
                }
                LaunchedEffect(isDarkTheme, window) {
                    SwingUtilities.invokeLater {
                        MacOSThemeManager.setMacOSTitleBarTheme(window, isDarkTheme)
                    }
                }
                App(isDarkTheme)
            }

            else -> {
                App()
            }
        }
    }
}