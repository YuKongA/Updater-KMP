package viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.DataHelper
import data.DeviceInfoHelper
import data.repository.DeviceListRepository
import data.repository.LoginState
import data.repository.SessionRepository
import data.storage.PreferencesStorage
import data.usecase.FetchRomInfoUseCase
import data.usecase.RomInfoQuery
import data.usecase.RomInfoResult
import data.usecase.persistTo
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.StringResource
import platform.DeviceInfo
import platform.getDeviceInfo
import updater.app.shared.generated.resources.Res
import updater.app.shared.generated.resources.copy_successful
import updater.app.shared.generated.resources.current_device_info_filled
import updater.app.shared.generated.resources.download_start
import updater.app.shared.generated.resources.toast_crash_info
import updater.app.shared.generated.resources.toast_ing
import updater.app.shared.generated.resources.toast_no_info
import updater.app.shared.generated.resources.toast_no_ultimate_link
import updater.app.shared.generated.resources.toast_success_info
import updater.app.shared.generated.resources.toast_wrong_info

data class RomQueryUiState(
    val deviceName: String = "",
    val codeName: String = "",
    val deviceRegion: String = "Default (CN)",
    val deviceCarrier: String = "Default (Xiaomi)",
    val androidVersion: String = "16.0",
    val systemVersion: String = "",
    val rustVersion: String = "1.3.0",
    val deviceNames: List<String> = emptyList(),
    val codeNames: List<String> = emptyList(),
    val curRomInfo: DataHelper.RomInfoData = DataHelper.RomInfoData(),
    val incRomInfo: DataHelper.RomInfoData = DataHelper.RomInfoData(),
    val curIconInfo: List<DataHelper.IconInfoData> = emptyList(),
    val incIconInfo: List<DataHelper.IconInfoData> = emptyList(),
    val curImageInfo: List<DataHelper.ImageInfoData> = emptyList(),
    val incImageInfo: List<DataHelper.ImageInfoData> = emptyList(),
    val xmsInfo: DataHelper.XmsInfoData = DataHelper.XmsInfoData(),
    val isLoading: Boolean = false,
    val searchHistory: List<DataHelper.SearchHistoryEntry> = emptyList(),
    val searchHistorySelected: Int = 0,
    val currentDeviceInfo: DeviceInfo? = null,
    val advancedOptions: Boolean = false,
)

class RomQueryViewModel(
    private val fetchRomInfoUseCase: FetchRomInfoUseCase,
    private val preferences: PreferencesStorage,
    private val session: SessionRepository,
    private val deviceListRepository: DeviceListRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RomQueryUiState())
    val uiState: StateFlow<RomQueryUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<UiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    private fun showMessage(resource: StringResource, duration: Long = 1000L) {
        _uiEvent.trySend(UiEvent.ShowMessage(resource, duration))
    }

    private object Keys {
        const val DEVICE_NAME = "deviceName"
        const val CODE_NAME = "codeName"
        const val DEVICE_REGION = "deviceRegion"
        const val DEVICE_CARRIER = "deviceCarrier"
        const val ANDROID_VERSION = "androidVersion"
        const val SYSTEM_VERSION = "systemVersion"
        const val RUST_VERSION = "rustVersion"
        const val ADVANCED_OPTIONS = "advancedOptions"
        const val SEARCH_HISTORY = "searchHistory"
        const val LEGACY_SEARCH_KEYWORDS = "searchKeywords"
    }

    init {
        val deviceInfo = getDeviceInfo()
        _uiState.update { it.copy(currentDeviceInfo = deviceInfo) }
        viewModelScope.launch {
            loadQueryPreferences()
            // History (the last saved query) takes priority. Only when there is no prior
            // query at all do we pre-fill the form from the current device.
            if (deviceInfo != null && _uiState.value.codeName.isEmpty()) {
                applyCurrentDevice(deviceInfo, notify = false)
            }
        }
        viewModelScope.launch {
            deviceListRepository.devices.collect { devices ->
                _uiState.update {
                    it.copy(
                        deviceNames = devices.map { d -> d.deviceName },
                        codeNames = devices.map { d -> d.deviceCodeName },
                    )
                }
            }
        }
    }

    private suspend fun loadQueryPreferences() {
        val deviceName = preferences.get(Keys.DEVICE_NAME) ?: ""
        val codeName = preferences.get(Keys.CODE_NAME) ?: ""
        val deviceRegion = preferences.get(Keys.DEVICE_REGION) ?: "Default (CN)"
        val deviceCarrier = preferences.get(Keys.DEVICE_CARRIER) ?: "Default (Xiaomi)"
        val androidVersion = preferences.get(Keys.ANDROID_VERSION) ?: "16.0"
        val systemVersion = preferences.get(Keys.SYSTEM_VERSION) ?: ""
        val rustVersion = preferences.get(Keys.RUST_VERSION) ?: "1.3.0"
        val advancedOptions = preferences.get(Keys.ADVANCED_OPTIONS)?.toBooleanStrictOrNull() ?: false
        val searchHistory = loadSearchHistoryFromPrefs()

        _uiState.update {
            it.copy(
                deviceName = deviceName,
                codeName = codeName,
                deviceRegion = deviceRegion,
                deviceCarrier = deviceCarrier,
                androidVersion = androidVersion,
                systemVersion = systemVersion,
                rustVersion = rustVersion,
                advancedOptions = advancedOptions,
                searchHistory = searchHistory,
            )
        }
    }

    private suspend fun loadSearchHistoryFromPrefs(): List<DataHelper.SearchHistoryEntry> {
        val current = preferences.get(Keys.SEARCH_HISTORY) ?: ""
        if (current.isNotEmpty()) {
            return runCatching {
                Json.decodeFromString<List<DataHelper.SearchHistoryEntry>>(current)
            }.getOrDefault(emptyList())
        }
        val oldStr = preferences.get(Keys.LEGACY_SEARCH_KEYWORDS) ?: "[]"
        return runCatching {
            val oldKeywords = Json.decodeFromString<List<String>>(oldStr)
            val migrated = oldKeywords.mapNotNull { keyword ->
                val parts = keyword.split("-")
                if (parts.size >= 6) {
                    DataHelper.SearchHistoryEntry(
                        deviceName = parts[0],
                        codeName = parts[1],
                        deviceRegion = parts[2],
                        deviceCarrier = parts[3],
                        androidVersion = parts[4],
                        systemVersion = parts[5],
                    )
                } else null
            }
            if (migrated.isNotEmpty()) {
                preferences.set(Keys.SEARCH_HISTORY, Json.encodeToString(migrated))
                preferences.remove(Keys.LEGACY_SEARCH_KEYWORDS)
            }
            migrated
        }.getOrDefault(emptyList())
    }

    fun updateDeviceName(name: String) {
        _uiState.update { state ->
            if (state.deviceName == name) return@update state
            val mappedCode = deviceListRepository.codeNameOf(name)
            state.copy(
                deviceName = name,
                codeName = if (mappedCode.isNotEmpty() && mappedCode != state.codeName) mappedCode else state.codeName,
            )
        }
    }

    fun updateCodeName(name: String) {
        _uiState.update { state ->
            if (state.codeName == name) return@update state
            val mappedName = deviceListRepository.deviceNameOf(name)
            state.copy(
                codeName = name,
                deviceName = if (mappedName.isNotEmpty() && mappedName != state.deviceName) mappedName else state.deviceName,
            )
        }
    }

    fun updateDeviceRegion(region: String) {
        _uiState.update { it.copy(deviceRegion = region) }
    }

    fun updateDeviceCarrier(carrier: String) {
        _uiState.update { it.copy(deviceCarrier = carrier) }
    }

    fun updateAndroidVersion(version: String) {
        _uiState.update { it.copy(androidVersion = version) }
    }

    fun updateSystemVersion(version: String) {
        _uiState.update { it.copy(systemVersion = version) }
    }

    fun updateRustVersion(version: String) {
        _uiState.update { it.copy(rustVersion = version) }
    }

    fun toggleAdvancedOptions() {
        val enabled = !_uiState.value.advancedOptions
        _uiState.update { it.copy(advancedOptions = enabled) }
        viewModelScope.launch { preferences.set(Keys.ADVANCED_OPTIONS, enabled.toString()) }
    }

    fun updateSearchHistorySelected(index: Int) {
        _uiState.update { it.copy(searchHistorySelected = index) }
    }

    fun clearSearchHistory() {
        _uiState.update { it.copy(searchHistory = emptyList()) }
        viewModelScope.launch { preferences.remove(Keys.SEARCH_HISTORY) }
    }

    /** Triggered by the "read from current device" button. */
    fun fillWithCurrent() {
        val info = _uiState.value.currentDeviceInfo ?: return
        applyCurrentDevice(info, notify = true)
    }

    private fun applyCurrentDevice(info: DeviceInfo, notify: Boolean) {
        val decoded = deviceListRepository.decodeVersionInfo(info.codeName, info.incremental)
        val mappedName = deviceListRepository.deviceNameOf(info.codeName)
        _uiState.update { state ->
            state.copy(
                codeName = info.codeName,
                deviceName = mappedName.ifEmpty { info.marketName.ifEmpty { info.model } },
                systemVersion = info.incremental,
                androidVersion = decoded?.androidVersion
                    ?: normalizeAndroidVersion(info.androidVersion)
                    ?: state.androidVersion,
                deviceRegion = decoded?.regionName ?: state.deviceRegion,
                deviceCarrier = decoded?.carrierName ?: state.deviceCarrier,
                rustVersion = info.rustVersion.ifEmpty { state.rustVersion },
            )
        }
        if (notify) showMessage(Res.string.current_device_info_filled)
    }

    /** "16" -> "16.0", and only return it when it matches a known dropdown option. */
    private fun normalizeAndroidVersion(release: String): String? {
        val candidate = if (release.contains('.')) release else "$release.0"
        return DeviceInfoHelper.androidVersions.firstOrNull { it == candidate }
    }

    fun loadSearchHistory(entry: DataHelper.SearchHistoryEntry) {
        _uiState.update {
            it.copy(
                deviceName = entry.deviceName,
                codeName = entry.codeName,
                deviceRegion = entry.deviceRegion,
                deviceCarrier = entry.deviceCarrier,
                androidVersion = entry.androidVersion,
                systemVersion = entry.systemVersion
            )
        }
    }

    fun notifyCopySuccess() {
        showMessage(Res.string.copy_successful)
    }

    fun notifyDownloadStart() {
        showMessage(Res.string.download_start)
    }

    private data class FetchSnapshot(
        val deviceName: String,
        val codeName: String,
        val deviceRegion: String,
        val deviceCarrier: String,
        val androidVersion: String,
        val systemVersion: String,
        val rustVersion: String,
        val loggedInUserId: String?,
    )

    private var fetchJob: Job? = null
    private var lastFetchSnapshot: FetchSnapshot? = null

    private suspend fun saveQueryPreferences(state: RomQueryUiState) {
        preferences.set(Keys.DEVICE_NAME, state.deviceName)
        preferences.set(Keys.CODE_NAME, state.codeName)
        preferences.set(Keys.DEVICE_REGION, state.deviceRegion)
        preferences.set(Keys.DEVICE_CARRIER, state.deviceCarrier)
        preferences.set(Keys.SYSTEM_VERSION, state.systemVersion)
        preferences.set(Keys.ANDROID_VERSION, state.androidVersion)
        preferences.set(Keys.RUST_VERSION, state.rustVersion)
    }

    fun fetchRomInfo() {
        val current = _uiState.value
        if (current.codeName.isEmpty() || current.androidVersion.isEmpty() || current.systemVersion.isEmpty()) {
            showMessage(Res.string.toast_no_info)
            return
        }
        val loginData = (session.state.value as? LoginState.LoggedIn)?.loginData
        val snapshot = FetchSnapshot(
            deviceName = current.deviceName,
            codeName = current.codeName,
            deviceRegion = current.deviceRegion,
            deviceCarrier = current.deviceCarrier,
            androidVersion = current.androidVersion,
            systemVersion = current.systemVersion,
            rustVersion = current.rustVersion,
            loggedInUserId = loginData?.userId,
        )
        val running = fetchJob
        if (running?.isActive == true) {
            if (snapshot == lastFetchSnapshot) return
            running.cancel()
        }
        lastFetchSnapshot = snapshot

        fetchJob = viewModelScope.launch {
            val self = coroutineContext[Job]
            try {
                _uiState.update {
                    it.copy(
                        isLoading = true,
                        curRomInfo = DataHelper.RomInfoData(),
                        incRomInfo = DataHelper.RomInfoData(),
                        curIconInfo = emptyList(),
                        incIconInfo = emptyList(),
                        curImageInfo = emptyList(),
                        incImageInfo = emptyList(),
                        xmsInfo = DataHelper.XmsInfoData(),
                    )
                }

                showMessage(Res.string.toast_ing)

                val state = _uiState.value
                val request = RomInfoQuery(
                    deviceName = state.deviceName,
                    codeName = state.codeName,
                    deviceRegion = state.deviceRegion,
                    deviceCarrier = state.deviceCarrier,
                    androidVersion = state.androidVersion,
                    systemVersion = state.systemVersion,
                    rustVersion = state.rustVersion,
                    loginData = loginData,
                )

                val outcome = fetchRomInfoUseCase.fetch(request)
                outcome.sessionUpdate.persistTo(session)
                when (val result = outcome.result) {
                    RomInfoResult.NetworkError -> showMessage(Res.string.toast_crash_info, 5000L)
                    RomInfoResult.NoData -> showMessage(Res.string.toast_no_info)
                    is RomInfoResult.Found -> applyFoundResult(result, state)
                }
            } finally {
                if (fetchJob === self) {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    private suspend fun applyFoundResult(result: RomInfoResult.Found, state: RomQueryUiState) {
        _uiState.update {
            it.copy(
                curRomInfo = result.curRomInfo,
                curIconInfo = result.curIconInfo,
                curImageInfo = result.curImageInfo,
                incRomInfo = result.incRomInfo,
                incIconInfo = result.incIconInfo,
                incImageInfo = result.incImageInfo,
                xmsInfo = result.xmsInfo,
            )
        }
        if (result.isFallback) {
            showMessage(Res.string.toast_wrong_info)
        } else {
            showMessage(if (result.noUltimateLink) Res.string.toast_no_ultimate_link else Res.string.toast_success_info)
            saveQueryPreferences(state)
            updateSearchHistory(
                state.deviceName, state.codeName, state.deviceRegion,
                state.deviceCarrier, state.androidVersion, state.systemVersion
            )
        }
    }

    private suspend fun updateSearchHistory(
        deviceName: String,
        codeName: String,
        deviceRegion: String,
        deviceCarrier: String,
        androidVersion: String,
        systemVersion: String
    ) {
        val newEntry = DataHelper.SearchHistoryEntry(
            deviceName = deviceName,
            codeName = codeName,
            deviceRegion = deviceRegion,
            deviceCarrier = deviceCarrier,
            androidVersion = androidVersion,
            systemVersion = systemVersion,
        )
        val currentHistory = _uiState.value.searchHistory.toMutableList()

        currentHistory.remove(newEntry)
        if (currentHistory.size >= 8) currentHistory.removeAt(currentHistory.size - 1)
        currentHistory.add(0, newEntry)

        _uiState.update { it.copy(searchHistory = currentHistory, searchHistorySelected = 0) }
        preferences.set(Keys.SEARCH_HISTORY, Json.encodeToString(currentHistory))
    }
}
