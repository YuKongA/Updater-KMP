package ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material.icons.rounded.Update
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import misc.VersionInfo
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.extra.SuperDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.MiuixPopupUtil.Companion.dismissDialog
import updater.composeapp.generated.resources.Res
import updater.composeapp.generated.resources.about
import updater.composeapp.generated.resources.app_name
import updater.composeapp.generated.resources.join_group
import updater.composeapp.generated.resources.opensource_info
import updater.composeapp.generated.resources.view_source

@Composable
fun AboutDialog() {
    val showDialog = remember { mutableStateOf(false) }

    val hapticFeedback = LocalHapticFeedback.current

    IconButton(
        modifier = Modifier.padding(start = 15.dp),
        onClick = {
            showDialog.value = true
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }) {
        Icon(
            imageVector = Icons.Rounded.Update,
            contentDescription = null,
        )
    }

    SuperDialog(
        show = showDialog,
        title = stringResource(Res.string.about),
        onDismissRequest = {
            dismissDialog(showDialog)
        }
    ) {
        Row(
            modifier = Modifier.padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MiuixTheme.colorScheme.primary)
            ) {
                Image(
                    imageVector = Icons.Outlined.Update,
                    colorFilter = ColorFilter.tint(Color.White),
                    contentDescription = null,
                    modifier = Modifier.size(25.dp),
                )
            }
            Column {
                Text(
                    text = stringResource(Res.string.app_name),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = VersionInfo.VERSION_NAME + " (" + VersionInfo.VERSION_CODE + ")",
                )
            }
        }
        val uriHandler = LocalUriHandler.current
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.view_source) + " ",
            )
            Text(
                text = AnnotatedString(
                    text = "GitHub",
                    spanStyle = SpanStyle(
                        textDecoration = TextDecoration.Underline,
                        color = MiuixTheme.colorScheme.primary
                    )
                ),
                modifier = Modifier.clickable(
                    onClick = {
                        uriHandler.openUri("https://github.com/YuKongA/Updater-KMP")
                    }
                )
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.join_group) + " ",
            )
            Text(
                text = AnnotatedString(
                    text = "Telegram",
                    spanStyle = SpanStyle(
                        textDecoration = TextDecoration.Underline,
                        color = MiuixTheme.colorScheme.primary
                    )
                ),
                modifier = Modifier.clickable(
                    onClick = {
                        uriHandler.openUri("https://t.me/YuKongA13579")
                    },
                )
            )
        }
        Text(
            modifier = Modifier.padding(top = 10.dp),
            text = stringResource(Res.string.opensource_info)
        )
    }
}