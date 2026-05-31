package platform

actual fun getDeviceInfo(): DeviceInfo? {
    return DeviceInfo(
        model = android.os.Build.MODEL,
        codeName = android.os.Build.PRODUCT,
        version = android.os.Build.VERSION.INCREMENTAL,
    )
}
