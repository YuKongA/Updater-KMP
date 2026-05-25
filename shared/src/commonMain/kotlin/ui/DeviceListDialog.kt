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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import data.DeviceListSource
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import updater.shared.generated.resources.Res
import updater.shared.generated.resources.cancel
import updater.shared.generated.resources.device_list_embedded
import updater.shared.generated.resources.device_list_no_updates
import updater.shared.generated.resources.device_list_remote
import updater.shared.generated.resources.device_list_settings
import updater.shared.generated.resources.device_list_source
import updater.shared.generated.resources.device_list_update_failed
import updater.shared.generated.resources.device_list_update_now
import updater.shared.generated.resources.device_list_updated
import updater.shared.generated.resources.device_list_updating
import updater.shared.generated.resources.device_list_version
import viewmodel.DeviceListUpdateState

@Composable
fun DeviceListDialog(
    show: Boolean,
    source: DeviceListSource,
    version: String,
    updateState: DeviceListUpdateState,
    onSourceChange: (DeviceListSource) -> Unit,
    onRefresh: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    val updateButtonText = when (updateState) {
        DeviceListUpdateState.Idle -> stringResource(Res.string.device_list_update_now)
        DeviceListUpdateState.Updating -> stringResource(Res.string.device_list_updating)
        DeviceListUpdateState.Updated -> stringResource(Res.string.device_list_updated)
        DeviceListUpdateState.NoUpdates -> stringResource(Res.string.device_list_no_updates)
        DeviceListUpdateState.Failed -> stringResource(Res.string.device_list_update_failed)
    }

    OverlayDialog(
        show = show,
        title = stringResource(Res.string.device_list_settings),
        onDismissRequest = onDismissRequest,
        insideMargin = DpSize(0.dp, 24.dp),
        content = {
            OverlayDropdownPreference(
                title = stringResource(Res.string.device_list_source),
                items = listOf(
                    stringResource(Res.string.device_list_remote),
                    stringResource(Res.string.device_list_embedded)
                ),
                selectedIndex = if (source == DeviceListSource.REMOTE) 0 else 1,
                onSelectedIndexChange = { index ->
                    val next = if (index == 0) DeviceListSource.REMOTE else DeviceListSource.EMBEDDED
                    onSourceChange(next)
                },
                insideMargin = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
            )
            AnimatedVisibility(
                visible = source == DeviceListSource.REMOTE,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    BasicComponent(
                        title = stringResource(Res.string.device_list_version),
                        endActions = {
                            Text(
                                text = version,
                                fontSize = MiuixTheme.textStyles.body2.fontSize,
                                color = MiuixTheme.colorScheme.onSurfaceVariantActions,
                                textAlign = TextAlign.End,
                            )
                        },
                        insideMargin = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
                    )
                    TextButton(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 16.dp),
                        text = updateButtonText,
                        colors = ButtonDefaults.textButtonColorsPrimary(),
                        onClick = onRefresh,
                        enabled = updateState == DeviceListUpdateState.Idle,
                    )
                }
            }
            TextButton(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                text = stringResource(Res.string.cancel),
                onClick = onDismissRequest,
            )
        })
}
