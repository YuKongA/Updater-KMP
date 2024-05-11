import platform.Foundation.NSProcessInfo

class MacOSPlatform : Platform {
    private val osVersion = NSProcessInfo.processInfo.operatingSystemVersionString
    override val name: String = "MacOS $osVersion".replace("Version ", "")
}

actual fun getPlatform(): Platform = MacOSPlatform()