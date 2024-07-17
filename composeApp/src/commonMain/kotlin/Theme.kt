import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun AppTheme(
    colorMode: Int = 0,
    content: @Composable () -> Unit
) {
    val darkTheme = isSystemInDarkTheme()
    return MaterialTheme(
        colorScheme = when (colorMode) {
            1 -> platformLightColor()
            2 -> platformDarkColor()
            else -> if (darkTheme) platformDarkColor() else platformLightColor()
        },
        shapes = MaterialTheme.shapes,
        typography = MaterialTheme.typography,
        content = content
    )
}

expect fun platformDarkColor(): ColorScheme
expect fun platformLightColor(): ColorScheme