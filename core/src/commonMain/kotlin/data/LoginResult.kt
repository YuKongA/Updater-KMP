package data

sealed interface LoginResult {
    data class Success(val loginData: DataHelper.LoginData) : LoginResult
    data class TwoFactorRequired(val options: List<Int>) : LoginResult
    data object EmptyCredentials : LoginResult
    data object NetworkError : LoginResult
    data object LoginFailed : LoginResult
    data object SecurityError : LoginResult
    data object TwoFactorUnsupported : LoginResult
    data object VerificationCodeError : LoginResult
}
