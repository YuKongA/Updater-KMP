package ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import isWasm
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import updater.composeapp.generated.resources.Res
import updater.composeapp.generated.resources.logged_in
import updater.composeapp.generated.resources.login_desc
import updater.composeapp.generated.resources.login_expired
import updater.composeapp.generated.resources.login_expired_desc
import updater.composeapp.generated.resources.no_account
import updater.composeapp.generated.resources.using_v2

@Composable
fun LoginCardView(
    isLogin: MutableState<Int>
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
    val icon = if (isLogin.value == 1) Icons.Rounded.DoneAll else Icons.Rounded.Done

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .padding(top = 12.dp, bottom = 6.dp),
        insideMargin = DpSize(16.dp, 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                modifier = Modifier.size(28.dp).padding(start = 6.dp),
                imageVector = icon,
                tint = MiuixTheme.colorScheme.onPrimary,
                contentDescription = null
            )
            Column(modifier = Modifier.padding(start = 24.dp)) {
                Text(
                    text = if (!isWasm()) account else "WebAssembly",
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = info
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            if (!isWasm()) LoginDialog(isLogin)
        }
    }
}