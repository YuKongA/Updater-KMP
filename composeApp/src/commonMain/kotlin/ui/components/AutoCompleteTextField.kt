package ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MenuAnchorType.Companion.PrimaryEditable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import top.yukonga.miuix.kmp.basic.MiuixTextField
import top.yukonga.miuix.kmp.theme.MiuixTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoCompleteTextField(
    text: MutableState<String>,
    items: List<String>,
    onValueChange: MutableStateFlow<String>,
    label: String
) {
    var isDropdownExpanded by remember { mutableStateOf(false) }

    val hapticFeedback = LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current

    ExposedDropdownMenuBox(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .background(MiuixTheme.colorScheme.secondaryBackground)
            .fillMaxWidth(),
        expanded = isDropdownExpanded,
        onExpandedChange = { isDropdownExpanded = text.value.isNotEmpty() }
    ) {
        MiuixTextField(
            isSecondary = true,
            value = text.value,
            onValueChange = {
                onValueChange.value = it
                isDropdownExpanded = it.isNotEmpty()
            },
            singleLine = true,
            label = label,
            modifier = Modifier
                .menuAnchor(type = PrimaryEditable, enabled = true),
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
            modifier = Modifier
                .exposedDropdownSize()
                .heightIn(max = 250.dp),
            containerColor = MiuixTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(16.dp),
            expanded = isDropdownExpanded && list.isNotEmpty(),
            onDismissRequest = { isDropdownExpanded = false }
        ) {
            list.forEach { text ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = text,
                            color = MiuixTheme.colorScheme.onBackground
                        )
                    },
                    onClick = {
                        onValueChange.value = text
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
