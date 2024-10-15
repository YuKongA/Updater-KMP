package ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Login
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import getPassword
import kotlinx.coroutines.launch
import login
import logout
import misc.MessageUtils.Companion.showMessage
import org.jetbrains.compose.resources.stringResource
import perfGet
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Checkbox
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.extra.SuperDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.MiuixPopupUtil.Companion.dismissDialog
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

@Composable
fun LoginDialog(
    isLogin: MutableState<Int>
) {
    val coroutineScope = rememberCoroutineScope()
    var account by mutableStateOf(getPassword().first)
    var password by mutableStateOf(getPassword().second)

    var global by remember { mutableStateOf(false) }
    var savePassword by remember { mutableStateOf(perfGet("savePassword") ?: "0") }
    val showDialog = remember { mutableStateOf(false) }

    val hapticFeedback = LocalHapticFeedback.current

    val icon = when (isLogin.value) {
        1 -> Icons.AutoMirrored.Rounded.Logout
        else -> Icons.AutoMirrored.Rounded.Login
    }

    val messageLoginIn = stringResource(Res.string.logging_in)
    val messageLoginSuccess = stringResource(Res.string.login_successful)
    val messageEmpty = stringResource(Res.string.account_or_password_empty)
    val messageError = stringResource(Res.string.login_error)
    val messageSecurityError = stringResource(Res.string.security_error)
    val messageLogoutSuccessful = stringResource(Res.string.logout_successful)
    val messageCrashInfo = stringResource(Res.string.toast_crash_info)

    IconButton(
        onClick = {
            showDialog.value = true
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MiuixTheme.colorScheme.onSurface
        )
    }

    if (isLogin.value != 1) {
        SuperDialog(
            show = showDialog,
            title = stringResource(Res.string.login),
            onDismissRequest = {
                dismissDialog(showDialog)
            }
        ) {
            Column {
                TextField(
                    value = account,
                    onValueChange = { account = it },
                    label = stringResource(Res.string.account),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                var passwordVisibility by remember { mutableStateOf(false) }
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = stringResource(Res.string.password),
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(
                            onClick = { passwordVisibility = !passwordVisibility },
                            content = {
                                Icon(
                                    imageVector = Icons.Rounded.Visibility,
                                    tint = if (passwordVisibility) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSecondaryContainer,
                                    contentDescription = null
                                )
                            }
                        )
                    }
                )
                Row(
                    modifier = Modifier.padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Checkbox(
                            modifier = Modifier.padding(end = 10.dp),
                            checked = global,
                            onCheckedChange = {
                                global = it
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            })
                        Text(
                            text = stringResource(Res.string.global)
                        )
                    }
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Checkbox(
                            modifier = Modifier.padding(end = 10.dp),
                            checked = savePassword == "1",
                            onCheckedChange = {
                                savePassword = if (it) "1" else "0"
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            })
                        Text(
                            text = stringResource(Res.string.save_password)
                        )
                    }
                }
                Row {
                    Button(
                        modifier = Modifier.weight(1f),
                        text = stringResource(Res.string.login),
                        submit = true,
                        onClick = {
                            dismissDialog(showDialog)
                            showMessage(message = messageLoginIn)
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            coroutineScope.launch {
                                val int = login(account, password, global, savePassword, isLogin)
                                when (int) {
                                    0 -> showMessage(message = messageLoginSuccess)
                                    1 -> showMessage(message = messageEmpty)
                                    2 -> showMessage(message = messageCrashInfo)
                                    3 -> showMessage(message = messageError)
                                    4 -> showMessage(message = messageSecurityError)
                                }
                            }
                        }
                    )
                    Spacer(Modifier.width(20.dp))
                    Button(
                        modifier = Modifier.weight(1f),
                        text = stringResource(Res.string.cancel),
                        onClick = {
                            dismissDialog(showDialog)
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    )
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
                dismissDialog(showDialog)
            }
        ) {
            Row {
                Button(
                    modifier = Modifier.weight(1f),
                    text = stringResource(Res.string.logout),
                    submit = true,
                    onClick = {
                        coroutineScope.launch {
                            val boolean = logout(isLogin)
                            if (boolean) showMessage(message = messageLogoutSuccessful)
                        }
                        dismissDialog(showDialog)
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                )
                Spacer(Modifier.width(20.dp))
                Button(
                    modifier = Modifier.weight(1f),
                    text = stringResource(Res.string.cancel),
                    onClick = {
                        dismissDialog(showDialog)
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                )
            }
        }
    }
}
