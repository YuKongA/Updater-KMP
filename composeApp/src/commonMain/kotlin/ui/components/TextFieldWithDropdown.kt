package ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MenuAnchorType.Companion.PrimaryNotEditable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.MiuixTextField
import top.yukonga.miuix.kmp.theme.MiuixTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextFieldWithDropdown(
    text: MutableState<String>,
    items: List<String>,
    label: String
) {
    var isDropdownExpanded by remember { mutableStateOf(false) }

    val hapticFeedback = LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current

    ExposedDropdownMenuBox(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth(),
        expanded = isDropdownExpanded,
        onExpandedChange = {
            isDropdownExpanded = it
        },
    ) {
        MiuixTextField(
            insideMargin = DpSize(16.dp, 20.dp),
            value = text.value,
            onValueChange = {},
            label = label,
            readOnly = true,
            singleLine = true,
            backgroundColor = MiuixTheme.colorScheme.primaryContainer,
            modifier = Modifier.menuAnchor(type = PrimaryNotEditable, enabled = true)
        )
        ExposedDropdownMenu(
            modifier = Modifier
                .exposedDropdownSize()
                .heightIn(max = 250.dp),
            containerColor = MiuixTheme.colorScheme.textFieldBg,
            shape = RoundedCornerShape(16.dp),
            expanded = isDropdownExpanded,
            onDismissRequest = { isDropdownExpanded = false },
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    modifier = Modifier.background(Color.Transparent),
                    text = {
                        Text(
                            text = item,
                            color = MiuixTheme.colorScheme.onBackground
                        )
                    },
                    onClick = {
                        text.value = item
                        isDropdownExpanded = false
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        focusManager.clearFocus()
                    }
                )
            }
        }
    }
}
