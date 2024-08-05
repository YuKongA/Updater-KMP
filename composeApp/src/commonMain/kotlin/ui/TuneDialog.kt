package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType.Companion.PrimaryNotEditable
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import isSupportMiuiStrongToast
import org.jetbrains.compose.resources.stringResource
import perfGet
import perfSet
import updater.composeapp.generated.resources.Res
import updater.composeapp.generated.resources.dark_mode
import updater.composeapp.generated.resources.dark_theme
import updater.composeapp.generated.resources.extension_settings
import updater.composeapp.generated.resources.light_mode
import updater.composeapp.generated.resources.system_default

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TuneDialog(
    colorMode: MutableState<Int>
) {
    var showDialog by remember { mutableStateOf(false) }

    val hapticFeedback = LocalHapticFeedback.current
    val extensionSettings = stringResource(Res.string.extension_settings)

    IconButton(
        modifier = Modifier.widthIn(max = 48.dp),
        onClick = {
            showDialog = true
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    ) {
        Icon(
            imageVector = Icons.Default.Tune,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface
        )
    }

    if (showDialog) {
        BasicAlertDialog(
            onDismissRequest = { showDialog = false }) {
            Column(
                modifier = Modifier
                    .widthIn(min = 350.dp, max = 380.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 24.dp).padding(top = 24.dp, bottom = 12.dp),
                    text = extensionSettings,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = MaterialTheme.typography.titleLarge.fontSize
                )
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp)
                ) {
                    miuiStrongToast()
                    uiColorMode(colorMode)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun miuiStrongToast() {
    if (isSupportMiuiStrongToast()) {
        var isUseMiuiStrongToast by remember { mutableStateOf(perfGet("isUseMiuiStrongToast") == "true") }
        Row(
            modifier = Modifier.padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = "MiuiStrongToast",
                fontWeight = FontWeight.SemiBold,
                fontSize = MaterialTheme.typography.bodyMedium.fontSize
            )
            Switch(
                checked = isUseMiuiStrongToast,
                onCheckedChange = {
                    isUseMiuiStrongToast = it
                    perfSet("isUseMiuiStrongToast", it.toString())
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun uiColorMode(colorMode: MutableState<Int>) {
    var isDropdownExpanded by remember { mutableStateOf(false) }
    val darkTheme = stringResource(Res.string.dark_theme)
    val systemDefault = stringResource(Res.string.system_default)
    val lightMode = stringResource(Res.string.light_mode)
    val darkMode = stringResource(Res.string.dark_mode)
    val options = listOf(systemDefault, lightMode, darkMode)
    val selectedOption = remember { mutableStateOf(options[colorMode.value]) }
    val textWidthDp = options.maxOfOrNull { option ->
        with(LocalDensity.current) { rememberTextMeasurer().measure(text = option, style = MaterialTheme.typography.bodyMedium).size.width.toDp() }
    }

    Row(
        modifier = Modifier
            .padding(bottom = 12.dp)
            .fillMaxWidth()
            .clickable { isDropdownExpanded = true },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = darkTheme,
            fontWeight = FontWeight.SemiBold,
            fontSize = MaterialTheme.typography.bodyMedium.fontSize
        )
        ExposedDropdownMenuBox(
            expanded = isDropdownExpanded,
            onExpandedChange = { isDropdownExpanded = it }
        ) {
            Text(
                modifier = Modifier
                    .menuAnchor(PrimaryNotEditable)
                    .widthIn(min = textWidthDp?.plus(24.dp) ?: 100.dp),
                text = selectedOption.value,
                fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                textAlign = TextAlign.End
            )
            ExposedDropdownMenu(
                modifier = Modifier.exposedDropdownSize(),
                shape = RoundedCornerShape(10.dp),
                expanded = isDropdownExpanded,
                onDismissRequest = { isDropdownExpanded = false },
            ) {
                options.forEachIndexed { index, option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                modifier = Modifier.fillMaxWidth(),
                                text = option,
                                textAlign = TextAlign.Center
                            )
                        },
                        onClick = {
                            selectedOption.value = option
                            colorMode.value = index
                            perfSet("colorMode", index.toString())
                            isDropdownExpanded = false
                        }
                    )
                }
            }
        }
    }
}