import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import data.DeviceInfoHelper
import data.RomInfoHelper
import kotlinx.coroutines.launch
import misc.json
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App() {
    val deviceName = rememberSaveable { mutableStateOf("Xiaomi 14") }
    val codeName = rememberSaveable { mutableStateOf("houji") }
    val deviceRegion = rememberSaveable { mutableStateOf("CN") }
    val systemVersion = rememberSaveable { mutableStateOf("OS1.0.36.0.UNCCNXM") }
    val androidVersion = rememberSaveable { mutableStateOf("14.0") }

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

    AppTheme {
        MaterialTheme {
            val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
            Scaffold(
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                topBar = { TopAppBar(scrollBehavior) },
                floatingActionButton = {
                    FloatActionButton(
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
                        changeLog
                    )
                },
                floatingActionButtonPosition = FabPosition.End
            ) { padding ->
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .padding(padding)
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LoginCardView()
                    EditTextFields(deviceName, codeName, deviceRegion, systemVersion, androidVersion)
                    MessageCardViews(device, version, codebase, branch, bigVersion)
                    MoreCardViews(fileName, fileSize, changeLog)
                    DownloadCardViews(officialDownload, officialText, cdn1Download, cdn2Download)
                    Text(
                        text = "当前运行在 ${getPlatform().name}!",
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopAppBar(scrollBehavior: TopAppBarScrollBehavior) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "Updater KMM",
                style = MaterialTheme.typography.titleLarge
            )
        },
        navigationIcon = { AboutDialog() },
        actions = {
            IconButton(
                onClick = {
                    //TODO: Login Dialog
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Login,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}

@Composable
private fun FloatActionButton(
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
    changeLog: MutableState<String>
) {
    val coroutineScope = rememberCoroutineScope()
    ExtendedFloatingActionButton(
        onClick = {
            val regionCode = DeviceInfoHelper.regionCode(deviceRegion.value)
            val regionNameExt = DeviceInfoHelper.regionNameExt(deviceRegion.value)
            val codeNameExt = codeName.value + regionNameExt
            val deviceCode = DeviceInfoHelper.deviceCode(androidVersion.value, codeName.value, regionCode)
            coroutineScope.launch {
                val recoveryRomInfo = json.decodeFromString<RomInfoHelper.RomInfo>(
                    getRecoveryRomInfo(
                        codeNameExt,
                        regionCode,
                        systemVersion.value.replace("OS1", "V816").replace("AUTO", deviceCode),
                        androidVersion.value
                    )
                )
                println(recoveryRomInfo)
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

                }
            }
        }) {
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
fun LoginCardView() {
    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Cancel,
                contentDescription = null,
                modifier = Modifier.size(56.dp).padding(12.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "未登录",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "正在使用 v1 接口",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable

fun EditTextFields(
    deviceName: MutableState<String>,
    deviceRegion: MutableState<String>,
    regionCode: MutableState<String>,
    systemVersion: MutableState<String>,
    androidVersion: MutableState<String>
) {

    @Composable
    fun _OutlinedTextField(
        value: String, onValueChange: (String) -> Unit, label: String, leadingIcon: ImageVector
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            leadingIcon = { Icon(imageVector = leadingIcon, null) },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }

    Column(
        modifier = Modifier.background(Color.Unspecified).fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        _OutlinedTextField(
            value = deviceName.value,
            onValueChange = { deviceName.value = it },
            label = "设备名称",
            leadingIcon = Icons.Outlined.Smartphone
        )
        _OutlinedTextField(
            value = deviceRegion.value,
            onValueChange = { deviceRegion.value = it },
            label = "设备代号",
            leadingIcon = Icons.Outlined.DeveloperMode
        )

        val itemsA = listOf("CN", "GL", "EEA", "RU", "TW", "ID", "TR", "IN", "JP", "KR")
        TextFieldWithDropdown(
            text = regionCode,
            items = itemsA, label = "区域代号",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.TravelExplore,
                    contentDescription = null,
                )
            })
        _OutlinedTextField(
            value = systemVersion.value,
            onValueChange = { systemVersion.value = it },
            label = "系统版本",
            leadingIcon = Icons.Outlined.Analytics
        )
        val itemsB = listOf("14.0", "13.0", "12.0", "11.0", "10.0", "9.0", "8.1", "8.0", "7.1", "7.0", "6.0", "5.1", "5.0", "4.4")
        TextFieldWithDropdown(
            text = androidVersion,
            items = itemsB,
            label = "安卓版本",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Android,
                    contentDescription = null,
                )
            }
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
                                text = "v1.0.0",
                                modifier = Modifier,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp).padding(top = 85.dp)
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
                            modifier = Modifier,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            })
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
    leadingIcon: @Composable (() -> Unit)
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        OutlinedTextField(
            value = text.value,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            singleLine = true,
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            leadingIcon = leadingIcon
        )
        DropdownMenu(
            modifier = Modifier.exposedDropdownSize().fillMaxHeight(0.6f),
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            items.forEach { item ->
                DropdownMenuItem(modifier = Modifier.background(Color.Transparent),
                    text = { Text(item) },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    onClick = {
                        text.value = item
                        expanded = false
                    })
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
                modifier = Modifier.fillMaxWidth().padding(10.dp)
            ) {
                MoreTextView("文件名称", fileName.value)
                MoreTextView("文件大小", fileSize.value)
                MoreTextView("更新日志", changeLog.value)
            }
        }
    }

}

@Composable
fun DownloadCardViews(
    officialDownload: MutableState<String>,
    officialText: MutableState<String>,
    cdn1Download: MutableState<String>,
    cdn2Download: MutableState<String>
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
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 16.dp),
                fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                fontWeight = FontWeight.Bold
            )
            DownloadTextView("Official (${officialText.value})", officialDownload.value, officialDownload.value)
            DownloadTextView("CDN (cdnorg)", cdn1Download.value, cdn1Download.value)
            DownloadTextView("CDN (aliyuncs)", cdn2Download.value, cdn2Download.value)
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

    Column(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
        MessageTextView("设备代号", codeName)
        MessageTextView("系统版本", systemVersion)
        MessageTextView("小米版本", xiaomiVersion)
        MessageTextView("安卓版本", androidVersion)
        MessageTextView("分支版本", branchVersion)
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
        modifier = Modifier.fillMaxWidth().padding(6.dp),
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
    text: String
) {
    val content = remember { mutableStateOf("") }
    content.value = text

    Column(
        modifier = Modifier.fillMaxWidth().padding(6.dp)
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
fun DownloadTextView(
    title: String,
    copy: String,
    download: String
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 6.dp),
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
                    //TODO: Download button
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