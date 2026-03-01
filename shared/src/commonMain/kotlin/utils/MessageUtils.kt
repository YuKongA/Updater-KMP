package utils

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import platform.showToast
import platform.useToast
import top.yukonga.miuix.kmp.basic.SnackbarDuration
import top.yukonga.miuix.kmp.basic.SnackbarHost
import top.yukonga.miuix.kmp.basic.SnackbarHostState

class MessageUtils {
    companion object {
        private val snackbarMessage = mutableStateOf("")
        private var snackbarDuration = mutableStateOf(1000L)
        private var isSnackbarVisible = mutableStateOf(false)

        fun showMessage(message: String, duration: Long = 1000L) {
            if (useToast()) {
                showToast(message, duration)
            } else {
                snackbarMessage.value = message
                snackbarDuration.value = duration
                isSnackbarVisible.value = true
            }
        }

        @Composable
        fun Snackbar() {
            val snackbarHostState = remember { SnackbarHostState() }
            val snackCoroutineScope = rememberCoroutineScope()

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding(),
                contentAlignment = Alignment.BottomCenter
            ) {
                SnackbarHost(
                    state = snackbarHostState
                )
            }
            if (snackbarMessage.value.isNotEmpty()) {
                snackCoroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = snackbarMessage.value,
                        duration = SnackbarDuration.Custom(snackbarDuration.value)
                    )
                    isSnackbarVisible.value = false
                }
            }
        }
    }
}