package ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import updaterkmp.composeapp.generated.resources.Res
import updaterkmp.composeapp.generated.resources.app_name
import updaterkmp.composeapp.generated.resources.join_group
import updaterkmp.composeapp.generated.resources.opensource_info
import updaterkmp.composeapp.generated.resources.view_source

const val version = "v1.3.0"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutDialog() {
    var showDialog by remember { mutableStateOf(false) }

    val hapticFeedback = LocalHapticFeedback.current

    IconButton(
        modifier = Modifier.widthIn(max = 48.dp),
        onClick = {
            showDialog = true
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }) {
        Icon(
            imageVector = Icons.Outlined.Update,
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
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .padding(top = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(50.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        ) {
                            Image(
                                imageVector = Icons.Outlined.Update,
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary),
                                contentDescription = null,
                                modifier = Modifier.size(25.dp),
                            )
                        }
                        Column {
                            Text(
                                text = stringResource(Res.string.app_name),
                                fontSize = MaterialTheme.typography.titleLarge.fontSize,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = version,
                                fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                            )
                        }
                    }
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .padding(top = 12.dp, bottom = 18.dp)
                    ) {
                        val uriHandler = LocalUriHandler.current
                        Row {
                            Text(
                                text = stringResource(Res.string.view_source) + " ",
                                fontSize = MaterialTheme.typography.bodyMedium.fontSize
                            )
                            Text(
                                text = AnnotatedString(
                                    text = "GitHub",
                                    spanStyle = SpanStyle(textDecoration = TextDecoration.Underline, color = MaterialTheme.colorScheme.primary)
                                ),
                                fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                modifier = Modifier.clickable(
                                    onClick = {
                                        uriHandler.openUri("https://github.com/YuKongA/Updater-KMP")
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                )
                            )
                        }
                        Row {
                            Text(
                                text = stringResource(Res.string.join_group) + " ",
                                fontSize = MaterialTheme.typography.bodyMedium.fontSize
                            )
                            Text(
                                text = AnnotatedString(
                                    text = "Telegram",
                                    spanStyle = SpanStyle(textDecoration = TextDecoration.Underline, color = MaterialTheme.colorScheme.primary)
                                ),
                                fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                modifier = Modifier.clickable(
                                    onClick = {
                                        uriHandler.openUri("https://t.me/YuKongA13579")
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    },
                                )
                            )
                        }
                        Spacer(modifier = Modifier.padding(top = 12.dp))
                        Text(
                            text = stringResource(Res.string.opensource_info),
                            fontSize = MaterialTheme.typography.bodyMedium.fontSize
                        )
                    }
                }
            }
        )
    }
}