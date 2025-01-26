package misc

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
import iconLink
import isWeb
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import misc.MessageUtils.Companion.showMessage
import org.jetbrains.compose.resources.stringResource
import perfSet
import provider
import updater.composeapp.generated.resources.Res
import updater.composeapp.generated.resources.toast_crash_info
import updater.composeapp.generated.resources.toast_ing
import updater.composeapp.generated.resources.toast_no_info
import updater.composeapp.generated.resources.toast_no_ultimate_link
import updater.composeapp.generated.resources.toast_success_info
import updater.composeapp.generated.resources.toast_wrong_info

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
fun updateRomInfo(
    deviceName: MutableState<String>,
    codeName: MutableState<String>,
    deviceRegion: MutableState<String>,
    androidVersion: MutableState<String>,
    systemVersion: MutableState<String>,
    loginData: DataHelper.LoginData?,
    isLogin: MutableState<Int>,
    curRomInfo: MutableState<DataHelper.RomInfoData>,
    incRomInfo: MutableState<DataHelper.RomInfoData>,
    curIconInfo: MutableState<List<DataHelper.IconInfoData>>,
    incIconInfo: MutableState<List<DataHelper.IconInfoData>>,
    updateRomInfo: MutableState<Int>,
    searchKeywords: MutableState<List<String>>
) {
    val regionCode = DeviceInfoHelper.regionCode(deviceRegion.value)
    val deviceCode = DeviceInfoHelper.deviceCode(androidVersion.value, codeName.value, regionCode)
    val regionNameExt = DeviceInfoHelper.regionNameExt(deviceRegion.value)
    val codeNameExt = codeName.value + regionNameExt
    val systemVersionExt = systemVersion.value.uppercase().replace("^OS1".toRegex(), "V816").replace("AUTO$".toRegex(), deviceCode)
    val branchExt = if (systemVersion.value.uppercase().endsWith(".DEV")) "X" else "F"

    val messageIng = stringResource(Res.string.toast_ing)
    val messageNoResult = stringResource(Res.string.toast_no_info)
    val messageSuccessResult = stringResource(Res.string.toast_success_info)
    val messageWrongResult = stringResource(Res.string.toast_wrong_info)
    val messageCrashResult = stringResource(Res.string.toast_crash_info)
    val messageNoUltimateLink = stringResource(Res.string.toast_no_ultimate_link)

    var noUltimateLink = false

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
                            perfSet("loginInfo", json.encodeToString(loginData))
                        }
                    }

                    if (recoveryRomInfo.currentRom?.bigversion != null) {
                        val curRomDownload =
                            if (recoveryRomInfo.currentRom.md5 != recoveryRomInfo.latestRom?.md5) {
                                val romInfoCurrent =
                                    getRecoveryRomInfo("", codeNameExt, regionCode, systemVersionExt, androidVersion.value, isLogin)
                                val recoveryRomInfoCurrent = if (romInfoCurrent.isNotEmpty()) json.decodeFromString<RomInfoHelper.RomInfo>(romInfoCurrent) else null
                                if (recoveryRomInfoCurrent?.currentRom != null && recoveryRomInfoCurrent.latestRom != null) {
                                    noUltimateLink = false
                                    downloadUrl(recoveryRomInfoCurrent.currentRom.version!!, recoveryRomInfoCurrent.latestRom.filename!!)
                                } else {
                                    noUltimateLink = true
                                    showMessage(messageNoUltimateLink)
                                    downloadUrl(recoveryRomInfo.currentRom.version!!, recoveryRomInfo.currentRom.filename!!)
                                }
                            } else {
                                downloadUrl(recoveryRomInfo.currentRom.version!!, recoveryRomInfo.latestRom?.filename!!)
                            }

                        handleRomInfo(recoveryRomInfo, recoveryRomInfo.currentRom, curRomInfo, curIconInfo, curRomDownload, noUltimateLink)

                        perfSet("deviceName", deviceName.value)
                        perfSet("codeName", codeName.value)
                        perfSet("deviceRegion", deviceRegion.value)
                        perfSet("systemVersion", systemVersion.value)
                        perfSet("androidVersion", androidVersion.value)

                        if (recoveryRomInfo.incrementRom?.bigversion != null) {
                            handleRomInfo(recoveryRomInfo, recoveryRomInfo.incrementRom, incRomInfo, incIconInfo)
                        } else if (recoveryRomInfo.crossRom?.bigversion != null) {
                            handleRomInfo(recoveryRomInfo, recoveryRomInfo.crossRom, incRomInfo, incIconInfo)
                        } else {
                            clearRomInfo(incRomInfo)
                        }

                        if (noUltimateLink) {
                            showMessage(messageNoUltimateLink, 1000L)
                        } else {
                            showMessage(messageSuccessResult, 1000L)
                        }

                    } else if (recoveryRomInfo.incrementRom?.bigversion != null) {

                        handleRomInfo(recoveryRomInfo, recoveryRomInfo.incrementRom, curRomInfo, curIconInfo)
                        clearRomInfo(incRomInfo)
                        showMessage(messageWrongResult)

                    } else if (recoveryRomInfo.crossRom?.bigversion != null) {

                        handleRomInfo(recoveryRomInfo, recoveryRomInfo.crossRom, curRomInfo, curIconInfo)
                        clearRomInfo(incRomInfo)
                        showMessage(messageWrongResult)

                    } else {

                        clearRomInfo(curRomInfo)
                        clearRomInfo(incRomInfo)
                        showMessage(messageNoResult)

                    }

                    val newKeyword = "${deviceName.value}-${codeName.value}-${deviceRegion.value}-${androidVersion.value}-${systemVersion.value}"
                    val updatedKeywords = searchKeywords.value.toMutableList()

                    if (updatedKeywords.contains(newKeyword)) {
                        updatedKeywords.remove(newKeyword)
                    } else {
                        if (updatedKeywords.size >= 8) updatedKeywords.removeAt(updatedKeywords.size - 1)
                    }

                    updatedKeywords.add(0, newKeyword)
                    searchKeywords.value = updatedKeywords
                    perfSet("searchKeywords", json.encodeToString(updatedKeywords))

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
 * @param officialDownload: Official download URL
 * @param noUltimateLink: No ultimate download link
 */
fun handleRomInfo(
    recoveryRomInfo: RomInfoHelper.RomInfo,
    romInfo: RomInfoHelper.Rom?,
    romInfoData: MutableState<DataHelper.RomInfoData>,
    iconInfoData: MutableState<List<DataHelper.IconInfoData>>,
    officialDownload: String? = null,
    noUltimateLink: Boolean = false
) {
    if (romInfo?.bigversion != null) {
        val log = StringBuilder()
        romInfo.changelog?.forEach { log.append(it.key).append("\n").append(it.value.txt.joinToString("\n")).append("\n\n") }
        val changelogGroups = log.toString().trimEnd().split("\n\n")
        val changelog = changelogGroups.map { it.split("\n").drop(1).joinToString("\n") }
        val iconNames = changelogGroups.map { it.split("\n").first() }

        val iconMainLink = if (isWeb()) "https://updater.yukonga.top/icon/10/" else recoveryRomInfo.fileMirror!!.icon
        val iconNameLink = recoveryRomInfo.icon ?: mapOf()

        val iconLinks = iconLink(iconNames, iconMainLink, iconNameLink)

        iconInfoData.value = iconNames.mapIndexed { index, iconName ->
            DataHelper.IconInfoData(
                iconName = iconName,
                iconLink = iconLinks[iconName] ?: "",
                changelog = changelog[index]
            )
        }

        romInfoData.value = DataHelper.RomInfoData(
            type = romInfo.type.toString(),
            device = romInfo.device.toString(),
            version = romInfo.version.toString(),
            codebase = romInfo.codebase.toString(),
            branch = romInfo.branch.toString(),
            bigVersion = when {
                romInfo.osbigversion != ".0" && romInfo.osbigversion != "0.0" && romInfo.osbigversion != "" -> "HyperOS " + romInfo.osbigversion
                romInfo.bigversion.contains("816") -> romInfo.bigversion.replace("816", "HyperOS 1.0")
                else -> "MIUI ${romInfo.bigversion}"
            },
            fileName = romInfo.filename.toString().substringBefore(".zip") + ".zip",
            fileSize = romInfo.filesize.toString(),
            official1Download = if (noUltimateLink) "" else {
                "https://ultimateota.d.miui.com" + (officialDownload ?: downloadUrl(romInfo.version, romInfo.filename))
            },
            official2Download = if (noUltimateLink) "" else {
                "https://superota.d.miui.com" + (officialDownload ?: downloadUrl(romInfo.version, romInfo.filename))
            },
            cdn1Download = "https://bkt-sgp-miui-ota-update-alisgp.oss-ap-southeast-1.aliyuncs.com" + downloadUrl(romInfo.version, romInfo.filename),
            cdn2Download = "https://cdnorg.d.miui.com" + downloadUrl(romInfo.version, romInfo.filename),
            changelog = log.toString().trimEnd()
        )
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