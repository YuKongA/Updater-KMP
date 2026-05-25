package viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.DataHelper
import data.repository.DeviceListRepository
import data.repository.LoginState
import data.repository.SessionRepository
import data.storage.PreferencesStorage
import data.usecase.FetchRomInfoUseCase
import data.usecase.RomInfoQuery
import data.usecase.RomInfoResult
import data.usecase.SessionUpdate
import di.AppContainer
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
import updater.shared.generated.resources.Res
import updater.shared.generated.resources.copy_successful
import updater.shared.generated.resources.download_start
import updater.shared.generated.resources.toast_crash_info
import updater.shared.generated.resources.toast_ing
import updater.shared.generated.resources.toast_no_info
import updater.shared.generated.resources.toast_no_ultimate_link
import updater.shared.generated.resources.toast_success_info
import updater.shared.generated.resources.toast_wrong_info

data class RomQueryUiState(
    val deviceName: String = "",
    val codeName: String = "",
    val deviceRegion: String = "Default (CN)",
    val deviceCarrier: String = "Default (Xiaomi)",
    val androidVersion: String = "16.0",
    val systemVersion: String = "",
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
)

class RomQueryViewModel(
    private val fetchRomInfoUseCase: FetchRomInfoUseCase = AppContainer.fetchRomInfoUseCase,
    private val preferences: PreferencesStorage = AppContainer.preferences,
    private val session: SessionRepository = AppContainer.session,
    private val deviceListRepository: DeviceListRepository = AppContainer.deviceListRepository,
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
        const val SEARCH_HISTORY = "searchHistory"
        const val LEGACY_SEARCH_KEYWORDS = "searchKeywords"
    }

    init {
        viewModelScope.launch { loadQueryPreferences() }
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
        val searchHistory = loadSearchHistoryFromPrefs()

        _uiState.update {
            it.copy(
                deviceName = deviceName,
                codeName = codeName,
                deviceRegion = deviceRegion,
                deviceCarrier = deviceCarrier,
                androidVersion = androidVersion,
                systemVersion = systemVersion,
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

    fun updateSearchHistorySelected(index: Int) {
        _uiState.update { it.copy(searchHistorySelected = index) }
    }

    fun clearSearchHistory() {
        _uiState.update { it.copy(searchHistory = emptyList()) }
        viewModelScope.launch { preferences.remove(Keys.SEARCH_HISTORY) }
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
                    loginData = loginData,
                )

                val outcome = fetchRomInfoUseCase.fetch(request)
                when (val update = outcome.sessionUpdate) {
                    is SessionUpdate.Refreshed -> session.save(update.loginData)
                    is SessionUpdate.Expired -> session.save(update.loginData.copy(authResult = "3"))
                    null -> Unit
                }
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
