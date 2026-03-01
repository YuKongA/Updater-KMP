package ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
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
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.SpinnerEntry
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.extra.SuperDropdown
import top.yukonga.miuix.kmp.extra.SuperSpinner
import ui.components.AutoCompleteTextField
import updater.shared.generated.resources.Res
import updater.shared.generated.resources.android_version
import updater.shared.generated.resources.carrier_code
import updater.shared.generated.resources.code_name
import updater.shared.generated.resources.device_name
import updater.shared.generated.resources.region_code
import updater.shared.generated.resources.search_history
import updater.shared.generated.resources.submit
import updater.shared.generated.resources.system_version
import updater.shared.generated.resources.toast_no_info
import utils.MessageUtils.Companion.showMessage

@Composable
private fun SearchHistoryView(
    searchKeywords: List<String>,
    searchKeywordsSelected: Int,
    onHistorySelect: (Int, String) -> Unit
) {
    AnimatedVisibility(
        visible = searchKeywords.isNotEmpty(),
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        val focusManager = LocalFocusManager.current
        val spinnerOptions = searchKeywords.map { keyword ->
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
                selectedIndex = searchKeywordsSelected,
                showValue = false,
                onSelectedIndexChange = { index ->
                    onHistorySelect(index, searchKeywords[index])
                },
                maxHeight = 280.dp,
                modifier = Modifier.clickable {
                    focusManager.clearFocus()
                }
            )
        }
    }
}

@Composable
fun BasicViews(
    deviceName: String,
    codeName: String,
    androidVersion: String,
    deviceRegion: String,
    deviceCarrier: String,
    systemVersion: String,
    searchKeywords: List<String>,
    searchKeywordsSelected: Int,
    onDeviceNameChange: (String) -> Unit,
    onCodeNameChange: (String) -> Unit,
    onAndroidVersionChange: (String) -> Unit,
    onDeviceRegionChange: (String) -> Unit,
    onDeviceCarrierChange: (String) -> Unit,
    onSystemVersionChange: (String) -> Unit,
    onSearchKeywordsSelectedChange: (Int) -> Unit,
    onHistorySelect: (String) -> Unit,
    onSubmit: () -> Unit
) {
    val androidVersionSelected = remember(androidVersion) {
        mutableStateOf(DeviceInfoHelper.androidVersions.indexOf(androidVersion).takeIf { it >= 0 } ?: 0)
    }
    val regionSelected = remember(deviceRegion) {
        mutableStateOf(DeviceInfoHelper.regionNames.indexOf(deviceRegion).takeIf { it >= 0 } ?: 0)
    }

    val carrierSelected = remember(deviceCarrier) {
        mutableStateOf(DeviceInfoHelper.carrierNames.indexOf(deviceCarrier).takeIf { it >= 0 } ?: 0)
    }

    val deviceNames by DeviceInfoHelper.deviceNamesFlow.collectAsState()
    val codeNames by DeviceInfoHelper.codeNamesFlow.collectAsState()

    val toastNoInfo = stringResource(Res.string.toast_no_info)

    val focusManager = LocalFocusManager.current
    val onDeviceNameInputChange = remember(deviceName, codeName) {
        { newValue: String ->
            if (deviceName != newValue) {
                onDeviceNameChange(newValue)
                val mappedCodeName = DeviceInfoHelper.codeName(newValue)
                if (mappedCodeName.isNotEmpty() && mappedCodeName != codeName) {
                    onCodeNameChange(mappedCodeName)
                }
            }
        }
    }
    val onCodeNameInputChange = remember(deviceName, codeName) {
        { newValue: String ->
            if (codeName != newValue) {
                onCodeNameChange(newValue)
                val mappedDeviceName = DeviceInfoHelper.deviceName(newValue)
                if (mappedDeviceName.isNotEmpty() && mappedDeviceName != deviceName) {
                    onDeviceNameChange(mappedDeviceName)
                }
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
            onValueChange = onDeviceNameInputChange,
            label = stringResource(Res.string.device_name)
        )
        AutoCompleteTextField(
            text = codeName,
            items = codeNames,
            onValueChange = onCodeNameInputChange,
            label = stringResource(Res.string.code_name)
        )
        TextField(
            insideMargin = DpSize(16.dp, 20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .padding(bottom = 12.dp),
            value = systemVersion,
            onValueChange = onSystemVersionChange,
            label = stringResource(Res.string.system_version),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                focusManager.clearFocus()
                if (codeName != "" && androidVersion != "" && systemVersion != "") {
                    onSubmit()
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
                    onAndroidVersionChange(DeviceInfoHelper.androidVersions[index])
                },
                maxHeight = 280.dp,
                modifier = Modifier.clickable {
                    focusManager.clearFocus()
                }
            )
            SuperDropdown(
                title = stringResource(Res.string.region_code),
                items = DeviceInfoHelper.regionNames,
                selectedIndex = regionSelected.value,
                onSelectedIndexChange = { index ->
                    regionSelected.value = index
                    onDeviceRegionChange(DeviceInfoHelper.regionNames[index])
                },
                maxHeight = 280.dp,
                modifier = Modifier.clickable {
                    focusManager.clearFocus()
                }
            )
            SuperDropdown(
                title = stringResource(Res.string.carrier_code),
                items = DeviceInfoHelper.carrierNames,
                selectedIndex = carrierSelected.value,
                onSelectedIndexChange = { index ->
                    carrierSelected.value = index
                    onDeviceCarrierChange(DeviceInfoHelper.carrierNames[index])
                },
                maxHeight = 280.dp,
                modifier = Modifier.clickable {
                    focusManager.clearFocus()
                }
            )
        }
        SearchHistoryView(
            searchKeywords = searchKeywords,
            searchKeywordsSelected = searchKeywordsSelected,
            onHistorySelect = { index, keyword ->
                onHistorySelect(keyword)
                onSearchKeywordsSelectedChange(index)
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
                if (codeName != "" && androidVersion != "" && systemVersion != "") {
                    onSubmit()
                } else {
                    showMessage(toastNoInfo)
                }
            },
            text = stringResource(Res.string.submit)
        )
    }
}
