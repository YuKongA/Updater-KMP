package ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import data.DeviceInfoHelper
import kotlinx.coroutines.flow.MutableStateFlow
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
import updater.composeapp.generated.resources.carrier_code
import updater.composeapp.generated.resources.code_name
import updater.composeapp.generated.resources.device_name
import updater.composeapp.generated.resources.region_code
import updater.composeapp.generated.resources.search_history
import updater.composeapp.generated.resources.submit
import updater.composeapp.generated.resources.system_version
import updater.composeapp.generated.resources.toast_no_info

@Composable
private fun SearchHistoryView(
    searchKeywords: MutableState<List<String>>,
    searchKeywordsSelected: MutableState<Int>,
    onHistorySelect: (String) -> Unit
) {
    AnimatedVisibility(
        visible = searchKeywords.value.isNotEmpty(),
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        val localFocusManager = LocalFocusManager.current
        val spinnerOptions = searchKeywords.value.map { keyword ->
            val parts = keyword.split("-")
            SpinnerEntry(
                icon = null,
                title = "${parts.getOrElse(0) { "" }.ifEmpty { "Unknown" }} (${parts.getOrElse(1) { "" }})",
                summary = "${parts.getOrElse(2) { "" }}-${parts.getOrElse(3) { "" }}-${parts.getOrElse(4) { "" }}-${parts.getOrElse(5) { "" }}",
            )
        }
        Card(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .padding(top = 12.dp)
        ) {
            SuperSpinner(
                title = stringResource(Res.string.search_history),
                items = spinnerOptions,
                selectedIndex = searchKeywordsSelected.value,
                mode = SpinnerMode.AlwaysOnRight,
                showValue = false,
                onSelectedIndexChange = { index ->
                    onHistorySelect(searchKeywords.value[index])
                    searchKeywordsSelected.value = index
                },
                onClick = {
                    localFocusManager.clearFocus()
                },
                maxHeight = 280.dp
            )
        }
    }
}

@Composable
fun BasicViews(
    deviceName: MutableState<String>,
    codeName: MutableState<String>,
    androidVersion: MutableState<String>,
    deviceRegion: MutableState<String>,
    deviceCarrier: MutableState<String>,
    systemVersion: MutableState<String>,
    updateRomInfo: MutableState<Int>,
    searchKeywords: MutableState<List<String>>,
    searchKeywordsSelected: MutableState<Int>,
) {
    val androidVersionSelected = remember {
        mutableStateOf(DeviceInfoHelper.androidVersions.indexOf(androidVersion.value).takeIf { it >= 0 } ?: 0)
    }
    val regionSelected = remember {
        mutableStateOf(DeviceInfoHelper.regionNames.indexOf(deviceRegion.value).takeIf { it >= 0 } ?: 0)
    }

    val carrierSelected = remember {
        mutableStateOf(DeviceInfoHelper.carrierNames.indexOf(deviceCarrier.value).takeIf { it >= 0 } ?: 0)
    }

    val deviceNames by DeviceInfoHelper.deviceNamesFlow.collectAsState()
    val codeNames by DeviceInfoHelper.codeNamesFlow.collectAsState()

    val deviceNameFlow = remember { MutableStateFlow(deviceName.value) }
    val codeNameFlow = remember { MutableStateFlow(codeName.value) }

    val toastNoInfo = stringResource(Res.string.toast_no_info)

    val focusManager = LocalFocusManager.current

    LaunchedEffect(deviceNameFlow) {
        deviceNameFlow.collect { newValue ->
            if (deviceName.value != newValue) {
                deviceName.value = newValue
                val text = DeviceInfoHelper.codeName(newValue)
                if (text.isNotEmpty()) codeName.value = text
            }
        }
    }

    LaunchedEffect(codeNameFlow) {
        codeNameFlow.collect { newValue ->
            if (codeName.value != newValue) {
                codeName.value = newValue
                val text = DeviceInfoHelper.deviceName(newValue)
                if (text.isNotEmpty()) deviceName.value = text
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AutoCompleteTextField(
            text = deviceName,
            items = deviceNames,
            onValueChange = deviceNameFlow,
            label = stringResource(Res.string.device_name)
        )
        AutoCompleteTextField(
            text = codeName,
            items = codeNames,
            onValueChange = codeNameFlow,
            label = stringResource(Res.string.code_name)
        )
        TextField(
            insideMargin = DpSize(16.dp, 20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .padding(bottom = 12.dp),
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
        Card(
            modifier = Modifier
                .padding(horizontal = 12.dp)
        ) {
            SuperDropdown(
                title = stringResource(Res.string.android_version),
                items = DeviceInfoHelper.androidVersions,
                selectedIndex = androidVersionSelected.value,
                onSelectedIndexChange = { index ->
                    androidVersionSelected.value = index
                    androidVersion.value = DeviceInfoHelper.androidVersions[index]
                },
                onClick = {
                    focusManager.clearFocus()
                },
                mode = DropDownMode.AlwaysOnRight,
                maxHeight = 280.dp
            )
            SuperDropdown(
                title = stringResource(Res.string.region_code),
                items = DeviceInfoHelper.regionNames,
                selectedIndex = regionSelected.value,
                onSelectedIndexChange = { index ->
                    regionSelected.value = index
                    deviceRegion.value = DeviceInfoHelper.regionNames[index]
                },
                onClick = {
                    focusManager.clearFocus()
                },
                mode = DropDownMode.AlwaysOnRight,
                maxHeight = 280.dp
            )
            SuperDropdown(
                title = stringResource(Res.string.carrier_code),
                items = DeviceInfoHelper.carrierNames,
                selectedIndex = carrierSelected.value,
                onSelectedIndexChange = { index ->
                    carrierSelected.value = index
                    deviceCarrier.value = DeviceInfoHelper.carrierNames[index]
                },
                onClick = {
                    focusManager.clearFocus()
                },
                mode = DropDownMode.AlwaysOnRight,
                maxHeight = 280.dp
            )
        }
        SearchHistoryView(
            searchKeywords = searchKeywords,
            searchKeywordsSelected = searchKeywordsSelected,
            onHistorySelect = { keyword ->
                val parts = keyword.split("-")
                deviceName.value = parts[0]
                codeName.value = parts[1]
                deviceRegion.value = parts[2]
                deviceCarrier.value = parts[3]
                androidVersion.value = parts[4]
                systemVersion.value = parts[5]
                regionSelected.value = DeviceInfoHelper.regionNames.indexOf(parts[2])
                carrierSelected.value = DeviceInfoHelper.carrierNames.indexOf(parts[3])
                androidVersionSelected.value = DeviceInfoHelper.androidVersions.indexOf(parts[4])
            }
        )
        TextButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
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
