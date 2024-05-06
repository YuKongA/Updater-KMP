import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

val colorSchemeState: MutableState<ColorScheme?> = mutableStateOf(null)
val shapesState: MutableState<Shapes?> = mutableStateOf(null)
val typographyState: MutableState<Typography?> = mutableStateOf(null)
lateinit var baseColorScheme: ColorScheme
lateinit var baseShapes: Shapes
lateinit var baseTypography: Typography

val colorSchemeDefault: ColorScheme
    get() = colorSchemeState.value!!

val shapesDefault: Shapes
    get() = shapesState.value!!

val typographyDefault: Typography
    get() = typographyState.value!!

var initialized: Boolean = false

@Composable
fun AppTheme(
    colorScheme: ColorScheme? = null, shapes: Shapes? = null, typography: Typography? = null, content: @Composable () -> Unit
) {
    if (!initialized) {
        composableInit()
        platformComposableInit()
        initialized = true
    }
    return PlatformSpecifiedAppTheme(
        colorScheme ?: colorSchemeDefault, shapes ?: shapesDefault, typography ?: typographyDefault, content
    )
}

@Composable
internal expect fun PlatformSpecifiedAppTheme(
    colorScheme: ColorScheme, shapes: Shapes, typography: Typography, content: @Composable () -> Unit
)

@Composable
fun composableInit() {
    baseColorScheme = MaterialTheme.colorScheme
    baseShapes = MaterialTheme.shapes
    baseTypography = MaterialTheme.typography
    colorSchemeState.value = baseColorScheme
    shapesState.value = baseShapes
    typographyState.value = baseTypography
}

@Composable
expect fun platformComposableInit()