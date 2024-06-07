import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import data.DeviceInfoHelper
import data.LoginHelper
import data.RomInfoHelper
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import misc.json
import org.jetbrains.compose.resources.stringResource
import ui.AboutDialog
import ui.DownloadCardViews
import ui.LoginCardView
import ui.LoginDialog
import ui.MessageCardViews
import ui.MoreInfoCardViews
import ui.TextFieldViews
import updaterkmm.composeapp.generated.resources.Res
import updaterkmm.composeapp.generated.resources.app_name
import updaterkmm.composeapp.generated.resources.submit
import updaterkmm.composeapp.generated.resources.toast_ing
import updaterkmm.composeapp.generated.resources.toast_no_info
import updaterkmm.composeapp.generated.resources.toast_wrong_info

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

    val type = remember { mutableStateOf("") }
    val device = remember { mutableStateOf("") }
    val version = remember { mutableStateOf("") }
    val codebase = remember { mutableStateOf("") }
    val branch = remember { mutableStateOf("") }
    val fileName = remember { mutableStateOf("") }
    val fileSize = remember { mutableStateOf("") }
    val bigVersion = remember { mutableStateOf("") }
    val officialDownload = remember { mutableStateOf("") }
    val cdn1Download = remember { mutableStateOf("") }
    val cdn2Download = remember { mutableStateOf("") }
    val changeLog = remember { mutableStateOf("") }

    val typeIncrementRom = remember { mutableStateOf("") }
    val deviceIncrementRom = remember { mutableStateOf("") }
    val versionIncrementRom = remember { mutableStateOf("") }
    val codebaseIncrementRom = remember { mutableStateOf("") }
    val branchIncrementRom = remember { mutableStateOf("") }
    val fileNameIncrementRom = remember { mutableStateOf("") }
    val fileSizeIncrementRom = remember { mutableStateOf("") }
    val bigVersionIncrementRom = remember { mutableStateOf("") }
    val officialDownloadIncrementRom = remember { mutableStateOf("") }
    val cdn1DownloadIncrementRom = remember { mutableStateOf("") }
    val cdn2DownloadIncrementRom = remember { mutableStateOf("") }
    val changeLogIncrementRom = remember { mutableStateOf("") }

    val snackBarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val fabOffsetHeight by animateDpAsState(
        targetValue = if (scrollBehavior.state.contentOffset < -10) 80.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() else 0.dp,
        animationSpec = tween(durationMillis = 300)
    )
    val snackOffsetHeight by animateDpAsState(
        targetValue = if (scrollBehavior.state.contentOffset <= -10) 65.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() else 0.dp,
        animationSpec = tween(durationMillis = 300)
    )

    AppTheme {
        MaterialTheme {
            Scaffold(
                snackbarHost = {
                    SnackbarHost(
                        hostState = snackBarHostState,
                        modifier = Modifier.padding(horizontal = 3.dp).offset(y = snackOffsetHeight),
                    )
                },
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                topBar = { TopAppBar(scrollBehavior, snackBarHostState, isLogin) },
                floatingActionButton = {
                    FloatActionButton(
                        fabOffsetHeight,
                        deviceName,
                        codeName,
                        deviceRegion,
                        systemVersion,
                        androidVersion,
                        type,
                        device,
                        version,
                        codebase,
                        branch,
                        fileName,
                        fileSize,
                        bigVersion,
                        officialDownload,
                        cdn1Download,
                        cdn2Download,
                        changeLog,
                        typeIncrementRom,
                        deviceIncrementRom,
                        versionIncrementRom,
                        codebaseIncrementRom,
                        branchIncrementRom,
                        fileNameIncrementRom,
                        fileSizeIncrementRom,
                        bigVersionIncrementRom,
                        officialDownloadIncrementRom,
                        cdn1DownloadIncrementRom,
                        cdn2DownloadIncrementRom,
                        changeLogIncrementRom,
                        snackBarHostState,
                        isLogin
                    )
                },
                floatingActionButtonPosition = FabPosition.End
            ) { padding ->
                LazyColumn(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .padding(top = padding.calculateTopPadding())
                        .padding(horizontal = 20.dp)
                ) {
                    item {
                        LoginCardView(isLogin)
                        TextFieldViews(deviceName, codeName, deviceRegion, systemVersion, androidVersion)
                        MessageCardViews(type, device, version, bigVersion, codebase, branch)
                        MoreInfoCardViews(fileName, fileSize, changeLog, snackBarHostState)
                        DownloadCardViews(officialDownload, cdn1Download, cdn2Download, fileName, snackBarHostState)
                        MessageCardViews(
                            typeIncrementRom,
                            deviceIncrementRom,
                            versionIncrementRom,
                            bigVersionIncrementRom,
                            codebaseIncrementRom,
                            branchIncrementRom
                        )
                        MoreInfoCardViews(fileNameIncrementRom, fileSizeIncrementRom, changeLogIncrementRom, snackBarHostState)
                        DownloadCardViews(
                            officialDownloadIncrementRom,
                            cdn1DownloadIncrementRom,
                            cdn2DownloadIncrementRom,
                            fileNameIncrementRom,
                            snackBarHostState
                        )
                        Spacer(Modifier.height(padding.calculateBottomPadding()))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopAppBar(scrollBehavior: TopAppBarScrollBehavior, snackBarHostState: SnackbarHostState, isLogin: MutableState<Int>) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = stringResource(Res.string.app_name),
                style = MaterialTheme.typography.titleLarge
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
            LoginDialog(snackBarHostState, isLogin)
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
    type: MutableState<String>,
    device: MutableState<String>,
    version: MutableState<String>,
    codebase: MutableState<String>,
    branch: MutableState<String>,
    fileName: MutableState<String>,
    fileSize: MutableState<String>,
    bigVersion: MutableState<String>,
    officialDownload: MutableState<String>,
    cdn1Download: MutableState<String>,
    cdn2Download: MutableState<String>,
    changeLog: MutableState<String>,
    typeIncrementRom: MutableState<String>,
    deviceIncrementRom: MutableState<String>,
    versionIncrementRom: MutableState<String>,
    codebaseIncrementRom: MutableState<String>,
    branchIncrementRom: MutableState<String>,
    fileNameIncrementRom: MutableState<String>,
    fileSizeIncrementRom: MutableState<String>,
    bigVersionIncrementRom: MutableState<String>,
    officialDownloadIncrementRom: MutableState<String>,
    cdn1DownloadIncrementRom: MutableState<String>,
    cdn2DownloadIncrementRom: MutableState<String>,
    changeLogIncrementRom: MutableState<String>,
    snackBarHostState: SnackbarHostState,
    isLogin: MutableState<Int>
) {
    val coroutineScope = rememberCoroutineScope()
    val messageIng = stringResource(Res.string.toast_ing)
    val messageNoResult = stringResource(Res.string.toast_no_info)
    val messageWrongResult = stringResource(Res.string.toast_wrong_info)

    ExtendedFloatingActionButton(modifier = Modifier.offset(y = fabOffsetHeight), onClick = {
        val regionCode = DeviceInfoHelper.regionCode(deviceRegion.value)
        val regionNameExt = DeviceInfoHelper.regionNameExt(deviceRegion.value)
        val codeNameExt = codeName.value + regionNameExt
        val deviceCode = DeviceInfoHelper.deviceCode(androidVersion.value, codeName.value, regionCode)

        val branchExt = if (systemVersion.value.contains(".DEV")) "X" else "F"

        coroutineScope.launch {
            snackBarHostState.showSnackbar(message = messageIng)
        }
        coroutineScope.launch {
            val recoveryRomInfo = json.decodeFromString<RomInfoHelper.RomInfo>(
                getRecoveryRomInfo(
                    branchExt, codeNameExt, regionCode, systemVersion.value.uppercase().replace("OS1", "V816").replace("AUTO", deviceCode), androidVersion.value
                )
            )
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

                officialDownload.value = if (recoveryRomInfo.currentRom.md5 != recoveryRomInfo.latestRom?.md5) {
                    val recoveryRomInfoCurrent = json.decodeFromString<RomInfoHelper.RomInfo>(
                        getRecoveryRomInfo(
                            "",
                            codeNameExt,
                            regionCode,
                            systemVersion.value.uppercase().replace("OS1", "V816").replace("AUTO", deviceCode),
                            androidVersion.value
                        )
                    )
                   "https://ultimateota.d.miui.com/" + recoveryRomInfoCurrent.currentRom?.version + "/" + recoveryRomInfoCurrent.latestRom?.filename
                } else "https://ultimateota.d.miui.com/" + recoveryRomInfo.currentRom.version + "/" + recoveryRomInfo.latestRom?.filename

                handleRomInfo(
                    recoveryRomInfo.currentRom,
                    type,
                    device,
                    version,
                    codebase,
                    branch,
                    fileName,
                    fileSize,
                    bigVersion,
                    cdn1Download,
                    cdn2Download,
                    changeLog
                )

                perfSet("deviceName", deviceName.value)
                perfSet("codeName", codeName.value)
                perfSet("deviceRegion", deviceRegion.value)
                perfSet("systemVersion", systemVersion.value)
                perfSet("androidVersion", androidVersion.value)

                if (recoveryRomInfo.incrementRom?.bigversion != null) {
                    officialDownloadIncrementRom.value =
                        "https://ultimateota.d.miui.com/" + recoveryRomInfo.incrementRom.version + "/" + recoveryRomInfo.incrementRom.filename

                    handleRomInfo(
                        recoveryRomInfo.incrementRom,
                        typeIncrementRom,
                        deviceIncrementRom,
                        versionIncrementRom,
                        codebaseIncrementRom,
                        branchIncrementRom,
                        fileNameIncrementRom,
                        fileSizeIncrementRom,
                        bigVersionIncrementRom,
                        cdn1DownloadIncrementRom,
                        cdn2DownloadIncrementRom,
                        changeLogIncrementRom
                    )
                } else {
                    typeIncrementRom.value = ""
                    deviceIncrementRom.value = ""
                    versionIncrementRom.value = ""
                    codebaseIncrementRom.value = ""
                    branchIncrementRom.value = ""
                    fileNameIncrementRom.value = ""
                    fileSizeIncrementRom.value = ""
                    bigVersionIncrementRom.value = ""
                    officialDownloadIncrementRom.value = ""
                    cdn1DownloadIncrementRom.value = ""
                    cdn2DownloadIncrementRom.value = ""
                    changeLogIncrementRom.value = ""
                }

                snackBarHostState.currentSnackbarData?.dismiss()

            } else if (recoveryRomInfo.incrementRom?.bigversion != null) {

                officialDownload.value = "https://ultimateota.d.miui.com/" + recoveryRomInfo.incrementRom.version + "/" + recoveryRomInfo.incrementRom.filename

                handleRomInfo(
                    recoveryRomInfo.incrementRom,
                    type,
                    device,
                    version,
                    codebase,
                    branch,
                    fileName,
                    fileSize,
                    bigVersion,
                    cdn1Download,
                    cdn2Download,
                    changeLog
                )

                typeIncrementRom.value = ""
                deviceIncrementRom.value = ""
                versionIncrementRom.value = ""
                codebaseIncrementRom.value = ""
                branchIncrementRom.value = ""
                fileNameIncrementRom.value = ""
                fileSizeIncrementRom.value = ""
                bigVersionIncrementRom.value = ""
                officialDownloadIncrementRom.value = ""
                cdn1DownloadIncrementRom.value = ""
                cdn2DownloadIncrementRom.value = ""
                changeLogIncrementRom.value = ""

                snackBarHostState.currentSnackbarData?.dismiss()
                snackBarHostState.showSnackbar(messageWrongResult)

            } else if (recoveryRomInfo.crossRom?.bigversion != null) {

                officialDownload.value = "https://ultimateota.d.miui.com/" + recoveryRomInfo.crossRom.version + "/" + recoveryRomInfo.crossRom.filename

                handleRomInfo(
                    recoveryRomInfo.crossRom,
                    type,
                    device,
                    version,
                    codebase,
                    branch,
                    fileName,
                    fileSize,
                    bigVersion,
                    cdn1Download,
                    cdn2Download,
                    changeLog
                )

                typeIncrementRom.value = ""
                deviceIncrementRom.value = ""
                versionIncrementRom.value = ""
                codebaseIncrementRom.value = ""
                branchIncrementRom.value = ""
                fileNameIncrementRom.value = ""
                fileSizeIncrementRom.value = ""
                bigVersionIncrementRom.value = ""
                officialDownloadIncrementRom.value = ""
                cdn1DownloadIncrementRom.value = ""
                cdn2DownloadIncrementRom.value = ""
                changeLogIncrementRom.value = ""

                snackBarHostState.currentSnackbarData?.dismiss()
                snackBarHostState.showSnackbar(messageWrongResult)

            } else {
                type.value = ""
                device.value = ""
                version.value = ""
                codebase.value = ""
                branch.value = ""
                fileName.value = ""
                fileSize.value = ""
                bigVersion.value = ""
                officialDownload.value = ""
                cdn1Download.value = ""
                cdn2Download.value = ""
                changeLog.value = ""
                typeIncrementRom.value = ""
                deviceIncrementRom.value = ""
                versionIncrementRom.value = ""
                codebaseIncrementRom.value = ""
                branchIncrementRom.value = ""
                fileNameIncrementRom.value = ""
                fileSizeIncrementRom.value = ""
                bigVersionIncrementRom.value = ""
                officialDownloadIncrementRom.value = ""
                cdn1DownloadIncrementRom.value = ""
                cdn2DownloadIncrementRom.value = ""
                changeLogIncrementRom.value = ""

                snackBarHostState.currentSnackbarData?.dismiss()
                snackBarHostState.showSnackbar(messageNoResult)
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


fun handleRomInfo(
    romInfo: RomInfoHelper.Rom?,
    type: MutableState<String>,
    device: MutableState<String>,
    version: MutableState<String>,
    codebase: MutableState<String>,
    branch: MutableState<String>,
    fileName: MutableState<String>,
    fileSize: MutableState<String>,
    bigVersion: MutableState<String>,
    cdn1Download: MutableState<String>,
    cdn2Download: MutableState<String>,
    changeLog: MutableState<String>
) {
    if (romInfo?.bigversion != null) {
        val log = StringBuilder()
        romInfo.changelog!!.forEach { log.append(it.key).append("\n- ").append(it.value.txt.joinToString("\n- ")).append("\n\n") }
        type.value = romInfo.type.toString()
        device.value = romInfo.device.toString()
        version.value = romInfo.version.toString()
        codebase.value = romInfo.codebase.toString()
        branch.value = romInfo.branch.toString()
        fileName.value = romInfo.filename.toString().substringBefore(".zip") + ".zip"
        fileSize.value = romInfo.filesize.toString()
        bigVersion.value = if (romInfo.bigversion.contains("816")) romInfo.bigversion.replace("816", "HyperOS 1.0") else "MIUI ${romInfo.bigversion}"
        cdn1Download.value = "https://cdnorg.d.miui.com/" + romInfo.version + "/" + romInfo.filename
        cdn2Download.value = "https://bkt-sgp-miui-ota-update-alisgp.oss-ap-southeast-1.aliyuncs.com/" + romInfo.version + "/" + romInfo.filename
        changeLog.value = log.toString().trimEnd()
    } else {
        type.value = ""
        device.value = ""
        version.value = ""
        codebase.value = ""
        branch.value = ""
        fileName.value = ""
        fileSize.value = ""
        bigVersion.value = ""
        cdn1Download.value = ""
        cdn2Download.value = ""
        changeLog.value = ""
    }
}