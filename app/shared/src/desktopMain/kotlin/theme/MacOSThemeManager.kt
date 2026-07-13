package theme

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

object MacOSThemeManager {
    private const val POLL_INTERVAL_MS = 1500L

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

    suspend fun listenMacOSThemeChanges(onThemeChanged: (Boolean) -> Unit) {
        try {
            var lastValue = isMacOSDarkTheme()
            while (currentCoroutineContext().isActive) {
                val currentValue = isMacOSDarkTheme()
                if (currentValue != lastValue) {
                    lastValue = currentValue
                    onThemeChanged(currentValue)
                }
                delay(POLL_INTERVAL_MS)
            }
        } catch (_: Exception) {
        }
    }
}