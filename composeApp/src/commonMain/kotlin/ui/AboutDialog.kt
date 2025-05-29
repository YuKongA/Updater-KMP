package ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import misc.VersionInfo
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.extra.SuperDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.Platform
import top.yukonga.miuix.kmp.utils.platform
import updater.composeapp.generated.resources.Res
import updater.composeapp.generated.resources.about
import updater.composeapp.generated.resources.app_name
import updater.composeapp.generated.resources.icon
import updater.composeapp.generated.resources.join_channel
import updater.composeapp.generated.resources.opensource_info
import updater.composeapp.generated.resources.view_source

@Composable
fun AboutDialog(
) {
    val showDialog = remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    IconButton(
        modifier = Modifier.padding(start = if (platform() != Platform.IOS && platform() != Platform.Android) 10.dp else 20.dp),
        onClick = {
            showDialog.value = true
            focusManager.clearFocus()
        },
        holdDownState = showDialog.value
    ) {
        Image(
            painter = painterResource(Res.drawable.icon),
            contentDescription = "About",
            modifier = Modifier
                .size(32.dp)
                .padding(4.dp),
        )
    }

    SuperDialog(
        show = showDialog,
        title = stringResource(Res.string.about),
        onDismissRequest = {
            showDialog.value = false
        }
    ) {
        Row(
            modifier = Modifier.padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(Res.drawable.icon),
                contentDescription = "Icon",
                modifier = Modifier.size(45.dp),
            )
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
                text = stringResource(Res.string.join_channel) + " ",
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