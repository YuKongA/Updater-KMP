import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import top.yukonga.updater.kmp.AndroidAppContext

actual fun platformDarkColor(): ColorScheme {
    return when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = AndroidAppContext.getApplicationContext()
            dynamicDarkColorScheme(context!!)
        }

        else -> darkColorScheme()
    }
}

actual fun platformLightColor(): ColorScheme {
    return when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = AndroidAppContext.getApplicationContext()
            dynamicLightColorScheme(context!!)
        }

        else -> lightColorScheme()
    }
}