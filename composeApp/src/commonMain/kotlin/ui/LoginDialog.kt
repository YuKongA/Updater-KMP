package ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import getPassword
import kotlinx.coroutines.launch
import login
import logout
import misc.MessageUtils.Companion.showMessage
import org.jetbrains.compose.resources.stringResource
import perfGet
import updaterkmp.composeapp.generated.resources.Res
import updaterkmp.composeapp.generated.resources.account
import updaterkmp.composeapp.generated.resources.account_or_password_empty
import updaterkmp.composeapp.generated.resources.cancel
import updaterkmp.composeapp.generated.resources.global
import updaterkmp.composeapp.generated.resources.logging_in
import updaterkmp.composeapp.generated.resources.login
import updaterkmp.composeapp.generated.resources.login_error
import updaterkmp.composeapp.generated.resources.login_successful
import updaterkmp.composeapp.generated.resources.logout
import updaterkmp.composeapp.generated.resources.logout_successful
import updaterkmp.composeapp.generated.resources.password
import updaterkmp.composeapp.generated.resources.save_password
import updaterkmp.composeapp.generated.resources.security_error
import updaterkmp.composeapp.generated.resources.toast_crash_info

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginDialog(
    isLogin: MutableState<Int>
) {
    val coroutineScope = rememberCoroutineScope()
    var account by remember { mutableStateOf(TextFieldValue(getPassword().first)) }
    var password by remember { mutableStateOf(TextFieldValue(getPassword().second)) }

    var global by rememberSaveable { mutableStateOf(false) }
    var savePassword by rememberSaveable { mutableStateOf(perfGet("savePassword") ?: "0") }
    var showDialog by remember { mutableStateOf(false) }

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
        modifier = Modifier.widthIn(max = 48.dp),
        onClick = {
            showDialog = true
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface
        )
    }

    if (showDialog) {
        if (isLogin.value != 1) {
            BasicAlertDialog(
                onDismissRequest = { showDialog = false }
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(min = 350.dp, max = 380.dp)
                        .clip(RoundedCornerShape(30.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .padding(top = 24.dp, bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = stringResource(Res.string.login),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = MaterialTheme.typography.titleLarge.fontSize
                        )
                        Checkbox(
                            modifier = Modifier
                                .height(22.dp)
                                .padding(start = 0.dp, end = 10.dp)
                                .size(22.dp),
                            checked = global,
                            onCheckedChange = {
                                global = it
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            })
                        Text(
                            text = stringResource(Res.string.global),
                            fontSize = MaterialTheme.typography.titleMedium.fontSize
                        )
                    }
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        TextField(
                            value = account,
                            onValueChange = { account = it },
                            label = { Text(stringResource(Res.string.account)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                        )
                        var passwordVisibility by remember { mutableStateOf(false) }
                        TextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text(stringResource(Res.string.password)) },
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                            singleLine = true,
                            //keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(
                                    onClick = { passwordVisibility = !passwordVisibility }
                                ) {
                                    Icon(
                                        imageVector = if (passwordVisibility) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                        contentDescription = null
                                    )
                                }
                            })
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row {
                                Checkbox(
                                    modifier = Modifier
                                        .height(22.dp)
                                        .padding(start = 0.dp, end = 10.dp)
                                        .size(22.dp),
                                    checked = savePassword == "1",
                                    onCheckedChange = {
                                        savePassword = if (it) "1" else "0"
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    })
                                Text(
                                    text = stringResource(Res.string.save_password),
                                    fontSize = MaterialTheme.typography.bodyMedium.fontSize
                                )
                            }
                            Row {
                                TextButton(
                                    onClick = {
                                        showDialog = false
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    },
                                    modifier = Modifier.padding(end = 16.dp)
                                ) {
                                    Text(
                                        text = stringResource(Res.string.cancel),
                                        fontSize = MaterialTheme.typography.titleMedium.fontSize
                                    )
                                }
                                TextButton(
                                    onClick = {
                                        showMessage(message = messageLoginIn)
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        coroutineScope.launch {
                                            val int = login(account.text, password.text, global, savePassword, isLogin)
                                            when (int) {
                                                0 -> showMessage(message = messageLoginSuccess)
                                                1 -> showMessage(message = messageEmpty)
                                                2 -> showMessage(message = messageCrashInfo)
                                                3 -> showMessage(message = messageError)
                                                4 -> showMessage(message = messageSecurityError)
                                            }
                                        }
                                        showDialog = false
                                    }) {
                                    Text(
                                        text = stringResource(Res.string.login),
                                        fontSize = MaterialTheme.typography.titleMedium.fontSize
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            BasicAlertDialog(
                onDismissRequest = { showDialog = false }
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(min = 350.dp, max = 380.dp)
                        .clip(RoundedCornerShape(30.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .padding(top = 24.dp, bottom = 12.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.logout),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = MaterialTheme.typography.titleLarge.fontSize
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 80.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    showDialog = false
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                },
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
                                Text(
                                    text = stringResource(Res.string.cancel),
                                    modifier = Modifier,
                                    fontSize = MaterialTheme.typography.bodyMedium.fontSize
                                )
                            }
                            TextButton(
                                onClick = {
                                    coroutineScope.launch {
                                        val boolean = logout(isLogin)
                                        if (boolean) showMessage(message = messageLogoutSuccessful)
                                    }
                                    showDialog = false
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                }) {
                                Text(
                                    text = stringResource(Res.string.logout),
                                    modifier = Modifier,
                                    fontSize = MaterialTheme.typography.bodyMedium.fontSize
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}