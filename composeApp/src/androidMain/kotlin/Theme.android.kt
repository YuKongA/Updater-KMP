import android.annotation.SuppressLint
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@SuppressLint("ComposableNaming")
@Composable
actual fun platformComposableInit() {
}

@Composable
internal actual fun PlatformSpecifiedAppTheme(
    colorScheme: ColorScheme, shapes: Shapes, typography: androidx.compose.material3.Typography, content: @Composable () -> Unit
) {
    val darkTheme = isSystemInDarkTheme()
    val colorSchemeAndroid = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> darkColorScheme()
        else -> colorScheme
    }
    MaterialTheme(colorScheme = colorSchemeAndroid, shapes = shapes, typography = typography, content = content)
}