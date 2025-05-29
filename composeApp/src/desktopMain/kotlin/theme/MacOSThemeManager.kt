package theme

import androidx.compose.ui.awt.ComposeWindow
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive

object MacOSThemeManager {
    fun isMacOSDarkTheme(): Boolean {
        return try {
            val process = ProcessBuilder("defaults", "read", "-g", "AppleInterfaceStyle").start()
            val result = process.inputStream.bufferedReader().readText().trim()
            process.waitFor()
            result.equals("Dark", ignoreCase = true)
        } catch (_: Exception) {
            false
        }
    }

    fun setMacOSTitleBarTheme(window: ComposeWindow, isDark: Boolean) {
        try {
            // This needed JetBrains Runtime
            window.rootPane.putClientProperty(
                "apple.awt.windowAppearance",
                if (isDark) "NSAppearanceNameVibrantDark" else "NSAppearanceNameVibrantLight",
            )
        } catch (_: Exception) {
        }
    }

    suspend fun listenMacOSThemeChanges(onThemeChanged: (Boolean) -> Unit) {
        try {
            while (currentCoroutineContext().isActive) {
                val currentSystemThemeIsDark = isMacOSDarkTheme()
                onThemeChanged(currentSystemThemeIsDark)
            }
        } catch (_: Exception) {
        }
    }
}