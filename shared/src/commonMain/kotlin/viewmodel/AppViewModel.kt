package viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.DataHelper
import data.DeviceInfoHelper
import data.RomInfoHelper
import data.repository.RomInfoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
import updater.shared.generated.resources.toast_crash_info
import updater.shared.generated.resources.toast_ing
import updater.shared.generated.resources.toast_no_info
import updater.shared.generated.resources.toast_no_ultimate_link
import updater.shared.generated.resources.toast_success_info
import updater.shared.generated.resources.toast_wrong_info
import utils.MessageUtils
import utils.MetadataUtils
import kotlin.time.ExperimentalTime

data class AppUiState(
    val deviceName: String = "",
    val codeName: String = "",
    val deviceRegion: String = "Default (CN)",
    val deviceCarrier: String = "Default (Xiaomi)",
    val androidVersion: String = "16.0",
    val systemVersion: String = "",
    val isLogin: Int = 0,
    val loginData: DataHelper.LoginData? = null,
    val curRomInfo: DataHelper.RomInfoData = DataHelper.RomInfoData(),
    val incRomInfo: DataHelper.RomInfoData = DataHelper.RomInfoData(),
    val curIconInfo: List<DataHelper.IconInfoData> = emptyList(),
    val incIconInfo: List<DataHelper.IconInfoData> = emptyList(),
    val curImageInfo: List<DataHelper.ImageInfoData> = emptyList(),
    val incImageInfo: List<DataHelper.ImageInfoData> = emptyList(),
    val isLoading: Boolean = false,
    val searchKeywords: List<String> = emptyList(),
    val searchKeywordsSelected: Int = 0,
    val showMenuPopup: Boolean = false,
    val showDeviceSettingsDialog: Boolean = false
)

class AppViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    private val repository = RomInfoRepository()

    init {
        val deviceName = prefGet("deviceName") ?: ""
        val codeName = prefGet("codeName") ?: ""
        val deviceRegion = prefGet("deviceRegion") ?: "Default (CN)"
        val deviceCarrier = prefGet("deviceCarrier") ?: "Default (Xiaomi)"
        val androidVersion = prefGet("androidVersion") ?: "16.0"
        val systemVersion = prefGet("systemVersion") ?: ""
        val loginDataStr = prefGet("loginInfo")
        val loginData = loginDataStr?.let { Json.decodeFromString<DataHelper.LoginData>(it) }
        val isLogin = loginData?.authResult?.toIntOrNull() ?: 0
        val searchKeywordsStr = prefGet("searchKeywords") ?: "[]"
        val searchKeywords = Json.decodeFromString<List<String>>(searchKeywordsStr)

        _uiState.update {
            it.copy(
                deviceName = deviceName,
                codeName = codeName,
                deviceRegion = deviceRegion,
                deviceCarrier = deviceCarrier,
                androidVersion = androidVersion,
                systemVersion = systemVersion,
                loginData = loginData,
                isLogin = isLogin,
                searchKeywords = searchKeywords
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

    fun updateSearchKeywordsSelected(index: Int) {
        _uiState.update { it.copy(searchKeywordsSelected = index) }
    }

    fun updateShowMenuPopup(show: Boolean) {
        _uiState.update { it.copy(showMenuPopup = show) }
    }

    fun updateShowDeviceSettingsDialog(show: Boolean) {
        _uiState.update { it.copy(showDeviceSettingsDialog = show) }
    }

    fun updateLoginState(state: Int) {
        _uiState.update { it.copy(isLogin = state) }
    }

    fun clearSearchHistory() {
        _uiState.update { it.copy(searchKeywords = emptyList()) }
        prefRemove("searchKeywords")
    }

    fun loadSearchHistory(keyword: String) {
        val parts = keyword.split("-")
        if (parts.size >= 6) {
            _uiState.update {
                it.copy(
                    deviceName = parts.getOrElse(0) { "" },
                    codeName = parts.getOrElse(1) { "" },
                    deviceRegion = parts.getOrElse(2) { "Default (CN)" },
                    deviceCarrier = parts.getOrElse(3) { "Default (Xiaomi)" },
                    androidVersion = parts.getOrElse(4) { "16.0" },
                    systemVersion = parts.getOrElse(5) { "" }
                )
            }
        }
    }

    fun fetchRomInfo() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            _uiState.update {
                it.copy(
                    curRomInfo = DataHelper.RomInfoData(),
                    incRomInfo = DataHelper.RomInfoData(),
                    curIconInfo = emptyList(),
                    incIconInfo = emptyList(),
                    curImageInfo = emptyList(),
                    incImageInfo = emptyList()
                )
            }

            val messageIng = getString(Res.string.toast_ing)
            MessageUtils.showMessage(messageIng)

            val state = _uiState.value
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

            val romInfoStr = repository.getRecoveryRomInfo(
                branchExt,
                codeNameExt,
                regionCode,
                systemVersionExt,
                state.androidVersion,
                state.isLogin == 1
            )

            if (romInfoStr.isNotEmpty()) {
                val recoveryRomInfo = Json.decodeFromString<RomInfoHelper.RomInfo>(romInfoStr)

                val authResult = state.loginData?.authResult
                if (authResult != null) {
                    if (recoveryRomInfo.authResult != 1 && authResult != "3") {
                        val newLoginData = state.loginData.copy(authResult = "3")
                        _uiState.update { it.copy(loginData = newLoginData, isLogin = 3) }
                        prefSet("loginInfo", Json.encodeToString(newLoginData))
                    }
                }

                if (recoveryRomInfo.currentRom?.bigversion != null) {
                    var noUltimateLink = false
                    val curRomDownload = if (recoveryRomInfo.currentRom.md5 != recoveryRomInfo.latestRom?.md5) {
                        val romInfoCurrentStr = repository.getRecoveryRomInfo(
                            "",
                            codeNameExt,
                            regionCode,
                            systemVersionExt,
                            state.androidVersion,
                            state.isLogin == 1
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
                            MessageUtils.showMessage(getString(Res.string.toast_no_ultimate_link))
                            downloadUrl(recoveryRomInfo.currentRom.version!!, recoveryRomInfo.currentRom.filename!!)
                        }
                    } else {
                        downloadUrl(recoveryRomInfo.currentRom.version!!, recoveryRomInfo.latestRom?.filename!!)
                    }

                    val (curRomData, curIcons, curImages) = processRomInfo(
                        recoveryRomInfo,
                        recoveryRomInfo.currentRom,
                        curRomDownload,
                        noUltimateLink
                    )
                    _uiState.update {
                        it.copy(
                            curRomInfo = curRomData,
                            curIconInfo = curIcons,
                            curImageInfo = curImages
                        )
                    }

                    prefSet("deviceName", state.deviceName)
                    prefSet("codeName", state.codeName)
                    prefSet("deviceRegion", state.deviceRegion)
                    prefSet("deviceCarrier", state.deviceCarrier)
                    prefSet("systemVersion", state.systemVersion)
                    prefSet("androidVersion", state.androidVersion)

                    updateSearchKeywords(
                        state.deviceName,
                        state.codeName,
                        state.deviceRegion,
                        state.deviceCarrier,
                        state.androidVersion,
                        state.systemVersion
                    )

                    if (recoveryRomInfo.incrementRom?.bigversion != null) {
                        val (incRomData, incIcons, incImages) = processRomInfo(recoveryRomInfo, recoveryRomInfo.incrementRom)
                        _uiState.update { it.copy(incRomInfo = incRomData, incIconInfo = incIcons, incImageInfo = incImages) }
                    } else if (recoveryRomInfo.crossRom?.bigversion != null) {
                        val (incRomData, incIcons, incImages) = processRomInfo(recoveryRomInfo, recoveryRomInfo.crossRom)
                        _uiState.update { it.copy(incRomInfo = incRomData, incIconInfo = incIcons, incImageInfo = incImages) }
                    }

                    if (noUltimateLink) {
                        MessageUtils.showMessage(getString(Res.string.toast_no_ultimate_link), 1000L)
                    } else {
                        MessageUtils.showMessage(getString(Res.string.toast_success_info), 1000L)
                    }

                    if (!isWeb()) {
                        val downloadUrl = if (noUltimateLink) curRomData.cdn1Download else curRomData.official1Download
                        if (downloadUrl.isNotEmpty()) {
                            val metadata = MetadataUtils.getMetadata(downloadUrl)
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
                    }

                } else if (recoveryRomInfo.incrementRom?.bigversion != null) {
                    val (curRomData, curIcons, curImages) = processRomInfo(recoveryRomInfo, recoveryRomInfo.incrementRom)
                    _uiState.update { it.copy(curRomInfo = curRomData, curIconInfo = curIcons, curImageInfo = curImages) }
                    MessageUtils.showMessage(getString(Res.string.toast_wrong_info))
                } else if (recoveryRomInfo.crossRom?.bigversion != null) {
                    val (curRomData, curIcons, curImages) = processRomInfo(recoveryRomInfo, recoveryRomInfo.crossRom)
                    _uiState.update { it.copy(curRomInfo = curRomData, curIconInfo = curIcons, curImageInfo = curImages) }
                    MessageUtils.showMessage(getString(Res.string.toast_wrong_info))
                } else {
                    MessageUtils.showMessage(getString(Res.string.toast_no_info))
                }
            } else {
                MessageUtils.showMessage(getString(Res.string.toast_crash_info), 5000L)
            }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun updateSearchKeywords(
        deviceName: String,
        codeName: String,
        deviceRegion: String,
        deviceCarrier: String,
        androidVersion: String,
        systemVersion: String
    ) {
        val newKeyword = "$deviceName-$codeName-$deviceRegion-$deviceCarrier-$androidVersion-$systemVersion"
        val currentKeywords = _uiState.value.searchKeywords.toMutableList()

        if (currentKeywords.contains(newKeyword)) {
            currentKeywords.remove(newKeyword)
        } else {
            if (currentKeywords.size >= 8) currentKeywords.removeAt(currentKeywords.size - 1)
        }
        currentKeywords.add(0, newKeyword)
        _uiState.update { it.copy(searchKeywords = currentKeywords, searchKeywordsSelected = 0) }
        prefSet("searchKeywords", Json.encodeToString(currentKeywords))
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
