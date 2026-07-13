package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import data.repository.LoginState
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable

@Serializable
data class StatusJsonOutput(
    val loggedIn: Boolean,
    val expired: Boolean = false,
    val userId: String? = null,
    val accountType: String? = null,
)

class StatusCommand : CliktCommand(name = "status") {
    override fun help(context: Context) = "Show login session status"

    private val json by option("--json", help = "Print machine-readable JSON").flag()

    override fun run() {
        val exit = runBlocking {
            CliDi.session.load()
            val out = when (val s = CliDi.session.state.value) {
                LoginState.NotLoggedIn -> StatusJsonOutput(loggedIn = false)
                is LoginState.LoggedIn -> StatusJsonOutput(true, false, s.loginData.userId, s.loginData.accountType)
                is LoginState.Expired -> StatusJsonOutput(false, true, s.loginData.userId, s.loginData.accountType)
            }
            if (json) {
                echo(cliJson.encodeToString(out))
            } else {
                echo(
                    when {
                        out.loggedIn -> "Logged in as ${out.userId} (${out.accountType})"
                        out.expired -> "Session expired for ${out.userId}; run `updater login`"
                        else -> "Not logged in"
                    }
                )
            }
            if (out.loggedIn) ExitCodes.OK else ExitCodes.AUTH
        }
        throw ProgramResult(exit)
    }
}

class LogoutCommand : CliktCommand(name = "logout") {
    override fun help(context: Context) = "Clear the stored session and credentials"

    override fun run() {
        runBlocking {
            CliDi.session.clear()
            CliDi.credentials.delete()
        }
        echo("Logged out.")
    }
}
