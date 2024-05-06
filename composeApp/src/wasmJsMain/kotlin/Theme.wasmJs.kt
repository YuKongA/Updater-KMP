import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import cn.edu.buct.snc.xware.manage.misc.LoadFontAsync
import cn.edu.buct.snc.xware.manage.misc.loadFontAsync

@Composable
actual fun PlatformSpecifiedAppTheme(
    colorScheme: ColorScheme, shapes: Shapes, typography: Typography, content: @Composable () -> Unit
) = MaterialTheme(
    colorScheme = colorScheme, shapes = shapes, typography = typography, content = content
)

@Composable
actual fun platformComposableInit() {
}

fun loadFontAsyncListened(
    path: String,
    weight: FontWeight,
    style: FontStyle,
    identifier: String? = null,
): LoadFontAsync = loadFontAsync(path, weight, style, identifier, false).also {
    it.onCompleted { result ->
        result.onSuccess {
            rebuildFonts()
        }
        result.onFailure {}
    }
}

val black by loadFontAsyncListened(
    "fonts/NotoSansSC-Black.ttf", FontWeight.Black, FontStyle.Normal
)
val bold by loadFontAsyncListened(
    "fonts/NotoSansSC-Bold.ttf", FontWeight.Bold, FontStyle.Normal
)
val extraBold by loadFontAsyncListened(
    "fonts/NotoSansSC-ExtraBold.ttf", FontWeight.ExtraBold, FontStyle.Normal
)
val extraLight by loadFontAsyncListened(
    "fonts/NotoSansSC-ExtraLight.ttf", FontWeight.ExtraLight, FontStyle.Normal
)
val light by loadFontAsyncListened(
    "fonts/NotoSansSC-Light.ttf", FontWeight.Light, FontStyle.Normal
)
val medium by loadFontAsyncListened(
    "fonts/NotoSansSC-Medium.ttf", FontWeight.Medium, FontStyle.Normal
)
val regular by loadFontAsyncListened(
    "fonts/NotoSansSC-Regular.ttf", FontWeight.Normal, FontStyle.Normal
)
val semiBold by loadFontAsyncListened(
    "fonts/NotoSansSC-SemiBold.ttf", FontWeight.SemiBold, FontStyle.Normal
)
val thin by loadFontAsyncListened(
    "fonts/NotoSansSC-Thin.ttf", FontWeight.Thin, FontStyle.Normal
)

val fontFamilyState: MutableState<FontFamily> = mutableStateOf(FontFamily.Default)
var fontFamily: FontFamily
    get() = fontFamilyState.value
    set(value) {
        fontFamilyState.value = value
    }

fun rebuildFonts() {
    val fontList = listOfNotNull(
        black,
        bold,
        extraBold,
        extraLight,
        light,
        medium,
        regular,
        semiBold,
        thin,
    )
    fontFamily = if (fontList.isNotEmpty()) FontFamily(
        fonts = fontList
    ) else FontFamily.Default

    typographyState.value = Typography(
        displayLarge = baseTypography.displayLarge.copy(fontFamily = fontFamily),
        displayMedium = baseTypography.displayMedium.copy(fontFamily = fontFamily),
        displaySmall = baseTypography.displaySmall.copy(fontFamily = fontFamily),
        headlineLarge = baseTypography.headlineLarge.copy(fontFamily = fontFamily),
        headlineMedium = baseTypography.headlineMedium.copy(fontFamily = fontFamily),
        headlineSmall = baseTypography.headlineSmall.copy(fontFamily = fontFamily),
        titleLarge = baseTypography.titleLarge.copy(fontFamily = fontFamily),
        titleMedium = baseTypography.titleMedium.copy(fontFamily = fontFamily),
        titleSmall = baseTypography.titleSmall.copy(fontFamily = fontFamily),
        bodyLarge = baseTypography.bodyLarge.copy(fontFamily = fontFamily),
        bodyMedium = baseTypography.bodyMedium.copy(fontFamily = fontFamily),
        bodySmall = baseTypography.bodySmall.copy(fontFamily = fontFamily),
        labelLarge = baseTypography.labelLarge.copy(fontFamily = fontFamily),
        labelMedium = baseTypography.labelMedium.copy(fontFamily = fontFamily),
        labelSmall = baseTypography.labelSmall.copy(fontFamily = fontFamily),
    )
}