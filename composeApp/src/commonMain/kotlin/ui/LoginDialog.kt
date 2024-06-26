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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import getPassword
import kotlinx.coroutines.launch
import login
import logout
import misc.SnackbarUtils.Companion.showSnackbar
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
import updaterkmp.composeapp.generated.resources.request_sign_failed
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
    val messageSign = stringResource(Res.string.request_sign_failed)
    val messageError = stringResource(Res.string.login_error)
    val messageSecurityError = stringResource(Res.string.security_error)
    val messageLogoutSuccessful = stringResource(Res.string.logout_successful)
    val messageCrashInfo = stringResource(Res.string.toast_crash_info)

    IconButton(
        modifier = Modifier.widthIn(max = 48.dp),
        onClick = {
            showDialog = true
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface
        )
    }

    if (showDialog) {
        if (isLogin.value != 1) {
            BasicAlertDialog(
                onDismissRequest = { showDialog = false },
                content = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .size(280.dp, 275.dp)
                            .clip(RoundedCornerShape(30.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        Row(
                            modifier = Modifier.padding(24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                modifier = Modifier.weight(1f),
                                text = stringResource(Res.string.login),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                            )
                            Checkbox(
                                modifier = Modifier.height(22.dp).padding(start = 0.dp, end = 10.dp).size(22.dp),
                                checked = global,
                                onCheckedChange = {
                                    global = it
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                })
                            Text(
                                text = stringResource(Res.string.global),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Column(
                            modifier = Modifier.padding(horizontal = 24.dp).padding(top = 65.dp)
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
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
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
                                }
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row {
                                    Checkbox(
                                        modifier = Modifier.height(22.dp).padding(start = 0.dp, end = 10.dp).size(22.dp),
                                        checked = savePassword == "1",
                                        onCheckedChange = {
                                            savePassword = if (it) "1" else "0"
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        }
                                    )
                                    Text(
                                        text = stringResource(Res.string.save_password),
                                        style = MaterialTheme.typography.bodyMedium
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
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    TextButton(
                                        onClick = {
                                            showSnackbar(message = messageLoginIn)
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                            coroutineScope.launch {
                                                val int = login(account.text, password.text, global, savePassword, isLogin)
                                                when (int) {
                                                    0 -> showSnackbar(message = messageLoginSuccess)
                                                    1 -> showSnackbar(message = messageEmpty)
                                                    2 -> showSnackbar(message = messageSign)
                                                    3 -> showSnackbar(message = messageError)
                                                    4 -> showSnackbar(message = messageSecurityError)
                                                    5 -> showSnackbar(message = messageCrashInfo)
                                                }
                                            }
                                            showDialog = false
                                        }
                                    ) {
                                        Text(
                                            text = stringResource(Res.string.login),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            )
        } else {
            BasicAlertDialog(
                onDismissRequest = { showDialog = false },
                content = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .size(280.dp, 165.dp)
                            .clip(RoundedCornerShape(30.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        Box(modifier = Modifier.padding(24.dp)) {
                            Text(
                                text = stringResource(Res.string.logout),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
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
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                TextButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            val boolean = logout(isLogin)
                                            if (boolean) showSnackbar(message = messageLogoutSuccessful)
                                        }
                                        showDialog = false
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                ) {
                                    Text(
                                        text = stringResource(Res.string.logout),
                                        modifier = Modifier,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}