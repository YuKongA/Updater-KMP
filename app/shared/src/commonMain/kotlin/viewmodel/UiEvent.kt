package viewmodel

import org.jetbrains.compose.resources.StringResource

sealed interface UiEvent {
    data class ShowMessage(val resource: StringResource, val duration: Long = 1000L) : UiEvent
}
