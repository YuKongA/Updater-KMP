package top.yukonga.updater.kmp

import App
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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

        enableEdgeToEdge()
        if (atLeast(Build.VERSION_CODES.Q)) {
            window.isNavigationBarContrastEnforced = false
        }

        if (atLeast(Build.VERSION_CODES.P)) {
            HiddenApiBypass.addHiddenApiExemptions("Lmiui/os/Build;")
        }

        setContent {
            val colorMode = remember { mutableIntStateOf(perfGet("colorMode")?.toInt() ?: 0) }
            val darkTheme = colorMode.intValue == 2

            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        Color.TRANSPARENT,
                        Color.TRANSPARENT,
                    ) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(
                        Color.argb(0xe6, 0xFF, 0xFF, 0xFF),
                        Color.argb(0x80, 0x1b, 0x1b, 0x1b),
                    ) { darkTheme },
                )
                onDispose { }
            }
            App(colorMode)
        }
    }
}