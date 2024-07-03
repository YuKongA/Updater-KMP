import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import data.DeviceInfoHelper
import data.IconInfoHelper
import data.LoginHelper
import data.RomInfoHelper
import data.RomInfoStateHelper
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import misc.SnackbarUtils.Companion.Snackbar
import misc.SnackbarUtils.Companion.showSnackbar
import misc.clearRomInfo
import misc.handleRomInfo
import misc.json
import org.jetbrains.compose.resources.stringResource
import ui.AboutDialog
import ui.DownloadCardViews
import ui.LoginCardView
import ui.LoginDialog
import ui.MessageCardViews
import ui.MoreInfoCardViews
import ui.TextFieldViews
import updaterkmp.composeapp.generated.resources.Res
import updaterkmp.composeapp.generated.resources.app_name
import updaterkmp.composeapp.generated.resources.submit
import updaterkmp.composeapp.generated.resources.toast_crash_info
import updaterkmp.composeapp.generated.resources.toast_ing
import updaterkmp.composeapp.generated.resources.toast_no_info
import updaterkmp.composeapp.generated.resources.toast_success_info
import updaterkmp.composeapp.generated.resources.toast_wrong_info

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val deviceName = remember { mutableStateOf(perfGet("deviceName") ?: "") }
    val codeName = remember { mutableStateOf(perfGet("codeName") ?: "") }
    val deviceRegion = remember { mutableStateOf(perfGet("deviceRegion") ?: "") }
    val systemVersion = remember { mutableStateOf(perfGet("systemVersion") ?: "") }
    val androidVersion = remember { mutableStateOf(perfGet("androidVersion") ?: "") }

    val loginInfo = perfGet("loginInfo")?.let { json.decodeFromString<LoginHelper>(it) }
    val isLogin = remember { mutableStateOf(loginInfo?.authResult?.toInt() ?: 0) }

    val curRomInfo = remember { mutableStateOf(RomInfoStateHelper()) }
    val incRomInfo = remember { mutableStateOf(RomInfoStateHelper()) }

    val curIconInfo: MutableState<List<IconInfoHelper>> = remember { mutableStateOf(listOf()) }
    val incIconInfo: MutableState<List<IconInfoHelper>> = remember { mutableStateOf(listOf()) }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val fabOffsetHeight by animateDpAsState(
        targetValue = if (scrollBehavior.state.contentOffset < -35) 80.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() else 0.dp,
        animationSpec = tween(durationMillis = 300)
    )
    val snackOffsetHeight by animateDpAsState(
        targetValue = if (scrollBehavior.state.contentOffset <= -35) 65.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() else 0.dp,
        animationSpec = tween(durationMillis = 300)
    )

    AppTheme {
        Scaffold(
            modifier = Modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .background(MaterialTheme.colorScheme.background)
                .displayCutoutPadding(),
            topBar = {
                TopAppBar(scrollBehavior, isLogin)
            },
            snackbarHost = {
                Snackbar(
                    offsetY = snackOffsetHeight
                )
            },
            floatingActionButton = {
                FloatActionButton(
                    fabOffsetHeight, deviceName, codeName, deviceRegion, systemVersion,
                    androidVersion, curRomInfo, incRomInfo, curIconInfo, incIconInfo, isLogin
                )
            },
            floatingActionButtonPosition = FabPosition.End
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .padding(top = padding.calculateTopPadding())
                    .padding(horizontal = 20.dp)
            ) {
                item {
                    BoxWithConstraints {
                        if (maxWidth < 768.dp) {
                            Column {
                                LoginCardView(isLogin)
                                TextFieldViews(deviceName, codeName, deviceRegion, systemVersion, androidVersion)
                                MessageCardViews(curRomInfo)
                                MoreInfoCardViews(curRomInfo, curIconInfo)
                                DownloadCardViews(curRomInfo)
                                MessageCardViews(incRomInfo)
                                MoreInfoCardViews(incRomInfo, incIconInfo)
                                DownloadCardViews(incRomInfo)
                                Spacer(Modifier.height(padding.calculateBottomPadding()))
                            }
                        } else {
                            Column {
                                Row {
                                    Column(modifier = Modifier.weight(0.8f).padding(end = 20.dp)) {
                                        LoginCardView(isLogin)
                                        TextFieldViews(deviceName, codeName, deviceRegion, systemVersion, androidVersion)
                                    }
                                    Column(modifier = Modifier.weight(1.0f)) {
                                        MessageCardViews(curRomInfo)
                                        MoreInfoCardViews(curRomInfo, curIconInfo)
                                        DownloadCardViews(curRomInfo)
                                        MessageCardViews(incRomInfo)
                                        MoreInfoCardViews(incRomInfo, incIconInfo)
                                        DownloadCardViews(incRomInfo)
                                    }
                                }
                                Spacer(Modifier.height(padding.calculateBottomPadding()))
                            }
                        }
                    }

                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopAppBar(scrollBehavior: TopAppBarScrollBehavior, isLogin: MutableState<Int>) {
    CenterAlignedTopAppBar(
        modifier = Modifier.displayCutoutPadding(),
        title = {
            Text(
                text = stringResource(Res.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
            )
        },
        colors = TopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            actionIconContentColor = MaterialTheme.colorScheme.onBackground,
            navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            scrolledContainerColor = MaterialTheme.colorScheme.background,
        ),
        navigationIcon = {
            AboutDialog()
        },
        actions = {
            LoginDialog(isLogin)
        },
        scrollBehavior = scrollBehavior
    )
}

@Composable
private fun FloatActionButton(
    fabOffsetHeight: Dp,
    deviceName: MutableState<String>,
    codeName: MutableState<String>,
    deviceRegion: MutableState<String>,
    systemVersion: MutableState<String>,
    androidVersion: MutableState<String>,
    curRomInfo: MutableState<RomInfoStateHelper>,
    incRomInfo: MutableState<RomInfoStateHelper>,
    curIconInfo: MutableState<List<IconInfoHelper>>,
    incIconInfo: MutableState<List<IconInfoHelper>>,
    isLogin: MutableState<Int>
) {
    val coroutineScope = rememberCoroutineScope()
    val messageIng = stringResource(Res.string.toast_ing)
    val messageNoResult = stringResource(Res.string.toast_no_info)
    val messageSuccessResult = stringResource(Res.string.toast_success_info)
    val messageWrongResult = stringResource(Res.string.toast_wrong_info)
    val messageCrashResult = stringResource(Res.string.toast_crash_info)

    val hapticFeedback = LocalHapticFeedback.current

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

                        val currentRomDownload = if (recoveryRomInfo.currentRom.md5 != recoveryRomInfo.latestRom?.md5) {
                            val romInfoCurrent = getRecoveryRomInfo("", codeNameExt, regionCode, systemVersionExt, androidVersion.value)
                            val recoveryRomInfoCurrent = json.decodeFromString<RomInfoHelper.RomInfo>(romInfoCurrent)
                            "https://ultimateota.d.miui.com/" + recoveryRomInfoCurrent.currentRom?.version + "/" + recoveryRomInfoCurrent.latestRom?.filename
                        } else "https://ultimateota.d.miui.com/" + recoveryRomInfo.currentRom.version + "/" + recoveryRomInfo.latestRom?.filename

                        handleRomInfo(recoveryRomInfo, recoveryRomInfo.currentRom, curRomInfo, curIconInfo, currentRomDownload)

                        perfSet("deviceName", deviceName.value)
                        perfSet("codeName", codeName.value)
                        perfSet("deviceRegion", deviceRegion.value)
                        perfSet("systemVersion", systemVersion.value)
                        perfSet("androidVersion", androidVersion.value)

                        if (recoveryRomInfo.incrementRom?.bigversion != null) {

                            val incrementRomDownload =
                                "https://ultimateota.d.miui.com/" + recoveryRomInfo.incrementRom.version + "/" + recoveryRomInfo.incrementRom.filename

                            handleRomInfo(recoveryRomInfo, recoveryRomInfo.incrementRom, incRomInfo, incIconInfo, incrementRomDownload)

                        } else {

                            val romInfoCross = getRecoveryRomInfo("", codeNameExt, regionCode, systemVersionExt, androidVersion.value)
                            val recoveryRomInfoCross = json.decodeFromString<RomInfoHelper.RomInfo>(romInfoCross)
                            if (recoveryRomInfoCross.crossRom?.bigversion != null) {

                                val crossRomDownload =
                                    "https://ultimateota.d.miui.com/" + recoveryRomInfoCross.crossRom.version + "/" + recoveryRomInfoCross.crossRom.filename

                                handleRomInfo(recoveryRomInfoCross, recoveryRomInfoCross.crossRom, incRomInfo, incIconInfo, crossRomDownload)

                            } else {
                                clearRomInfo(incRomInfo)
                            }
                        }

                        showSnackbar(messageSuccessResult, 1000L)

                    } else if (recoveryRomInfo.incrementRom?.bigversion != null) {

                        val incrementRomDownload =
                            "https://ultimateota.d.miui.com/" + recoveryRomInfo.incrementRom.version + "/" + recoveryRomInfo.incrementRom.filename

                        handleRomInfo(recoveryRomInfo, recoveryRomInfo.incrementRom, curRomInfo, curIconInfo, incrementRomDownload)
                        clearRomInfo(incRomInfo)

                        showSnackbar(messageWrongResult)

                    } else if (recoveryRomInfo.crossRom?.bigversion != null) {

                        val crossRomDownload =
                            "https://ultimateota.d.miui.com/" + recoveryRomInfo.crossRom.version + "/" + recoveryRomInfo.crossRom.filename

                        handleRomInfo(recoveryRomInfo, recoveryRomInfo.crossRom, curRomInfo, curIconInfo, crossRomDownload)
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