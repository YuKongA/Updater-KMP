package cli

import data.repository.LoginState
import di.AppContainer
import kotlinx.serialization.json.Json

object ExitCodes {
    const val OK = 0
    const val USAGE = 1
    const val NETWORK = 2
    const val NOT_FOUND = 3
    const val AUTH = 4
}

val cliJson = Json {
    encodeDefaults = true
    prettyPrint = true
}

suspend fun loadedLoginData(): data.DataHelper.LoginData? {
    AppContainer.session.load()
    return (AppContainer.session.state.value as? LoginState.LoggedIn)?.loginData
}
