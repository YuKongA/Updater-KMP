package utils

import data.DeviceInfoHelper
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import platform.httpClientPlatform
import platform.prefGet
import platform.prefSet

/**
 * Manages device list updates
 */
object DeviceListUtils {
    private const val DEVICE_LIST_URL = "https://raw.githubusercontent.com/YuKongA/Updater-KMP/device-list/device.json"
    private const val DEVICE_LIST_CACHED_KEY = "deviceListCached"
    private const val DEVICE_LIST_VERSION_KEY = "deviceListVersion"
    private const val DEVICE_LIST_SOURCE_KEY = "deviceListSource"
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Get cached device list, or null if no cached data exists
     */
    fun getCachedDeviceList(): List<DeviceInfoHelper.Device>? {
        val cachedData = prefGet(DEVICE_LIST_CACHED_KEY) ?: return null
        return try {
            val remoteData = json.decodeFromString<DeviceInfoHelper.RemoteDevices>(cachedData)
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
     * Clean JSON string by removing trailing commas
     */
    private fun cleanJson(json: String): String {
        return json.replace(Regex(""",\s*([}\]])"""), "$1")
    }

    /**
     * Fetch and cache device list from remote source
     * Returns updated device list or null if update failed
     */
    suspend fun updateDeviceList(): Int {
        return try {
            withContext(Dispatchers.Default) {
                val client = httpClientPlatform()
                val response = client.get(DEVICE_LIST_URL)
                val jsonContent = cleanJson(response.bodyAsText())
                val remoteData = json.decodeFromString<DeviceInfoHelper.RemoteDevices>(jsonContent)

                val currentVersion = getCachedVersion()
                val currentData = getCachedDeviceList()
                if (currentVersion != null && currentData != null && currentVersion >= remoteData.version) {
                    return@withContext 1
                }

                prefSet(DEVICE_LIST_VERSION_KEY, remoteData.version)
                prefSet(DEVICE_LIST_CACHED_KEY, jsonContent)

                0
            }
        } catch (_: Exception) {
            2
        }
    }

    enum class DeviceListSource { REMOTE, EMBEDDED }

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
    fun getDeviceList(embeddedList: List<DeviceInfoHelper.Device>): List<DeviceInfoHelper.Device> {
        return when (getDeviceListSource()) {
            DeviceListSource.REMOTE -> {
                val cachedList = getCachedDeviceList()
                if (!cachedList.isNullOrEmpty()) {
                    return cachedList
                }
                embeddedList
            }

            DeviceListSource.EMBEDDED -> embeddedList
        }
    }
}