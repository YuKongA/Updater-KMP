package top.yukonga.updater.kmp

import App
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import org.lsposed.hiddenapibypass.HiddenApiBypass
import perfGet
import top.yukonga.updater.kmp.misc.AppUtils.atLeast

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AndroidAppContext.init(this)

        if (atLeast(Build.VERSION_CODES.P)) {
            HiddenApiBypass.addHiddenApiExemptions("Lmiui/os/Build;") // Xiaomi moment, for reflection check Xiaomi devices
        }

        setContent {
            val colorMode = remember { mutableIntStateOf(perfGet("colorMode")?.toInt() ?: 0) }
            val darkMode = colorMode.intValue == 2 || (isSystemInDarkTheme() && colorMode.intValue == 0)

            DisposableEffect(darkMode) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkMode },
                    navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkMode },
                )
                if (atLeast(Build.VERSION_CODES.Q)) {
                    window.isNavigationBarContrastEnforced = false // Xiaomi moment, this code must be here
                }
                onDispose {}
            }

            App(colorMode)
        }
    }
}