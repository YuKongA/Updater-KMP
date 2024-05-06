import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android SDK${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()