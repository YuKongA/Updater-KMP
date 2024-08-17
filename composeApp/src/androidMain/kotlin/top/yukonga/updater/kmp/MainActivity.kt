package top.yukonga.updater.kmp

import App
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import top.yukonga.updater.kmp.misc.AppUtils.atLeast

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        if (atLeast(Build.VERSION_CODES.Q)) {
            window.isNavigationBarContrastEnforced = false // Xiaomi moment, this code must be here
        }

        AndroidAppContext.init(this)

        setContent {
            App()
        }
    }
}