package data

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import platform.httpClientPlatform
import platform.prefGet
import platform.prefSet
import platform.prefRemove

/**
 * Manages remote device list updates
 */
object RemoteDeviceListManager {

    @Serializable
    data class RemoteDeviceListData(
        val devices: List<DeviceInfoHelper.Device>,
        val version: String,
        val lastUpdated: String
    )

    private const val DEVICE_LIST_URL = "https://raw.githubusercontent.com/YuKongA/Updater-KMP/device-list/device-list.json"
    private const val CACHED_DEVICE_LIST_KEY = "cached_device_list"
    private const val DEVICE_LIST_VERSION_KEY = "device_list_version"
    private const val DEVICE_LIST_LAST_UPDATE_KEY = "device_list_last_update"
    private const val DEVICE_LIST_UPDATE_ENABLED_KEY = "device_list_update_enabled"

    private val client = httpClientPlatform()
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Check if remote device list updates are enabled
     */
    fun isUpdateEnabled(): Boolean {
        return prefGet(DEVICE_LIST_UPDATE_ENABLED_KEY)?.toBoolean() ?: true
    }

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
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get cached device list version
     */
    fun getCachedVersion(): String? = prefGet(DEVICE_LIST_VERSION_KEY)

    /**
     * Get last update timestamp
     */
    fun getLastUpdateTime(): String? = prefGet(DEVICE_LIST_LAST_UPDATE_KEY)

    /**
     * Fetch and cache device list from remote source
     * Returns updated device list or null if update failed
     */
    suspend fun updateDeviceList(): List<DeviceInfoHelper.Device>? {
        if (!isUpdateEnabled()) return null

        return try {
            withContext(Dispatchers.IO) {
                val response = client.get(DEVICE_LIST_URL)
                val jsonContent = response.bodyAsText()
                
                val remoteData = json.decodeFromString<RemoteDeviceListData>(jsonContent)
                
                // Check if this is a newer version
                val currentVersion = getCachedVersion()
                if (currentVersion != null && currentVersion >= remoteData.version) {
                    return@withContext getCachedDeviceList()
                }

                // Cache the new data
                prefSet(CACHED_DEVICE_LIST_KEY, jsonContent)
                prefSet(DEVICE_LIST_VERSION_KEY, remoteData.version)
                prefSet(DEVICE_LIST_LAST_UPDATE_KEY, remoteData.lastUpdated)

                remoteData.devices
            }
        } catch (e: Exception) {
            // Update failed, return cached data if available
            getCachedDeviceList()
        }
    }

    /**
     * Clear cached device list data
     */
    fun clearCache() {
        prefRemove(CACHED_DEVICE_LIST_KEY)
        prefRemove(DEVICE_LIST_VERSION_KEY)
        prefRemove(DEVICE_LIST_LAST_UPDATE_KEY)
    }

    /**
     * Get combined device list: remote data if available, fallback to embedded list
     */
    suspend fun getDeviceList(embeddedList: List<DeviceInfoHelper.Device>): List<DeviceInfoHelper.Device> {
        if (!isUpdateEnabled()) return embeddedList

        // Try to get updated remote list
        val remoteList = updateDeviceList()
        if (!remoteList.isNullOrEmpty()) {
            return remoteList
        }

        // Fallback to cached data
        val cachedList = getCachedDeviceList()
        if (!cachedList.isNullOrEmpty()) {
            return cachedList
        }

        // Final fallback to embedded list
        return embeddedList
    }
}