package ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuAnchorType.Companion.PrimaryEditable
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
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
    val selectionRange = remember { mutableStateOf(TextRange(text.value.length)) }

    val hapticFeedback = LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current

    ExposedDropdownMenuBox(
        expanded = isDropdownExpanded,
        onExpandedChange = { isDropdownExpanded = text.value.isNotEmpty() }
    ) {
        OutlinedTextField(
            value = TextFieldValue(text.value, selectionRange.value),
            onValueChange = {
                onValueChange.value = it.text
                selectionRange.value = it.selection
                isDropdownExpanded = it.text.isNotEmpty()
            },
            singleLine = true,
            label = { Text(label) },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.menuAnchor(type = PrimaryEditable, enabled = true).fillMaxWidth(),
            leadingIcon = { Icon(imageVector = leadingIcon, null) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                isDropdownExpanded = false
                focusManager.clearFocus()
            })
        )
        val listForItems = ArrayList(items)
        val list = listForItems.filter {
            it.startsWith(text.value, ignoreCase = true) || it.contains(text.value, ignoreCase = true)
                    || it.replace(" ", "").contains(text.value, ignoreCase = true)
        }.sortedBy {
            !it.startsWith(text.value, ignoreCase = true)
        }
        ExposedDropdownMenu(
            modifier = Modifier.exposedDropdownSize().heightIn(max = 250.dp).imePadding(),
            shape = RoundedCornerShape(10.dp),
            expanded = isDropdownExpanded && list.isNotEmpty(),
            onDismissRequest = { isDropdownExpanded = false }
        ) {
            list.forEach { text ->
                DropdownMenuItem(
                    text = { Text(text) },
                    onClick = {
                        onValueChange.value = text
                        selectionRange.value = TextRange(text.length)
                        isDropdownExpanded = false
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        KeyboardOptions(imeAction = ImeAction.Done)
                        focusManager.clearFocus()
                    }
                )
            }
        }
    }
}
