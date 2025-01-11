package ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.extra.DropDownMode
import top.yukonga.miuix.kmp.extra.SpinnerEntry
import top.yukonga.miuix.kmp.extra.SpinnerMode
import top.yukonga.miuix.kmp.extra.SuperDropdown
import top.yukonga.miuix.kmp.extra.SuperSpinner
import top.yukonga.miuix.kmp.theme.MiuixTheme
import ui.components.AutoCompleteTextField
import updater.composeapp.generated.resources.Res
import updater.composeapp.generated.resources.android_version
import updater.composeapp.generated.resources.code_name
import updater.composeapp.generated.resources.device_name
import updater.composeapp.generated.resources.regions_code
import updater.composeapp.generated.resources.search_history
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
    updateRomInfo: MutableState<Int>,
    searchKeywords: MutableState<List<String>>,
    searchKeywordsSelected: MutableState<Int>
) {
    val androidVersionSelected = remember { mutableStateOf(0) }
    val regionSelected = remember { mutableStateOf(0) }

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
        Card(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .padding(bottom = 12.dp)
        ) {
            SuperDropdown(
                title = stringResource(Res.string.android_version),
                items = DeviceInfoHelper.androidVersions,
                selectedIndex = androidVersionSelected.value,
                onSelectedIndexChange = { index ->
                    focusManager.clearFocus()
                    androidVersionSelected.value = index
                    androidVersion.value = DeviceInfoHelper.androidVersions[index]
                },
                mode = DropDownMode.AlwaysOnRight,
                maxHeight = 280.dp
            )
            SuperDropdown(
                title = stringResource(Res.string.regions_code),
                items = DeviceInfoHelper.regionNames,
                selectedIndex = regionSelected.value,
                onSelectedIndexChange = { index ->
                    regionSelected.value = index
                    deviceRegion.value = DeviceInfoHelper.regionNames[index]
                },
                mode = DropDownMode.AlwaysOnRight,
                maxHeight = 280.dp
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
        if (searchKeywords.value.isNotEmpty()) {
            val spinnerOptions = searchKeywords.value.map { keyword ->
                val parts = keyword.split("-")
                SpinnerEntry(
                    icon = null,
                    title = "${parts[0].ifEmpty { "Unknown" }} (${parts[1]})",
                    summary = "${parts[2]}-${parts[3]}-${parts[4]}",
                )
            }
            Card(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .padding(vertical = 12.dp)
            ) {
                SuperSpinner(
                    title = stringResource(Res.string.search_history),
                    items = spinnerOptions,
                    selectedIndex = searchKeywordsSelected.value,
                    mode = SpinnerMode.AlwaysOnRight,
                    showValue = false,
                    onSelectedIndexChange = { index ->
                        focusManager.clearFocus()
                        val parts = searchKeywords.value[index].split("-")
                        deviceName.value = parts[0]
                        codeName.value = parts[1]
                        deviceRegion.value = parts[2]
                        androidVersion.value = parts[3]
                        systemVersion.value = parts[4]
                        searchKeywordsSelected.value = index
                        regionSelected.value = DeviceInfoHelper.regionNames.indexOf(parts[2])
                        androidVersionSelected.value = DeviceInfoHelper.androidVersions.indexOf(parts[3])
                    },
                    maxHeight = 280.dp
                )
            }
        }
        TextButton(
            modifier = if (searchKeywords.value.isNotEmpty()) {
                Modifier
            } else {
                Modifier.padding(top = 12.dp)
            }
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            colors = ButtonDefaults.textButtonColorsPrimary(),
            onClick = {
                focusManager.clearFocus()
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
