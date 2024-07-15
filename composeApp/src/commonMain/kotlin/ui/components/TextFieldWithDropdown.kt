package ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuAnchorType.Companion.PrimaryNotEditable
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextFieldWithDropdown(
    text: MutableState<String>,
    items: List<String>,
    label: String,
    leadingIcon: ImageVector
) {
    var isDropdownExpanded by remember { mutableStateOf(false) }

    val hapticFeedback = LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current

    ExposedDropdownMenuBox(
        expanded = isDropdownExpanded,
        onExpandedChange = {
            isDropdownExpanded = it
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        },
    ) {
        OutlinedTextField(
            value = text.value,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.menuAnchor(type = PrimaryNotEditable, enabled = true).fillMaxWidth(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(isDropdownExpanded) },
            leadingIcon = { Icon(imageVector = leadingIcon, null) }
        )
        ExposedDropdownMenu(
            modifier = Modifier.exposedDropdownSize().heightIn(max = 250.dp),
            shape = RoundedCornerShape(10.dp),
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
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        focusManager.clearFocus()
                    }
                )
            }
        }
    }
}
