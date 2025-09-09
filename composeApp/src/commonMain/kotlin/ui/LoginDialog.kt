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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import getPassword
import handle2FATicket
import kotlinx.coroutines.launch
import login
import logout
import misc.MessageUtils.Companion.showMessage
import org.jetbrains.compose.resources.stringResource
import platform.prefGet
import platform.prefRemove
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.extra.SuperCheckbox
import top.yukonga.miuix.kmp.extra.SuperDialog
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.icons.useful.Blocklist
import top.yukonga.miuix.kmp.icon.icons.useful.RemoveBlocklist
import top.yukonga.miuix.kmp.icon.icons.useful.Rename
import top.yukonga.miuix.kmp.theme.MiuixTheme
import updater.composeapp.generated.resources.Res
import updater.composeapp.generated.resources.account
import updater.composeapp.generated.resources.account_or_password_empty
import updater.composeapp.generated.resources.cancel
import updater.composeapp.generated.resources.global
import updater.composeapp.generated.resources.logging_in
import updater.composeapp.generated.resources.login
import updater.composeapp.generated.resources.login_error
import updater.composeapp.generated.resources.login_successful
import updater.composeapp.generated.resources.logout
import updater.composeapp.generated.resources.logout_confirm
import updater.composeapp.generated.resources.logout_successful
import updater.composeapp.generated.resources.password
import updater.composeapp.generated.resources.save_password
import updater.composeapp.generated.resources.security_error
import updater.composeapp.generated.resources.toast_crash_info

private const val PASSWORD_SAVE_KEY = "savePassword"
private const val PASSWORD_SAVE_ENABLED = "1"
private const val PASSWORD_SAVE_DISABLED = "0"

@Composable
fun LoginDialog(
    isLogin: MutableState<Int>
) {
    val coroutineScope = rememberCoroutineScope()
    var account by remember { mutableStateOf(getPassword().first) }
    var password by remember { mutableStateOf(getPassword().second) }

    var global by remember { mutableStateOf(false) }
    var savePassword by remember { mutableStateOf(prefGet(PASSWORD_SAVE_KEY) ?: PASSWORD_SAVE_DISABLED) }
    val showDialog = remember { mutableStateOf(false) }
    var showNotificationUrl by remember { mutableStateOf(false) }

    var showTicketInput by remember { mutableStateOf(false) }
    var ticketCode by remember { mutableStateOf("") }
    var isVerifyingTicket by remember { mutableStateOf(false) }
    var verifyError by remember { mutableStateOf("") }

    val icon = when (isLogin.value) {
        1 -> MiuixIcons.Useful.Blocklist
        else -> MiuixIcons.Useful.RemoveBlocklist
    }

    val messageLoginIn = stringResource(Res.string.logging_in)
    val messageLoginSuccess = stringResource(Res.string.login_successful)
    val messageEmpty = stringResource(Res.string.account_or_password_empty)
    val messageError = stringResource(Res.string.login_error)
    val messageSecurityError = stringResource(Res.string.security_error)
    val messageLogoutSuccessful = stringResource(Res.string.logout_successful)
    val messageCrashInfo = stringResource(Res.string.toast_crash_info)

    val focusManager = LocalFocusManager.current

    IconButton(
        onClick = {
            showDialog.value = true
            focusManager.clearFocus()
        },
        holdDownState = showDialog.value
    ) {
        Icon(
            imageVector = icon,
            tint = MiuixTheme.colorScheme.onSurface,
            contentDescription = "Login"
        )
    }

    if (isLogin.value != 1) {
        SuperDialog(
            show = showDialog,
            title = stringResource(Res.string.login),
            onDismissRequest = {
                showDialog.value = false
            }
        ) {
            Column {
                TextField(
                    value = account,
                    onValueChange = { account = it },
                    label = stringResource(Res.string.account),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )
                var passwordVisibility by remember { mutableStateOf(false) }
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = stringResource(Res.string.password),
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(
                            modifier = Modifier.padding(end = 6.dp),
                            onClick = {
                                passwordVisibility = !passwordVisibility
                            },
                            content = {
                                Icon(
                                    imageVector = MiuixIcons.Useful.Rename,
                                    tint = if (passwordVisibility) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSecondaryContainer,
                                    contentDescription = null
                                )
                            }
                        )
                    }
                )


                if (showNotificationUrl) {
                    val uriHandler = LocalUriHandler.current
                    TextButton(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        text = "获取验证码",
                        onClick = {
                            val notificationUrl = prefGet("notificationUrl")
                            notificationUrl?.let { uriHandler.openUri(it) }
                        },
                        colors = ButtonDefaults.textButtonColorsPrimary()
                    )
                    TextButton(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        text = "输入验证码",
                        onClick = {
                            showTicketInput = true
                            verifyError = ""
                        },
                        colors = ButtonDefaults.textButtonColorsPrimary()
                    )
                }

                AnimatedVisibility(
                    visible = !showTicketInput
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            SuperCheckbox(
                                title = stringResource(Res.string.global),
                                checked = global,
                                onCheckedChange = {
                                    global = it
                                }
                            )
                        }
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            SuperCheckbox(
                                title = stringResource(Res.string.save_password),
                                checked = savePassword == PASSWORD_SAVE_ENABLED,
                                onCheckedChange = {
                                    savePassword = if (it) PASSWORD_SAVE_ENABLED else PASSWORD_SAVE_DISABLED
                                }
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = showTicketInput
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                    ) {
                        TextField(
                            value = ticketCode,
                            onValueChange = { ticketCode = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                        )
                        if (verifyError.isNotEmpty()) {
                            Text(text = verifyError, color = Color.Red)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                        ) {
                            TextButton(
                                modifier = Modifier.weight(1f),
                                text = if (isVerifyingTicket) "验证中..." else "提交",
                                enabled = !isVerifyingTicket && ticketCode.isNotBlank(),
                                colors = ButtonDefaults.textButtonColorsPrimary(),
                                onClick = {
                                    coroutineScope.launch {
                                        isVerifyingTicket = true
                                        handle2FATicket(
                                            ticketCode = ticketCode,
                                            isLogin = isLogin,
                                            setVerifyError = { verifyError = it },
                                            setTicketCode = { ticketCode = it },
                                            setShowTicketInput = { showTicketInput = it },
                                            setShowDialog = { showDialog.value = it },
                                            setShowNotificationUrl = { showNotificationUrl = it },
                                            showMessage = { showMessage(it) },
                                            focusManager = focusManager
                                        )
                                        isVerifyingTicket = false
                                    }
                                }
                            )
                            Spacer(Modifier.width(20.dp))
                            TextButton(
                                modifier = Modifier.weight(1f),
                                text = "取消",
                                colors = ButtonDefaults.textButtonColors(),
                                onClick = {
                                    showTicketInput = false
                                    ticketCode = ""
                                    verifyError = ""
                                }
                            )
                        }
                    }
                }

                AnimatedVisibility(
                    visible = !showTicketInput
                ) {
                    Row {
                        TextButton(
                            modifier = Modifier.weight(1f),
                            text = stringResource(Res.string.login),
                            colors = ButtonDefaults.textButtonColorsPrimary(),
                            onClick = {
                                showDialog.value = false
                                showMessage(message = messageLoginIn)
                                coroutineScope.launch {
                                    val int = login(account, password, global, savePassword, isLogin)
                                    when (int) {
                                        0 -> {
                                            showMessage(message = messageLoginSuccess)
                                        }

                                        1 -> showMessage(message = messageEmpty)
                                        2 -> showMessage(message = messageCrashInfo)
                                        3 -> {
                                            showMessage(message = messageError)
                                            prefRemove("password")
                                            prefRemove("passwordIv")
                                        }

                                        4 -> showMessage(message = messageSecurityError)

                                        5 -> {
                                            prefGet("notificationUrl")
                                            showNotificationUrl = true
                                            showMessage("检测到二次验证，请打开并完成验证后重试")
                                        }
                                    }
                                }
                            }
                        )
                        Spacer(Modifier.width(20.dp))
                        TextButton(
                            modifier = Modifier.weight(1f),
                            text = stringResource(Res.string.cancel),
                            colors = ButtonDefaults.textButtonColors(),
                            onClick = {
                                showDialog.value = false
                            }
                        )
                    }
                }
            }
        }
    }

    if (isLogin.value == 1) {
        SuperDialog(
            show = showDialog,
            title = stringResource(Res.string.logout),
            summary = stringResource(Res.string.logout_confirm),
            onDismissRequest = {
                showDialog.value = false
            }
        ) {
            Row {
                TextButton(
                    modifier = Modifier.weight(1f),
                    text = stringResource(Res.string.logout),
                    colors = ButtonDefaults.textButtonColorsPrimary(),
                    onClick = {
                        coroutineScope.launch {
                            val boolean = logout(isLogin)
                            if (boolean) showMessage(message = messageLogoutSuccessful)
                        }
                        showDialog.value = false
                    }
                )
                Spacer(Modifier.width(20.dp))
                TextButton(
                    modifier = Modifier.weight(1f),
                    text = stringResource(Res.string.cancel),
                    colors = ButtonDefaults.textButtonColors(),
                    onClick = {
                        showDialog.value = false
                    }
                )
            }
        }
    }
}
