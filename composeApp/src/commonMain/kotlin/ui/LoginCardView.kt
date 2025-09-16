package ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import isWeb
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.icons.useful.Confirm
import top.yukonga.miuix.kmp.icon.icons.useful.Info
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import updater.composeapp.generated.resources.Res
import updater.composeapp.generated.resources.logged_in
import updater.composeapp.generated.resources.login_desc
import updater.composeapp.generated.resources.login_expired
import updater.composeapp.generated.resources.login_expired_desc
import updater.composeapp.generated.resources.no_account
import updater.composeapp.generated.resources.using_v2

@Composable
fun LoginCardView(
    isLogin: MutableState<Int>,
    isDarkTheme: Boolean
) {
    val account = when (isLogin.value) {
        1 -> stringResource(Res.string.logged_in)
        0 -> stringResource(Res.string.no_account)
        else -> stringResource(Res.string.login_expired)
    }
    val info = when (isLogin.value) {
        1 -> stringResource(Res.string.using_v2)
        0 -> stringResource(Res.string.login_desc)
        else -> stringResource(Res.string.login_expired_desc)
    }
    val icon = if (isLogin.value == 1) MiuixIcons.Useful.Confirm else MiuixIcons.Useful.Info
    val color = when {
        isDarkTheme && isLogin.value == 1 -> Color(0xFF1A3825)
        isDarkTheme && isLogin.value != 1 -> Color(0xFF310808)
        !isDarkTheme && isLogin.value == 1 -> Color(0xFFDFFAE4)
        else -> Color(0xFFF8E2E2)
    }
    val showDialog = remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 12.dp),
        insideMargin = PaddingValues(16.dp),
        pressFeedbackType = PressFeedbackType.Sink,
        onLongPress = { showDialog.value = true },
        colors = CardDefaults.defaultColors(color = color),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                modifier = Modifier.padding(start = 6.dp),
                imageVector = icon,
                tint = MiuixTheme.colorScheme.onSurface,
                contentDescription = null
            )
            Column(
                modifier = Modifier.padding(start = 20.dp)
            ) {
                Text(
                    text = if (!isWeb()) account else "WebPage",
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = info
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            if (!isWeb()) {
                LoginDialog(showDialog, isLogin)
            }
        }
    }
}
