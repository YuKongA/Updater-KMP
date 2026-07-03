import cli.DevicesCommand
import cli.LoginCommand
import cli.LogoutCommand
import cli.QueryCommand
import cli.StatusCommand
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.versionOption
import misc.VersionInfo

class Updater : CliktCommand(name = "updater") {
    init {
        versionOption("${VersionInfo.VERSION_NAME} (${VersionInfo.VERSION_CODE})")
    }

    override fun run() = Unit
}

fun main(args: Array<String>) = Updater()
    .subcommands(QueryCommand(), LoginCommand(), StatusCommand(), LogoutCommand(), DevicesCommand())
    .main(args)
