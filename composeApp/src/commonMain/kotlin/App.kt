import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Android
import androidx.compose.material.icons.outlined.DeveloperMode
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import data.DeviceInfoHelper
import data.RomInfoHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import misc.json

var version = "v1.0.0"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val deviceName = remember { mutableStateOf(perfGet("deviceName") ?: "") }
    val codeName = remember { mutableStateOf(perfGet("codeName") ?: "") }
    val deviceRegion = remember { mutableStateOf(perfGet("deviceRegion") ?: "") }
    val systemVersion = remember { mutableStateOf(perfGet("systemVersion") ?: "") }
    val androidVersion = remember { mutableStateOf(perfGet("androidVersion") ?: "") }

    val isLogin = remember { mutableStateOf(perfGet("loginInfo") != null) }

    val device = remember { mutableStateOf("") }
    val version = remember { mutableStateOf("") }
    val codebase = remember { mutableStateOf("") }
    val branch = remember { mutableStateOf("") }
    val fileName = remember { mutableStateOf("") }
    val fileSize = remember { mutableStateOf("") }
    val bigVersion = remember { mutableStateOf("") }
    val officialDownload = remember { mutableStateOf("") }
    val officialText = remember { mutableStateOf("") }
    val cdn1Download = remember { mutableStateOf("") }
    val cdn2Download = remember { mutableStateOf("") }
    val changeLog = remember { mutableStateOf("") }

    val snackBarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val scrollState = rememberScrollState()
    val fabOffsetHeight by animateDpAsState(
        targetValue = if (scrollState.value > 0) 80.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() else 0.dp,
        animationSpec = tween(durationMillis = 300)
    )

    AppTheme {
        MaterialTheme {
            Scaffold(
                snackbarHost = { SnackbarHost(snackBarHostState) },
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
                        device,
                        version,
                        codebase,
                        branch,
                        fileName,
                        fileSize,
                        bigVersion,
                        officialDownload,
                        officialText,
                        cdn1Download,
                        cdn2Download,
                        changeLog,
                        snackBarHostState
                    )
                },
                floatingActionButtonPosition = FabPosition.End
            ) { padding ->
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .padding(top = padding.calculateTopPadding())
                        .padding(horizontal = 24.dp)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LoginCardView(isLogin)
                    EditTextFields(deviceName, codeName, deviceRegion, systemVersion, androidVersion)
                    MessageCardViews(device, version, bigVersion, codebase, branch)
                    MoreCardViews(fileName, fileSize, changeLog)
                    DownloadCardViews(officialText, officialDownload, cdn1Download, cdn2Download, fileName)
                    Text(
                        text = "当前运行在 ${getPlatform().name}!",
                        modifier = Modifier.padding(bottom = 18.dp + padding.calculateBottomPadding()),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopAppBar(scrollBehavior: TopAppBarScrollBehavior, snackbarHostState: SnackbarHostState, isLogin: MutableState<Boolean>) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "UpdaterKMM",
                style = MaterialTheme.typography.titleLarge
            )
        },
        navigationIcon = { AboutDialog() },
        actions = { LoginDialog(snackbarHostState, isLogin) },
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
    device: MutableState<String>,
    version: MutableState<String>,
    codebase: MutableState<String>,
    branch: MutableState<String>,
    fileName: MutableState<String>,
    fileSize: MutableState<String>,
    bigVersion: MutableState<String>,
    officialDownload: MutableState<String>,
    officialText: MutableState<String>,
    cdn1Download: MutableState<String>,
    cdn2Download: MutableState<String>,
    changeLog: MutableState<String>,
    snackbarHostState: SnackbarHostState
) {
    val coroutineScope = rememberCoroutineScope()

    ExtendedFloatingActionButton(
        modifier = Modifier.offset(y = fabOffsetHeight),
        onClick = {
            val regionCode = DeviceInfoHelper.regionCode(deviceRegion.value)
            val regionNameExt = DeviceInfoHelper.regionNameExt(deviceRegion.value)
            val codeNameExt = codeName.value + regionNameExt
            val deviceCode = DeviceInfoHelper.deviceCode(androidVersion.value, codeName.value, regionCode)
            coroutineScope.launch {
                snackbarHostState.showSnackbar(message = "正在查询...")
            }
            coroutineScope.launch {
                val recoveryRomInfo = json.decodeFromString<RomInfoHelper.RomInfo>(
                    getRecoveryRomInfo(
                        codeNameExt,
                        regionCode,
                        systemVersion.value.replace("OS1", "V816").replace("AUTO", deviceCode),
                        androidVersion.value
                    )
                )
                if (recoveryRomInfo.currentRom?.branch != null) {
                    val log = StringBuilder()
                    recoveryRomInfo.currentRom.changelog!!.forEach {
                        log.append(it.key).append("\n- ").append(it.value.txt.joinToString("\n- ")).append("\n\n")
                    }
                    device.value = recoveryRomInfo.currentRom.device.toString()
                    version.value = recoveryRomInfo.currentRom.version.toString()
                    codebase.value = recoveryRomInfo.currentRom.codebase.toString()
                    branch.value = recoveryRomInfo.currentRom.branch.toString()
                    fileName.value = recoveryRomInfo.currentRom.filename.toString()
                    fileSize.value = recoveryRomInfo.currentRom.filesize.toString()
                    bigVersion.value = if (recoveryRomInfo.currentRom.bigversion?.contains("816") == true) {
                        recoveryRomInfo.currentRom.bigversion.replace("816", "HyperOS 1.0")
                    } else {
                        "MIUI ${recoveryRomInfo.currentRom.bigversion}"
                    }
                    officialDownload.value = if (recoveryRomInfo.currentRom.md5 == recoveryRomInfo.latestRom?.md5) {
                        "https://ultimateota.d.miui.com/" + recoveryRomInfo.currentRom.version + "/" + recoveryRomInfo.latestRom?.filename
                    } else {
                        "https://bigota.d.miui.com/" + recoveryRomInfo.currentRom.version + "/" + recoveryRomInfo.currentRom.filename
                    }
                    officialText.value = if (recoveryRomInfo.currentRom.md5 == recoveryRomInfo.latestRom?.md5) {
                        "ultimateota"
                    } else {
                        "bigota"
                    }
                    cdn1Download.value = if (recoveryRomInfo.currentRom.md5 == recoveryRomInfo.latestRom?.md5) {
                        "https://cdnorg.d.miui.com/" + recoveryRomInfo.currentRom.version + "/" + recoveryRomInfo.latestRom?.filename
                    } else {
                        "https://cdnorg.d.miui.com/" + recoveryRomInfo.currentRom.version + "/" + recoveryRomInfo.currentRom.filename
                    }
                    cdn2Download.value = if (recoveryRomInfo.currentRom.md5 == recoveryRomInfo.latestRom?.md5) {
                        "https://bkt-sgp-miui-ota-update-alisgp.oss-ap-southeast-1.aliyuncs.com/" + recoveryRomInfo.currentRom.version + "/" + recoveryRomInfo.latestRom?.filename
                    } else {
                        "https://bkt-sgp-miui-ota-update-alisgp.oss-ap-southeast-1.aliyuncs.com/" + recoveryRomInfo.currentRom.version + "/" + recoveryRomInfo.currentRom.filename
                    }
                    changeLog.value = log.toString().trimEnd()

                    perfSet("deviceName", deviceName.value)
                    perfSet("codeName", codeName.value)
                    perfSet("deviceRegion", deviceRegion.value)
                    perfSet("systemVersion", systemVersion.value)
                    perfSet("androidVersion", androidVersion.value)

                    snackbarHostState.currentSnackbarData?.dismiss()
                } else {
                    device.value = ""
                    version.value = ""
                    codebase.value = ""
                    branch.value = ""
                    fileName.value = ""
                    fileSize.value = ""
                    bigVersion.value = ""
                    officialDownload.value = ""
                    officialText.value = ""
                    cdn1Download.value = ""
                    cdn2Download.value = ""
                    changeLog.value = ""
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(message = "未查询到结果")
                }
            }
        }
    ) {
        Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = null,
            modifier = Modifier.height(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "查询",
            modifier = Modifier.height(20.dp)
        )
    }
}

@Composable
fun LoginCardView(
    isLogin: MutableState<Boolean>
) {
    val account = if (isLogin.value) "已登录" else "未登录"
    val info = if (isLogin.value) "正在使用 v2 接口" else "正在使用 v1 接口"
    val icon = if (isLogin.value) Icons.Filled.DoneAll else Icons.Filled.Done

    Card(
        elevation = CardDefaults.cardElevation(1.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(18.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp).padding(start = 8.dp)
            )
            Column(modifier = Modifier.padding(start = 20.dp)) {
                Text(
                    text = account,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = info,
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }
}

@Composable

fun EditTextFields(
    deviceName: MutableState<String>,
    codeName: MutableState<String>,
    deviceRegion: MutableState<String>,
    systemVersion: MutableState<String>,
    androidVersion: MutableState<String>
) {
    val deviceNameFlow = MutableStateFlow(deviceName.value)
    val codeNameFlow = MutableStateFlow(codeName.value)
    val coroutineScope = rememberCoroutineScope()

    coroutineScope.launch {
        deviceNameFlow.collect { newValue ->
            if (deviceName.value != deviceNameFlow.value) {
                val text = DeviceInfoHelper.codeName(newValue)
                if (text != "") codeName.value = text
                deviceName.value = newValue
            }
        }
    }

    coroutineScope.launch {
        codeNameFlow.collect { newValue ->
            if (codeName.value != codeNameFlow.value) {
                val text = DeviceInfoHelper.deviceName(newValue)
                if (text != "") deviceName.value = text
                codeName.value = newValue
            }
        }
    }
    Column(
        modifier = Modifier.background(Color.Unspecified).fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AutoCompleteTextField(
            text = deviceName,
            items = DeviceInfoHelper.deviceNames,
            onValueChange = deviceNameFlow,
            label = "设备名称",
            leadingIcon = Icons.Outlined.Smartphone
        )
        AutoCompleteTextField(
            text = codeName,
            items = DeviceInfoHelper.codeNames,
            onValueChange = codeNameFlow,
            label = "设备代号",
            leadingIcon = Icons.Outlined.DeveloperMode
        )
        val itemsA = listOf("CN", "GL", "EEA", "RU", "TW", "ID", "TR", "IN", "JP", "KR")
        TextFieldWithDropdown(
            text = deviceRegion,
            items = itemsA, label = "区域代号",
            leadingIcon = Icons.Outlined.TravelExplore
        )
        OutlinedTextField(
            value = systemVersion.value,
            onValueChange = { systemVersion.value = it },
            label = { Text("系统版本") },
            leadingIcon = { Icon(imageVector = Icons.Outlined.Analytics, null) },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        val itemsB = listOf("14.0", "13.0", "12.0", "11.0", "10.0", "9.0", "8.1", "8.0", "7.1", "7.0", "6.0", "5.1", "5.0", "4.4")
        TextFieldWithDropdown(
            text = androidVersion,
            items = itemsB,
            label = "安卓版本",
            leadingIcon = Icons.Outlined.Android
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutDialog() {
    var showDialog by remember { mutableStateOf(false) }
    IconButton(
        onClick = { showDialog = true }) {
        Icon(
            imageVector = Icons.Outlined.Update,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface
        )
    }

    if (showDialog) {
        BasicAlertDialog(
            onDismissRequest = { showDialog = false },
            content = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .size(280.dp, 155.dp)
                        .clip(RoundedCornerShape(30.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Row(modifier = Modifier.padding(24.dp)) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(50.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        ) {
                            Image(
                                imageVector = Icons.Outlined.Update,
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary),
                                contentDescription = null,
                                modifier = Modifier.size(25.dp),
                            )
                        }
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = "Updater KMM",
                                modifier = Modifier,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = version,
                                modifier = Modifier,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp).padding(top = 90.dp)
                    ) {
                        Row {
                            Text(
                                text = "在 ",
                                modifier = Modifier,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            GitHubLink()
                            Text(
                                text = " 查看源码",
                                modifier = Modifier,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Text(
                            text = "版权所有 © 2024 YuKongA, AkaneTan",
                            modifier = Modifier.padding(top = 2.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginDialog(
    snackBarHostState: SnackbarHostState,
    isLogin: MutableState<Boolean>
) {
    var account by remember { mutableStateOf(TextFieldValue("")) }
    var password by remember { mutableStateOf(TextFieldValue("")) }
    var global by rememberSaveable { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val icon = perfGet("loginInfo")?.let { Icons.AutoMirrored.Outlined.Logout } ?: Icons.AutoMirrored.Outlined.Login

    IconButton(
        onClick = { showDialog = true }) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface
        )
    }

    if (showDialog) {
        if (!isLogin.value) {
            BasicAlertDialog(
                onDismissRequest = { showDialog = false },
                content = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .size(280.dp, 280.dp)
                            .clip(RoundedCornerShape(30.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        Row(modifier = Modifier.padding(24.dp)) {
                            Text(
                                modifier = Modifier.align(Alignment.CenterVertically).weight(1f),
                                text = "登录",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                            )
                            Checkbox(
                                modifier = Modifier.height(22.dp).align(Alignment.CenterVertically),
                                checked = global,
                                onCheckedChange = { global = it })
                            Text(
                                modifier = Modifier.align(Alignment.CenterVertically),
                                text = "全球账户",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Column(
                            modifier = Modifier.padding(horizontal = 24.dp).padding(top = 65.dp)
                        ) {
                            TextField(
                                value = account,
                                onValueChange = { account = it },
                                label = { Text("账号") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                            )
                            var passwordVisibility by remember { mutableStateOf(false) }
                            TextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("密码") },
                                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(
                                        onClick = { passwordVisibility = !passwordVisibility }
                                    ) {
                                        Icon(
                                            imageVector = if (passwordVisibility) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                            contentDescription = null
                                        )
                                    }
                                }
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = { showDialog = false },
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                ) {
                                    Text(
                                        text = "取消",
                                        modifier = Modifier,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                TextButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            snackBarHostState.showSnackbar(message = "正在登录...")
                                        }
                                        coroutineScope.launch {
                                            login(account.text, password.text, global, coroutineScope, snackBarHostState, isLogin)
                                        }
                                        showDialog = false
                                    }
                                ) {
                                    Text(
                                        text = "登录",
                                        modifier = Modifier,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                })
        } else {
            BasicAlertDialog(
                onDismissRequest = { showDialog = false },
                content = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .size(280.dp, 165.dp)
                            .clip(RoundedCornerShape(30.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        Box(modifier = Modifier.padding(24.dp)) {
                            Text(
                                text = "登出",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 80.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = { showDialog = false },
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                ) {
                                    Text(
                                        text = "取消",
                                        modifier = Modifier,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                TextButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            snackBarHostState.showSnackbar(message = "正在登出...")
                                        }
                                        coroutineScope.launch {
                                            logout(coroutineScope, snackBarHostState, isLogin)
                                        }
                                        showDialog = false
                                    }
                                ) {
                                    Text(
                                        text = "登出",
                                        modifier = Modifier,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                })
        }
    }
}

@Composable
fun GitHubLink() {
    val uriHandler = LocalUriHandler.current
    ClickableText(
        text = AnnotatedString(
            text = "GitHub",
            spanStyle = SpanStyle(textDecoration = TextDecoration.Underline)
        ),
        onClick = { uriHandler.openUri("https://github.com/YuKongA/Updater-KMM") },
        style = MaterialTheme.typography.bodyMedium + SpanStyle(color = MaterialTheme.colorScheme.primary)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextFieldWithDropdown(
    text: MutableState<String>,
    items: List<String>,
    label: String,
    leadingIcon: ImageVector
) {
    var isDropdownExpanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = isDropdownExpanded,
        onExpandedChange = { isDropdownExpanded = it },
    ) {
        OutlinedTextField(
            value = text.value,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            singleLine = true,
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(isDropdownExpanded) },
            leadingIcon = { Icon(imageVector = leadingIcon, null) },
        )
        DropdownMenu(
            modifier = Modifier.exposedDropdownSize().heightIn(max = 250.dp),
            expanded = isDropdownExpanded,
            onDismissRequest = { isDropdownExpanded = false },
        ) {
            items.forEach { item ->
                DropdownMenuItem(modifier = Modifier.background(Color.Transparent),
                    text = { Text(item) },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    onClick = {
                        text.value = item
                        isDropdownExpanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoCompleteTextField(
    text: MutableState<String>,
    items: List<String>,
    onValueChange: MutableStateFlow<String>,
    label: String,
    leadingIcon: ImageVector
) {
    var isDropdownExpanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = isDropdownExpanded,
        onExpandedChange = { isDropdownExpanded = it },
    ) {
        OutlinedTextField(
            value = text.value,
            onValueChange = {
                onValueChange.value = it
                isDropdownExpanded = it.isNotEmpty()
            },
            singleLine = true,
            label = { Text(label) },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            leadingIcon = { Icon(imageVector = leadingIcon, null) },
        )
        val listForItems = ArrayList(items)
        val list = listForItems.filter {
            it.startsWith(text.value, ignoreCase = true) || it.contains(text.value, ignoreCase = true)
                    || it.replace(" ", "").contains(text.value, ignoreCase = true)
        }.sortedBy {
            !it.startsWith(text.value, ignoreCase = true)
        }
        DropdownMenu(
            modifier = Modifier.exposedDropdownSize().heightIn(max = 250.dp).imePadding(),
            expanded = isDropdownExpanded && list.isNotEmpty(),
            onDismissRequest = { isDropdownExpanded = false },
            properties = PopupProperties(focusable = false)
        ) {
            list.forEach { text ->
                DropdownMenuItem(
                    text = { Text(text) },
                    onClick = {
                        onValueChange.value = text
                        isDropdownExpanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun MessageCardViews(
    codeName: MutableState<String>,
    systemVersion: MutableState<String>,
    xiaomiVersion: MutableState<String>,
    androidVersion: MutableState<String>,
    branchVersion: MutableState<String>
) {
    val isVisible = remember { mutableStateOf(false) }
    isVisible.value = codeName.value.isNotEmpty()

    AnimatedVisibility(
        visible = isVisible.value,
        enter = fadeIn(animationSpec = tween(400)),
        exit = fadeOut(animationSpec = tween(400))
    ) {
        Card(
            colors = CardDefaults.cardColors(
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column {
                MessageCardView(
                    codeName.value,
                    systemVersion.value,
                    xiaomiVersion.value,
                    androidVersion.value,
                    branchVersion.value
                )
            }
        }
    }
}

@Composable
fun MessageCardView(
    codeName: String,
    systemVersion: String,
    xiaomiVersion: String,
    androidVersion: String,
    branchVersion: String
) {
    val content = remember { mutableStateOf("") }
    content.value = codeName + systemVersion

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp)) {
        Spacer(modifier = Modifier.height(4.dp))
        MessageTextView("设备代号", codeName)
        MessageTextView("系统版本", systemVersion)
        MessageTextView("主要版本", xiaomiVersion)
        MessageTextView("安卓版本", androidVersion)
        MessageTextView("分支版本", branchVersion)
        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
fun MoreCardViews(
    fileName: MutableState<String>,
    fileSize: MutableState<String>,
    changeLog: MutableState<String>,
) {
    val isVisible = remember { mutableStateOf(false) }
    isVisible.value = fileName.value.isNotEmpty()

    AnimatedVisibility(
        visible = isVisible.value,
        enter = fadeIn(animationSpec = tween(400)),
        exit = fadeOut(animationSpec = tween(400))
    ) {
        Card(
            colors = CardDefaults.cardColors(
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))
                MoreTextView("文件名称", fileName.value)
                MoreTextView("文件大小", fileSize.value)
                MoreTextView("更新日志", changeLog.value, true)
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
fun DownloadCardViews(
    officialText: MutableState<String>,
    officialDownload: MutableState<String>,
    cdn1Download: MutableState<String>,
    cdn2Download: MutableState<String>,
    fileName: MutableState<String>,
) {
    val isVisible = remember { mutableStateOf(false) }
    isVisible.value = officialDownload.value.isNotEmpty()

    AnimatedVisibility(
        visible = isVisible.value,
        enter = fadeIn(animationSpec = tween(400)),
        exit = fadeOut(animationSpec = tween(400))
    ) {
        Card(
            colors = CardDefaults.cardColors(
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                "下载链接",
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 8.dp),
                fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                fontWeight = FontWeight.Bold
            )
            DownloadTextView("Official (${officialText.value})", officialDownload.value, officialDownload.value, fileName.value)
            DownloadTextView("CDN (cdnorg)", cdn1Download.value, cdn1Download.value, fileName.value)
            DownloadTextView("CDN (aliyuncs)", cdn2Download.value, cdn2Download.value, fileName.value)
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
fun MessageTextView(
    title: String,
    text: String
) {
    val content = remember { mutableStateOf("") }
    content.value = text

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            modifier = Modifier,
            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
            fontWeight = FontWeight.Bold
        )
        AnimatedContent(
            targetState = content.value,
            transitionSpec = {
                fadeIn(animationSpec = tween(1500)) togetherWith fadeOut(animationSpec = tween(300))
            }) { targetContent ->
            Text(
                text = targetContent,
                modifier = Modifier,
                fontSize = MaterialTheme.typography.bodyMedium.fontSize
            )
        }
    }
}

@Composable
fun MoreTextView(
    title: String,
    text: String,
    copy: Boolean = false
) {
    val content = remember { mutableStateOf("") }
    content.value = text

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Text(
            text = title,
            modifier = Modifier,
            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
            fontWeight = FontWeight.Bold
        )
        AnimatedContent(
            targetState = content.value,
            transitionSpec = {
                fadeIn(animationSpec = tween(1500)) togetherWith fadeOut(animationSpec = tween(300))
            }) { targetContent ->
            Text(
                text = targetContent,
                modifier = Modifier.clickable(copy, onClick = { copyToClipboard(targetContent) }),
                fontSize = MaterialTheme.typography.bodyMedium.fontSize
            )
        }
    }
}

@Composable
fun DownloadTextView(
    title: String,
    copy: String,
    download: String,
    fileName: String
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
            textAlign = TextAlign.Start,
            modifier = Modifier.align(Alignment.CenterVertically).fillMaxWidth(0.5f),
        )
        Row(
            modifier = Modifier,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (copy.isNotEmpty()) {
                TextButton(onClick = {
                    copyToClipboard(copy)
                }) {
                    Text(
                        text = "复制",
                        modifier = Modifier,
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize
                    )
                }
                TextButton(onClick = {
                    downloadToLocal(download, fileName)
                }) {
                    Text(
                        text = "下载",
                        modifier = Modifier,
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize
                    )
                }
            }
        }
    }
}