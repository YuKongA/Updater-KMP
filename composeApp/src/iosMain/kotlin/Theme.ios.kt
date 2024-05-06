import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable

@Composable
actual fun PlatformSpecifiedAppTheme(
    colorScheme: ColorScheme, shapes: Shapes, typography: Typography, content: @Composable () -> Unit
) = MaterialTheme(
    colorScheme = colorScheme, shapes = shapes, typography = typography, content = content
)

@Composable
actual fun platformComposableInit() {
}