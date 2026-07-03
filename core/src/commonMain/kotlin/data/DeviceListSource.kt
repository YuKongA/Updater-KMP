package data

enum class DeviceListSource { REMOTE, EMBEDDED }

sealed interface DeviceListRefreshResult {
    data object Success : DeviceListRefreshResult
    data object UpToDate : DeviceListRefreshResult
    data object Failed : DeviceListRefreshResult
}
