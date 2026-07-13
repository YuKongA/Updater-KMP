package theme

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer

object LinuxThemeManager {
    private val darkThemeRegex = ".*dark.*".toRegex(RegexOption.IGNORE_CASE)

    private val GET_THEME_COMMANDS = arrayOf(
        "gsettings get org.gnome.desktop.interface gtk-theme",
        "gsettings get org.gnome.desktop.interface color-scheme"
    )

    private const val MONITORING_CMD = "gsettings monitor org.gnome.desktop.interface"

    @Volatile
    private var monitoringThread: Thread? = null

    private val listeners: MutableSet<Consumer<Boolean>> = ConcurrentHashMap.newKeySet()

    fun isLinuxDarkTheme(): Boolean {
        return try {
            for (cmd in GET_THEME_COMMANDS) {
                val cmdParts = cmd.split(" ")
                val process = ProcessBuilder(*cmdParts.toTypedArray()).start()
                BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                    val line = reader.readLine()
                    if (line != null && isDarkTheme(line)) return true
                }
            }
            false
        } catch (_: Exception) {
            false
        }
    }

    suspend fun listenLinuxThemeChanges(onThemeChanged: (Boolean) -> Unit) {
        try {
            val listener = Consumer<Boolean> { isDark ->
                onThemeChanged(isDark)
            }
            registerListener(listener)

            while (currentCoroutineContext().isActive) {
                delay(1000)
            }

            removeListener(listener)
        } catch (_: Exception) {
            var lastValue = isLinuxDarkTheme()
            while (currentCoroutineContext().isActive) {
                val currentValue = isLinuxDarkTheme()
                if (currentValue != lastValue) {
                    lastValue = currentValue
                    onThemeChanged(currentValue)
                }
                delay(2000)
            }
        }
    }

    private fun isDarkTheme(text: String): Boolean {
        return darkThemeRegex.containsMatchIn(text)
    }

    private fun registerListener(listener: Consumer<Boolean>) {
        val wasEmpty = listeners.isEmpty()
        listeners.add(listener)
        if (wasEmpty) {
            startMonitoring()
        }
    }

    private fun removeListener(listener: Consumer<Boolean>) {
        listeners.remove(listener)
        if (listeners.isEmpty()) {
            monitoringThread?.interrupt()
            monitoringThread = null
        }
    }

    private fun startMonitoring() {
        if (monitoringThread?.isAlive == true) return

        monitoringThread = Thread {
            var lastValue = isLinuxDarkTheme()

            try {
                val cmdParts = MONITORING_CMD.split(" ")
                val process = ProcessBuilder(*cmdParts.toTypedArray()).start()
                BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                    while (!Thread.currentThread().isInterrupted) {
                        val line = reader.readLine() ?: break

                        if (!line.contains("gtk-theme", ignoreCase = true) &&
                            !line.contains("color-scheme", ignoreCase = true)
                        ) {
                            continue
                        }

                        val currentIsDark = isLinuxDarkTheme()
                        if (currentIsDark != lastValue) {
                            lastValue = currentIsDark

                            listeners.forEach {
                                try {
                                    it.accept(currentIsDark)
                                } catch (_: Exception) {
                                }
                            }
                        }
                    }

                    if (process.isAlive) {
                        process.destroy()
                    }
                }
            } catch (_: Exception) {
                while (!Thread.currentThread().isInterrupted) {
                    try {
                        val currentIsDark = isLinuxDarkTheme()
                        if (currentIsDark != lastValue) {
                            lastValue = currentIsDark
                            listeners.forEach { it.accept(currentIsDark) }
                        }
                        Thread.sleep(2000)
                    } catch (_: InterruptedException) {
                        break
                    } catch (_: Exception) {
                    }
                }
            }
        }.apply {
            isDaemon = true
            start()
        }
    }
}