package data.repository

import data.DataHelper
import data.LoginResult

interface LoginService {
    suspend fun login(
        account: String,
        password: String,
        global: Boolean,
        savePassword: Boolean,
        captcha: String = "",
        flag: Int? = null,
        ticket: String = "",
    ): LoginResult

    suspend fun sendTicket(flag: Int): Boolean

    suspend fun refreshServiceToken(loginData: DataHelper.LoginData): DataHelper.LoginData?
}
