package theme

import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.Advapi32
import com.sun.jna.platform.win32.Advapi32Util
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinDef.HWND
import com.sun.jna.platform.win32.WinError
import com.sun.jna.platform.win32.WinNT
import com.sun.jna.platform.win32.WinReg
import com.sun.jna.win32.StdCallLibrary
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive

object WindowsThemeManager {
    private const val REGISTRY_KEY_PATH = "Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize"
    private const val REGISTRY_VALUE_NAME = "AppsUseLightTheme"

    private interface DwmApi : StdCallLibrary {
        fun DwmSetWindowAttribute(
            hwnd: HWND,
            dwAttribute: Int,
            pvAttribute: Pointer,
            cbAttribute: Int
        ): Int

        companion object {
            val INSTANCE: DwmApi by lazy {
                Native.load("dwmapi", DwmApi::class.java)
            }
            const val DWMWA_USE_IMMERSIVE_DARK_MODE = 20
        }
    }

    fun isWindowsDarkTheme(): Boolean {
        return try {
            val value = Advapi32Util.registryGetIntValue(
                WinReg.HKEY_CURRENT_USER,
                REGISTRY_KEY_PATH,
                REGISTRY_VALUE_NAME
            )
            value == 0
        } catch (_: Exception) {
            false
        }
    }

    fun setWindowsTitleBarTheme(window: java.awt.Window, isDark: Boolean) {
        try {
            val hwnd = HWND(Native.getComponentPointer(window))
            val darkModeValue = WinDef.BOOLByReference(WinDef.BOOL(isDark))

            DwmApi.INSTANCE.DwmSetWindowAttribute(
                hwnd,
                DwmApi.DWMWA_USE_IMMERSIVE_DARK_MODE,
                darkModeValue.pointer,
                4,
            )
        } catch (_: Throwable) {
        }
    }

    suspend fun listenWindowsThemeChanges(onThemeChanged: (isDark: Boolean) -> Unit) {
        val advapi32 = Advapi32.INSTANCE
        val hKeyByRef = WinReg.HKEYByReference()

        val openResult = advapi32.RegOpenKeyEx(
            WinReg.HKEY_CURRENT_USER,
            REGISTRY_KEY_PATH,
            0,
            WinNT.KEY_NOTIFY,
            hKeyByRef,
        )

        if (openResult != WinError.ERROR_SUCCESS) return

        val hKey = hKeyByRef.value
        try {
            while (currentCoroutineContext().isActive) {
                val notifyResult = advapi32.RegNotifyChangeKeyValue(
                    hKey,
                    false,
                    WinNT.REG_NOTIFY_CHANGE_LAST_SET,
                    null,
                    false
                )

                if (!currentCoroutineContext().isActive) break
                if (notifyResult == WinError.ERROR_SUCCESS) {
                    val currentSystemThemeIsDark = isWindowsDarkTheme()
                    onThemeChanged(currentSystemThemeIsDark)
                } else {
                    break
                }
            }
        } finally {
            advapi32.RegCloseKey(hKey)
        }
    }
}