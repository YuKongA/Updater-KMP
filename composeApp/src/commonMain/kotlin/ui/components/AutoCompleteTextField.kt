package ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MenuAnchorType.Companion.PrimaryEditable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.extra.DropdownImpl
import top.yukonga.miuix.kmp.theme.MiuixTheme
import ui.components.SuperPopupUtil.Companion.dismissOwnPopup

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoCompleteTextField(
    text: MutableState<String>,
    items: List<String>,
    onValueChange: MutableStateFlow<String>,
    label: String
) {
    val listForItems = ArrayList(items)
    val showTopPopup = remember { mutableStateOf(false) }
    val list = listForItems.filter {
        it.startsWith(text.value, ignoreCase = true)
                || it.contains(text.value, ignoreCase = true)
                || it.replace(" ", "").contains(text.value, ignoreCase = true)
    }.sortedBy { !it.startsWith(text.value, ignoreCase = true) }

    LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current

    ExposedDropdownMenuBox(
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .padding(bottom = 12.dp)
            .fillMaxWidth(),
        expanded = showTopPopup.value,
        onExpandedChange = {
            showTopPopup.value = text.value.isNotEmpty()
        }
    ) {
        println("list.isNotEmpty(): ${list.isNotEmpty()}, list: $list")

        TextField(
            insideMargin = DpSize(16.dp, 20.dp),
            value = text.value,
            onValueChange = {
                onValueChange.value = it
                showTopPopup.value = it.isNotEmpty() && list.isNotEmpty()
            },
            singleLine = true,
            label = label,
            backgroundColor = MiuixTheme.colorScheme.surface,
            modifier = Modifier.menuAnchor(type = PrimaryEditable, enabled = true),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
                dismissOwnPopup(showTopPopup)
            })
        )
        SuperPopup(
            show = showTopPopup,
            onDismissRequest = {
                showTopPopup.value = false
            },
            maxHeight = 300.dp
        ) {
            ListPopupColumn {
                if (list.isEmpty()) {
                    DropdownImpl(
                        text = "",
                        optionSize = 1,
                        onSelectedIndexChange = {},
                        isSelected = false,
                        index = 0,
                    ) // Currently needed, fix crash.
                    showTopPopup.value = false
                    dismissOwnPopup(showTopPopup)
                } else {
                    list.forEach { text ->
                        DropdownImpl(
                            text = text,
                            optionSize = list.size,
                            onSelectedIndexChange = {
                                onValueChange.value = text
                                KeyboardOptions(imeAction = ImeAction.Done)
                                focusManager.clearFocus()
                                dismissOwnPopup(showTopPopup)
                                showTopPopup.value = false
                            },
                            isSelected = false,
                            index = 0,
                        )
                    }
                }
            }
        }
    }
}
