package misc

import data.DeviceInfoHelper
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import platform.httpClientPlatform
import platform.prefGet
import platform.prefSet

/**
 * Manages remote device list updates
 */
object RemoteDeviceListManager {

    @Serializable
    data class RemoteDeviceListData(
        val devices: List<DeviceInfoHelper.Device>,
        val version: String,
    )

    private const val DEVICE_LIST_URL = "https://raw.githubusercontent.com/YuKongA/Updater-KMP/device-list/device.json"
    private const val CACHED_DEVICE_LIST_KEY = "cached_device_list"
    private const val DEVICE_LIST_VERSION_KEY = "device_list_version"
    private const val DEVICE_LIST_UPDATE_ENABLED_KEY = "device_list_update_enabled"

    private val client = httpClientPlatform()
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Enable or disable remote device list updates
     */
    fun setUpdateEnabled(enabled: Boolean) {
        prefSet(DEVICE_LIST_UPDATE_ENABLED_KEY, enabled.toString())
    }

    /**
     * Get cached device list, or null if no cached data exists
     */
    fun getCachedDeviceList(): List<DeviceInfoHelper.Device>? {
        val cachedData = prefGet(CACHED_DEVICE_LIST_KEY) ?: return null
        return try {
            val remoteData = json.decodeFromString<RemoteDeviceListData>(cachedData)
            remoteData.devices
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Get cached device list version
     */
    fun getCachedVersion(): String? = prefGet(DEVICE_LIST_VERSION_KEY)

    /**
     * Fetch and cache device list from remote source
     * Returns updated device list or null if update failed
     */
    suspend fun updateDeviceList(): List<DeviceInfoHelper.Device>? {
        return try {
            withContext(Dispatchers.Main) {
                val response = client.get(DEVICE_LIST_URL)
                val jsonContent = response.bodyAsText()

                val remoteData = json.decodeFromString<RemoteDeviceListData>(jsonContent)

                val currentVersion = getCachedVersion()
                if (currentVersion != null && currentVersion >= remoteData.version) {
                    return@withContext getCachedDeviceList()
                }

                prefSet(CACHED_DEVICE_LIST_KEY, jsonContent)
                prefSet(DEVICE_LIST_VERSION_KEY, remoteData.version)

                remoteData.devices
            }
        } catch (_: Exception) {
            getCachedDeviceList()
        }
    }

    enum class DeviceListSource { REMOTE, EMBEDDED }

    private const val DEVICE_LIST_SOURCE_KEY = "device_list_source"

    fun getDeviceListSource(): DeviceListSource {
        return when (prefGet(DEVICE_LIST_SOURCE_KEY)) {
            "REMOTE" -> DeviceListSource.REMOTE
            "EMBEDDED" -> DeviceListSource.EMBEDDED
            else -> DeviceListSource.EMBEDDED
        }
    }

    fun setDeviceListSource(source: DeviceListSource) {
        prefSet(DEVICE_LIST_SOURCE_KEY, source.name)
    }

    /**
     * Get device list based on the selected source
     */
    suspend fun getDeviceList(embeddedList: List<DeviceInfoHelper.Device>): List<DeviceInfoHelper.Device> {
        return when (getDeviceListSource()) {
            DeviceListSource.REMOTE -> {
                val cachedList = getCachedDeviceList()
                if (!cachedList.isNullOrEmpty()) {
                    return cachedList
                }
                val remoteList = updateDeviceList()
                if (!remoteList.isNullOrEmpty()) {
                    return remoteList
                }
                embeddedList
            }

            DeviceListSource.EMBEDDED -> embeddedList
        }
    }
}