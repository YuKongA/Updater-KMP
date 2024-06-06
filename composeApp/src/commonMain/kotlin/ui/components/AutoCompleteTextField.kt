package ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.flow.MutableStateFlow

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
