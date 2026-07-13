package cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.terminal
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import data.DataHelper
import data.DeviceInfoHelper
import data.usecase.RomInfoQuery
import data.usecase.RomInfoResult
import data.usecase.persistTo
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable

@Serializable
data class QueryJsonOutput(
    val codeName: String,
    val curRom: DataHelper.RomInfoData,
    val incRom: DataHelper.RomInfoData,
    val xms: DataHelper.XmsInfoData,
    val noUltimateLink: Boolean,
    val isFallback: Boolean,
)

class QueryCommand : CliktCommand(name = "query") {
    override fun help(context: Context) = "Query ROM updates for a device"

    private val device by argument(help = "Device codename or marketing name")
    private val region by option("-r", "--region", help = "Region code, e.g. CN, MI (global), EU (eea), TW").default("CN")
    private val carrier by option("-c", "--carrier", help = "Carrier code, e.g. XM, DM").default("XM")
    private val androidVersion by option("-a", "--android", help = "Android version, e.g. 16.0").default("16.0")
    private val systemVersion by option("-o", "--os", help = "System version, e.g. OS2.0.105.0.WOCCNXM").required()
    private val rustVersion by option("--rust-version", help = "Rust runtime version sent to the XMS API").default("1.3.0")
    private val json by option("--json", help = "Print machine-readable JSON").flag()

    override fun run() {
        val regionName = DeviceInfoHelper.regionNameOfCode(region.uppercase()) ?: run {
            echo("error: unknown region code '$region'", err = true)
            throw ProgramResult(ExitCodes.USAGE)
        }
        val carrierName = DeviceInfoHelper.carrierNameOfCode(carrier.uppercase()) ?: run {
            echo("error: unknown carrier code '$carrier'", err = true)
            throw ProgramResult(ExitCodes.USAGE)
        }

        val t = currentContext.terminal
        val exit = runBlocking {
            val repo = CliDi.deviceListRepository
            repo.load()
            val (codeName, deviceName) = resolveDevice(repo.deviceNameOf(device), repo.codeNameOf(device))
            val loginData = loadedLoginData()

            val outcome = CliDi.fetchRomInfoUseCase.fetch(
                RomInfoQuery(
                    deviceName = deviceName,
                    codeName = codeName,
                    deviceRegion = regionName,
                    deviceCarrier = carrierName,
                    androidVersion = androidVersion,
                    systemVersion = systemVersion,
                    rustVersion = rustVersion,
                    loginData = loginData,
                )
            )
            outcome.sessionUpdate.persistTo(CliDi.session)

            when (val result = outcome.result) {
                RomInfoResult.NetworkError -> {
                    echo("error: network or server failure", err = true)
                    ExitCodes.NETWORK
                }

                RomInfoResult.NoData -> {
                    echo("No ROM found for $codeName ($systemVersion)", err = true)
                    ExitCodes.NOT_FOUND
                }

                is RomInfoResult.Found -> {
                    if (json) {
                        echo(
                            cliJson.encodeToString(
                                QueryJsonOutput(
                                    codeName = codeName,
                                    curRom = result.curRomInfo,
                                    incRom = result.incRomInfo,
                                    xms = result.xmsInfo,
                                    noUltimateLink = result.noUltimateLink,
                                    isFallback = result.isFallback,
                                )
                            )
                        )
                    } else {
                        if (result.isFallback) t.println("note: exact version not found, showing latest available")
                        if (result.noUltimateLink) t.println("note: official link unavailable, CDN links only")
                        t.renderRom("Current ROM", result.curRomInfo)
                        t.renderRom("Incremental ROM", result.incRomInfo)
                        t.renderXms(result.xmsInfo)
                    }
                    ExitCodes.OK
                }
            }
        }
        throw ProgramResult(exit)
    }

    /** Codename match wins, then marketing name; unknown input is passed through for the server to judge. */
    private fun resolveDevice(nameByCode: String, codeByName: String): Pair<String, String> = when {
        nameByCode.isNotEmpty() -> device to nameByCode
        codeByName.isNotEmpty() -> codeByName to device
        else -> device to device
    }
}
