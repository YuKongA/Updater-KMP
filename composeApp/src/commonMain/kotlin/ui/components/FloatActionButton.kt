package ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import data.DataHelper
import data.DeviceInfoHelper
import data.RomInfoHelper
import getRecoveryRomInfo
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import misc.clearRomInfo
import misc.downloadUrl
import misc.handleRomInfo
import misc.json
import org.jetbrains.compose.resources.stringResource
import perfGet
import perfSet
import misc.SnackbarUtils.Companion.showSnackbar
import updaterkmp.composeapp.generated.resources.Res
import updaterkmp.composeapp.generated.resources.submit
import updaterkmp.composeapp.generated.resources.toast_crash_info
import updaterkmp.composeapp.generated.resources.toast_ing
import updaterkmp.composeapp.generated.resources.toast_no_info
import updaterkmp.composeapp.generated.resources.toast_success_info
import updaterkmp.composeapp.generated.resources.toast_wrong_info


@Composable
fun FloatActionButton(
    fabOffsetHeight: Dp,
    deviceName: MutableState<String>,
    codeName: MutableState<String>,
    deviceRegion: MutableState<String>,
    systemVersion: MutableState<String>,
    androidVersion: MutableState<String>,
    curRomInfo: MutableState<DataHelper.RomInfoData>,
    incRomInfo: MutableState<DataHelper.RomInfoData>,
    curIconInfo: MutableState<List<DataHelper.IconInfoData>>,
    incIconInfo: MutableState<List<DataHelper.IconInfoData>>,
    isLogin: MutableState<Int>
) {
    val coroutineScope = rememberCoroutineScope()
    val messageIng = stringResource(Res.string.toast_ing)
    val messageNoResult = stringResource(Res.string.toast_no_info)
    val messageSuccessResult = stringResource(Res.string.toast_success_info)
    val messageWrongResult = stringResource(Res.string.toast_wrong_info)
    val messageCrashResult = stringResource(Res.string.toast_crash_info)

    val hapticFeedback = LocalHapticFeedback.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(18.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        ExtendedFloatingActionButton(
            modifier = Modifier.offset(y = fabOffsetHeight),
            onClick = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)

                val regionCode = DeviceInfoHelper.regionCode(deviceRegion.value)
                val deviceCode = DeviceInfoHelper.deviceCode(androidVersion.value, codeName.value, regionCode)
                val regionNameExt = DeviceInfoHelper.regionNameExt(deviceRegion.value)
                val codeNameExt = codeName.value + regionNameExt
                val systemVersionExt = systemVersion.value.uppercase().replace("^OS1".toRegex(), "V816").replace("AUTO$".toRegex(), deviceCode)
                val branchExt = if (systemVersion.value.uppercase().endsWith(".DEV")) "X" else "F"

                showSnackbar(message = messageIng)

                coroutineScope.launch {

                    val romInfo = getRecoveryRomInfo(branchExt, codeNameExt, regionCode, systemVersionExt, androidVersion.value)

                    if (romInfo.isNotEmpty()) {

                        val recoveryRomInfo = json.decodeFromString<RomInfoHelper.RomInfo>(romInfo)

                        val loginInfo = perfGet("loginInfo") ?: ""
                        if (loginInfo.isNotEmpty()) {
                            val cookies = json.decodeFromString<MutableMap<String, String>>(loginInfo)
                            val description = cookies["description"] ?: ""
                            val authResult = cookies["authResult"].toString()
                            if (description.isNotEmpty() && recoveryRomInfo.authResult != 1 && authResult != "3") {
                                cookies.clear()
                                cookies["authResult"] = "3"
                                isLogin.value = 3
                                perfSet("loginInfo", json.encodeToString(cookies))
                            }
                        }

                        if (recoveryRomInfo.currentRom?.bigversion != null) {

                            val curRomDownload = if (recoveryRomInfo.currentRom.md5 != recoveryRomInfo.latestRom?.md5) {
                                val romInfoCurrent = getRecoveryRomInfo("", codeNameExt, regionCode, systemVersionExt, androidVersion.value)
                                val recoveryRomInfoCurrent = json.decodeFromString<RomInfoHelper.RomInfo>(romInfoCurrent)
                                downloadUrl(recoveryRomInfoCurrent.currentRom?.version!!, recoveryRomInfoCurrent.latestRom?.filename!!)
                            } else downloadUrl(recoveryRomInfo.currentRom.version!!, recoveryRomInfo.latestRom?.filename!!)

                            handleRomInfo(recoveryRomInfo, recoveryRomInfo.currentRom, curRomInfo, curIconInfo, curRomDownload)

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

                            showSnackbar(messageSuccessResult, 1000L)

                        } else if (recoveryRomInfo.incrementRom?.bigversion != null) {

                            handleRomInfo(recoveryRomInfo, recoveryRomInfo.incrementRom, curRomInfo, curIconInfo)
                            clearRomInfo(incRomInfo)
                            showSnackbar(messageWrongResult)

                        } else if (recoveryRomInfo.crossRom?.bigversion != null) {

                            handleRomInfo(recoveryRomInfo, recoveryRomInfo.crossRom, curRomInfo, curIconInfo)
                            clearRomInfo(incRomInfo)
                            showSnackbar(messageWrongResult)

                        } else {

                            clearRomInfo(curRomInfo)
                            clearRomInfo(incRomInfo)
                            showSnackbar(messageNoResult)

                        }
                    } else {

                        clearRomInfo(curRomInfo)
                        clearRomInfo(incRomInfo)
                        showSnackbar(messageCrashResult, 5000L)

                    }
                }
            }) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                modifier = Modifier.height(20.dp)
            )
            Spacer(
                modifier = Modifier.width(8.dp)
            )
            Text(
                text = stringResource(Res.string.submit),
                modifier = Modifier.height(20.dp)
            )
        }
    }
}