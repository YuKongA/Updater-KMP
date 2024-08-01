package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Android
import androidx.compose.material.icons.outlined.DeveloperMode
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import data.DeviceInfoHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import ui.components.AutoCompleteTextField
import ui.components.TextFieldWithDropdown
import updater.composeapp.generated.resources.Res
import updater.composeapp.generated.resources.android_version
import updater.composeapp.generated.resources.code_name
import updater.composeapp.generated.resources.device_name
import updater.composeapp.generated.resources.regions_code
import updater.composeapp.generated.resources.system_version

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
        modifier = Modifier.background(Color.Unspecified).fillMaxWidth().padding(vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AutoCompleteTextField(
            text = deviceName,
            items = DeviceInfoHelper.deviceNames,
            onValueChange = deviceNameFlow,
            label = stringResource(Res.string.device_name),
            leadingIcon = Icons.Outlined.Smartphone
        )
        AutoCompleteTextField(
            text = codeName,
            items = DeviceInfoHelper.codeNames,
            onValueChange = codeNameFlow,
            label = stringResource(Res.string.code_name),
            leadingIcon = Icons.Outlined.DeveloperMode
        )
        TextFieldWithDropdown(
            text = deviceRegion,
            items = DeviceInfoHelper.regionNames,
            label = stringResource(Res.string.regions_code),
            leadingIcon = Icons.Outlined.TravelExplore
        )
        TextFieldWithDropdown(
            text = androidVersion,
            items = DeviceInfoHelper.androidVersions,
            label = stringResource(Res.string.android_version),
            leadingIcon = Icons.Outlined.Android
        )
        OutlinedTextField(
            value = systemVersion.value,
            onValueChange = { systemVersion.value = it },
            label = { Text(stringResource(Res.string.system_version)) },
            leadingIcon = { Icon(imageVector = Icons.Outlined.Analytics, null) },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                focusManager.clearFocus()
                updateRomInfo.value++
            })
        )
    }
}
