package ui

import Login
import Password
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.seiko.imageloader.rememberImagePainter
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
import updater.composeapp.generated.resources.login_tips1
import updater.composeapp.generated.resources.login_tips2
import updater.composeapp.generated.resources.logout
import updater.composeapp.generated.resources.logout_confirm
import updater.composeapp.generated.resources.logout_successful
import updater.composeapp.generated.resources.password
import updater.composeapp.generated.resources.save_password
import updater.composeapp.generated.resources.security_error
import updater.composeapp.generated.resources.send_email_code
import updater.composeapp.generated.resources.send_phone_code
import updater.composeapp.generated.resources.submit
import updater.composeapp.generated.resources.toast_crash_info
import updater.composeapp.generated.resources.verification_code
import updater.composeapp.generated.resources.verification_code_get
import updater.composeapp.generated.resources.verifying
import utils.MessageUtils.Companion.showMessage

@Composable
fun LoginDialog(
    showDialog: MutableState<Boolean>,
    isLogin: MutableState<Int>
) {
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    var account by remember { mutableStateOf(Password().getPassword().first) }
    var password by remember { mutableStateOf(Password().getPassword().second) }

    var global by remember { mutableStateOf(false) }
    var savePassword by remember { mutableStateOf(prefGet("savePassword") ?: "0") }

    var showCaptchaUrl by remember { mutableStateOf(false) }
    var showCaptchaInput by remember { mutableStateOf(false) }

    var showTicketUrl by remember { mutableStateOf(false) }
    var showTicketInput by remember { mutableStateOf(false) }

    var ticket by remember { mutableStateOf("") }
    var isVerifying by remember { mutableStateOf(false) }

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
    val messageLoginTips1 = stringResource(Res.string.login_tips1)
    val messageLoginTips2 = stringResource(Res.string.login_tips2)

    // 主页登录按钮
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
                                    imageVector = MiuixIcons.Useful.Rename,
                                    tint = if (passwordVisibility) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSecondaryContainer,
                                    contentDescription = null
                                )
                            }
                        )
                    }
                )

                // 图片验证码
                AnimatedVisibility(
                    visible = showCaptchaUrl
                ) {
                    // 获取图片验证码
                    AnimatedVisibility(
                        visible = !showCaptchaInput
                    ) {
                        TextButton(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                            text = stringResource(Res.string.verification_code_get),
                            onClick = {
                                showCaptchaInput = true
                            },
                            colors = ButtonDefaults.textButtonColorsPrimary()
                        )
                    }
                    // 显示图片验证码
                    AnimatedVisibility(
                        visible = showCaptchaInput
                    ) {
                        val captchaUrl = "https://account.xiaomi.com" + prefGet("captchaUrl")
                        Image(
                            painter = rememberImagePainter(captchaUrl),
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                            contentDescription = "Captcha"
                        )
                    }
                }

                // 发送二次认证验证码
                AnimatedVisibility(
                    visible = showTicketUrl
                ) {
                    AnimatedVisibility(
                        visible = !showTicketInput
                    ) {
                        val optionsStr = prefGet("2FAOptions") ?: "[]"
                        val options = Json.decodeFromString<List<Int>>(optionsStr)
                        Column {
                            if (options.contains(4)) {
                                TextButton(
                                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                                    text = stringResource(Res.string.send_phone_code),
                                    onClick = {
                                        coroutineScope.launch {
                                            Login().login(
                                                account = account,
                                                password = password,
                                                global = global,
                                                savePassword = savePassword,
                                                isLogin = isLogin,
                                                flag = 4,
                                            )
                                            prefSet("2FAFlag", "4")
                                        }
                                        showTicketInput = true
                                    },
                                    colors = ButtonDefaults.textButtonColorsPrimary()
                                )
                            }
                            if (options.contains(8)) {
                                TextButton(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                                    text = stringResource(Res.string.send_email_code),
                                    onClick = {
                                        coroutineScope.launch {
                                            Login().login(
                                                account = account,
                                                password = password,
                                                global = global,
                                                savePassword = savePassword,
                                                isLogin = isLogin,
                                                flag = 8,
                                            )
                                            prefSet("2FAFlag", "8")
                                        }
                                        showTicketInput = true
                                    },
                                    colors = ButtonDefaults.textButtonColorsPrimary()
                                )
                            }
                        }
                    }
                }

                // 图片验证码 & 二次认证验证码输入框
                AnimatedVisibility(
                    visible = showTicketInput || showCaptchaInput
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
                                                showMessage(messageLoginSuccess)
                                                ticket = ""
                                                prefRemove("2FAContext")
                                                prefRemove("2FAOptions")
                                                prefRemove("2FAFlag")
                                                prefRemove("identity_session")
                                                showTicketInput = false
                                                showTicketUrl = false
                                                showDialog.value = false
                                            } else {
                                                showMessage(messageError)
                                            }
                                            isVerifying = false
                                        }
                                    } else if (showCaptchaInput) {
                                        // 提交图片验证码
                                        coroutineScope.launch {
                                            isVerifying = true
                                            val int = Login().login(
                                                account = account,
                                                password = password,
                                                global = global,
                                                savePassword = savePassword,
                                                isLogin = isLogin,
                                                captcha = ticket
                                            )
                                            if (int == 0) {
                                                showMessage(messageLoginSuccess)
                                                ticket = ""
                                                prefRemove("captchaUrl")
                                                showCaptchaInput = false
                                                showCaptchaUrl = false
                                                showDialog.value = false
                                            } else {
                                                showMessage(messageError)
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
                                    showCaptchaUrl = false
                                    showTicketInput = false
                                    showCaptchaInput = false
                                    ticket = ""
                                }
                            )
                        }
                    }
                }

                // 国际账号 & 保存密码
                AnimatedVisibility(
                    visible = !showTicketUrl && !showCaptchaUrl && !showTicketInput && !showCaptchaInput
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
                    visible = !showTicketInput && !showCaptchaInput
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
                                            showMessage(message = messageLoginTips1)
                                        }

                                        6 -> {
                                            showCaptchaUrl = true
                                            showMessage(message = messageLoginTips2)
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
