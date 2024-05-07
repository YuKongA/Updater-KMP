import kotlin.experimental.ExperimentalNativeApi

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform