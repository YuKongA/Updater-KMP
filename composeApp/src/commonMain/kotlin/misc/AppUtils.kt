package misc

import Metadata
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.sp
import data.DataHelper
import data.DeviceInfoHelper
import data.RomInfoHelper
import dev.whyoleg.cryptography.DelicateCryptographyApi
import dev.whyoleg.cryptography.algorithms.MD5
import getRecoveryRomInfo
import isWeb
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import misc.MessageUtils.Companion.showMessage
import org.jetbrains.compose.resources.stringResource
import platform.prefSet
import platform.provider
import updater.composeapp.generated.resources.Res
import updater.composeapp.generated.resources.toast_crash_info
import updater.composeapp.generated.resources.toast_ing
import updater.composeapp.generated.resources.toast_no_info
import updater.composeapp.generated.resources.toast_no_ultimate_link
import updater.composeapp.generated.resources.toast_success_info
import updater.composeapp.generated.resources.toast_wrong_info
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

val json = Json { ignoreUnknownKeys = true }

val bodyFontSize = 16.sp
val bodySmallFontSize = 13.sp

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
    val systemVersionExt = systemVersion.value.uppercase().replace("^OS1".toRegex(), "V816").replace("AUTO$".toRegex(), deviceCode)
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
                showMessage(message = messageIng)

                val romInfo = getRecoveryRomInfo(branchExt, codeNameExt, regionCode, systemVersionExt, androidVersion.value, isLogin)

                if (romInfo.isNotEmpty()) {

                    val recoveryRomInfo = json.decodeFromString<RomInfoHelper.RomInfo>(romInfo)

                    val authResult = loginData?.authResult
                    if (authResult != null) {
                        if (recoveryRomInfo.authResult != 1 && authResult != "3") {
                            loginData.authResult = "3"
                            isLogin.value = 3
                            prefSet("loginInfo", json.encodeToString(loginData))
                        }
                    }

                    if (recoveryRomInfo.currentRom?.bigversion != null) {
                        noUltimateLink = false
                        val curRomDownload =
                            if (recoveryRomInfo.currentRom.md5 != recoveryRomInfo.latestRom?.md5) {
                                val romInfoCurrent =
                                    getRecoveryRomInfo("", codeNameExt, regionCode, systemVersionExt, androidVersion.value, isLogin)
                                val recoveryRomInfoCurrent =
                                    if (romInfoCurrent.isNotEmpty()) json.decodeFromString<RomInfoHelper.RomInfo>(romInfoCurrent) else json.decodeFromString<RomInfoHelper.RomInfo>(
                                        romInfoCurrent
                                    )
                                if (recoveryRomInfoCurrent.latestRom?.filename != null) {
                                    downloadUrl(recoveryRomInfoCurrent.currentRom?.version!!, recoveryRomInfoCurrent.latestRom.filename)
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
                            handleRomInfo(recoveryRomInfo, recoveryRomInfo.incrementRom, incRomInfo, incIconInfo, coroutineScope)
                        } else if (recoveryRomInfo.crossRom?.bigversion != null) {
                            handleRomInfo(recoveryRomInfo, recoveryRomInfo.crossRom, incRomInfo, incIconInfo, coroutineScope)
                        } else {
                            clearRomInfo(incRomInfo)
                        }

                        if (noUltimateLink) {
                            showMessage(messageNoUltimateLink, 1000L)
                        } else {
                            showMessage(messageSuccessResult, 1000L)
                        }

                    } else if (recoveryRomInfo.incrementRom?.bigversion != null) {

                        handleRomInfo(recoveryRomInfo, recoveryRomInfo.incrementRom, curRomInfo, curIconInfo, coroutineScope)
                        clearRomInfo(incRomInfo)
                        showMessage(messageWrongResult)

                    } else if (recoveryRomInfo.crossRom?.bigversion != null) {

                        handleRomInfo(recoveryRomInfo, recoveryRomInfo.crossRom, curRomInfo, curIconInfo, coroutineScope)
                        clearRomInfo(incRomInfo)
                        showMessage(messageWrongResult)

                    } else {

                        clearRomInfo(curRomInfo)
                        clearRomInfo(incRomInfo)
                        showMessage(messageNoResult)

                    }

                    searchKeywordsSelected.value = 0

                } else {

                    clearRomInfo(curRomInfo)
                    clearRomInfo(incRomInfo)
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
 * @param coroutineScope: Coroutine scope
 * @param officialDownload: Official download URL
 * @param noUltimateLink: No ultimate download link
 */
fun handleRomInfo(
    recoveryRomInfo: RomInfoHelper.RomInfo,
    romInfo: RomInfoHelper.Rom?,
    romInfoData: MutableState<DataHelper.RomInfoData>,
    iconInfoData: MutableState<List<DataHelper.IconInfoData>>,
    coroutineScope: CoroutineScope,
    officialDownload: String? = null,
    noUltimateLink: Boolean = false,
) {
    if (romInfo?.bigversion != null) {
        val log = StringBuilder()
        romInfo.changelog?.forEach { log.append(it.key).append("\n").append(it.value.txt.joinToString("\n")).append("\n\n") }
        val changelogGroups = log.toString().trimEnd().split("\n\n")
        val changelog = changelogGroups.map { it.split("\n").drop(1).joinToString("\n") }

        val gentle = StringBuilder()
        val formattedGentleNotice = recoveryRomInfo.gentleNotice?.text?.replace("<li>", "\n· ")
            ?.replace("</li>", "")?.replace("<p>", "\n")?.replace("</p>", "")?.replace("&nbsp;", " ")
            ?.replace("&#160;", "")?.replace(Regex("<[^>]*>"), "")?.trim()
        formattedGentleNotice?.forEach { gentle.append(it) }
        val gentleNotice = gentle.toString().trimEnd().split("\n").drop(1).joinToString("\n")

        val iconNames = changelogGroups.map { it.split("\n").first() }
        val iconMainLink = recoveryRomInfo.fileMirror!!.icon
        val iconNameLink = recoveryRomInfo.icon ?: mapOf()
        val iconLinks = iconLink(iconNames, iconMainLink, iconNameLink)
        iconInfoData.value = iconNames.mapIndexed { index, iconName ->
            DataHelper.IconInfoData(
                iconName = iconName,
                iconLink = if (isWeb()) "" else iconLinks[iconName] ?: "",
                changelog = changelog[index]
            )
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
            "https://bkt-sgp-miui-ota-update-alisgp.oss-ap-southeast-1.aliyuncs.com" + downloadUrl(romInfo.version, romInfo.filename)
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
                val metadata = Metadata.getMetadata(if (noUltimateLink) cdn1Download else official1Download)
                val fingerprint = Metadata.getMetadataValue(metadata, "post-build=")
                val securityPatchLevel = Metadata.getMetadataValue(metadata, "post-security-patch-level=")
                val timestamp = convertTimestampToDateTime(Metadata.getMetadataValue(metadata, "post-timestamp="))

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
fun clearRomInfo(romInfoData: MutableState<DataHelper.RomInfoData>) {
    romInfoData.value = DataHelper.RomInfoData()
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
 * Generate MD5 hash.
 *
 * @param input: Input string
 *
 * @return MD5 hash
 */
@OptIn(DelicateCryptographyApi::class)
suspend fun md5Hash(input: String): String {
    val md = provider().get(MD5)
    return md.hasher().hash(input.encodeToByteArray()).joinToString("") {
        val hex = (it.toInt() and 0xFF).toString(16).uppercase()
        if (hex.length == 1) "0$hex" else hex
    }
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
    prefSet("searchKeywords", json.encodeToString(updatedKeywords))
}

/**
 * Generate maps with links with corresponding names and icons.
 *
 * @param iconNames: Icon names included in the changelog
 * @param iconMainLink: Main link to get the icon
 * @param iconNameLink: Links that correspond to each icon name
 *
 * @return Links to icons with corresponding names
 */
fun iconLink(iconNames: List<String>, iconMainLink: String, iconNameLink: Map<String, String>): MutableMap<String, String> {
    val iconMap = mutableMapOf<String, String>()
    if (iconNameLink.isNotEmpty()) {
        for (name in iconNames) {
            val icon = iconNameLink[name]
            if (icon != null) {
                val realLink = iconMainLink + icon
                iconMap[name] = realLink
            }
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
