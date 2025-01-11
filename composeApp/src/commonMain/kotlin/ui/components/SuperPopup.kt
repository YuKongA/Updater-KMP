package ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsets.Companion
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.BackHandler
import top.yukonga.miuix.kmp.utils.SmoothRoundedCornerShape
import top.yukonga.miuix.kmp.utils.getWindowSize
import ui.components.SuperPopupUtil.Companion.dismissOwnPopup
import ui.components.SuperPopupUtil.Companion.showOwnPopup

@Composable
fun SuperPopup(
    show: MutableState<Boolean>,
    popupModifier: Modifier = Modifier,
    popupPositionProvider: PopupPositionProvider = ListPopupDefaults.ContextMenuPositionProvider,
    onDismissRequest: (() -> Unit)? = null,
    maxHeight: Dp? = null,
    content: @Composable () -> Unit
) {
    var offset by remember { mutableStateOf(IntOffset.Zero) }

    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val getWindowSize = rememberUpdatedState(getWindowSize())
    var windowSize by remember { mutableStateOf(IntSize(getWindowSize.value.width, getWindowSize.value.height)) }

    var parentBounds by remember { mutableStateOf(IntRect.Zero) }
    val windowBounds by rememberUpdatedState(with(density) {
        IntRect(
            left = WindowInsets.displayCutout.asPaddingValues(density).calculateLeftPadding(layoutDirection).roundToPx(),
            top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding().roundToPx(),
            right = windowSize.width -
                    WindowInsets.displayCutout.asPaddingValues(density).calculateRightPadding(layoutDirection).roundToPx(),
            bottom = windowSize.height -
                    WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding().roundToPx() -
                    WindowInsets.captionBar.asPaddingValues().calculateBottomPadding().roundToPx()
        )
    })
    var popupContentSize = IntSize.Zero
    var popupMargin by remember { mutableStateOf(IntRect.Zero) }


    var transformOrigin by remember { mutableStateOf(TransformOrigin.Center) }

    if (!listPopupStates.contains(show)) listPopupStates.add(show)

    LaunchedEffect(show.value) {
        if (show.value) {
            listPopupStates.forEach { state -> if (state != show) state.value = false }
        }
    }

    BackHandler(enabled = show.value) {
        dismissOwnPopup(show)
        onDismissRequest?.let { it1 -> it1() }
    }

    DisposableEffect(Unit) {
        onDispose {
            dismissOwnPopup(show)
            onDismissRequest?.let { it1 -> it1() }
        }
    }

    DisposableEffect(popupPositionProvider) {
        val popupMargins = popupPositionProvider.getMargins()
        popupMargin = with(density) {
            IntRect(
                left = popupMargins.calculateLeftPadding(layoutDirection).roundToPx(),
                top = popupMargins.calculateTopPadding().roundToPx(),
                right = popupMargins.calculateRightPadding(layoutDirection).roundToPx(),
                bottom = popupMargins.calculateBottomPadding().roundToPx()
            )
        }
        if (popupContentSize != IntSize.Zero) {
            offset = popupPositionProvider.calculatePosition(
                parentBounds,
                windowBounds,
                layoutDirection,
                popupContentSize,
                popupMargin
            )
        }
        onDispose {}
    }

    if (show.value) {
        val dropdownElevation by rememberUpdatedState(with(density) { 22.dp.toPx() })
        showOwnPopup(
            transformOrigin = { transformOrigin }
        ) {
            Box(
                modifier = popupModifier
                    .pointerInput(Unit) {
                        detectTapGestures {
                            dismissOwnPopup(show)
                            onDismissRequest?.let { it1 -> it1() }
                        }
                    }
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(
                            constraints.copy(
                                minHeight = 50.dp.roundToPx(),
                                maxHeight = if (maxHeight != null) maxHeight.roundToPx() else windowBounds.height - popupMargin.top - popupMargin.bottom
                            )
                        )
                        popupContentSize = IntSize(placeable.width, placeable.height)
                        offset = popupPositionProvider.calculatePosition(
                            parentBounds,
                            windowBounds,
                            layoutDirection,
                            popupContentSize,
                            popupMargin
                        )
                        layout(constraints.maxWidth, constraints.maxHeight) {
                            placeable.place(offset)
                        }
                    }
            ) {
                Box(
                    Modifier
                        .align(AbsoluteAlignment.TopLeft)
                        .graphicsLayer(
                            clip = true,
                            shape = SmoothRoundedCornerShape(16.dp),
                            shadowElevation = dropdownElevation,
                            ambientShadowColor = MiuixTheme.colorScheme.windowDimming,
                            spotShadowColor = MiuixTheme.colorScheme.windowDimming
                        )
                        .background(MiuixTheme.colorScheme.surface)
                ) {
                    content.invoke()
                }
            }
        }
    }

    Layout(
        content = {},
        modifier = Modifier.onGloballyPositioned { childCoordinates ->
            val parentCoordinates = childCoordinates.parentLayoutCoordinates!!
            val positionInWindow = parentCoordinates.positionInWindow()
            parentBounds = IntRect(
                left = positionInWindow.x.toInt(),
                top = positionInWindow.y.toInt(),
                right = positionInWindow.x.toInt() + parentCoordinates.size.width,
                bottom = positionInWindow.y.toInt() + parentCoordinates.size.height
            )
            val windowHeightPx = getWindowSize.value.height
            val windowWidthPx = getWindowSize.value.width
            windowSize = IntSize(windowWidthPx, windowHeightPx)
            with(density) {
                val xInWindow = parentBounds.left + popupMargin.left + 64.dp.roundToPx()
                val yInWindow = parentBounds.top + parentBounds.height / 2 - 56.dp.roundToPx()
                transformOrigin = TransformOrigin(
                    xInWindow / windowWidthPx.toFloat(),
                    yInWindow / windowHeightPx.toFloat()
                )
            }
        }
    ) { _, _ ->
        layout(0, 0) {}
    }
}

interface PopupPositionProvider {
    /**
     * Calculate the position (offset) of Popup
     *
     * @param anchorBounds Bounds of the anchored (parent) component
     * @param windowBounds Bounds of the safe area of window (excluding the [WindowInsets.Companion.statusBars], [WindowInsets.Companion.navigationBars] and [WindowInsets.Companion.captionBar])
     * @param layoutDirection [LayoutDirection]
     * @param popupContentSize Actual size of the popup content
     * @param popupMargin (Extra) Margins for the popup content. See [PopupPositionProvider.getMargins]
     */
    fun calculatePosition(
        anchorBounds: IntRect,
        windowBounds: IntRect,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
        popupMargin: IntRect,
    ): IntOffset

    /**
     * (Extra) Margins for the popup content.
     */
    fun getMargins(): PaddingValues
}

object ListPopupDefaults {
    val ContextMenuPositionProvider = object : PopupPositionProvider {
        override fun calculatePosition(
            anchorBounds: IntRect,
            windowBounds: IntRect,
            layoutDirection: LayoutDirection,
            popupContentSize: IntSize,
            popupMargin: IntRect
        ): IntOffset {

            val offsetX: Int = anchorBounds.left
            val offsetY: Int = anchorBounds.bottom + popupMargin.top

            return IntOffset(
                x = offsetX.coerceIn(windowBounds.left, windowBounds.right - popupContentSize.width - popupMargin.right),
                y = offsetY.coerceIn(windowBounds.top + popupMargin.top, windowBounds.bottom - popupContentSize.height - popupMargin.bottom)
            )
        }

        override fun getMargins(): PaddingValues {
            return PaddingValues(horizontal = 20.dp, vertical = 0.dp)
        }
    }
}

val listPopupStates = mutableStateListOf<MutableState<Boolean>>()