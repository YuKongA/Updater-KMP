package data.repository

import data.DeviceInfoHelper
import data.DeviceListRefreshResult
import data.DeviceListSource
import data.storage.PreferencesStorage
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import platform.httpClientPlatform

class DeviceListRepositoryImpl(
    private val prefs: PreferencesStorage,
    private val embedded: List<DeviceInfoHelper.Device> = DeviceInfoHelper.embeddedDeviceList,
    private val client: HttpClient = httpClientPlatform(),
) : DeviceListRepository {
    private val json = Json { ignoreUnknownKeys = true }

    private val _devices = MutableStateFlow(embedded)
    override val devices: StateFlow<List<DeviceInfoHelper.Device>> = _devices.asStateFlow()

    override suspend fun cachedDeviceList(): List<DeviceInfoHelper.Device>? {
        val cached = prefs.get(KEY_CACHED) ?: return null
        return runCatching {
            json.decodeFromString<DeviceInfoHelper.RemoteDevices>(cached).devices
        }.getOrNull()
    }

    override suspend fun cachedVersion(): String? = prefs.get(KEY_VERSION)

    override suspend fun source(): DeviceListSource = when (prefs.get(KEY_SOURCE)) {
        "REMOTE" -> DeviceListSource.REMOTE
        else -> DeviceListSource.EMBEDDED
    }

    /**
     * Load the persisted source and publish the matching device list.
     * Called once on app start before the UI consumes [devices].
     */
    override suspend fun load() {
        _devices.value = resolveDeviceList()
    }

    override suspend fun setSource(source: DeviceListSource) {
        prefs.set(KEY_SOURCE, source.name)
        _devices.value = resolveDeviceList()
    }

    /**
     * Pull remote device list, persist if newer, and publish to [devices] when
     * the active source is REMOTE.
     */
    override suspend fun refresh(): DeviceListRefreshResult = withContext(Dispatchers.Default) {
        try {
            val response = client.get(DEVICE_LIST_URL)
            val jsonContent = cleanJson(response.bodyAsText())
            val remoteData = json.decodeFromString<DeviceInfoHelper.RemoteDevices>(jsonContent)

            val currentVersion = cachedVersion()
            val currentData = cachedDeviceList()
            if (currentVersion != null && currentData != null && currentVersion >= remoteData.version) {
                return@withContext DeviceListRefreshResult.UpToDate
            }
            prefs.set(KEY_VERSION, remoteData.version)
            prefs.set(KEY_CACHED, jsonContent)
            if (source() == DeviceListSource.REMOTE) {
                _devices.value = remoteData.devices
            }
            DeviceListRefreshResult.Success
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            DeviceListRefreshResult.Failed
        }
    }

    override fun codeNameOf(deviceName: String): String =
        _devices.value.firstOrNull { it.deviceName == deviceName }?.deviceCodeName ?: ""

    override fun deviceNameOf(codeName: String): String =
        _devices.value.firstOrNull { it.deviceCodeName == codeName }?.deviceName ?: ""

    override fun deviceCodeOf(
        androidVersionCode: String,
        codeName: String,
        regionCode: String,
        carrierCode: String,
    ): String {
        val letter = DeviceInfoHelper.androidLetterOf(androidVersionCode) ?: return ""
        val device = _devices.value.firstOrNull { it.deviceCodeName == codeName } ?: return ""
        return "$letter${device.deviceCode}$regionCode$carrierCode"
    }

    override fun decodeVersionInfo(codeName: String, systemVersion: String): DecodedDeviceVersion? {
        val device = _devices.value.firstOrNull { it.deviceCodeName == codeName } ?: return null
        // Strip a trailing ".DEV" (dev builds) so it does not become the parsed token.
        val core = systemVersion.uppercase().removeSuffix(".DEV")
        val token = core.substringAfterLast('.')
        val deviceCode = device.deviceCode.uppercase()
        if (token.length <= deviceCode.length) return null

        val letter = token.take(1)
        val afterLetter = token.drop(1)
        if (!afterLetter.startsWith(deviceCode)) return null
        val tail = afterLetter.removePrefix(deviceCode) // region(2) + carrier(2)

        val androidVersion = DeviceInfoHelper.androidVersionOfLetter(letter)
        val regionName = tail.takeIf { it.length >= 2 }
            ?.let { DeviceInfoHelper.regionNameOfCode(it.take(2)) }
        val carrierName = when {
            tail.length >= 4 -> DeviceInfoHelper.carrierNameOfCode(tail.substring(2, 4))
            tail.length == 2 -> DeviceInfoHelper.carrierNameOfCode("XM") // default Xiaomi when absent
            else -> null
        }
        return DecodedDeviceVersion(androidVersion, regionName, carrierName)
    }

    private suspend fun resolveDeviceList(): List<DeviceInfoHelper.Device> = when (source()) {
        DeviceListSource.REMOTE -> cachedDeviceList()?.takeIf { it.isNotEmpty() } ?: embedded
        DeviceListSource.EMBEDDED -> embedded
    }

    private fun cleanJson(raw: String): String = raw.replace(Regex(""",\s*([}\]])"""), "$1")

    companion object {
        private const val DEVICE_LIST_URL = "https://raw.githubusercontent.com/YuKongA/Updater-KMP/device-list/device.json"
        private const val KEY_CACHED = "deviceListCached"
        private const val KEY_VERSION = "deviceListVersion"
        private const val KEY_SOURCE = "deviceListSource"
    }
}
