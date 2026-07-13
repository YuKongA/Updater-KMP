package viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.DeviceListRefreshResult
import data.DeviceListSource
import data.repository.DeviceListRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface DeviceListUpdateState {
    data object Idle : DeviceListUpdateState
    data object Updating : DeviceListUpdateState
    data object Updated : DeviceListUpdateState
    data object NoUpdates : DeviceListUpdateState
    data object Failed : DeviceListUpdateState
}

data class DeviceListUiState(
    val source: DeviceListSource = DeviceListSource.EMBEDDED,
    val version: String = "-",
    val updateState: DeviceListUpdateState = DeviceListUpdateState.Idle,
)

class DeviceListViewModel(
    private val deviceListRepository: DeviceListRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DeviceListUiState())
    val uiState: StateFlow<DeviceListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            deviceListRepository.load()
            loadState()
        }
    }

    private suspend fun loadState() {
        val source = deviceListRepository.source()
        val version = deviceListRepository.cachedVersion() ?: "-"
        _uiState.update { it.copy(source = source, version = version) }
    }

    fun setSource(source: DeviceListSource) {
        viewModelScope.launch {
            deviceListRepository.setSource(source)
            _uiState.update { it.copy(source = source) }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(updateState = DeviceListUpdateState.Updating) }
            val result = deviceListRepository.refresh()
            val newUpdateState = when (result) {
                DeviceListRefreshResult.Success -> {
                    val version = deviceListRepository.cachedVersion() ?: "-"
                    _uiState.update { it.copy(version = version) }
                    DeviceListUpdateState.Updated
                }

                DeviceListRefreshResult.UpToDate -> DeviceListUpdateState.NoUpdates
                DeviceListRefreshResult.Failed -> DeviceListUpdateState.Failed
            }
            _uiState.update { it.copy(updateState = newUpdateState) }
        }
    }

    fun resetUpdateState() {
        _uiState.update { it.copy(updateState = DeviceListUpdateState.Idle) }
    }
}
