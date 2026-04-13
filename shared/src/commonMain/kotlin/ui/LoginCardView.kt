package ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Info
import top.yukonga.miuix.kmp.icon.extended.Ok
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PressFeedbackType
import updater.shared.generated.resources.Res
import updater.shared.generated.resources.logged_in
import updater.shared.generated.resources.login_desc
import updater.shared.generated.resources.login_expired
import updater.shared.generated.resources.login_expired_desc
import updater.shared.generated.resources.no_account
import updater.shared.generated.resources.using_v2
import utils.isWeb
import viewmodel.AppUiState
import viewmodel.LoginEvent
import viewmodel.LoginState

@Composable
fun LoginCardView(
    uiState: AppUiState,
    isDarkTheme: Boolean,
    onShowLoginDialog: () -> Unit,
    onLoginEvent: (LoginEvent) -> Unit,
) {
    val loginState = uiState.loginState
    val isLoggedIn = loginState is LoginState.LoggedIn
    val account = when (loginState) {
        is LoginState.LoggedIn -> stringResource(Res.string.logged_in)
        is LoginState.NotLoggedIn -> stringResource(Res.string.no_account)
        is LoginState.Expired -> stringResource(Res.string.login_expired)
    }
    val info = when (loginState) {
        is LoginState.LoggedIn -> stringResource(Res.string.using_v2)
        is LoginState.NotLoggedIn -> stringResource(Res.string.login_desc)
        is LoginState.Expired -> stringResource(Res.string.login_expired_desc)
    }
    val icon = if (isLoggedIn) MiuixIcons.Ok else MiuixIcons.Info
    val color = when {
        isDarkTheme && isLoggedIn -> Color(0xFF1A3825)
        isDarkTheme && !isLoggedIn -> Color(0xFF310808)
        !isDarkTheme && isLoggedIn -> Color(0xFFDFFAE4)
        else -> Color(0xFFF8E2E2)
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 12.dp),
        insideMargin = PaddingValues(16.dp),
        pressFeedbackType = PressFeedbackType.Sink,
        onLongPress = { onShowLoginDialog() },
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
                LoginDialog(
                    show = uiState.showLoginDialog,
                    loginState = loginState,
                    account = uiState.loginAccount,
                    password = uiState.loginPassword,
                    global = uiState.loginGlobal,
                    savePassword = uiState.loginSavePassword,
                    showTicketInput = uiState.showTicketInput,
                    available2FAOptions = uiState.available2FAOptions,
                    isVerifying = uiState.isVerifying,
                    ticket = uiState.loginTicket,
                    isVerificationRequested = uiState.isVerificationRequested,
                    isLoggingIn = uiState.isLoggingIn,
                    onShowDialog = onShowLoginDialog,
                    onEvent = onLoginEvent,
                )
            }
        }
    }
}
