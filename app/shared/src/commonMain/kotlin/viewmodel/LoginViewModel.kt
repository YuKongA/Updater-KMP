package viewmodel

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import data.LoginResult
import data.repository.LoginService
import data.repository.LoginState
import data.repository.SessionRepository
import data.storage.CredentialsStorage
import data.storage.LoginFlowStorage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import updater.app.shared.generated.resources.Res
import updater.app.shared.generated.resources.account_or_password_empty
import updater.app.shared.generated.resources.logging_in
import updater.app.shared.generated.resources.login_error
import updater.app.shared.generated.resources.login_successful
import updater.app.shared.generated.resources.login_tips
import updater.app.shared.generated.resources.logout_successful
import updater.app.shared.generated.resources.security_error
import updater.app.shared.generated.resources.send_code_error
import updater.app.shared.generated.resources.toast_crash_info
import updater.app.shared.generated.resources.two_factor_unsupported
import updater.app.shared.generated.resources.verification_code_error

sealed interface LoginEvent {
    data class GlobalChanged(val value: Boolean) : LoginEvent
    data class SavePasswordChanged(val value: Boolean) : LoginEvent
    data class VerificationRequested(val value: Boolean) : LoginEvent
    data class SelectTwoFactorMethod(val flag: Int) : LoginEvent
    data object LoginClicked : LoginEvent
    data object LogoutClicked : LoginEvent
    data object SubmitTwoFactor : LoginEvent
    data object CancelTicket : LoginEvent
    data object DismissDialog : LoginEvent
}

data class LoginUiState(
    val loginState: LoginState = LoginState.NotLoggedIn,
    val showLoginDialog: Boolean = false,
    val isGlobal: Boolean = false,
    val savePasswordEnabled: Boolean = false,
    val showTicketInput: Boolean = false,
    val availableTwoFactorOptions: List<Int> = emptyList(),
    val selectedTwoFactorFlag: Int? = null,
    val isVerifying: Boolean = false,
    val isVerificationRequested: Boolean = false,
    val isLoggingIn: Boolean = false,
)

class LoginViewModel(
    private val session: SessionRepository,
    private val credentials: CredentialsStorage,
    private val loginFlow: LoginFlowStorage,
    private val loginService: LoginService,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    val accountState = TextFieldState()
    val passwordState = TextFieldState()
    val ticketState = TextFieldState()

    private val _uiEvent = Channel<UiEvent>(Channel.BUFFERED)
    val uiEvent = _uiEvent.receiveAsFlow()

    private fun showMessage(resource: StringResource, duration: Long = 1000L) {
        _uiEvent.trySend(UiEvent.ShowMessage(resource, duration))
    }

    init {
        viewModelScope.launch {
            session.load()
            session.state.collect { newState ->
                _uiState.update { it.copy(loginState = newState) }
            }
        }
        viewModelScope.launch { loadCredentials() }
    }

    private suspend fun loadCredentials() {
        val savedCredentials = credentials.load()
        val savedSavePassword = credentials.isSaveEnabled()
        accountState.setTextAndPlaceCursorAtEnd(savedCredentials.account)
        passwordState.setTextAndPlaceCursorAtEnd(savedCredentials.password)
        _uiState.update {
            it.copy(savePasswordEnabled = savedSavePassword)
        }
    }

    fun showLoginDialog() {
        viewModelScope.launch { loginFlow.clearTransient() }
        ticketState.clearText()
        _uiState.update {
            it.copy(
                showLoginDialog = true,
                showTicketInput = false,
                availableTwoFactorOptions = emptyList(),
                selectedTwoFactorFlag = null,
                isVerifying = false,
                isLoggingIn = false,
                isVerificationRequested = false,
            )
        }
    }

    fun dismissLoginDialog() {
        _uiState.update { it.copy(showLoginDialog = false) }
    }

    fun onLoginEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.GlobalChanged -> _uiState.update { it.copy(isGlobal = event.value) }
            is LoginEvent.SavePasswordChanged -> _uiState.update { it.copy(savePasswordEnabled = event.value) }
            is LoginEvent.VerificationRequested -> _uiState.update { it.copy(isVerificationRequested = event.value) }
            is LoginEvent.SelectTwoFactorMethod -> selectTwoFactorMethod(event.flag)
            is LoginEvent.LoginClicked -> performLogin()
            is LoginEvent.LogoutClicked -> performLogout()
            is LoginEvent.SubmitTwoFactor -> submitTwoFactorTicket()
            is LoginEvent.CancelTicket -> {
                ticketState.clearText()
                _uiState.update {
                    it.copy(showTicketInput = false, availableTwoFactorOptions = emptyList())
                }
            }

            is LoginEvent.DismissDialog -> dismissLoginDialog()
        }
    }

    private fun selectTwoFactorMethod(flag: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(selectedTwoFactorFlag = flag) }
            val sent = loginService.sendTicket(flag)
            if (sent) {
                _uiState.update { it.copy(availableTwoFactorOptions = emptyList(), isVerificationRequested = true) }
            } else {
                showMessage(Res.string.send_code_error)
            }
        }
    }

    private fun performLogin() {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.update { it.copy(isLoggingIn = true) }
            showMessage(Res.string.logging_in)

            val result = loginService.login(
                account = accountState.text.toString(),
                password = passwordState.text.toString(),
                global = state.isGlobal,
                savePassword = state.savePasswordEnabled
            )
            handleLoginResult(result)
            _uiState.update { it.copy(isLoggingIn = false) }
        }
    }

    private fun performLogout() {
        viewModelScope.launch {
            session.clear()
            showMessage(Res.string.logout_successful)
            _uiState.update { it.copy(showLoginDialog = false) }
        }
    }

    private fun submitTwoFactorTicket() {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.update { it.copy(isVerifying = true) }
            val result = loginService.login(
                account = accountState.text.toString(),
                password = passwordState.text.toString(),
                global = state.isGlobal,
                savePassword = state.savePasswordEnabled,
                flag = state.selectedTwoFactorFlag,
                ticket = ticketState.text.toString(),
            )
            handleLoginResult(result)
            ticketState.clearText()
            _uiState.update { it.copy(isVerifying = false) }
        }
    }

    private suspend fun handleLoginResult(result: LoginResult) {
        when (result) {
            is LoginResult.Success -> {
                showMessage(Res.string.login_successful)
                session.save(result.loginData)
                _uiState.update {
                    it.copy(showLoginDialog = false)
                }
            }

            is LoginResult.TwoFactorRequired -> {
                showMessage(Res.string.login_tips)
                _uiState.update { it.copy(showTicketInput = true, availableTwoFactorOptions = result.options) }
            }

            LoginResult.EmptyCredentials -> showMessage(Res.string.account_or_password_empty)
            LoginResult.NetworkError -> showMessage(Res.string.toast_crash_info)
            LoginResult.LoginFailed -> {
                showMessage(Res.string.login_error)
                credentials.deleteOnlyPassword()
            }

            LoginResult.SecurityError -> showMessage(Res.string.security_error)
            LoginResult.TwoFactorUnsupported -> showMessage(Res.string.two_factor_unsupported)
            LoginResult.VerificationCodeError -> showMessage(Res.string.verification_code_error)
        }
    }
}
