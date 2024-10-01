package ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import data.DeviceInfoHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import misc.MessageUtils.Companion.showMessage
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.theme.MiuixTheme
import ui.components.AutoCompleteTextField
import ui.components.TextFieldWithDropdown
import updater.composeapp.generated.resources.Res
import updater.composeapp.generated.resources.android_version
import updater.composeapp.generated.resources.code_name
import updater.composeapp.generated.resources.device_name
import updater.composeapp.generated.resources.regions_code
import updater.composeapp.generated.resources.submit
import updater.composeapp.generated.resources.system_version
import updater.composeapp.generated.resources.toast_no_info

@Composable
fun TextFieldViews(
    deviceName: MutableState<String>,
    codeName: MutableState<String>,
    deviceRegion: MutableState<String>,
    androidVersion: MutableState<String>,
    systemVersion: MutableState<String>,
    updateRomInfo: MutableState<Int>
) {
    val deviceNameFlow = MutableStateFlow(deviceName.value)
    val codeNameFlow = MutableStateFlow(codeName.value)

    val toastNoInfo = stringResource(Res.string.toast_no_info)

    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    coroutineScope.launch {
        deviceNameFlow.collect { newValue ->
            if (deviceName.value != deviceNameFlow.value) {
                val text = DeviceInfoHelper.codeName(newValue)
                if (text != "") codeName.value = text
                deviceName.value = newValue
            }
        }
    }

    coroutineScope.launch {
        codeNameFlow.collect { newValue ->
            if (codeName.value != codeNameFlow.value) {
                val text = DeviceInfoHelper.deviceName(newValue)
                if (text != "") deviceName.value = text
                codeName.value = newValue
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AutoCompleteTextField(
            text = deviceName,
            items = DeviceInfoHelper.deviceNames,
            onValueChange = deviceNameFlow,
            label = stringResource(Res.string.device_name)
        )
        AutoCompleteTextField(
            text = codeName,
            items = DeviceInfoHelper.codeNames,
            onValueChange = codeNameFlow,
            label = stringResource(Res.string.code_name)
        )
        Row {
            TextFieldWithDropdown(
                modifier = Modifier.weight(1f).padding(start = 12.dp, end = 6.dp),
                text = deviceRegion,
                items = DeviceInfoHelper.regionNames,
                label = stringResource(Res.string.regions_code)
            )
            TextFieldWithDropdown(
                modifier = Modifier.weight(1f).padding(start = 6.dp, end = 12.dp),
                text = androidVersion,
                items = DeviceInfoHelper.androidVersions,
                label = stringResource(Res.string.android_version)
            )
        }
        TextField(
            insideMargin = DpSize(16.dp, 20.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            value = systemVersion.value,
            onValueChange = { systemVersion.value = it },
            label = stringResource(Res.string.system_version),
            singleLine = true,
            backgroundColor = MiuixTheme.colorScheme.surface,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                focusManager.clearFocus()
                if (codeName.value != "" && androidVersion.value != "" && systemVersion.value != "") {
                    updateRomInfo.value++
                } else {
                    showMessage(toastNoInfo)
                }
            })
        )
        Button(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            submit = true,
            onClick = {
                if (codeName.value != "" && androidVersion.value != "" && systemVersion.value != "") {
                    updateRomInfo.value++
                } else {
                    showMessage(toastNoInfo)
                }
            },
            text = stringResource(Res.string.submit)
        )
    }
}
