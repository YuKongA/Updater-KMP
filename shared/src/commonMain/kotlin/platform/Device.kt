package platform

/**
 * Current device information, only available on Xiaomi (HyperOS/MIUI) Android devices.
 * Returns null on other manufacturers or non-Android platforms.
 */
expect fun getDeviceInfo(): DeviceInfo?

data class DeviceInfo(
    val model: String,          // Build.MODEL, e.g. 2509FPN0BC
    val marketName: String,     // ro.product.marketname, may be empty (friendly fallback only)
    val codeName: String,       // Build.DEVICE, the bare codename, e.g. popsicle
    val androidVersion: String, // Build.VERSION.RELEASE, e.g. "16"
    val incremental: String,    // Build.VERSION.INCREMENTAL, carries the decodable suffix
)
