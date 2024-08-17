package ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
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
import top.yukonga.miuix.kmp.MiuixSuperDialog
import top.yukonga.miuix.kmp.basic.MiuixButton
import top.yukonga.miuix.kmp.basic.MiuixText
import top.yukonga.miuix.kmp.basic.MiuixTextField
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.MiuixDialogUtil
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
    var account by remember { mutableStateOf(getPassword().first) }
    var password by remember { mutableStateOf(getPassword().second) }

    var global by rememberSaveable { mutableStateOf(false) }
    var savePassword by rememberSaveable { mutableStateOf(perfGet("savePassword") ?: "0") }
    val showDialog = remember { mutableStateOf(false) }

    val hapticFeedback = LocalHapticFeedback.current

    val icon = when (isLogin.value) {
        1 -> Icons.AutoMirrored.Outlined.Logout
        else -> Icons.AutoMirrored.Outlined.Login
    }

    val messageLoginIn = stringResource(Res.string.logging_in)
    val messageLoginSuccess = stringResource(Res.string.login_successful)
    val messageEmpty = stringResource(Res.string.account_or_password_empty)
    val messageError = stringResource(Res.string.login_error)
    val messageSecurityError = stringResource(Res.string.security_error)
    val messageLogoutSuccessful = stringResource(Res.string.logout_successful)
    val messageCrashInfo = stringResource(Res.string.toast_crash_info)

    IconButton(
        modifier = Modifier.size(32.dp),
        onClick = {
            showDialog.value = true
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MiuixTheme.colorScheme.onBackground
        )
    }
    MiuixDialogUtil.showDialog(
        visible = showDialog,
        content = {
            if (isLogin.value != 1) {
                MiuixSuperDialog(
                    title = stringResource(Res.string.login),
                    onDismissRequest = { showDialog.value = false }
                ) {
                    Column {
                        MiuixTextField(
                            value = account,
                            onValueChange = { account = it },
                            label = stringResource(Res.string.account),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )
                        val passwordVisibility by remember { mutableStateOf(false) }
                        MiuixTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = stringResource(Res.string.password),
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                            singleLine = true,
                            visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
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
                                    modifier = Modifier
                                        .height(22.dp)
                                        .padding(start = 0.dp, end = 10.dp)
                                        .size(22.dp),
                                    checked = global,
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = MiuixTheme.colorScheme.primary,
                                        uncheckedColor = MiuixTheme.colorScheme.subTextField,
                                        checkmarkColor = MiuixTheme.colorScheme.background
                                    ),
                                    onCheckedChange = {
                                        global = it
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    })
                                MiuixText(
                                    text = stringResource(Res.string.global),
                                    fontSize = MaterialTheme.typography.bodyMedium.fontSize
                                )
                            }
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Checkbox(
                                    modifier = Modifier
                                        .height(22.dp)
                                        .padding(start = 0.dp, end = 10.dp)
                                        .size(22.dp),
                                    checked = savePassword == "1",
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = MiuixTheme.colorScheme.primary,
                                        uncheckedColor = MiuixTheme.colorScheme.subTextField,
                                        checkmarkColor = MiuixTheme.colorScheme.background
                                    ),
                                    onCheckedChange = {
                                        savePassword = if (it) "1" else "0"
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    })
                                MiuixText(
                                    text = stringResource(Res.string.save_password),
                                    fontSize = MaterialTheme.typography.bodyMedium.fontSize
                                )
                            }
                        }
                        Row {
                            MiuixButton(
                                modifier = Modifier.weight(1f),
                                text = stringResource(Res.string.login),
                                submit = true,
                                onClick = {
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
                                    showDialog.value = false
                                }
                            )
                            Spacer(Modifier.width(20.dp))
                            MiuixButton(
                                modifier = Modifier.weight(1f),
                                text = stringResource(Res.string.cancel),
                                onClick = {
                                    showDialog.value = false
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            )
                        }
                    }
                }
            } else {
                MiuixSuperDialog(
                    title = stringResource(Res.string.logout),
                    summary = "",
                    onDismissRequest = { showDialog.value = false }
                ) {
                    Row {
                        MiuixButton(
                            modifier = Modifier.weight(1f),
                            text = stringResource(Res.string.logout),
                            submit = true,
                            onClick = {
                                coroutineScope.launch {
                                    val boolean = logout(isLogin)
                                    if (boolean) showMessage(message = messageLogoutSuccessful)
                                }
                                showDialog.value = false
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        )
                        Spacer(Modifier.width(20.dp))
                        MiuixButton(
                            modifier = Modifier.weight(1f),
                            text = stringResource(Res.string.cancel),
                            onClick = {
                                showDialog.value = false
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        )
                    }
                }
            }
        }
    )
}