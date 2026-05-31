package platform

expect fun getDeviceInfo(): DeviceInfo?

data class DeviceInfo(
    val model: String,
    val codeName: String,
    val version: String,
)
