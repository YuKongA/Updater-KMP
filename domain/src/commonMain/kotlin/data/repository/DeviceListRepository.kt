package data.repository

import data.DeviceInfoHelper
import data.DeviceListRefreshResult
import data.DeviceListSource
import kotlinx.coroutines.flow.StateFlow

data class DecodedDeviceVersion(
    val androidVersion: String?,
    val regionName: String?,
    val carrierName: String?,
)

interface DeviceListRepository {
    val devices: StateFlow<List<DeviceInfoHelper.Device>>
    suspend fun cachedDeviceList(): List<DeviceInfoHelper.Device>?
    suspend fun cachedVersion(): String?
    suspend fun source(): DeviceListSource
    suspend fun load()
    suspend fun setSource(source: DeviceListSource)
    suspend fun refresh(): DeviceListRefreshResult
    fun codeNameOf(deviceName: String): String
    fun deviceNameOf(codeName: String): String
    fun deviceCodeOf(
        androidVersionCode: String,
        codeName: String,
        regionCode: String,
        carrierCode: String,
    ): String

    fun decodeVersionInfo(codeName: String, systemVersion: String): DecodedDeviceVersion?
}
