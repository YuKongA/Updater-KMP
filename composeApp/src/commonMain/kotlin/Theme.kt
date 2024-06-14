import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    val darkTheme = isSystemInDarkTheme()
    return MaterialTheme(
        colorScheme = platformColor() ?: when {
            darkTheme -> darkColorScheme()
            else -> lightColorScheme()
        },
        shapes = MaterialTheme.shapes,
        typography = MaterialTheme.typography,
        content = content
    )
}

@Composable
expect fun platformColor(): ColorScheme?