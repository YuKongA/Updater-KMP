package ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import data.DeviceInfoHelper
import data.RemoteDeviceListManager
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import platform.showToast
import top.yukonga.miuix.kmp.basic.*
import top.yukonga.miuix.kmp.theme.MiuixTheme
import updater.composeapp.generated.resources.*

@Composable
fun DeviceListSettingsDialog(
    showDialog: MutableState<Boolean>,
    onDeviceListUpdated: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    var isUpdating by remember { mutableStateOf(false) }
    var updateEnabled by remember { mutableStateOf(RemoteDeviceListManager.isUpdateEnabled()) }
    val lastUpdateTime = remember { mutableStateOf(RemoteDeviceListManager.getLastUpdateTime()) }
    val cachedVersion = remember { mutableStateOf(RemoteDeviceListManager.getCachedVersion()) }

    if (showDialog.value) {
        BasicDialog(
            title = stringResource(Res.string.device_list_settings),
            onDismissRequest = {
                showDialog.value = false
            },
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Auto-update toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(Res.string.enable_auto_update),
                            style = MiuixTheme.textStyles.body1
                        )
                        Switch(
                            checked = updateEnabled,
                            onCheckedChange = { enabled ->
                                updateEnabled = enabled
                                RemoteDeviceListManager.setUpdateEnabled(enabled)
                            }
                        )
                    }

                    // Status information
                    if (updateEnabled) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            cachedVersion.value?.let { version ->
                                Text(
                                    text = "Version: $version",
                                    style = MiuixTheme.textStyles.body2
                                )
                            }
                            
                            lastUpdateTime.value?.let { time ->
                                Text(
                                    text = "Last updated: $time",
                                    style = MiuixTheme.textStyles.body2
                                )
                            }

                            if (cachedVersion.value == null && lastUpdateTime.value == null) {
                                Text(
                                    text = "No cached device list data",
                                    style = MiuixTheme.textStyles.body2
                                )
                            }
                        }
                    }

                    if (isUpdating) {
                        Text(
                            text = "Updating device list...",
                            style = MiuixTheme.textStyles.body2,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Action buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            text = if (updateEnabled && !isUpdating) stringResource(Res.string.update_device_list) else "Close",
                            onClick = {
                                if (!isUpdating && updateEnabled) {
                                    isUpdating = true
                                    coroutineScope.launch {
                                        try {
                                            DeviceInfoHelper.updateDeviceList()
                                            lastUpdateTime.value = RemoteDeviceListManager.getLastUpdateTime()
                                            cachedVersion.value = RemoteDeviceListManager.getCachedVersion()
                                            onDeviceListUpdated()
                                            showToast("Device list updated successfully", 2000)
                                        } catch (e: Exception) {
                                            showToast("Failed to update device list", 2000)
                                        } finally {
                                            isUpdating = false
                                        }
                                    }
                                } else {
                                    showDialog.value = false
                                }
                            }
                        )
                        
                        Button(
                            text = "Clear Cache",
                            onClick = {
                                RemoteDeviceListManager.clearCache()
                                lastUpdateTime.value = null
                                cachedVersion.value = null
                                showToast("Device cache cleared", 2000)
                            }
                        )
                    }
                }
            }
        )
    }
}