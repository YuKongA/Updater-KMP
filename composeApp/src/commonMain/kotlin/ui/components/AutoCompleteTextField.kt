package ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.hapticfeedback.HapticFeedbackType.Companion.LongPress
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import top.yukonga.miuix.kmp.basic.ListPopup
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.extra.DropdownImpl
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.MiuixPopupUtils.Companion.dismissPopup

@Composable
fun AutoCompleteTextField(
    text: MutableState<String>,
    items: List<String>,
    onValueChange: MutableStateFlow<String>,
    label: String
) {
    val listForItems = ArrayList(items)
    val showPopup = remember { mutableStateOf(false) }
    val list = listForItems.filter {
        it.startsWith(text.value, ignoreCase = true)
                || it.contains(text.value, ignoreCase = true)
                || it.replace(" ", "").contains(text.value, ignoreCase = true)
    }.sortedBy { !it.startsWith(text.value, ignoreCase = true) }

    val hapticFeedback = LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .padding(bottom = 12.dp)
            .fillMaxWidth()
    ) {
        TextField(
            insideMargin = DpSize(16.dp, 20.dp),
            value = text.value,
            onValueChange = {
                onValueChange.value = it
                showPopup.value = it.isNotEmpty() && list.isNotEmpty()
                if (it.isEmpty()) dismissPopup(showPopup)
            },
            singleLine = true,
            label = label,
            backgroundColor = MiuixTheme.colorScheme.surface,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
                dismissPopup(showPopup)
            }),
            modifier = Modifier
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        showPopup.value = text.value.isNotEmpty() && list.isNotEmpty()
                    }
                }
        )
        ListPopup(
            show = showPopup,
            onDismissRequest = {
                dismissPopup(showPopup)
            },
            popupPositionProvider = AutoCompletePositionProvider,
            alignment = PopupPositionProvider.Align.TopLeft,
            windowDimming = false,
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
                    dismissPopup(showPopup)
                } else {
                    list.forEach { text ->
                        DropdownImpl(
                            text = text,
                            optionSize = list.size,
                            onSelectedIndexChange = {
                                hapticFeedback.performHapticFeedback(LongPress)
                                onValueChange.value = text
                                KeyboardOptions(imeAction = ImeAction.Done)
                                dismissPopup(showPopup)
                            },
                            isSelected = false,
                            index = list.indexOf(text),
                        )
                    }
                }
            }
        }
    }
}

val AutoCompletePositionProvider = object : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowBounds: IntRect,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
        popupMargin: IntRect,
        alignment: PopupPositionProvider.Align
    ): IntOffset {

        val offsetX: Int = anchorBounds.left
        val offsetY: Int = anchorBounds.bottom + popupMargin.top

        return IntOffset(
            x = offsetX.coerceIn(
                minimumValue = windowBounds.left,
                maximumValue = (windowBounds.right - popupContentSize.width - popupMargin.right).coerceAtLeast(windowBounds.left)
            ),
            y = offsetY.coerceIn(
                minimumValue = (windowBounds.top + popupMargin.top),
                maximumValue = (windowBounds.bottom - popupContentSize.height - popupMargin.bottom).coerceAtLeast(windowBounds.top + popupMargin.top)
            )
        )
    }

    override fun getMargins(): PaddingValues {
        return PaddingValues(horizontal = 20.dp, vertical = 0.dp)
    }
}
