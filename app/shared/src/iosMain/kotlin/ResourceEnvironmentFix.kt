@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
@file:OptIn(ExperimentalResourceApi::class, InternalResourceApi::class)

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import org.jetbrains.compose.resources.ComposeEnvironment
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.LanguageQualifier
import org.jetbrains.compose.resources.LocalComposeEnvironment
import org.jetbrains.compose.resources.RegionQualifier
import org.jetbrains.compose.resources.ResourceEnvironment
import org.jetbrains.compose.resources.getResourceEnvironment
import org.jetbrains.compose.resources.getSystemEnvironment
import platform.Foundation.NSLocale
import platform.Foundation.NSLocaleScriptCode
import platform.Foundation.currentLocale
import platform.Foundation.preferredLanguages

// https://youtrack.jetbrains.com/issue/CMP-6614/iOS-Localization-strings-for-language-qualifiers-that-are-not-the-same-between-platforms-appear-not-translated

val resourceEnvironmentFix: Unit = run {
    getResourceEnvironment = ::myResourceEnvironment
}

@Composable
fun ResourceEnvironmentFix(content: @Composable () -> Unit) {
    resourceEnvironmentFix

    val default = LocalComposeEnvironment.current
    CompositionLocalProvider(
        LocalComposeEnvironment provides object : ComposeEnvironment {
            @Composable
            override fun rememberEnvironment(): ResourceEnvironment {
                val environment = default.rememberEnvironment()
                return mapEnvironment(environment)
            }
        }
    ) {
        content()
    }
}

private fun myResourceEnvironment(): ResourceEnvironment {
    val environment = getSystemEnvironment()
    return mapEnvironment(environment)
}

private fun mapEnvironment(environment: ResourceEnvironment): ResourceEnvironment {
    val locale = NSLocale.preferredLanguages.firstOrNull()
        ?.let { NSLocale(it as String) }
        ?: NSLocale.currentLocale
    val script = locale.objectForKey(NSLocaleScriptCode) as? String

    return ResourceEnvironment(
        language = when (environment.language.language) {
            "he" -> LanguageQualifier("iw")
            "id" -> LanguageQualifier("in")
            else -> environment.language
        },
        region = when (environment.language.language) {
            "en" -> when (environment.region.region) {
                "" -> RegionQualifier("")
                "US" -> RegionQualifier("")
                "AU" -> RegionQualifier("AU")
                else -> RegionQualifier("GB")
            }

            "zh" -> when (script) {
                "Hans" -> RegionQualifier("CN")
                "Hant" -> RegionQualifier("TW")
                else -> environment.region
            }

            else -> environment.region
        },
        theme = environment.theme,
        density = environment.density
    )
}