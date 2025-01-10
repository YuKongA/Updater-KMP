package ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.TransformOrigin.Companion
import androidx.compose.ui.zIndex
import top.yukonga.miuix.kmp.anim.DecelerateEasing
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * A util class for show popup and dialog.
 */
class SuperPopupUtil {

    companion object {
        private var isPopupShowing = mutableStateOf(false)
        private var popupContext = mutableStateOf<(@Composable () -> Unit)?>(null)
        private var popupTransformOrigin = mutableStateOf({ TransformOrigin.Center })


        /**
         * Show a popup.
         *
         * @param transformOrigin The pivot point in terms of fraction of the overall size,
         *   used for scale transformations. By default it's [TransformOrigin.Center].
         * @param content The [Composable] content of the popup.
         */
        @Composable
        fun showOwnPopup(
            transformOrigin: (() -> TransformOrigin) = { TransformOrigin.Center },
            content: (@Composable () -> Unit)? = null,
        ) {
            if (isPopupShowing.value) return
            popupTransformOrigin.value = transformOrigin
            isPopupShowing.value = true
            popupContext.value = content
        }

        /**
         * Dismiss the popup.
         *
         * @param show The show state of the popup.
         */
        fun dismissOwnPopup(
            show: MutableState<Boolean>,
        ) {
            isPopupShowing.value = false
            show.value = false
        }

        /**
         * A host for show popup and dialog. Already added to the [Scaffold] by default.
         */
        @Composable
        fun SuperPopupHost() {
            AnimatedVisibility(
                visible = isPopupShowing.value,
                modifier = Modifier.zIndex(2f).fillMaxSize(),
                enter = fadeIn(
                    animationSpec = tween(150, easing = DecelerateEasing(1.5f))
                ) + scaleIn(
                    initialScale = 0.8f,
                    animationSpec = tween(150, easing = DecelerateEasing(1.5f)),
                    transformOrigin = popupTransformOrigin.value.invoke()
                ),
                exit = fadeOut(
                    animationSpec = tween(150, easing = DecelerateEasing(1.5f))
                ) + scaleOut(
                    targetScale = 0.8f,
                    animationSpec = tween(150, easing = DecelerateEasing(1.5f)),
                    transformOrigin = popupTransformOrigin.value.invoke()
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    popupContext.value?.invoke()
                }
            }
        }
    }
}