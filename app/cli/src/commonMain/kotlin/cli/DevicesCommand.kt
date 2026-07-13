package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.mordant.table.table
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable

@Serializable
data class DeviceJsonEntry(val deviceName: String, val codeName: String, val deviceCode: String)

class DevicesCommand : CliktCommand(name = "devices") {
    override fun help(context: Context) = "Search the known device list"

    private val keyword by argument(help = "Filter by name or codename").optional()
    private val json by option("--json", help = "Print machine-readable JSON").flag()

    override fun run() = runBlocking {
        val repo = CliDi.deviceListRepository
        repo.load()
        val all = repo.devices.value
        val filtered = keyword?.let { k ->
            all.filter {
                it.deviceName.contains(k, ignoreCase = true) || it.deviceCodeName.contains(k, ignoreCase = true)
            }
        } ?: all
        if (json) {
            echo(cliJson.encodeToString(filtered.map { DeviceJsonEntry(it.deviceName, it.deviceCodeName, it.deviceCode) }))
        } else {
            currentContext.terminal.println(table {
                header { row("Device", "Codename", "Code") }
                body { filtered.forEach { row(it.deviceName, it.deviceCodeName, it.deviceCode) } }
            })
        }
    }
}
