package misc

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Snackbar
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
import kotlinx.coroutines.launch

class SnackbarUtil {

    companion object {
        private val snackbarMessage = mutableStateOf("")
        private var isSnackbarVisible = mutableStateOf(false)

        fun showSnackbar(message: String) {
            snackbarMessage.value = message
            isSnackbarVisible.value = true
        }

        fun getSnackbarMessage() = snackbarMessage.value

        fun hideSnackbar() {
            isSnackbarVisible.value = false
        }

        fun isSnackbarVisible() = isSnackbarVisible.value

        @Composable
        fun Snackbar(
            message: String,
            isVisible: Boolean,
            offsetY: Dp
        ) {
            val snackbarHostState = remember { SnackbarHostState() }
            val snackCoroutineScope = rememberCoroutineScope()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = offsetY),
                contentAlignment = Alignment.BottomCenter
            ) {
                SnackbarHost(
                    modifier = Modifier,
                    hostState = snackbarHostState
                ) {
                    Snackbar(
                        snackbarData = it
                    )
                }
            }

            if (isVisible) {
                LaunchedEffect(isVisible) {
                    snackCoroutineScope.launch {
                        snackbarHostState.showSnackbar(message)
                        isSnackbarVisible.value = false
                    }
                }
            } else {
                isSnackbarVisible.value = false
                snackbarHostState.currentSnackbarData?.dismiss()
            }
        }
    }
}