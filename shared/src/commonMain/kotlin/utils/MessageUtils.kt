package utils

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import platform.showToast
import platform.useToast
import top.yukonga.miuix.kmp.basic.SnackbarDuration
import top.yukonga.miuix.kmp.basic.SnackbarHost
import top.yukonga.miuix.kmp.basic.SnackbarHostState

class MessageUtils {
    companion object {
        private val snackbarMessage = mutableStateOf("")
        private val snackbarDuration = mutableStateOf(1000L)
        private val snackbarTrigger = mutableStateOf(0)

        fun showMessage(message: String, duration: Long = 1000L) {
            if (useToast()) {
                showToast(message, duration)
            } else {
                snackbarMessage.value = message
                snackbarDuration.value = duration
                snackbarTrigger.value += 1
            }
        }

        @Composable
        fun Snackbar() {
            val snackbarHostState = remember { SnackbarHostState() }
            val trigger by snackbarTrigger

            LaunchedEffect(trigger) {
                if (trigger > 0) {
                    snackbarHostState.showSnackbar(
                        message = snackbarMessage.value,
                        duration = SnackbarDuration.Custom(snackbarDuration.value),
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding(),
                contentAlignment = Alignment.BottomCenter,
            ) {
                SnackbarHost(state = snackbarHostState)
            }
        }
    }
}