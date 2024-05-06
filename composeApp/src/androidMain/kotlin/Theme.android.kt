import android.annotation.SuppressLint
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable

@SuppressLint("ComposableNaming")
@Composable
actual fun platformComposableInit() {
}

@Composable
internal actual fun PlatformSpecifiedAppTheme(
    colorScheme: ColorScheme, shapes: Shapes, typography: androidx.compose.material3.Typography, content: @Composable () -> Unit
) = MaterialTheme(
    colorScheme = colorScheme, shapes = shapes, typography = typography, content = content
)