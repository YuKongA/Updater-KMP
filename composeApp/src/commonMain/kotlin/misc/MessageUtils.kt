package misc

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import perfGet
import showExtToast
import showToast
import useToast

class MessageUtils {

    companion object {
        private val snackbarMessage = mutableStateOf("")
        private var snackbarDuration = mutableStateOf(1000L)
        private var isSnackbarVisible = mutableStateOf(false)
        private var snackbarCoroutineJob: Job? = null
        private var snackbarKey = mutableStateOf(0)

        fun showMessage(message: String, duration: Long = 1000L) {
            if (useToast()) {
                if (perfGet("isUseMiuiStrongToast") == "true") {
                    showExtToast(message, duration)
                } else {
                    showToast(message, duration)
                }
            } else {
                snackbarCoroutineJob?.cancel()
                snackbarMessage.value = message
                snackbarDuration.value = duration
                isSnackbarVisible.value = true
                snackbarKey.value++
            }
        }

        @Composable
        fun Snackbar(
            offsetY: Dp
        ) {
            val snackbarHostState = remember { SnackbarHostState() }
            val snackCoroutineScope = rememberCoroutineScope()

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 56.dp + 18.dp) // FAB + FAB Padding
                    .offset(y = offsetY)
                    .systemBarsPadding(),
                contentAlignment = Alignment.BottomCenter
            ) {
                SnackbarHost(
                    hostState = snackbarHostState
                ) {
                    Snackbar(
                        snackbarData = it
                    )
                }
            }
            if (snackbarMessage.value.isNotEmpty()) {
                LaunchedEffect(snackbarKey.value) {
                    snackbarCoroutineJob = snackCoroutineScope.launch {
                        snackbarHostState.showSnackbar(message = snackbarMessage.value, duration = SnackbarDuration.Indefinite)
                        isSnackbarVisible.value = false
                    }
                    delay(snackbarDuration.value)
                    snackbarHostState.currentSnackbarData?.dismiss()
                }
            }
        }
    }
}