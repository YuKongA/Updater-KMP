package data.repository

import data.DataHelper
import data.storage.PreferencesStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json

class SessionRepositoryImpl(private val prefs: PreferencesStorage) : SessionRepository {
    private val _state = MutableStateFlow<LoginState>(LoginState.NotLoggedIn)
    override val state: StateFlow<LoginState> = _state.asStateFlow()

    override suspend fun load() {
        val loginDataStr = prefs.get(LOGIN_KEY) ?: return
        val loginData = runCatching { Json.decodeFromString<DataHelper.LoginData>(loginDataStr) }.getOrNull() ?: return
        _state.value = toLoginState(loginData)
    }

    override suspend fun save(loginData: DataHelper.LoginData) {
        prefs.set(LOGIN_KEY, Json.encodeToString(loginData))
        _state.value = toLoginState(loginData)
    }

    override suspend fun clear() {
        prefs.remove(LOGIN_KEY)
        _state.value = LoginState.NotLoggedIn
    }

    private fun toLoginState(loginData: DataHelper.LoginData): LoginState = when (loginData.authResult) {
        "1" -> LoginState.LoggedIn(loginData)
        "3" -> LoginState.Expired(loginData)
        else -> LoginState.NotLoggedIn
    }

    companion object {
        private const val LOGIN_KEY = "loginInfo"
    }
}
