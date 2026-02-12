package ui

import Login
import Password
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.stringResource
import platform.prefGet
import platform.prefRemove
import platform.prefSet
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.extra.SuperCheckbox
import top.yukonga.miuix.kmp.extra.SuperDialog
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Blocklist
import top.yukonga.miuix.kmp.icon.extended.RemoveContact
import top.yukonga.miuix.kmp.icon.extended.Rename
import top.yukonga.miuix.kmp.theme.MiuixTheme
import updater.shared.generated.resources.Res
import updater.shared.generated.resources.account
import updater.shared.generated.resources.account_or_password_empty
import updater.shared.generated.resources.cancel
import updater.shared.generated.resources.global
import updater.shared.generated.resources.logging_in
import updater.shared.generated.resources.login
import updater.shared.generated.resources.login_error
import updater.shared.generated.resources.login_successful
import updater.shared.generated.resources.login_tips1
import updater.shared.generated.resources.login_tips2
import updater.shared.generated.resources.logout
import updater.shared.generated.resources.logout_confirm
import updater.shared.generated.resources.logout_successful
import updater.shared.generated.resources.password
import updater.shared.generated.resources.save_password
import updater.shared.generated.resources.security_error
import updater.shared.generated.resources.submit
import updater.shared.generated.resources.toast_crash_info
import updater.shared.generated.resources.verification_code
import updater.shared.generated.resources.verifying
import utils.MessageUtils.Companion.showMessage

@Composable
fun LoginDialog(
    showDialog: MutableState<Boolean>,
    isLogin: MutableState<Int>
) {
    val focusManager = LocalFocusManager.current
    val uriHandler = LocalUriHandler.current
    val coroutineScope = rememberCoroutineScope()

    var account by remember { mutableStateOf(Password().getPassword().first) }
    var password by remember { mutableStateOf(Password().getPassword().second) }

    var global by remember { mutableStateOf(false) }
    var savePassword by remember { mutableStateOf(prefGet("savePassword") ?: "0") }

    var showTicketUrl by remember { mutableStateOf(false) }
    var showTicketInput by remember { mutableStateOf(false) }

    var ticket by remember { mutableStateOf("") }
    var isVerifying by remember { mutableStateOf(false) }

    val icon = when (isLogin.value) {
        1 -> MiuixIcons.Blocklist
        else -> MiuixIcons.RemoveContact
    }

    val messageLoginIn = stringResource(Res.string.logging_in)
    val messageLoginSuccess = stringResource(Res.string.login_successful)
    val messageEmpty = stringResource(Res.string.account_or_password_empty)
    val messageError = stringResource(Res.string.login_error)
    val messageSecurityError = stringResource(Res.string.security_error)
    val messageLogoutSuccessful = stringResource(Res.string.logout_successful)
    val messageCrashInfo = stringResource(Res.string.toast_crash_info)
    val messageLoginTips1 = stringResource(Res.string.login_tips1)
    val messageLoginTips2 = stringResource(Res.string.login_tips2)

    // 主页登录按钮
    IconButton(
        onClick = {
            prefRemove("captchaUrl")
            prefRemove("identity_session")
            prefRemove("2FAContext")
            prefRemove("2FAOptions")
            prefRemove("2FAFlag")

            showTicketInput = false
            showTicketUrl = false

            focusManager.clearFocus()

            showDialog.value = true
        },
        holdDownState = showDialog.value
    ) {
        Icon(
            imageVector = icon,
            tint = MiuixTheme.colorScheme.onSurface,
            contentDescription = stringResource(Res.string.login)
        )
    }

    // 登录对话框
    if (isLogin.value != 1) {
        SuperDialog(
            show = showDialog,
            title = stringResource(Res.string.login),
            onDismissRequest = {
                showDialog.value = false
            }
        ) {
            Column {
                // 账号输入框
                TextField(
                    value = account,
                    onValueChange = { account = it },
                    label = stringResource(Res.string.account),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )

                // 密码输入框
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
                        // 显示 & 隐藏密码
                        IconButton(
                            modifier = Modifier.padding(end = 6.dp),
                            onClick = {
                                passwordVisibility = !passwordVisibility
                            },
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

                // 二次认证验证码（浏览器请求）
                AnimatedVisibility(
                    visible = showTicketUrl
                ) {
                    val optionsStr = prefGet("2FAOptions") ?: "[]"
                    var options by remember { mutableStateOf(Json.decodeFromString<MutableList<Int>>(optionsStr)) }
                    if (options.isEmpty()) {
                        showDialog.value = false
                        showMessage(message = messageError)
                    }
                }

                // 二次认证验证码输入框
                AnimatedVisibility(
                    visible = showTicketInput
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                    ) {
                        TextField(
                            value = ticket,
                            onValueChange = { ticket = it },
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
                                enabled = !isVerifying && ticket.isNotBlank(),
                                colors = ButtonDefaults.textButtonColorsPrimary(),
                                onClick = {
                                    if (showTicketInput) {
                                        // 提交二次认证验证码
                                        coroutineScope.launch {
                                            isVerifying = true
                                            val int = Login().login(
                                                account = account,
                                                password = password,
                                                global = global,
                                                savePassword = savePassword,
                                                isLogin = isLogin,
                                                flag = prefGet("2FAFlag")?.toInt(),
                                                ticket = ticket
                                            )
                                            if (int == 0) {
                                                showMessage(message = messageLoginSuccess)
                                                ticket = ""
                                                showDialog.value = false
                                            } else {
                                                showMessage(message = messageError)
                                            }
                                            isVerifying = false
                                        }
                                    }
                                }
                            )
                            Spacer(Modifier.width(16.dp))
                            TextButton(
                                modifier = Modifier.weight(1f),
                                text = stringResource(Res.string.cancel),
                                colors = ButtonDefaults.textButtonColors(),
                                onClick = {
                                    showTicketUrl = false
                                    showTicketInput = false
                                    ticket = ""
                                }
                            )
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
                                checked = savePassword == "1",
                                onCheckedChange = {
                                    savePassword = if (it) "1" else "0"
                                }
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
                            colors = ButtonDefaults.textButtonColorsPrimary(),
                            onClick = {
                                showMessage(message = messageLoginIn)
                                coroutineScope.launch {
                                    val int = Login().login(
                                        account = account,
                                        password = password,
                                        global = global,
                                        savePassword = savePassword,
                                        isLogin = isLogin
                                    )
                                    when (int) {
                                        0 -> {
                                            showMessage(message = messageLoginSuccess)
                                            showDialog.value = false
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
                                            showTicketUrl = true
                                            showTicketInput = true
                                            showMessage(message = messageLoginTips1)
                                            val optionsStr = prefGet("2FAOptions") ?: "[]"
                                            val options = Json.decodeFromString<List<Int>>(optionsStr)
                                            val flag = if (options.contains(4)) 4 else 8
                                            prefSet("2FAFlag", flag.toString())
                                            val url = prefGet("notificationUrl")
                                            if (!url.isNullOrBlank()) {
                                                uriHandler.openUri(url)
                                            }
                                        }

                                        6 -> {
                                            showMessage(message = messageLoginTips2)
                                            val url = prefGet("captchaUrl")
                                            if (!url.isNullOrBlank()) {
                                                uriHandler.openUri("https://account.xiaomi.com$url")
                                            }
                                        }
                                    }
                                }
                            }
                        )
                        Spacer(Modifier.width(16.dp))
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

    // 登出对话框
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
                            val boolean = Login().logout(isLogin)
                            if (boolean) showMessage(message = messageLogoutSuccessful)
                        }
                        showDialog.value = false
                    }
                )
                Spacer(Modifier.width(16.dp))
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
