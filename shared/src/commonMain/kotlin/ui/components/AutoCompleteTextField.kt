package ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.hapticfeedback.HapticFeedbackType.Companion.LongPress
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import top.yukonga.miuix.kmp.basic.DropdownColors
import top.yukonga.miuix.kmp.basic.DropdownDefaults
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.extra.SuperListPopup
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.math.min

@Composable
fun AutoCompleteTextField(
    text: MutableState<String>,
    items: List<String>,
    onValueChange: MutableStateFlow<String>,
    label: String
) {
    val filteredList = remember(text.value, items) {
        items.filter {
            it.startsWith(text.value, ignoreCase = true)
                    || it.contains(text.value, ignoreCase = true)
                    || it.replace(" ", "").contains(text.value, ignoreCase = true)
        }.sortedBy { !it.startsWith(text.value, ignoreCase = true) }
    }
    var isFocused by remember { mutableStateOf(false) }
    val showPopup = remember { mutableStateOf(false) }
    val hapticFeedback = LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(isFocused, onValueChange.collectAsState().value) {
        showPopup.value = isFocused && text.value.isNotEmpty()
    }

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
            },
            singleLine = true,
            label = label,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
                showPopup.value = false
            }),
            modifier = Modifier.onFocusChanged { focusState ->
                isFocused = focusState.isFocused
            }
        )
        SuperListPopup(
            show = showPopup,
            onDismissRequest = {
                focusManager.clearFocus()
                showPopup.value = false
            },
            popupPositionProvider = AutoCompletePositionProvider,
            alignment = PopupPositionProvider.Align.TopStart,
            enableWindowDim = false,
            maxHeight = 280.dp
        ) {
            AutoCompleteListPopupColumn {
                if (filteredList.isNotEmpty()) {
                    filteredList.forEachIndexed { index, item ->
                        AutoCompleteDropdownImpl(
                            text = item,
                            optionSize = filteredList.size,
                            onSelectedIndexChange = {
                                hapticFeedback.performHapticFeedback(LongPress)
                                onValueChange.value = item
                                focusManager.clearFocus()
                                showPopup.value = false
                            },
                            isSelected = false,
                            index = index,
                        )
                    }
                } else {
                    AutoCompleteDropdownImpl(
                        text = null,
                        optionSize = 0,
                        onSelectedIndexChange = {},
                        isSelected = false,
                        index = 0,
                    )
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

@Composable
fun AutoCompleteListPopupColumn(
    content: @Composable () -> Unit
) {
    SubcomposeLayout(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .animateContentSize(
                spring(
                    stiffness = 8000f,
                    visibilityThreshold = IntSize.VisibilityThreshold
                )
            )
    ) { constraints ->
        var listHeight = 0
        val tempConstraints = constraints.copy(minWidth = 0, maxWidth = 288.dp.roundToPx(), minHeight = 0)
        val listWidth = subcompose("miuixPopupListFake", content).map {
            it.measure(tempConstraints)
        }.maxOf { it.width }.coerceIn(0, 288.dp.roundToPx())
        val childConstraints = constraints.copy(minWidth = listWidth, maxWidth = listWidth, minHeight = 0)
        val placeables = subcompose("miuixPopupListReal", content).map {
            val placeable = it.measure(childConstraints)
            listHeight += placeable.height
            placeable
        }
        layout(listWidth, min(constraints.maxHeight, listHeight)) {
            var height = 0
            placeables.forEach {
                it.place(0, height)
                height += it.height
            }
        }
    }
}

@Composable
fun AutoCompleteDropdownImpl(
    text: String?,
    optionSize: Int,
    isSelected: Boolean,
    index: Int,
    dropdownColors: DropdownColors = DropdownDefaults.dropdownColors(),
    onSelectedIndexChange: (Int) -> Unit
) {
    val additionalTopPadding = if (index == 0) 20f.dp else 12f.dp
    val additionalBottomPadding = if (index == optionSize - 1) 20f.dp else 12f.dp
    val textColor = if (isSelected) {
        dropdownColors.selectedContentColor
    } else {
        dropdownColors.contentColor
    }
    val backgroundColor = if (isSelected) {
        dropdownColors.selectedContainerColor
    } else {
        dropdownColors.containerColor
    }

    if (text != null) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .clickable {
                    onSelectedIndexChange(index)
                }
                .background(backgroundColor)
                .padding(horizontal = 20.dp)
                .padding(top = additionalTopPadding, bottom = additionalBottomPadding)
        ) {
            Text(
                text = text,
                fontSize = MiuixTheme.textStyles.body1.fontSize,
                fontWeight = FontWeight.Medium,
                color = textColor,
            )
        }
    } else {
        Box(
            modifier = Modifier
                .padding(horizontal = 20.dp)
        ) {}
    }
}
