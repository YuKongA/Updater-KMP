package ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import platform.prefGet
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Blocklist
import top.yukonga.miuix.kmp.icon.extended.RemoveContact
import top.yukonga.miuix.kmp.icon.extended.Rename
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.preference.CheckboxPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import updater.shared.generated.resources.Res
import updater.shared.generated.resources.account
import updater.shared.generated.resources.cancel
import updater.shared.generated.resources.do_not_enter_in_browser
import updater.shared.generated.resources.get_verification_code
import updater.shared.generated.resources.global
import updater.shared.generated.resources.login
import updater.shared.generated.resources.logout
import updater.shared.generated.resources.logout_confirm
import updater.shared.generated.resources.password
import updater.shared.generated.resources.save_password
import updater.shared.generated.resources.submit
import updater.shared.generated.resources.verification_code
import updater.shared.generated.resources.verifying
import viewmodel.LoginEvent
import viewmodel.LoginState

@Composable
fun LoginDialog(
    show: Boolean,
    loginState: LoginState,
    account: String,
    password: String,
    global: Boolean,
    savePassword: String,
    showTicketUrl: Boolean,
    showTicketInput: Boolean,
    isVerifying: Boolean,
    ticket: String,
    isVerificationRequested: Boolean,
    isLoggingIn: Boolean,
    onShowDialog: () -> Unit,
    onEvent: (LoginEvent) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val uriHandler = LocalUriHandler.current

    var localAccount by remember(show) { mutableStateOf(account) }
    var localPassword by remember(show) { mutableStateOf(password) }
    var localTicket by remember(show) { mutableStateOf(ticket) }

    val isLoggedIn = loginState is LoginState.LoggedIn

    val icon = when (loginState) {
        is LoginState.LoggedIn -> MiuixIcons.Blocklist
        else -> MiuixIcons.RemoveContact
    }

    IconButton(
        onClick = {
            focusManager.clearFocus()
            onShowDialog()
        },
        holdDownState = show
    ) {
        Icon(
            imageVector = icon,
            tint = MiuixTheme.colorScheme.onSurface,
            contentDescription = stringResource(Res.string.login)
        )
    }

    if (!isLoggedIn) {
        OverlayDialog(
            show = show,
            title = stringResource(Res.string.login),
            onDismissRequest = { onEvent(LoginEvent.DismissDialog) },
            content = {
                Column {
                    // 账号输入框
                    TextField(
                        value = localAccount,
                        onValueChange = { localAccount = it },
                        label = stringResource(Res.string.account),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )

                    // 密码输入框
                    var passwordVisibility by remember { mutableStateOf(false) }
                    TextField(
                        value = localPassword,
                        onValueChange = { localPassword = it },
                        label = stringResource(Res.string.password),
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(
                                modifier = Modifier.padding(end = 6.dp),
                                onClick = { passwordVisibility = !passwordVisibility },
                                content = {
                                    Icon(
                                        imageVector = MiuixIcons.Rename,
                                        tint = if (passwordVisibility) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSecondaryContainer,
                                        contentDescription = null
                                    )
                                }
                            )
                        }
                    )

                    // 二次认证输入框
                    AnimatedVisibility(
                        visible = showTicketInput
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                        ) {
                            val captchaUrl = prefGet("captchaUrl")
                            val notificationUrl = prefGet("notificationUrl")
                            val verificationUrl = if (!captchaUrl.isNullOrBlank()) {
                                "https://account.xiaomi.com$captchaUrl"
                            } else if (!notificationUrl.isNullOrBlank()) {
                                notificationUrl
                            } else {
                                null
                            }

                            if (verificationUrl != null) {
                                AnimatedVisibility(
                                    visible = !isVerificationRequested
                                ) {
                                    Column {
                                        TextButton(
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                            text = stringResource(Res.string.get_verification_code),
                                            colors = ButtonDefaults.textButtonColorsPrimary(),
                                            onClick = {
                                                uriHandler.openUri(verificationUrl)
                                                onEvent(LoginEvent.VerificationRequested(true))
                                            }
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = stringResource(Res.string.do_not_enter_in_browser),
                                                color = MiuixTheme.colorScheme.onPrimaryVariant,
                                                fontSize = 12.sp,
                                            )
                                        }
                                    }
                                }
                            }

                            AnimatedVisibility(
                                visible = verificationUrl == null || isVerificationRequested
                            ) {
                                Column {
                                    TextField(
                                        value = localTicket,
                                        onValueChange = { localTicket = it },
                                        label = stringResource(Res.string.verification_code),
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                                    ) {
                                        TextButton(
                                            modifier = Modifier.weight(1f),
                                            text = if (isVerifying) stringResource(Res.string.verifying) else stringResource(Res.string.submit),
                                            enabled = !isVerifying && localTicket.isNotBlank(),
                                            colors = ButtonDefaults.textButtonColorsPrimary(),
                                            onClick = {
                                                onEvent(LoginEvent.AccountChanged(localAccount))
                                                onEvent(LoginEvent.PasswordChanged(localPassword))
                                                onEvent(LoginEvent.TicketChanged(localTicket))
                                                onEvent(LoginEvent.Submit2FA)
                                            }
                                        )
                                        Spacer(Modifier.width(16.dp))
                                        TextButton(
                                            modifier = Modifier.weight(1f),
                                            text = stringResource(Res.string.cancel),
                                            colors = ButtonDefaults.textButtonColors(),
                                            onClick = { onEvent(LoginEvent.CancelTicket) }
                                        )
                                    }
                                }
                            }
                            if (verificationUrl != null && !isVerificationRequested) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                                ) {
                                    TextButton(
                                        modifier = Modifier.weight(1f),
                                        text = stringResource(Res.string.cancel),
                                        colors = ButtonDefaults.textButtonColors(),
                                        onClick = { onEvent(LoginEvent.CancelTicket) }
                                    )
                                }
                            }
                        }
                    }

                    // 国际账号 & 保存密码
                    AnimatedVisibility(
                        visible = !showTicketUrl && !showTicketInput
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CheckboxPreference(
                                    title = stringResource(Res.string.global),
                                    checked = global,
                                    onCheckedChange = { onEvent(LoginEvent.GlobalChanged(it)) }
                                )
                            }
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CheckboxPreference(
                                    title = stringResource(Res.string.save_password),
                                    checked = savePassword == "1",
                                    onCheckedChange = { onEvent(LoginEvent.SavePasswordChanged(if (it) "1" else "0")) }
                                )
                            }
                        }
                    }

                    // 登录 & 取消
                    AnimatedVisibility(
                        visible = !showTicketInput
                    ) {
                        Row {
                            TextButton(
                                modifier = Modifier.weight(1f),
                                text = stringResource(Res.string.login),
                                enabled = !isLoggingIn,
                                colors = ButtonDefaults.textButtonColorsPrimary(),
                                onClick = {
                                    onEvent(LoginEvent.AccountChanged(localAccount))
                                    onEvent(LoginEvent.PasswordChanged(localPassword))
                                    onEvent(LoginEvent.LoginClicked)
                                }
                            )
                            Spacer(Modifier.width(16.dp))
                            TextButton(
                                modifier = Modifier.weight(1f),
                                text = stringResource(Res.string.cancel),
                                colors = ButtonDefaults.textButtonColors(),
                                onClick = { onEvent(LoginEvent.DismissDialog) }
                            )
                        }
                    }
                }
            })
    }

    // 退出登录
    if (isLoggedIn) {
        OverlayDialog(
            show = show,
            title = stringResource(Res.string.logout),
            summary = stringResource(Res.string.logout_confirm),
            onDismissRequest = { onEvent(LoginEvent.DismissDialog) },
            content = {
                Row {
                    TextButton(
                        modifier = Modifier.weight(1f),
                        text = stringResource(Res.string.logout),
                        colors = ButtonDefaults.textButtonColorsPrimary(),
                        onClick = { onEvent(LoginEvent.LogoutClicked) }
                    )
                    Spacer(Modifier.width(16.dp))
                    TextButton(
                        modifier = Modifier.weight(1f),
                        text = stringResource(Res.string.cancel),
                        colors = ButtonDefaults.textButtonColors(),
                        onClick = { onEvent(LoginEvent.DismissDialog) }
                    )
                }
            })
    }
}
