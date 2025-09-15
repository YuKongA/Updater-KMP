package ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import data.DeviceInfoHelper
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.extra.SuperDialog
import top.yukonga.miuix.kmp.extra.SuperDropdown
import top.yukonga.miuix.kmp.theme.MiuixTheme
import updater.composeapp.generated.resources.Res
import updater.composeapp.generated.resources.cancel
import updater.composeapp.generated.resources.device_list_embedded
import updater.composeapp.generated.resources.device_list_remote
import updater.composeapp.generated.resources.device_list_settings
import updater.composeapp.generated.resources.device_list_source
import updater.composeapp.generated.resources.device_list_update_failed
import updater.composeapp.generated.resources.device_list_update_now
import updater.composeapp.generated.resources.device_list_updated
import updater.composeapp.generated.resources.device_list_updating
import updater.composeapp.generated.resources.device_list_version
import utils.DeviceListUtils

@Composable
fun DeviceListDialog(
    showDeviceSettingsDialog: MutableState<Boolean>,
) {
    val coroutinesScope = rememberCoroutineScope()
    val version = remember { mutableStateOf(DeviceListUtils.getCachedVersion() ?: "-") }
    val source = remember { mutableStateOf(DeviceListUtils.getDeviceListSource()) }
    val updateResultMsg = remember(showDeviceSettingsDialog.value) { mutableStateOf("") }

    fun refreshDeviceListInfo() {
        version.value = DeviceListUtils.getCachedVersion() ?: "-"
        source.value = DeviceListUtils.getDeviceListSource()
    }

    SuperDialog(
        show = showDeviceSettingsDialog,
        onDismissRequest = {
            showDeviceSettingsDialog.value = false
        },
        title = stringResource(Res.string.device_list_settings),
        insideMargin = DpSize(0.dp, 24.dp)
    ) {
        SuperDropdown(
            title = stringResource(Res.string.device_list_source),
            items = listOf(
                stringResource(Res.string.device_list_remote),
                stringResource(Res.string.device_list_embedded)
            ),
            selectedIndex = if (source.value == DeviceListUtils.DeviceListSource.REMOTE) 0 else 1,
            onSelectedIndexChange = {
                source.value = if (it == 0) {
                    DeviceListUtils.DeviceListSource.REMOTE
                } else {
                    DeviceListUtils.DeviceListSource.EMBEDDED
                }
                DeviceListUtils.setDeviceListSource(source.value)
                coroutinesScope.launch {
                    DeviceInfoHelper.updateDeviceList()
                    refreshDeviceListInfo()
                }
            },
            insideMargin = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
        )
        AnimatedVisibility(
            visible = source.value == DeviceListUtils.DeviceListSource.REMOTE,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column {
                BasicComponent(
                    title = stringResource(Res.string.device_list_version),
                    rightActions = {
                        Text(
                            text = version.value,
                            fontSize = MiuixTheme.textStyles.body2.fontSize,
                            color = MiuixTheme.colorScheme.onSurfaceVariantActions,
                            textAlign = TextAlign.End,
                        )
                    },
                    insideMargin = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
                )
                val updatingText = stringResource(Res.string.device_list_updating)
                val updatedText = stringResource(Res.string.device_list_updated)
                val updateFailedText = stringResource(Res.string.device_list_update_failed)
                val updateNowText = stringResource(Res.string.device_list_update_now)
                TextButton(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 16.dp),
                    text = updateResultMsg.value.ifEmpty { updateNowText },
                    colors = ButtonDefaults.textButtonColorsPrimary(),
                    onClick = {
                        updateResultMsg.value = updatingText
                        coroutinesScope.launch {
                            val result = DeviceListUtils.updateDeviceList()
                            DeviceInfoHelper.updateDeviceList()
                            refreshDeviceListInfo()
                            updateResultMsg.value = if (result != null && result.isNotEmpty()) updatedText else updateFailedText
                        }
                    },
                    enabled = updateResultMsg.value.isEmpty()
                )
            }
        }
        TextButton(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            text = stringResource(Res.string.cancel),
            onClick = {
                showDeviceSettingsDialog.value = false
            }
        )
    }
}
