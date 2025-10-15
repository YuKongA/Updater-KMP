import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import data.DataHelper
import data.DeviceInfoHelper
import data.RomInfoHelper
import io.ktor.client.call.body
import io.ktor.client.request.cookie
import io.ktor.client.request.forms.submitForm
import io.ktor.http.Parameters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.stringResource
import platform.httpClientPlatform
import platform.miuiDecrypt
import platform.miuiEncrypt
import platform.prefGet
import platform.prefSet
import updater.composeapp.generated.resources.Res
import updater.composeapp.generated.resources.toast_crash_info
import updater.composeapp.generated.resources.toast_ing
import updater.composeapp.generated.resources.toast_no_info
import updater.composeapp.generated.resources.toast_no_ultimate_link
import updater.composeapp.generated.resources.toast_success_info
import updater.composeapp.generated.resources.toast_wrong_info
import utils.MessageUtils.Companion.showMessage
import utils.MetadataUtils
import kotlin.io.encoding.Base64
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class RomInfo {
    private val CN_RECOVERY_URL =
        if (isWeb()) "https://updater.yukonga.top/updates/miotaV3.php" else "https://update.miui.com/updates/miotaV3.php"
    private val INTL_RECOVERY_URL =
        if (isWeb()) "https://updater.yukonga.top/intl-updates/miotaV3.php" else "https://update.intl.miui.com/updates/miotaV3.php"
    private var accountType = "CN"
    private var port = "1"
    private var ssecurity = ""
    private var securityKey = "miuiotavalided11".encodeToByteArray()
    private var serviceToken = ""
    private var userId = ""
    private var cUserId = ""

    /**
     * Set default request info.
     */
    fun setDefaultRequestInfo() {
        accountType = "CN"
        port = "1"
        ssecurity = ""
        securityKey = "miuiotavalided11".encodeToByteArray()
        serviceToken = ""
        userId = ""
    }

    /**
     * Generate JSON data for recovery ROM info request.
     *
     * @param branch: Branch name
     * @param codeNameExt: CodeName with region extension
     * @param regionCode: Region code
     * @param romVersion: ROM version
     * @param androidVersion: Android version
     * @param userId: Xiaomi ID
     * @param security: Security key
     * @param token: Service token
     *
     * @return JSON data
     */
    fun generateJson(
        branch: String,
        codeNameExt: String,
        regionCode: String,
        romVersion: String,
        androidVersion: String,
        userId: String,
        security: String,
        token: String
    ): String {
        val data = DataHelper.RequestData(
            b = branch,
            c = androidVersion,
            d = codeNameExt,
            f = "1",
            id = userId,
            l = if (!codeNameExt.contains("_global")) "zh_CN" else "en_US",
            ov = romVersion,
            p = codeNameExt,
            pn = codeNameExt,
            r = regionCode,
            security = security,
            token = token,
            unlock = "0",
            v = "MIUI-$romVersion",
            options = DataHelper.Options(av = "9.1.3")
        )
        return Json.encodeToString(data)
    }

    /**
     * Get recovery ROM info form xiaomi server.
     *
     * @param branch: Branch name
     * @param codeNameExt: CodeName with region extension
     * @param regionCode: Region code
     * @param romVersion: ROM version
     * @param androidVersion: Android version
     * @param isLogin: Xiaomi account login status
     *
     * @return Recovery ROM info
     */
    suspend fun getRecoveryRomInfo(
        branch: String,
        codeNameExt: String,
        regionCode: String,
        romVersion: String,
        androidVersion: String,
        isLogin: MutableState<Int>
    ): String {
        if (prefGet("loginInfo") != null && isLogin.value == 1) {
            val loginInfo = prefGet("loginInfo")?.let { Json.decodeFromString<DataHelper.LoginData>(it) }
            val authResult = loginInfo?.authResult
            if (authResult != "3") {
                accountType = loginInfo?.accountType.toString().ifEmpty { "CN" }
                port = "2"
                ssecurity = loginInfo?.ssecurity.toString()
                securityKey = Base64.Mime.decode(ssecurity)
                serviceToken = loginInfo?.serviceToken.toString()
                userId = loginInfo?.userId.toString()
                cUserId = loginInfo?.cUserId.toString()
            } else setDefaultRequestInfo()
        } else setDefaultRequestInfo()

        val jsonData = generateJson(branch, codeNameExt, regionCode, romVersion, androidVersion, userId, ssecurity, serviceToken)
        val encryptedText = miuiEncrypt(jsonData, securityKey)
        val client = httpClientPlatform()
        val parameters = Parameters.build {
            append("q", encryptedText)
            append("t", serviceToken)
            append("s", port)
        }
        val recoveryUrl = if (accountType != "CN") INTL_RECOVERY_URL else CN_RECOVERY_URL
        try {
            val response = client.submitForm(recoveryUrl, parameters) {
                if (serviceToken.isNotEmpty() && cUserId.isNotEmpty()) {
                    cookie("serviceToken", serviceToken)
                    cookie("uid", cUserId)
                    cookie("s", "1")
                }
            }
            val requestedEncryptedText = response.body<String>()
            client.close()
            val decrypted = miuiDecrypt(requestedEncryptedText, securityKey)
            return decrypted
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    /**
     * Update the data used to display ROM info.
     *
     * @param deviceName: Device name
     * @param codeName: Code name
     * @param deviceRegion: Device region
     * @param androidVersion: Android version
     * @param systemVersion: System version
     * @param loginData: Login data
     * @param isLogin: Login status
     * @param curRomInfo: Data used to display current ROM info
     * @param incRomInfo: Data used to display incremental ROM info
     * @param curIconInfo: Data used to display current changelog icons
     * @param incIconInfo: Data used to display incremental changelog icons
     * @param updateRomInfo: Update ROM info status
     * @param searchKeywords: Recent search keywords
     */
    @Composable
    fun UpdateRomInfo(
        deviceName: MutableState<String>,
        codeName: MutableState<String>,
        deviceRegion: MutableState<String>,
        deviceCarrier: MutableState<String>,
        androidVersion: MutableState<String>,
        systemVersion: MutableState<String>,
        loginData: DataHelper.LoginData?,
        isLogin: MutableState<Int>,
        curRomInfo: MutableState<DataHelper.RomInfoData>,
        incRomInfo: MutableState<DataHelper.RomInfoData>,
        curIconInfo: MutableState<List<DataHelper.IconInfoData>>,
        incIconInfo: MutableState<List<DataHelper.IconInfoData>>,
        curImageInfo: MutableState<List<DataHelper.ImageInfoData>>,
        incImageInfo: MutableState<List<DataHelper.ImageInfoData>>,
        updateRomInfo: MutableState<Int>,
        searchKeywords: MutableState<List<String>>,
        searchKeywordsSelected: MutableState<Int>,
    ) {
        val regionCode = DeviceInfoHelper.regionCode(deviceRegion.value)
        val carrierCode = DeviceInfoHelper.carrierCode(deviceCarrier.value)
        val deviceCode = DeviceInfoHelper.deviceCode(androidVersion.value, codeName.value, regionCode, carrierCode)
        val regionCodeName = DeviceInfoHelper.regionCodeName(deviceRegion.value)
        val carrierCodeName = DeviceInfoHelper.carrierCodeName(deviceCarrier.value)

        val codeNameExt = if (regionCodeName.isNotEmpty()) {
            codeName.value + regionCodeName.replace("_global", "") + carrierCodeName + "_global"
        } else {
            if (regionCode == "CN" && carrierCode == "DM") {
                codeName.value + "_demo"
            } else {
                codeName.value + carrierCodeName
            }
        }
        val systemVersionExt =
            systemVersion.value.uppercase().replace("^OS1".toRegex(), "V816").replace("AUTO$".toRegex(), deviceCode)
        val branchExt = if (systemVersion.value.uppercase().endsWith(".DEV")) "X" else "F"

        val messageIng = stringResource(Res.string.toast_ing)
        val messageNoResult = stringResource(Res.string.toast_no_info)
        val messageSuccessResult = stringResource(Res.string.toast_success_info)
        val messageWrongResult = stringResource(Res.string.toast_wrong_info)
        val messageCrashResult = stringResource(Res.string.toast_crash_info)
        val messageNoUltimateLink = stringResource(Res.string.toast_no_ultimate_link)

        var noUltimateLink: Boolean

        val coroutineScope = rememberCoroutineScope()

        if (updateRomInfo.value != 0) {
            LaunchedEffect(updateRomInfo.value) {
                coroutineScope.launch {
                    clearRomInfo(curRomInfo, curIconInfo, curImageInfo)
                    clearRomInfo(incRomInfo, incIconInfo, incImageInfo)

                    showMessage(message = messageIng)

                    val romInfo = getRecoveryRomInfo(
                        branchExt,
                        codeNameExt,
                        regionCode,
                        systemVersionExt,
                        androidVersion.value,
                        isLogin
                    )

                    if (romInfo.isNotEmpty()) {

                        val recoveryRomInfo = Json.decodeFromString<RomInfoHelper.RomInfo>(romInfo)

                        val authResult = loginData?.authResult
                        if (authResult != null) {
                            if (recoveryRomInfo.authResult != 1 && authResult != "3") {
                                loginData.authResult = "3"
                                isLogin.value = 3
                                prefSet("loginInfo", Json.encodeToString(loginData))
                            }
                        }

                        if (recoveryRomInfo.currentRom?.bigversion != null) {
                            noUltimateLink = false
                            val curRomDownload =
                                if (recoveryRomInfo.currentRom.md5 != recoveryRomInfo.latestRom?.md5) {
                                    val romInfoCurrent =
                                        getRecoveryRomInfo(
                                            "",
                                            codeNameExt,
                                            regionCode,
                                            systemVersionExt,
                                            androidVersion.value,
                                            isLogin
                                        )
                                    val recoveryRomInfoCurrent =
                                        if (romInfoCurrent.isNotEmpty()) {
                                            Json.decodeFromString<RomInfoHelper.RomInfo>(romInfoCurrent)
                                        } else {
                                            Json.decodeFromString<RomInfoHelper.RomInfo>(romInfoCurrent)
                                        }
                                    if (recoveryRomInfoCurrent.latestRom?.filename != null) {
                                        downloadUrl(
                                            recoveryRomInfoCurrent.currentRom?.version!!,
                                            recoveryRomInfoCurrent.latestRom.filename
                                        )
                                    } else {
                                        noUltimateLink = true
                                        showMessage(messageNoUltimateLink)
                                        downloadUrl(recoveryRomInfo.currentRom.version!!, recoveryRomInfo.currentRom.filename!!)
                                    }
                                } else {
                                    downloadUrl(recoveryRomInfo.currentRom.version!!, recoveryRomInfo.latestRom?.filename!!)
                                }

                            handleRomInfo(
                                recoveryRomInfo,
                                recoveryRomInfo.currentRom,
                                curRomInfo,
                                curIconInfo,
                                curImageInfo,
                                coroutineScope,
                                curRomDownload,
                                noUltimateLink
                            )

                            prefSet("deviceName", deviceName.value)
                            prefSet("codeName", codeName.value)
                            prefSet("deviceRegion", deviceRegion.value)
                            prefSet("deviceCarrier", deviceCarrier.value)
                            prefSet("systemVersion", systemVersion.value)
                            prefSet("androidVersion", androidVersion.value)

                            updateSearchKeywords(
                                deviceName.value, codeName.value, deviceRegion.value, deviceCarrier.value,
                                androidVersion.value, systemVersion.value, searchKeywords
                            )

                            if (recoveryRomInfo.incrementRom?.bigversion != null) {
                                handleRomInfo(
                                    recoveryRomInfo,
                                    recoveryRomInfo.incrementRom,
                                    incRomInfo,
                                    incIconInfo,
                                    incImageInfo,
                                    coroutineScope
                                )
                            } else if (recoveryRomInfo.crossRom?.bigversion != null) {
                                handleRomInfo(
                                    recoveryRomInfo,
                                    recoveryRomInfo.crossRom,
                                    incRomInfo,
                                    incIconInfo,
                                    incImageInfo,
                                    coroutineScope
                                )
                            }

                            if (noUltimateLink) {
                                showMessage(messageNoUltimateLink, 1000L)
                            } else {
                                showMessage(messageSuccessResult, 1000L)
                            }

                        } else if (recoveryRomInfo.incrementRom?.bigversion != null) {
                            handleRomInfo(
                                recoveryRomInfo,
                                recoveryRomInfo.incrementRom,
                                curRomInfo,
                                curIconInfo,
                                curImageInfo,
                                coroutineScope
                            )
                            showMessage(messageWrongResult)
                        } else if (recoveryRomInfo.crossRom?.bigversion != null) {
                            handleRomInfo(
                                recoveryRomInfo,
                                recoveryRomInfo.crossRom,
                                curRomInfo,
                                curIconInfo,
                                curImageInfo,
                                coroutineScope
                            )
                            showMessage(messageWrongResult)
                        } else {
                            showMessage(messageNoResult)
                        }
                        searchKeywordsSelected.value = 0
                    } else {
                        showMessage(messageCrashResult, 5000L)
                    }
                }
            }
        }
    }

    /**
     * Handle ROM info.
     *
     * @param recoveryRomInfo: All returned info
     * @param romInfo: Current ROM info
     * @param romInfoData: Data used to display ROM info
     * @param iconInfoData: Data used to display changelog icons
     * @param imageInfoData: Data used to display changelog images (now List<ImageInfoData>)
     * @param coroutineScope: Coroutine scope
     * @param officialDownload: Official download URL
     * @param noUltimateLink: No ultimate download link
     */
    fun handleRomInfo(
        recoveryRomInfo: RomInfoHelper.RomInfo,
        romInfo: RomInfoHelper.Rom?,
        romInfoData: MutableState<DataHelper.RomInfoData>,
        iconInfoData: MutableState<List<DataHelper.IconInfoData>>,
        imageInfoData: MutableState<List<DataHelper.ImageInfoData>>,
        coroutineScope: CoroutineScope,
        officialDownload: String? = null,
        noUltimateLink: Boolean = false,
    ) {
        if (romInfo?.bigversion != null) {
            val log = StringBuilder()
            romInfo.changelog?.forEach { (category, items) ->
                log.append(category).append("\n")
                items.forEach { item ->
                    if (item.txt.isNotBlank()) {
                        log.append(item.txt.trim()).append("\n")
                    }
                }
                log.append("\n")
            }

            val gentle = StringBuilder()
            val formattedGentleNotice = recoveryRomInfo.gentleNotice?.text?.replace("<li>", "\n· ")
                ?.replace("</li>", "")?.replace("<p>", "\n")?.replace("</p>", "")?.replace("&nbsp;", " ")
                ?.replace("&#160;", "")?.replace(Regex("<[^>]*>"), "")?.trim()
            formattedGentleNotice?.forEach { gentle.append(it) }
            val gentleNotice = gentle.toString().trimEnd().split("\n").drop(1).joinToString("\n")

            val isNewChangelog = romInfo.osbigversion?.toFloatOrNull()?.let { it >= 3.0 } ?: false
            if (isNewChangelog) {
                iconInfoData.value = emptyList()

                val imageBaseUrl = recoveryRomInfo.fileMirror?.image?.let {
                    if (it.startsWith("http://")) "https://" + it.removePrefix("http://") else it
                } ?: ""

                val flatChangelogList = mutableListOf<DataHelper.ImageInfoData>()
                romInfo.changelog?.forEach { (categoryTitle, items) ->
                    items.forEach { item ->
                        val image = item.image?.firstOrNull()
                        flatChangelogList.add(
                            DataHelper.ImageInfoData(
                                title = categoryTitle,
                                text = item.txt,
                                imageUrl = image?.path?.let { if (isWeb()) "" else imageBaseUrl + it },
                                imageWidth = image?.w?.toIntOrNull(),
                                imageHeight = image?.h?.toIntOrNull()
                            )
                        )
                    }
                }
                imageInfoData.value = flatChangelogList

            } else {
                imageInfoData.value = emptyList()

                val iconMainLink = recoveryRomInfo.fileMirror?.icon ?: ""
                val iconNameLink = recoveryRomInfo.icon ?: mapOf()
                val iconNames = romInfo.changelog?.keys?.toList() ?: emptyList()
                val iconLinks = iconLink(iconNames, iconMainLink, iconNameLink)

                iconInfoData.value = romInfo.changelog?.map { (category, items) ->
                    val changelogText = items.mapNotNull { it.txt.trim().takeIf { txt -> txt.isNotBlank() } }.joinToString("\n")
                    DataHelper.IconInfoData(
                        iconName = category,
                        iconLink = if (isWeb()) "" else iconLinks[category] ?: "",
                        changelog = changelogText
                    )
                } ?: emptyList()
            }

            val bigVersion = when {
                romInfo.osbigversion != ".0" && romInfo.osbigversion != "0.0" && romInfo.osbigversion != "" -> "HyperOS " + romInfo.osbigversion
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
                "https://bkt-sgp-miui-ota-update-alisgp.oss-ap-southeast-1.aliyuncs.com" + downloadUrl(
                    romInfo.version,
                    romInfo.filename
                )
            val cdn2Download = "https://cdnorg.d.miui.com" + downloadUrl(romInfo.version, romInfo.filename)

            romInfoData.value = DataHelper.RomInfoData(
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

            if (!isWeb()) {
                coroutineScope.launch {
                    val metadata = MetadataUtils.getMetadata(if (noUltimateLink) cdn1Download else official1Download)
                    val fingerprint = MetadataUtils.getMetadataValue(metadata, "post-build=")
                    val securityPatchLevel = MetadataUtils.getMetadataValue(metadata, "post-security-patch-level=")
                    val timestamp = convertTimestampToDateTime(MetadataUtils.getMetadataValue(metadata, "post-timestamp="))

                    romInfoData.value = romInfoData.value.copy(
                        fingerprint = fingerprint,
                        securityPatchLevel = securityPatchLevel,
                        timestamp = timestamp,
                    )
                }
            }
        }
    }

    /**
     * Clear ROM info.
     *
     * @param romInfoData: Data used to display ROM info
     */
    fun clearRomInfo(
        romInfoData: MutableState<DataHelper.RomInfoData>,
        iconInfoData: MutableState<List<DataHelper.IconInfoData>>,
        imageInfoData: MutableState<List<DataHelper.ImageInfoData>>
    ) {
        romInfoData.value = DataHelper.RomInfoData()
        iconInfoData.value = listOf()
        imageInfoData.value = listOf()
    }

    /**
     * Generate ROM download URL.
     *
     * @param romVersion: ROM version
     * @param romFilename: ROM filename
     *
     * @return ROM download URL
     */
    fun downloadUrl(romVersion: String?, romFilename: String?): String {
        return "/$romVersion/$romFilename"
    }

    /**
     * Update search keywords.
     *
     * @param deviceName: Device name
     * @param codeName: Code name
     * @param deviceRegion: Device region
     * @param androidVersion: Android version
     * @param systemVersion: System version
     * @param searchKeywords: Recent search keywords
     */
    fun updateSearchKeywords(
        deviceName: String,
        codeName: String,
        deviceRegion: String,
        deviceCarrier: String,
        androidVersion: String,
        systemVersion: String,
        searchKeywords: MutableState<List<String>>
    ) {
        val newKeyword = "$deviceName-$codeName-$deviceRegion-$deviceCarrier-$androidVersion-$systemVersion"
        val updatedKeywords = searchKeywords.value.toMutableList()

        if (updatedKeywords.contains(newKeyword)) {
            updatedKeywords.remove(newKeyword)
        } else {
            if (updatedKeywords.size >= 8) updatedKeywords.removeAt(updatedKeywords.size - 1)
        }
        updatedKeywords.add(0, newKeyword)
        searchKeywords.value = updatedKeywords
        prefSet("searchKeywords", Json.encodeToString(updatedKeywords))
    }


    /**
     * Generate maps with links with corresponding names and icons.
     * (This function is for older MIUI changelogs)
     *
     * @param iconNames: Icon names included in the changelog
     * @param iconMainLink: Main link to get the icon
     * @param iconNameLink: Links that correspond to each icon name
     *
     * @return Links to icons with corresponding names
     */
    fun iconLink(iconNames: List<String>, iconMainLink: String, iconNameLink: Map<String, String>): MutableMap<String, String> {
        val iconMap = mutableMapOf<String, String>()
        val safeIconMainLink = if (iconMainLink.startsWith("http://")) {
            "https://" + iconMainLink.removePrefix("http://")
        } else {
            iconMainLink
        }
        if (safeIconMainLink.isNotEmpty() && iconNameLink.isNotEmpty()) {
            for (name in iconNames) {
                iconNameLink[name]?.let { iconMap[name] = safeIconMainLink + it }
            }
        }
        return iconMap
    }

    /**
     * Convert timestamp to date time.
     *
     * @param timestamp: Timestamp
     *
     * @return Date time
     */
    @OptIn(ExperimentalTime::class)
    fun convertTimestampToDateTime(timestamp: String): String {
        val epochSeconds = timestamp.toLongOrNull() ?: return ""
        val instant = Instant.fromEpochSeconds(epochSeconds)
        val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return "${dateTime.year}/${dateTime.month.number}/${dateTime.day} " +
                dateTime.hour.toString().padStart(2, '0') +
                ":${dateTime.minute.toString().padStart(2, '0')}" +
                ":${dateTime.second.toString().padStart(2, '0')}"
    }
}