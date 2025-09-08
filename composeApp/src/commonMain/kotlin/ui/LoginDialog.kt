package ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import getCaptchaInfo
import getPassword
import kotlinx.coroutines.launch
import login
import logout
import misc.MessageUtils.Companion.showMessage
import org.jetbrains.compose.resources.stringResource
import platform.loadImageFromUrl
import platform.prefGet
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
import updater.composeapp.generated.resources.captcha_invalid
import updater.composeapp.generated.resources.captcha_required
import updater.composeapp.generated.resources.enter_captcha
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
    var captchaCode by remember { mutableStateOf("") }

    var global by remember { mutableStateOf(false) }
    var savePassword by remember { mutableStateOf(prefGet(PASSWORD_SAVE_KEY) ?: PASSWORD_SAVE_DISABLED) }
    val showDialog = remember { mutableStateOf(false) }
    var showCaptcha by remember { mutableStateOf(false) }
    var captchaImageBitmap by remember { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    var isLoadingCaptcha by remember { mutableStateOf(false) }

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
    val messageCaptchaRequired = stringResource(Res.string.captcha_required)
    val messageCaptchaInvalid = stringResource(Res.string.captcha_invalid)

    val focusManager = LocalFocusManager.current

    // Load captcha image when needed
    LaunchedEffect(showCaptcha) {
        if (showCaptcha) {
            isLoadingCaptcha = true
            val captchaInfo = getCaptchaInfo()
            val captchaUrl = captchaInfo.first
            if (captchaUrl.isNotEmpty()) {
                captchaImageBitmap = loadImageFromUrl(captchaUrl)
            }
            isLoadingCaptcha = false
        }
    }

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
                showCaptcha = false
                captchaCode = ""
                captchaImageBitmap = null
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
                
                // Captcha section
                if (showCaptcha) {
                    Text(
                        text = stringResource(Res.string.captcha_required),
                        modifier = Modifier.padding(top = 16.dp),
                        color = MiuixTheme.colorScheme.primary
                    )
                    
                    // Captcha image
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            isLoadingCaptcha -> {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                            captchaImageBitmap != null -> {
                                Image(
                                    bitmap = captchaImageBitmap!!,
                                    contentDescription = "Captcha",
                                    modifier = Modifier
                                        .height(60.dp)
                                        .clickable {
                                            // Reload captcha on click
                                            coroutineScope.launch {
                                                isLoadingCaptcha = true
                                                val captchaInfo = getCaptchaInfo()
                                                val captchaUrl = captchaInfo.first
                                                if (captchaUrl.isNotEmpty()) {
                                                    captchaImageBitmap = loadImageFromUrl(captchaUrl)
                                                }
                                                isLoadingCaptcha = false
                                            }
                                        }
                                )
                            }
                            else -> {
                                Text(
                                    text = "Failed to load captcha",
                                    color = MiuixTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    
                    // Captcha input
                    TextField(
                        value = captchaCode,
                        onValueChange = { captchaCode = it },
                        label = stringResource(Res.string.enter_captcha),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                    )
                }
                Row {
                    TextButton(
                        modifier = Modifier.weight(1f),
                        text = stringResource(Res.string.login),
                        colors = ButtonDefaults.textButtonColorsPrimary(),
                        onClick = {
                            showDialog.value = false
                            showMessage(message = messageLoginIn)
                            coroutineScope.launch {
                                val captchaInfo = getCaptchaInfo()
                                val int = login(account, password, global, savePassword, isLogin, captchaCode, captchaInfo.second)
                                when (int) {
                                    0 -> {
                                        showMessage(message = messageLoginSuccess)
                                        showCaptcha = false
                                        captchaCode = ""
                                        captchaImageBitmap = null
                                    }
                                    1 -> showMessage(message = messageEmpty)
                                    2 -> showMessage(message = messageCrashInfo)
                                    3 -> showMessage(message = messageError)
                                    4 -> showMessage(message = messageSecurityError)
                                    5 -> {
                                        // Captcha required
                                        showMessage(message = messageCaptchaRequired)
                                        showCaptcha = true
                                        showDialog.value = true
                                    }
                                    6 -> {
                                        // Invalid captcha
                                        showMessage(message = messageCaptchaInvalid)
                                        captchaCode = ""
                                        showCaptcha = true
                                        showDialog.value = true
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
                            showCaptcha = false
                            captchaCode = ""
                            captchaImageBitmap = null
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
