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
import data.LoginHelper
import data.RomInfoHelper
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import misc.SnackbarUtil.Companion.Snackbar
import misc.SnackbarUtil.Companion.showSnackbar
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

    val typeCurRom = remember { mutableStateOf("") }
    val deviceCurRom = remember { mutableStateOf("") }
    val versionCurRom = remember { mutableStateOf("") }
    val codebaseCurRom = remember { mutableStateOf("") }
    val branchCurRom = remember { mutableStateOf("") }
    val fileNameCurRom = remember { mutableStateOf("") }
    val fileSizeCurRom = remember { mutableStateOf("") }
    val bigVersionCurRom = remember { mutableStateOf("") }
    val officialDownloadCurRom = remember { mutableStateOf("") }
    val cdn1DownloadCurRom = remember { mutableStateOf("") }
    val cdn2DownloadCurRom = remember { mutableStateOf("") }
    val changeLogCurRom = remember { mutableStateOf("") }

    val curRomInfo = listOf(
        typeCurRom, deviceCurRom, versionCurRom, codebaseCurRom, branchCurRom, fileNameCurRom, fileSizeCurRom,
        bigVersionCurRom, officialDownloadCurRom, cdn1DownloadCurRom, cdn2DownloadCurRom, changeLogCurRom
    )

    val typeIncRom = remember { mutableStateOf("") }
    val deviceIncRom = remember { mutableStateOf("") }
    val versionIncRom = remember { mutableStateOf("") }
    val codebaseIncRom = remember { mutableStateOf("") }
    val branchIncRom = remember { mutableStateOf("") }
    val fileNameIncRom = remember { mutableStateOf("") }
    val fileSizeIncRom = remember { mutableStateOf("") }
    val bigVersionIncRom = remember { mutableStateOf("") }
    val officialDownloadIncRom = remember { mutableStateOf("") }
    val cdn1DownloadIncRom = remember { mutableStateOf("") }
    val cdn2DownloadIncRom = remember { mutableStateOf("") }
    val changeLogIncRom = remember { mutableStateOf("") }

    val incRomInfo = listOf(
        typeIncRom, deviceIncRom, versionIncRom, codebaseIncRom, branchIncRom, fileNameIncRom, fileSizeIncRom,
        bigVersionIncRom, officialDownloadIncRom, cdn1DownloadIncRom, cdn2DownloadIncRom, changeLogIncRom
    )

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
                FloatActionButton(fabOffsetHeight, deviceName, codeName, deviceRegion, systemVersion, androidVersion, curRomInfo, incRomInfo, isLogin)
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
                                MessageCardViews(typeCurRom, deviceCurRom, versionCurRom, bigVersionCurRom, codebaseCurRom, branchCurRom)
                                MoreInfoCardViews(fileNameCurRom, fileSizeCurRom, changeLogCurRom)
                                DownloadCardViews(officialDownloadCurRom, cdn1DownloadCurRom, cdn2DownloadCurRom, fileNameCurRom)
                                MessageCardViews(typeIncRom, deviceIncRom, versionIncRom, bigVersionIncRom, codebaseIncRom, branchIncRom)
                                MoreInfoCardViews(fileNameIncRom, fileSizeIncRom, changeLogIncRom)
                                DownloadCardViews(officialDownloadIncRom, cdn1DownloadIncRom, cdn2DownloadIncRom, fileNameIncRom)
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
                                        MessageCardViews(typeCurRom, deviceCurRom, versionCurRom, bigVersionCurRom, codebaseCurRom, branchCurRom)
                                        MoreInfoCardViews(fileNameCurRom, fileSizeCurRom, changeLogCurRom)
                                        DownloadCardViews(officialDownloadCurRom, cdn1DownloadCurRom, cdn2DownloadCurRom, fileNameCurRom)
                                        MessageCardViews(typeIncRom, deviceIncRom, versionIncRom, bigVersionIncRom, codebaseIncRom, branchIncRom)
                                        MoreInfoCardViews(fileNameIncRom, fileSizeIncRom, changeLogIncRom)
                                        DownloadCardViews(officialDownloadIncRom, cdn1DownloadIncRom, cdn2DownloadIncRom, fileNameIncRom)
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
    curRomInfo: List<MutableState<String>>,
    incRomInfo: List<MutableState<String>>,
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
            val regionNameExt = DeviceInfoHelper.regionNameExt(deviceRegion.value)
            val codeNameExt = codeName.value + regionNameExt
            val deviceCode = DeviceInfoHelper.deviceCode(androidVersion.value, codeName.value, regionCode)

            val branchExt = if (systemVersion.value.contains(".DEV")) "X" else "F"

            showSnackbar(message = messageIng)

            coroutineScope.launch {

                val romInfo = getRecoveryRomInfo(
                    branchExt, codeNameExt, regionCode,
                    systemVersion.value.uppercase().replace("OS1", "V816").replace("AUTO", deviceCode),
                    androidVersion.value
                )

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

                        curRomInfo[8].value = if (recoveryRomInfo.currentRom.md5 != recoveryRomInfo.latestRom?.md5) {
                            val romInfoCurrent = getRecoveryRomInfo(
                                "", codeNameExt, regionCode,
                                systemVersion.value.uppercase().replace("OS1", "V816").replace("AUTO", deviceCode),
                                androidVersion.value
                            )
                            val recoveryRomInfoCurrent = json.decodeFromString<RomInfoHelper.RomInfo>(romInfoCurrent)
                            "https://ultimateota.d.miui.com/" + recoveryRomInfoCurrent.currentRom?.version + "/" + recoveryRomInfoCurrent.latestRom?.filename
                        } else "https://ultimateota.d.miui.com/" + recoveryRomInfo.currentRom.version + "/" + recoveryRomInfo.latestRom?.filename

                        handleRomInfo(recoveryRomInfo.currentRom, curRomInfo)

                        perfSet("deviceName", deviceName.value)
                        perfSet("codeName", codeName.value)
                        perfSet("deviceRegion", deviceRegion.value)
                        perfSet("systemVersion", systemVersion.value)
                        perfSet("androidVersion", androidVersion.value)

                        if (recoveryRomInfo.incrementRom?.bigversion != null) {

                            incRomInfo[8].value =
                                "https://ultimateota.d.miui.com/" + recoveryRomInfo.incrementRom.version + "/" + recoveryRomInfo.incrementRom.filename

                            handleRomInfo(recoveryRomInfo.incrementRom, incRomInfo)

                        } else {
                            clearRomInfo(incRomInfo)
                        }

                        showSnackbar(messageSuccessResult, 1000L)

                    } else if (recoveryRomInfo.incrementRom?.bigversion != null) {

                        curRomInfo[8].value =
                            "https://ultimateota.d.miui.com/" + recoveryRomInfo.incrementRom.version + "/" + recoveryRomInfo.incrementRom.filename

                        handleRomInfo(recoveryRomInfo.incrementRom, curRomInfo)
                        clearRomInfo(incRomInfo)

                        showSnackbar(messageWrongResult)

                    } else if (recoveryRomInfo.crossRom?.bigversion != null) {

                        curRomInfo[8].value = "https://ultimateota.d.miui.com/" + recoveryRomInfo.crossRom.version + "/" + recoveryRomInfo.crossRom.filename

                        handleRomInfo(recoveryRomInfo.crossRom, curRomInfo)
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