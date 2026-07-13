package data.repository

import data.DataHelper
import kotlinx.coroutines.flow.StateFlow

sealed interface LoginState {
    data object NotLoggedIn : LoginState
    data class LoggedIn(val loginData: DataHelper.LoginData) : LoginState
    data class Expired(val loginData: DataHelper.LoginData) : LoginState
}

interface SessionRepository {
    val state: StateFlow<LoginState>
    suspend fun load()
    suspend fun save(loginData: DataHelper.LoginData)
    suspend fun clear()
}
