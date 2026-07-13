package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.mordant.terminal.Terminal
import com.github.ajalt.mordant.terminal.prompt
import data.LoginResult
import kotlinx.coroutines.runBlocking

class LoginCommand : CliktCommand(name = "login") {
    override fun help(context: Context) = "Sign in with a Xiaomi account"

    private val account by option("--account", help = "Xiaomi account id / email / phone").prompt(text = "Account")
    private val password by option("--password", hidden = true).prompt(text = "Password", hideInput = true)
    private val global by option("--global", help = "Use the international (intl) account endpoint").flag()
    private val savePassword by option("--save-password", help = "Persist credentials in the config store").flag()

    override fun run() {
        val t = currentContext.terminal
        val exit = runBlocking {
            handle(t, CliDi.loginService.login(account, password, global, savePassword))
        }
        throw ProgramResult(exit)
    }

    private suspend fun handle(t: Terminal, result: LoginResult): Int = when (result) {
        is LoginResult.Success -> {
            CliDi.session.save(result.loginData)
            CliDi.loginFlow.clearTransient() // drop identity_session / 2FA context, matching the GUI contract
            t.println("Logged in as ${result.loginData.userId} (${result.loginData.accountType})")
            ExitCodes.OK
        }

        is LoginResult.TwoFactorRequired -> runTwoFactor(t, result.options)
        LoginResult.EmptyCredentials -> fail("account/password must not be empty", ExitCodes.USAGE)
        LoginResult.NetworkError -> fail("network or server failure", ExitCodes.NETWORK)
        LoginResult.LoginFailed -> fail("login failed (wrong credentials or captcha required)", ExitCodes.AUTH)
        LoginResult.SecurityError -> fail("security validation failed", ExitCodes.AUTH)
        LoginResult.TwoFactorUnsupported -> fail("this account's 2FA mode is unsupported", ExitCodes.AUTH)
        LoginResult.VerificationCodeError -> fail("wrong verification code", ExitCodes.AUTH)
    }

    private fun fail(message: String, code: Int): Int {
        echo("error: $message", err = true)
        return code
    }

    private suspend fun runTwoFactor(t: Terminal, options: List<Int>): Int {
        val labels = options.associateWith { if (it == 4) "SMS" else "Email" }
        t.println(
            "Two-factor verification required. Methods: " +
                    labels.entries.joinToString { "${it.key}=${it.value}" })
        val flag = t.prompt("Method")?.trim()?.toIntOrNull()?.takeIf { it in options }
            ?: return fail("invalid method selection", ExitCodes.USAGE)
        if (!CliDi.loginService.sendTicket(flag)) {
            return fail("failed to send verification code", ExitCodes.NETWORK)
        }
        t.println("Verification code sent.")
        repeat(3) {
            val ticket = t.prompt("Code")?.trim()
                ?: return fail("no verification code entered", ExitCodes.USAGE)
            val result = CliDi.loginService.login(
                account, password, global, savePassword, flag = flag, ticket = ticket
            )
            if (result is LoginResult.VerificationCodeError) {
                t.println("Wrong code, try again.")
            } else {
                return handle(t, result)
            }
        }
        return fail("too many wrong verification codes", ExitCodes.AUTH)
    }
}
