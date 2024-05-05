import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Android
import androidx.compose.material.icons.outlined.DeveloperMode
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                TopAppBar()
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    LoginCardView()
                    EditTextFields()
                    MessageCardViews()
                    MoreCardViews("xxx", "xxx", "xxx")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar() {
    CenterAlignedTopAppBar(colors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
    ), title = {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Updater KMM", style = MaterialTheme.typography.titleLarge)
        }
    }, navigationIcon = {
        IconButton(onClick = { /* Handle navigation icon click */ }) {
            Icon(
                imageVector = Icons.Outlined.Update,
                contentDescription = "Navigation Icon",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }, actions = {
        IconButton(onClick = { /* Handle action icon click */ }) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.Login,
                contentDescription = "Action Icon",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    })
}

@Composable
fun LoginCardView() {
    Card(
        shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(
            contentColor = MaterialTheme.colorScheme.primaryContainer,
            containerColor = MaterialTheme.colorScheme.onPrimaryContainer
        ), modifier = Modifier.fillMaxWidth().padding(
            start = 16.dp, end = 16.dp, top = 4.dp, bottom = 0.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Cancel,
                contentDescription = "cancel icon",
                modifier = Modifier.size(56.dp).padding(12.dp)
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
    val text1 = remember { mutableStateOf("") }
    val text2 = remember { mutableStateOf("") }
    val text3 = remember { mutableStateOf("") }
    val text4 = remember { mutableStateOf("") }
    val text5 = remember { mutableStateOf("") }

    OutlinedTextField(value = text1.value,
        onValueChange = { text1.value = it },
        label = { Text("设备名称") },
        modifier = Modifier.fillMaxWidth().padding(
            start = 16.dp, end = 16.dp, top = 4.dp, bottom = 0.dp
        ),
        shape = RoundedCornerShape(10.dp),
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Smartphone,
                contentDescription = null,
            )
        })
    OutlinedTextField(value = text2.value,
        onValueChange = { text2.value = it },
        label = { Text("设备代号") },
        modifier = Modifier.fillMaxWidth().padding(
            start = 16.dp, end = 16.dp, top = 4.dp, bottom = 0.dp
        ),
        shape = RoundedCornerShape(10.dp),
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.DeveloperMode,
                contentDescription = null,
            )
        })
    val itemsA = listOf("CN", "GL", "EEA", "RU", "TW", "ID", "TR", "IN", "JP", "KR")
    TextFieldWithDropdown(text = text3, items = itemsA, label = "区域代号", leadingIcon = {
        Icon(
            imageVector = Icons.Outlined.TravelExplore,
            contentDescription = null,
        )
    })
    OutlinedTextField(value = text4.value,
        onValueChange = { text4.value = it },
        label = { Text("系统版本") },
        modifier = Modifier.fillMaxWidth().padding(
            start = 16.dp, end = 16.dp, top = 4.dp, bottom = 0.dp
        ),
        shape = RoundedCornerShape(10.dp),
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Analytics,
                contentDescription = null,
            )
        })
    val itemsB = listOf(
        "14.0",
        "13.0",
        "12.0",
        "11.0",
        "10.0",
        "9.0",
        "8.1",
        "8.0",
        "7.1",
        "7.0",
        "6.0",
        "5.1",
        "5.0",
        "4.4"
    )
    TextFieldWithDropdown(text = text5, items = itemsB, label = "安卓版本", leadingIcon = {
        Icon(
            imageVector = Icons.Outlined.Android,
            contentDescription = null,
        )
    })
}

@Composable
fun TextFieldWithDropdown(
    text: MutableState<String>,
    items: List<String>,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit),
    onItemSelected: (String) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf("") }
    val interactionSource = remember { MutableInteractionSource() }

    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(value = text.value,
            onValueChange = {
                text.value = it
            },
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth().padding(
                start = 16.dp, end = 16.dp, top = 4.dp, bottom = 0.dp
            ),
            interactionSource = interactionSource,
            shape = RoundedCornerShape(10.dp),
            leadingIcon = leadingIcon,
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                        contentDescription = null
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxHeight(0.4f)
                ) {
                    items.forEach { item ->
                        DropdownMenuItem(text = { Text(item) }, onClick = {
                            item.let {
                                selectedText = it
                                text.value = it
                                expanded = false
                                onItemSelected(it)
                            }
                        })
                    }
                }
            })
    }
}

@Composable
fun MessageCardViews() {
    Card(
        modifier = Modifier.fillMaxWidth().padding(
            start = 16.dp, end = 16.dp, top = 12.dp, bottom = 0.dp
        ), shape = RoundedCornerShape(10.dp)
    ) {
        Column {
            MessageCardView("xxx", "xxx", "xxx", "xxx", "xxx")
        }
    }
}

@Composable
fun MoreCardViews(
    codeName: String,
    systemVersion: String,
    xiaomiVersion: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(
            start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp
        ), shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(start = 10.dp, end = 10.dp, top = 10.dp, bottom = 10.dp)
        ) {
            MoreTextView("文件名称", codeName)
            MoreTextView("文件大小", systemVersion)
            MoreTextView("更新日志", xiaomiVersion)
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
    Card(
        modifier = Modifier.fillMaxWidth()
            .padding(start = 10.dp, end = 10.dp, top = 10.dp, bottom = 10.dp)
    ) {
        Column {
            MessageTextView("设备代号", codeName)
            MessageTextView("系统版本", systemVersion)
            MessageTextView("小米版本", xiaomiVersion)
            MessageTextView("安卓版本", androidVersion)
            MessageTextView("分支版本", branchVersion)
        }
    }
}

@Composable
fun MessageTextView(
    title: String, content: String
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            title,
            modifier = Modifier,
            fontSize = MaterialTheme.typography.bodyLarge.fontSize,
            fontWeight = FontWeight(500)
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
            title,
            modifier = Modifier,
            fontSize = MaterialTheme.typography.bodyLarge.fontSize,
            fontWeight = FontWeight(500)
        )
        Text(
            content, modifier = Modifier, fontSize = MaterialTheme.typography.bodyMedium.fontSize
        )
    }
}