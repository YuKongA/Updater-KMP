import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import updaterkmm.composeapp.generated.resources.Res
import updaterkmm.composeapp.generated.resources.ic_cancel
import updaterkmm.composeapp.generated.resources.ic_login
import updaterkmm.composeapp.generated.resources.ic_update

@Composable
@Preview
fun App() {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Column {
                MyAppBar()
                MyCardView()
                MyTextFields()
            }
        }
    }
}

@OptIn(ExperimentalResourceApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MyAppBar() {
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
                painter = painterResource(Res.drawable.ic_update),
                contentDescription = "Navigation Icon",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }, actions = {
        IconButton(onClick = { /* Handle action icon click */ }) {
            Icon(
                painter = painterResource(Res.drawable.ic_login),
                contentDescription = "Action Icon",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    })
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun MyCardView() {
    Card(
        shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(
            contentColor = MaterialTheme.colorScheme.primaryContainer,
            containerColor = MaterialTheme.colorScheme.onPrimaryContainer
        ), modifier = Modifier.fillMaxWidth().padding(
            start = 16.dp, end = 16.dp, top = 12.dp, bottom = 2.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_cancel),
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

fun MyTextFields() {
    val text1 = remember { mutableStateOf("") }
    val text2 = remember { mutableStateOf("") }
    val text3 = remember { mutableStateOf("") }
    val text4 = remember { mutableStateOf("") }
    val text5 = remember { mutableStateOf("") }

    OutlinedTextField(value = text1.value,
        onValueChange = { text1.value = it },
        label = { Text("设备名称") },
        modifier = Modifier.fillMaxWidth().padding(
            start = 16.dp, end = 16.dp, top = 2.dp, bottom = 2.dp
        ),
        shape = RoundedCornerShape(10.dp)
    )
    OutlinedTextField(value = text2.value,
        onValueChange = { text2.value = it },
        label = { Text("设备代号") },
        modifier = Modifier.fillMaxWidth().padding(
            start = 16.dp, end = 16.dp, top = 2.dp, bottom = 2.dp
        ),
        shape = RoundedCornerShape(10.dp)
    )
    val items = listOf("Item1", "Item2", "Item3", "Item4", "Item5")
    AutoCompleteTextFieldB(items = items, label = "区域代号") { selected ->
        println("Selected item: $selected")
    }
    OutlinedTextField(value = text4.value,
        onValueChange = { text4.value = it },
        label = { Text("系统版本") },
        modifier = Modifier.fillMaxWidth().padding(
            start = 16.dp, end = 16.dp, top = 2.dp, bottom = 2.dp
        ),
        shape = RoundedCornerShape(10.dp)
    )
    OutlinedTextField(value = text5.value,
        onValueChange = { text5.value = it },
        label = { Text("安卓版本") },
        modifier = Modifier.fillMaxWidth().padding(
            start = 16.dp, end = 16.dp, top = 2.dp, bottom = 2.dp
        ),
        shape = RoundedCornerShape(10.dp)
    )
}

@Composable
fun AutoCompleteTextFieldB(
    items: List<String>,
    label: String,
    modifier: Modifier = Modifier,
    onItemSelected: (String) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf("") }
    var textFieldValue by remember { mutableStateOf("") }
    val interactionSource = remember { MutableInteractionSource() }

    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(value = textFieldValue,
            onValueChange = {
                textFieldValue = it
            },
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth().padding(
                start = 16.dp, end = 16.dp, top = 2.dp, bottom = 2.dp
            ),
            interactionSource = interactionSource,
            shape = RoundedCornerShape(10.dp),
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    items.forEach { item ->
                        DropdownMenuItem(text = { Text(item) }, onClick = {
                            item.let {
                                selectedText = it
                                textFieldValue = it
                                expanded = false
                                onItemSelected(it)
                            }
                        })
                    }
                }
            })

    }
}