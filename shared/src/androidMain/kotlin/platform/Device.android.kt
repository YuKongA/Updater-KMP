package platform

import android.os.Build

actual fun getDeviceInfo(): DeviceInfo? {
    if (!Build.MANUFACTURER.equals("Xiaomi", ignoreCase = true)) return null

    return DeviceInfo(
        model = Build.MODEL,
        marketName = systemProp("ro.product.marketname"),
        codeName = Build.DEVICE,
        androidVersion = Build.VERSION.RELEASE,
        incremental = Build.VERSION.INCREMENTAL,
    )
}

/**
 * Best-effort read of a system property via [android.os.SystemProperties].
 */
private fun systemProp(key: String): String = try {
    Class.forName("android.os.SystemProperties")
        .getMethod("get", String::class.java)
        .invoke(null, key) as? String ?: ""
} catch (_: Throwable) {
    ""
}
