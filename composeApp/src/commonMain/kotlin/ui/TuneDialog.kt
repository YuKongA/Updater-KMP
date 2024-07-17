package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import isSupportMiuiStrongToast
import perfGet
import perfSet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TuneDialog(
    colorMode: MutableState<Int>
) {
    var showDialog by remember { mutableStateOf(false) }

    val hapticFeedback = LocalHapticFeedback.current

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
            onDismissRequest = { showDialog = false },
            content = {
                Column(
                    modifier = Modifier
                        .widthIn(min = 350.dp, max = 380.dp)
                        .clip(RoundedCornerShape(30.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Text(
                        modifier = Modifier.padding(horizontal = 24.dp).padding(top = 24.dp, bottom = 12.dp),
                        text = "Extension Settings",
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
        )
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
    val options = listOf("System default", "Light mode", "Dark mode")
    val selectedOption = remember { mutableStateOf(options[colorMode.value]) }
    Row(
        modifier = Modifier.padding(bottom = 12.dp).fillMaxWidth().clickable { isDropdownExpanded = true },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(0.8f),
            text = "Theme",
            fontWeight = FontWeight.SemiBold,
            fontSize = MaterialTheme.typography.bodyMedium.fontSize
        )
        ExposedDropdownMenuBox(
            modifier = Modifier.weight(1f),
            expanded = isDropdownExpanded,
            onExpandedChange = { isDropdownExpanded = it },
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(PrimaryNotEditable),
                text = selectedOption.value,
                textAlign = TextAlign.End,
            )
            ExposedDropdownMenu(
                modifier = Modifier.exposedDropdownSize(),
                shape = RoundedCornerShape(10.dp),
                expanded = isDropdownExpanded,
                onDismissRequest = { isDropdownExpanded = false },
            ) {
                options.forEachIndexed { index, option ->
                    DropdownMenuItem(
                        text = { Text(option) },
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