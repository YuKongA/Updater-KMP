package top.yukonga.updater.kmp

import App
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import org.lsposed.hiddenapibypass.HiddenApiBypass
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
            App()
        }
    }
}