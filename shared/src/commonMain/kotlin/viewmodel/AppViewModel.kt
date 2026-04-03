package viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import Login
import Password
import data.DataHelper
import data.DeviceInfoHelper
import data.RomInfoHelper
import data.repository.RomInfoRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.getString
import platform.prefGet
import platform.prefRemove
import platform.prefSet
import top.yukonga.miuix.kmp.utils.Platform
import top.yukonga.miuix.kmp.utils.platform
import updater.shared.generated.resources.Res
import updater.shared.generated.resources.account_or_password_empty
import updater.shared.generated.resources.logging_in
import updater.shared.generated.resources.login_error
import updater.shared.generated.resources.login_successful
import updater.shared.generated.resources.login_tips
import updater.shared.generated.resources.logout_successful
import updater.shared.generated.resources.security_error
import updater.shared.generated.resources.toast_crash_info
import updater.shared.generated.resources.toast_ing
import updater.shared.generated.resources.toast_no_info
import updater.shared.generated.resources.toast_no_ultimate_link
import updater.shared.generated.resources.toast_success_info
import updater.shared.generated.resources.toast_wrong_info
import utils.MetadataUtils
import kotlin.time.ExperimentalTime

sealed interface LoginState {
    data object NotLoggedIn : LoginState
    data class LoggedIn(val loginData: DataHelper.LoginData) : LoginState
    data class Expired(val loginData: DataHelper.LoginData) : LoginState
}

sealed interface UiEvent {
    data class ShowMessage(val message: String, val duration: Long = 1000L) : UiEvent
}

sealed interface LoginEvent {
    data class AccountChanged(val value: String) : LoginEvent
    data class PasswordChanged(val value: String) : LoginEvent
    data class GlobalChanged(val value: Boolean) : LoginEvent
    data class SavePasswordChanged(val value: String) : LoginEvent
    data class TicketChanged(val value: String) : LoginEvent
    data class VerificationRequested(val value: Boolean) : LoginEvent
    data object LoginClicked : LoginEvent
    data object LogoutClicked : LoginEvent
    data object Submit2FA : LoginEvent
    data object CancelTicket : LoginEvent
    data object DismissDialog : LoginEvent
}

data class AppUiState(
    val deviceName: String = "",
    val codeName: String = "",
    val deviceRegion: String = "Default (CN)",
    val deviceCarrier: String = "Default (Xiaomi)",
    val androidVersion: String = "16.0",
    val systemVersion: String = "",
    val loginState: LoginState = LoginState.NotLoggedIn,
    val showLoginDialog: Boolean = false,
    val loginAccount: String = "",
    val loginPassword: String = "",
    val loginGlobal: Boolean = false,
    val loginSavePassword: String = "0",
    val showTicketUrl: Boolean = false,
    val showTicketInput: Boolean = false,
    val isVerifying: Boolean = false,
    val loginTicket: String = "",
    val isVerificationRequested: Boolean = false,
    val isLoggingIn: Boolean = false,
    val curRomInfo: DataHelper.RomInfoData = DataHelper.RomInfoData(),
    val incRomInfo: DataHelper.RomInfoData = DataHelper.RomInfoData(),
    val curIconInfo: List<DataHelper.IconInfoData> = emptyList(),
    val incIconInfo: List<DataHelper.IconInfoData> = emptyList(),
    val curImageInfo: List<DataHelper.ImageInfoData> = emptyList(),
    val incImageInfo: List<DataHelper.ImageInfoData> = emptyList(),
    val isLoading: Boolean = false,
    val searchHistory: List<DataHelper.SearchHistoryEntry> = emptyList(),
    val searchHistorySelected: Int = 0,
    val showMenuPopup: Boolean = false,
    val showDeviceSettingsDialog: Boolean = false,
    val showAboutDialog: Boolean = false
)

class AppViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    private val _uiEvent = Channel<UiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    private val repository = RomInfoRepository()

    private fun showMessage(message: String, duration: Long = 1000L) {
        _uiEvent.trySend(UiEvent.ShowMessage(message, duration))
    }

    init {
        val deviceName = prefGet("deviceName") ?: ""
        val codeName = prefGet("codeName") ?: ""
        val deviceRegion = prefGet("deviceRegion") ?: "Default (CN)"
        val deviceCarrier = prefGet("deviceCarrier") ?: "Default (Xiaomi)"
        val androidVersion = prefGet("androidVersion") ?: "16.0"
        val systemVersion = prefGet("systemVersion") ?: ""
        val loginDataStr = prefGet("loginInfo")
        val loginData = loginDataStr?.let { Json.decodeFromString<DataHelper.LoginData>(it) }
        val loginState = when {
            loginData == null -> LoginState.NotLoggedIn
            loginData.authResult == "3" -> LoginState.Expired(loginData)
            loginData.authResult == "1" -> LoginState.LoggedIn(loginData)
            else -> LoginState.NotLoggedIn
        }
        val searchHistoryStr = prefGet("searchHistory") ?: ""
        val searchHistory = if (searchHistoryStr.isNotEmpty()) {
            try {
                Json.decodeFromString<List<DataHelper.SearchHistoryEntry>>(searchHistoryStr)
            } catch (_: Exception) {
                emptyList()
            }
        } else {
            // Migrate from old format
            val oldStr = prefGet("searchKeywords") ?: "[]"
            try {
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
                    prefSet("searchHistory", Json.encodeToString(migrated))
                    prefRemove("searchKeywords")
                }
                migrated
            } catch (_: Exception) {
                emptyList()
            }
        }

        val savedPassword = Password.getPassword()
        val savedSavePassword = prefGet("savePassword") ?: "0"

        _uiState.update {
            it.copy(
                deviceName = deviceName,
                codeName = codeName,
                deviceRegion = deviceRegion,
                deviceCarrier = deviceCarrier,
                androidVersion = androidVersion,
                systemVersion = systemVersion,
                loginState = loginState,
                loginAccount = savedPassword.first,
                loginPassword = savedPassword.second,
                loginSavePassword = savedSavePassword,
                searchHistory = searchHistory
            )
        }

        viewModelScope.launch {
            DeviceInfoHelper.updateDeviceList()
        }
    }

    fun updateDeviceName(name: String) {
        _uiState.update { it.copy(deviceName = name) }
    }

    fun updateCodeName(name: String) {
        _uiState.update { it.copy(codeName = name) }
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

    fun updateShowMenuPopup(show: Boolean) {
        _uiState.update { it.copy(showMenuPopup = show) }
    }

    fun updateShowDeviceSettingsDialog(show: Boolean) {
        _uiState.update { it.copy(showDeviceSettingsDialog = show) }
    }

    fun updateShowAboutDialog(show: Boolean) {
        _uiState.update { it.copy(showAboutDialog = show) }
    }

    fun showLoginDialog() {
        prefRemove("captchaUrl")
        prefRemove("notificationUrl")
        prefRemove("identity_session")
        prefRemove("2FAContext")
        prefRemove("2FAOptions")
        prefRemove("2FAFlag")

        _uiState.update {
            it.copy(
                showLoginDialog = true,
                showTicketInput = false,
                showTicketUrl = false,
                loginTicket = "",
                isVerifying = false,
                isLoggingIn = false,
                isVerificationRequested = false,
            )
        }
    }

    fun dismissLoginDialog() {
        _uiState.update { it.copy(showLoginDialog = false) }
    }

    fun onLoginEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.AccountChanged -> _uiState.update { it.copy(loginAccount = event.value) }
            is LoginEvent.PasswordChanged -> _uiState.update { it.copy(loginPassword = event.value) }
            is LoginEvent.GlobalChanged -> _uiState.update { it.copy(loginGlobal = event.value) }
            is LoginEvent.SavePasswordChanged -> _uiState.update { it.copy(loginSavePassword = event.value) }
            is LoginEvent.TicketChanged -> _uiState.update { it.copy(loginTicket = event.value) }
            is LoginEvent.VerificationRequested -> _uiState.update { it.copy(isVerificationRequested = event.value) }
            is LoginEvent.LoginClicked -> performLogin()
            is LoginEvent.LogoutClicked -> performLogout()
            is LoginEvent.Submit2FA -> submit2FATicket()
            is LoginEvent.CancelTicket -> _uiState.update {
                it.copy(showTicketUrl = false, showTicketInput = false, loginTicket = "")
            }
            is LoginEvent.DismissDialog -> dismissLoginDialog()
        }
    }

    private fun performLogin() {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.update { it.copy(isLoggingIn = true) }
            showMessage(getString(Res.string.logging_in))

            val result = Login.login(
                account = state.loginAccount,
                password = state.loginPassword,
                global = state.loginGlobal,
                savePassword = state.loginSavePassword
            )
            when (result) {
                0 -> {
                    showMessage(getString(Res.string.login_successful))
                    val loginData = prefGet("loginInfo")?.let { Json.decodeFromString<DataHelper.LoginData>(it) }
                    if (loginData != null) {
                        _uiState.update { it.copy(loginState = LoginState.LoggedIn(loginData), showLoginDialog = false) }
                    }
                }
                1 -> showMessage(getString(Res.string.account_or_password_empty))
                2 -> showMessage(getString(Res.string.toast_crash_info))
                3 -> {
                    showMessage(getString(Res.string.login_error))
                    prefRemove("password")
                    prefRemove("passwordIv")
                }
                4 -> showMessage(getString(Res.string.security_error))
                5 -> {
                    showMessage(getString(Res.string.login_tips))
                    val optionsStr = prefGet("2FAOptions") ?: "[]"
                    val options = Json.decodeFromString<List<Int>>(optionsStr)
                    val flag = if (options.contains(4)) 4 else 8
                    prefSet("2FAFlag", flag.toString())
                    _uiState.update { it.copy(showTicketUrl = true, showTicketInput = true) }
                }
            }
            _uiState.update { it.copy(isLoggingIn = false) }
        }
    }

    private fun performLogout() {
        viewModelScope.launch {
            val success = Login.logout()
            if (success) {
                showMessage(getString(Res.string.logout_successful))
                _uiState.update { it.copy(loginState = LoginState.NotLoggedIn, showLoginDialog = false) }
            }
        }
    }

    private fun submit2FATicket() {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.update { it.copy(isVerifying = true) }
            val result = Login.login(
                account = state.loginAccount,
                password = state.loginPassword,
                global = state.loginGlobal,
                savePassword = state.loginSavePassword,
                flag = prefGet("2FAFlag")?.toInt(),
                ticket = state.loginTicket
            )
            if (result == 0) {
                showMessage(getString(Res.string.login_successful))
                val loginData = prefGet("loginInfo")?.let { Json.decodeFromString<DataHelper.LoginData>(it) }
                if (loginData != null) {
                    _uiState.update {
                        it.copy(
                            loginState = LoginState.LoggedIn(loginData),
                            showLoginDialog = false,
                            loginTicket = ""
                        )
                    }
                }
            } else {
                showMessage(getString(Res.string.login_error))
            }
            _uiState.update { it.copy(isVerifying = false) }
        }
    }

    fun clearSearchHistory() {
        _uiState.update { it.copy(searchHistory = emptyList()) }
        prefRemove("searchHistory")
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

    private data class RequestParams(
        val regionCode: String,
        val carrierCode: String,
        val codeNameExt: String,
        val systemVersionExt: String,
        val branchExt: String,
    )

    private fun buildRequestParams(state: AppUiState): RequestParams {
        val regionCode = DeviceInfoHelper.regionCode(state.deviceRegion)
        val carrierCode = DeviceInfoHelper.carrierCode(state.deviceCarrier)
        val deviceCode = DeviceInfoHelper.deviceCode(state.androidVersion, state.codeName, regionCode, carrierCode)
        val regionCodeName = DeviceInfoHelper.regionCodeName(state.deviceRegion)
        val carrierCodeName = DeviceInfoHelper.carrierCodeName(state.deviceCarrier)

        val codeNameExt = if (regionCodeName.isNotEmpty()) {
            state.codeName + regionCodeName.replace("_global", "") + carrierCodeName + "_global"
        } else {
            if (regionCode == "CN" && carrierCode == "DM") {
                state.codeName + "_demo"
            } else {
                state.codeName + carrierCodeName
            }
        }
        val systemVersionExt =
            state.systemVersion.uppercase().replace("^OS1".toRegex(), "V816").replace("AUTO$".toRegex(), deviceCode)
        val branchExt = if (state.systemVersion.uppercase().endsWith(".DEV")) "X" else "F"

        return RequestParams(regionCode, carrierCode, codeNameExt, systemVersionExt, branchExt)
    }

    private fun updateAuthState(recoveryRomInfo: RomInfoHelper.RomInfo, loginState: LoginState) {
        val currentLoginData = (loginState as? LoginState.LoggedIn)?.loginData
        if (currentLoginData != null && recoveryRomInfo.authResult != 1) {
            val expiredLoginData = currentLoginData.copy(authResult = "3")
            _uiState.update { it.copy(loginState = LoginState.Expired(expiredLoginData)) }
            prefSet("loginInfo", Json.encodeToString(expiredLoginData))
        }
    }

    private suspend fun fetchCurrentRomDownloadUrl(
        recoveryRomInfo: RomInfoHelper.RomInfo,
        romInfoStr: String,
        params: RequestParams,
        androidVersion: String,
        loginData: DataHelper.LoginData?,
    ): Pair<String, Boolean> {
        var noUltimateLink = false
        val curRomDownload = if (recoveryRomInfo.currentRom?.md5 != recoveryRomInfo.latestRom?.md5) {
            val romInfoCurrentStr = repository.getRecoveryRomInfo(
                "", params.codeNameExt, params.regionCode,
                params.systemVersionExt, androidVersion, loginData
            )
            val recoveryRomInfoCurrent = if (romInfoCurrentStr.isNotEmpty()) {
                Json.decodeFromString<RomInfoHelper.RomInfo>(romInfoCurrentStr)
            } else {
                Json.decodeFromString<RomInfoHelper.RomInfo>(romInfoStr)
            }

            if (recoveryRomInfoCurrent.latestRom?.filename != null) {
                downloadUrl(recoveryRomInfoCurrent.currentRom?.version!!, recoveryRomInfoCurrent.latestRom.filename)
            } else {
                noUltimateLink = true
                showMessage(getString(Res.string.toast_no_ultimate_link))
                downloadUrl(recoveryRomInfo.currentRom!!.version!!, recoveryRomInfo.currentRom.filename!!)
            }
        } else {
            downloadUrl(recoveryRomInfo.currentRom!!.version!!, recoveryRomInfo.latestRom?.filename!!)
        }
        return Pair(curRomDownload, noUltimateLink)
    }

    private fun saveQueryPreferences(state: AppUiState) {
        prefSet("deviceName", state.deviceName)
        prefSet("codeName", state.codeName)
        prefSet("deviceRegion", state.deviceRegion)
        prefSet("deviceCarrier", state.deviceCarrier)
        prefSet("systemVersion", state.systemVersion)
        prefSet("androidVersion", state.androidVersion)
    }

    private suspend fun fetchAndApplyMetadata(curRomData: DataHelper.RomInfoData, noUltimateLink: Boolean) {
        if (isWeb()) return
        val url = if (noUltimateLink) curRomData.cdn1Download else curRomData.official1Download
        if (url.isEmpty()) return

        val metadata = MetadataUtils.getMetadata(url)
        val fingerprint = MetadataUtils.getMetadataValue(metadata, "post-build=")
        val securityPatchLevel = MetadataUtils.getMetadataValue(metadata, "post-security-patch-level=")
        val timestamp = convertTimestampToDateTime(MetadataUtils.getMetadataValue(metadata, "post-timestamp="))

        _uiState.update {
            it.copy(
                curRomInfo = it.curRomInfo.copy(
                    fingerprint = fingerprint,
                    securityPatchLevel = securityPatchLevel,
                    timestamp = timestamp,
                )
            )
        }
    }

    private suspend fun handleCurrentRom(
        recoveryRomInfo: RomInfoHelper.RomInfo,
        romInfoStr: String,
        params: RequestParams,
        state: AppUiState,
        loginData: DataHelper.LoginData?,
    ) {
        val (curRomDownload, noUltimateLink) = fetchCurrentRomDownloadUrl(
            recoveryRomInfo, romInfoStr, params, state.androidVersion, loginData
        )

        val (curRomData, curIcons, curImages) = processRomInfo(
            recoveryRomInfo, recoveryRomInfo.currentRom, curRomDownload, noUltimateLink
        )
        _uiState.update { it.copy(curRomInfo = curRomData, curIconInfo = curIcons, curImageInfo = curImages) }

        saveQueryPreferences(state)
        updateSearchHistory(
            state.deviceName, state.codeName, state.deviceRegion,
            state.deviceCarrier, state.androidVersion, state.systemVersion
        )

        val incRom = recoveryRomInfo.incrementRom ?: recoveryRomInfo.crossRom
        if (incRom?.bigversion != null) {
            val (incRomData, incIcons, incImages) = processRomInfo(recoveryRomInfo, incRom)
            _uiState.update { it.copy(incRomInfo = incRomData, incIconInfo = incIcons, incImageInfo = incImages) }
        }

        if (noUltimateLink) {
            showMessage(getString(Res.string.toast_no_ultimate_link))
        } else {
            showMessage(getString(Res.string.toast_success_info))
        }

        fetchAndApplyMetadata(curRomData, noUltimateLink)
    }

    private suspend fun handleFallbackRom(recoveryRomInfo: RomInfoHelper.RomInfo, rom: RomInfoHelper.Rom) {
        val (curRomData, curIcons, curImages) = processRomInfo(recoveryRomInfo, rom)
        _uiState.update { it.copy(curRomInfo = curRomData, curIconInfo = curIcons, curImageInfo = curImages) }
        showMessage(getString(Res.string.toast_wrong_info))
    }

    fun fetchRomInfo() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    curRomInfo = DataHelper.RomInfoData(),
                    incRomInfo = DataHelper.RomInfoData(),
                    curIconInfo = emptyList(),
                    incIconInfo = emptyList(),
                    curImageInfo = emptyList(),
                    incImageInfo = emptyList()
                )
            }

            showMessage(getString(Res.string.toast_ing))

            val state = _uiState.value
            val params = buildRequestParams(state)
            val loginData = (state.loginState as? LoginState.LoggedIn)?.loginData

            val romInfoStr = repository.getRecoveryRomInfo(
                params.branchExt, params.codeNameExt, params.regionCode,
                params.systemVersionExt, state.androidVersion, loginData
            )

            if (romInfoStr.isEmpty()) {
                showMessage(getString(Res.string.toast_crash_info), 5000L)
            } else {
                val recoveryRomInfo = Json.decodeFromString<RomInfoHelper.RomInfo>(romInfoStr)
                updateAuthState(recoveryRomInfo, state.loginState)

                when {
                    recoveryRomInfo.currentRom?.bigversion != null ->
                        handleCurrentRom(recoveryRomInfo, romInfoStr, params, state, loginData)
                    recoveryRomInfo.incrementRom?.bigversion != null ->
                        handleFallbackRom(recoveryRomInfo, recoveryRomInfo.incrementRom)
                    recoveryRomInfo.crossRom?.bigversion != null ->
                        handleFallbackRom(recoveryRomInfo, recoveryRomInfo.crossRom)
                    else ->
                        showMessage(getString(Res.string.toast_no_info))
                }
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun updateSearchHistory(
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
        prefSet("searchHistory", Json.encodeToString(currentHistory))
    }

    private fun processRomInfo(
        recoveryRomInfo: RomInfoHelper.RomInfo,
        romInfo: RomInfoHelper.Rom?,
        officialDownload: String? = null,
        noUltimateLink: Boolean = false,
    ): Triple<DataHelper.RomInfoData, List<DataHelper.IconInfoData>, List<DataHelper.ImageInfoData>> {
        if (romInfo?.bigversion == null) return Triple(DataHelper.RomInfoData(), emptyList(), emptyList())

        val log = StringBuilder()
        romInfo.changelog?.forEach { (category, items) ->
            if (category.isNotEmpty()) log.append(category).append("\n")
            items.forEach { item ->
                val text = item.txt.trimEnd()
                if (text.isNotEmpty()) log.append(text).append("\n")
            }
            log.append("\n")
        }
        val changelogGroups = log.toString().trimEnd().split("\n\n")
        val changelog = changelogGroups.map { group -> group.lines().drop(1).joinToString("\n") }

        val gentle = StringBuilder()
        val formattedGentleNotice = recoveryRomInfo.gentleNotice?.text?.replace("<li>", "\n· ")
            ?.replace("</li>", "")?.replace("<p>", "\n")?.replace("</p>", "")?.replace("&nbsp;", " ")
            ?.replace("&#160;", "")?.replace(Regex("<[^>]*>"), "")?.trim()
        formattedGentleNotice?.forEach { gentle.append(it) }
        val gentleNotice = gentle.toString().trimEnd().split("\n").drop(1).joinToString("\n")

        var imageInfoData = emptyList<DataHelper.ImageInfoData>()
        var iconInfoData = emptyList<DataHelper.IconInfoData>()

        if (!romInfo.osbigversion.isNullOrEmpty() && romInfo.osbigversion.toFloat() >= 3.0f) {
            val imageMainLink = recoveryRomInfo.fileMirror?.image ?: ""
            imageInfoData = romInfo.changelog?.flatMap { (categoryTitle, items) ->
                items.map { item ->
                    val image = item.image?.firstOrNull()
                    DataHelper.ImageInfoData(
                        title = categoryTitle,
                        changelog = item.txt,
                        imageUrl = imageLink(imageMainLink, image?.path),
                        imageWidth = image?.w?.toIntOrNull(),
                        imageHeight = image?.h?.toIntOrNull()
                    )
                }
            } ?: emptyList()
        } else {
            val iconNames = changelogGroups.map { it.split("\n").first() }
            val iconMainLink = recoveryRomInfo.fileMirror?.icon ?: ""
            val iconNameLink = recoveryRomInfo.icon ?: mapOf()
            val iconLinks = iconLink(iconNames, iconMainLink, iconNameLink)
            iconInfoData = iconNames.mapIndexed { index, iconName ->
                DataHelper.IconInfoData(
                    iconName = iconName,
                    iconLink = iconLinks[iconName] ?: "",
                    changelog = changelog[index]
                )
            }
        }

        val bigVersion = when {
            !romInfo.osbigversion.isNullOrEmpty() && romInfo.osbigversion != ".0" && romInfo.osbigversion != "0.0" -> "HyperOS " + romInfo.osbigversion
            romInfo.bigversion.contains("816") -> romInfo.bigversion.replace("816", "HyperOS 1.0")
            else -> "MIUI ${romInfo.bigversion}"
        }

        val official1Download = if (noUltimateLink) "" else {
            "https://ultimateota.d.miui.com" + (officialDownload ?: downloadUrl(romInfo.version, romInfo.filename))
        }
        val official2Download = if (noUltimateLink) "" else {
            "https://superota.d.miui.com" + (officialDownload ?: downloadUrl(romInfo.version, romInfo.filename))
        }
        val cdn1Download =
            "https://bkt-sgp-miui-ota-update-alisgp.oss-ap-southeast-1.aliyuncs.com" + downloadUrl(romInfo.version, romInfo.filename)
        val cdn2Download = "https://cdnorg.d.miui.com" + downloadUrl(romInfo.version, romInfo.filename)

        val romInfoData = DataHelper.RomInfoData(
            type = romInfo.type.toString(),
            device = romInfo.device.toString(),
            version = romInfo.version.toString(),
            codebase = romInfo.codebase.toString(),
            branch = romInfo.branch.toString(),
            bigVersion = bigVersion,
            fileName = romInfo.filename.toString().substringBefore(".zip") + ".zip",
            fileSize = romInfo.filesize.toString(),
            md5 = romInfo.md5.toString(),
            isBeta = romInfo.isBeta == 1,
            isGov = romInfo.isGov == 1,
            official1Download = official1Download,
            official2Download = official2Download,
            cdn1Download = cdn1Download,
            cdn2Download = cdn2Download,
            changelog = log.toString().trimEnd(),
            gentleNotice = gentleNotice,
        )

        return Triple(romInfoData, iconInfoData, imageInfoData)
    }

    private fun downloadUrl(romVersion: String?, romFilename: String?): String {
        return "/$romVersion/$romFilename"
    }

    private fun iconLink(iconNames: List<String>, iconMainLink: String, iconNameLink: Map<String, String>): MutableMap<String, String> {
        if (isWeb()) return mutableMapOf()
        val iconMap = mutableMapOf<String, String>()
        val safeIconMainLink = iconMainLink.replace("http://", "https://")
        if (safeIconMainLink.isNotEmpty() && iconNameLink.isNotEmpty()) {
            for (name in iconNames) {
                iconNameLink[name]?.let { iconMap[name] = safeIconMainLink + it }
            }
        }
        return iconMap
    }

    private fun imageLink(mirrorImage: String, path: String?): String {
        if (isWeb()) return ""
        val base = mirrorImage.replace("http://", "https://")
        return base + (path ?: "")
    }

    @OptIn(ExperimentalTime::class)
    private fun convertTimestampToDateTime(timestamp: String): String {
        val epochSeconds = timestamp.toLongOrNull() ?: return ""
        val instant = kotlin.time.Instant.fromEpochSeconds(epochSeconds)
        val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return "${dateTime.year}/${dateTime.month.number}/${dateTime.day} " +
                dateTime.hour.toString().padStart(2, '0') +
                ":${dateTime.minute.toString().padStart(2, '0')}" +
                ":${dateTime.second.toString().padStart(2, '0')}"
    }

    private fun isWeb(): Boolean = platform() == Platform.WasmJs || platform() == Platform.Js
}
