import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App() {

    AppTheme {
        MaterialTheme {
            val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
            Scaffold(modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection), topBar = { TopAppBar(scrollBehavior) }) { padding ->
                Column(
                    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(padding).padding(horizontal = 24.dp)
                        .verticalScroll(
                            rememberScrollState()
                        ), verticalArrangement = Arrangement.spacedBy(18.dp), horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LoginCardView()
                    EditTextFields()
                    MessageCardViews()
                    MoreCardViews("xxx", "xxx", "xxx")
                    DownloadCardViews()
                    Text("当前运行在 ${getPlatform().name}!", modifier = Modifier.padding(bottom = 24.dp))
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopAppBar(scrollBehavior: TopAppBarScrollBehavior) {
    CenterAlignedTopAppBar(title = { Text(text = "Updater KMM", style = MaterialTheme.typography.titleLarge) }, navigationIcon = {
        AboutDialog()
    }, actions = {
        IconButton(onClick = {
            //TODO: Login Dialog
        }) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.Login, contentDescription = "Action Icon", tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }, scrollBehavior = scrollBehavior
    )
}

@Composable
fun LoginCardView() {
    Card(
        elevation = CardDefaults.cardElevation(2.dp), shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer, containerColor = MaterialTheme.colorScheme.primaryContainer
        ), modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Cancel, contentDescription = "cancel icon", modifier = Modifier.size(56.dp).padding(12.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = "未登录", style = MaterialTheme.typography.bodyLarge)
                Text(text = "正在使用 v1 接口", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable

fun EditTextFields() {
    val text1 = remember { mutableStateOf("Xiaomi 14") }
    val text2 = remember { mutableStateOf("houji") }
    val text3 = remember { mutableStateOf("CN") }
    val text4 = remember { mutableStateOf("OS1.0.36.0.UNCCNXM") }
    val text5 = remember { mutableStateOf("14.0") }

    @Composable
    fun _OutlinedTextField(
        value: String, onValueChange: (String) -> Unit, label: String, leadingIcon: ImageVector
    ) {
        OutlinedTextField(value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            leadingIcon = { Icon(imageVector = leadingIcon, null) },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        )
    }

    Column(
        modifier = Modifier.background(Color.Unspecified).fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        _OutlinedTextField(
            value = text1.value, onValueChange = { text1.value = it }, label = "设备名称", leadingIcon = Icons.Outlined.Smartphone
        )
        _OutlinedTextField(
            value = text2.value, onValueChange = { text2.value = it }, label = "设备代号", leadingIcon = Icons.Outlined.DeveloperMode
        )

        val itemsA = listOf("CN", "GL", "EEA", "RU", "TW", "ID", "TR", "IN", "JP", "KR")
        TextFieldWithDropdown(text = text3, items = itemsA, label = "区域代号", leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.TravelExplore,
                contentDescription = null,
            )
        })
        _OutlinedTextField(
            value = text4.value, onValueChange = { text4.value = it }, label = "系统版本", leadingIcon = Icons.Outlined.Analytics
        )
        val itemsB = listOf(
            "14.0", "13.0", "12.0", "11.0", "10.0", "9.0", "8.1", "8.0", "7.1", "7.0", "6.0", "5.1", "5.0", "4.4"
        )
        TextFieldWithDropdown(text = text5, items = itemsB, label = "安卓版本", leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Android,
                contentDescription = null,
            )
        })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutDialog() {
    var showDialog by remember { mutableStateOf(false) }
    IconButton(onClick = { showDialog = true }) {
        Icon(
            imageVector = Icons.Outlined.Update, contentDescription = "Navigation Icon", tint = MaterialTheme.colorScheme.onSurface
        )
    }

    if (showDialog) {
        BasicAlertDialog(onDismissRequest = { showDialog = false }, content = {
            Box(
                modifier = Modifier.fillMaxWidth().size(280.dp, 155.dp).clip(RoundedCornerShape(30.dp)).background(MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Row(modifier = Modifier.padding(24.dp)) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(50.dp)).background(MaterialTheme.colorScheme.primary)
                    ) {
                        Image(
                            imageVector = Icons.Outlined.Update,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary),
                            contentDescription = "Logo",
                            modifier = Modifier.size(25.dp),
                        )
                    }
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            "Updater KMM", modifier = Modifier, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold
                        )
                        Text(
                            "v1.0.0", modifier = Modifier, style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Column(modifier = Modifier.padding(horizontal = 24.dp).padding(top = 85.dp)) {
                    Row {
                        Text(
                            "在 ", modifier = Modifier, style = MaterialTheme.typography.bodyMedium
                        )
                        GitHubLink()
                        Text(
                            " 查看源码", modifier = Modifier, style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Text(
                        "版权所有 © 2024 YuKongA, AkaneTan", modifier = Modifier, style = MaterialTheme.typography.bodyMedium
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
        text = AnnotatedString("GitHub", SpanStyle(textDecoration = TextDecoration.Underline)), onClick = {
            uriHandler.openUri("https://github.com/YuKongA/Updater-KMM")
        }, style = MaterialTheme.typography.bodyMedium
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextFieldWithDropdown(
    text: MutableState<String>, items: List<String>, label: String, leadingIcon: @Composable (() -> Unit)
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        OutlinedTextField(value = text.value,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
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
fun MessageCardViews() {
    Card(
        colors = CardDefaults.cardColors(
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer, containerColor = MaterialTheme.colorScheme.surfaceContainer
        ), elevation = CardDefaults.cardElevation(2.dp), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)
    ) {
        Column {
            MessageCardView("xxx", "xxx", "xxx", "xxx", "xxx")
        }
    }
}

@Composable
fun MoreCardViews(
    fileName: String,
    fileSize: String,
    changeLog: String,
) {
    Card(
        colors = CardDefaults.cardColors(
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer, containerColor = MaterialTheme.colorScheme.surfaceContainer
        ), elevation = CardDefaults.cardElevation(2.dp), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(10.dp)
        ) {
            MoreTextView("文件名称", fileName)
            MoreTextView("文件大小", fileSize)
            MoreTextView("更新日志", changeLog)
        }
    }
}

@Composable
fun DownloadCardViews(
) {
    Card(
        colors = CardDefaults.cardColors(
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer, containerColor = MaterialTheme.colorScheme.surfaceContainer
        ), elevation = CardDefaults.cardElevation(2.dp), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)
    ) {
        Text(
            "下载链接",
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 0.dp),
            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
            fontWeight = FontWeight.Bold
        )
        DownloadTextView("Official", "xxx", "xxx")
        DownloadTextView("CDN (cdnorg)", "yyy", "yyy")
        DownloadTextView("CDN (aliyuncs)", "zzz", "zzz")
    }
}

@Composable
fun MessageCardView(
    codeName: String, systemVersion: String, xiaomiVersion: String, androidVersion: String, branchVersion: String
) {
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
    title: String, content: String
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(6.dp), horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            title, modifier = Modifier, fontSize = MaterialTheme.typography.bodyMedium.fontSize, fontWeight = FontWeight.Bold
        )
        Text(
            content, modifier = Modifier, fontSize = MaterialTheme.typography.bodyMedium.fontSize
        )
    }
}

@Composable
fun MoreTextView(
    title: String, content: String
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(6.dp)
    ) {
        Text(
            title, modifier = Modifier, fontSize = MaterialTheme.typography.bodyMedium.fontSize, fontWeight = FontWeight.Bold
        )
        Text(
            content, modifier = Modifier, fontSize = MaterialTheme.typography.bodyMedium.fontSize
        )
    }
}

@Composable
fun DownloadTextView(
    title: String, copy: String, download: String
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(
            start = 16.dp, end = 16.dp, top = 0.dp, bottom = 0.dp
        ), horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            title,
            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
            textAlign = TextAlign.Start,
            modifier = Modifier.align(Alignment.CenterVertically).fillMaxWidth(0.5f),
        )
        Row(
            modifier = Modifier, horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = {
                //TODO: Copy button
            }) {
                Text(
                    "复制", modifier = Modifier, fontSize = MaterialTheme.typography.bodyMedium.fontSize
                )
            }
            TextButton(onClick = {
                //TODO: Download button
            }) {
                Text(
                    "下载", modifier = Modifier, fontSize = MaterialTheme.typography.bodyMedium.fontSize
                )
            }
        }
    }
}