package theme

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.lang.foreign.Arena
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.Linker
import java.lang.foreign.MemorySegment
import java.lang.foreign.SymbolLookup
import java.lang.foreign.ValueLayout
import java.lang.invoke.MethodHandle

object WindowsThemeManager {
    private const val REGISTRY_KEY_PATH =
        "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize"
    private const val REGISTRY_VALUE_NAME = "AppsUseLightTheme"
    private const val POLL_INTERVAL_MS = 1500L
    private const val DWMWA_USE_IMMERSIVE_DARK_MODE = 20

    // FFM handle for dwmapi!DwmSetWindowAttribute(HWND, DWORD, LPCVOID, DWORD); null if unresolved.
    private val dwmSetWindowAttribute: MethodHandle? by lazy {
        try {
            val sym = SymbolLookup.libraryLookup("dwmapi", Arena.global())
                .find("DwmSetWindowAttribute").orElseThrow()
            Linker.nativeLinker().downcallHandle(
                sym,
                FunctionDescriptor.of(
                    ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT,
                    ValueLayout.ADDRESS, ValueLayout.JAVA_INT,
                ),
            )
        } catch (_: Throwable) {
            null
        }
    }

    // Reads AppsUseLightTheme via reg.exe; dark == value 0; missing/error => light.
    fun isWindowsDarkTheme(): Boolean {
        return try {
            val process = ProcessBuilder(
                "reg", "query", REGISTRY_KEY_PATH, "/v", REGISTRY_VALUE_NAME
            ).redirectErrorStream(true).start()
            val output = process.inputStream.bufferedReader().use { it.readText() }
            process.waitFor()
            val line = output.lineSequence()
                .firstOrNull { it.contains(REGISTRY_VALUE_NAME) } ?: return false
            val hexToken = line.trim().split(Regex("\\s+"))
                .firstOrNull { it.startsWith("0x", ignoreCase = true) } ?: return false
            hexToken.substring(2).toLongOrNull(16) == 0L
        } catch (_: Exception) {
            false
        }
    }

    // Sets the immersive dark/light title bar via FFM. invokeWithArguments (not the
    // signature-polymorphic MethodHandle.invoke) keeps this ProGuard-safe.
    fun setWindowsTitleBarTheme(window: java.awt.Window, isDark: Boolean) {
        try {
            val handle = dwmSetWindowAttribute ?: return
            val hwnd = getHwnd(window)
            if (hwnd == 0L) return
            Arena.ofConfined().use { arena ->
                val pv = arena.allocate(ValueLayout.JAVA_INT)
                pv.set(ValueLayout.JAVA_INT, 0L, if (isDark) 1 else 0)
                handle.invokeWithArguments(
                    MemorySegment.ofAddress(hwnd),
                    DWMWA_USE_IMMERSIVE_DARK_MODE,
                    pv,
                    4,
                )
            }
        } catch (_: Throwable) {
        }
    }

    // HWND of an AWT window without JNA (sun.awt.AWTAccessor -> WComponentPeer.getHWnd()).
    // Needs --add-opens java.desktop/sun.awt{,.windows} (build.gradle.kts); 0 if unavailable.
    private fun getHwnd(window: java.awt.Window): Long {
        return try {
            val componentAccessor = Class.forName("sun.awt.AWTAccessor")
                .getMethod("getComponentAccessor")
                .apply { isAccessible = true }
                .invoke(null)
            val peer = Class.forName("sun.awt.AWTAccessor\$ComponentAccessor")
                .getMethod("getPeer", java.awt.Component::class.java)
                .apply { isAccessible = true }
                .invoke(componentAccessor, window) ?: return 0L
            peer.javaClass.getMethod("getHWnd")
                .apply { isAccessible = true }
                .invoke(peer) as Long
        } catch (_: Throwable) {
            0L
        }
    }

    // Polls the theme and fires only on change (replaces the JNA RegNotifyChangeKeyValue listener).
    suspend fun listenWindowsThemeChanges(onThemeChanged: (isDark: Boolean) -> Unit) {
        var lastValue = isWindowsDarkTheme()
        while (currentCoroutineContext().isActive) {
            val currentValue = isWindowsDarkTheme()
            if (currentValue != lastValue) {
                lastValue = currentValue
                onThemeChanged(currentValue)
            }
            delay(POLL_INTERVAL_MS)
        }
    }
}
