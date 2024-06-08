package misc

import androidx.compose.runtime.MutableState
import data.RomInfoHelper
import kotlinx.serialization.json.Json

val json = Json { ignoreUnknownKeys = true }

fun handleRomInfo(
    romInfo: RomInfoHelper.Rom?,
    curRomInfo: List<MutableState<String>>
) {
    if (romInfo?.bigversion != null) {
        val log = StringBuilder()
        romInfo.changelog!!.forEach { log.append(it.key).append("\n- ").append(it.value.txt.joinToString("\n- ")).append("\n\n") }
        curRomInfo[0].value = romInfo.type.toString()
        curRomInfo[1].value = romInfo.device.toString()
        curRomInfo[2].value = romInfo.version.toString()
        curRomInfo[3].value = romInfo.codebase.toString()
        curRomInfo[4].value = romInfo.branch.toString()
        curRomInfo[5].value = romInfo.filename.toString().substringBefore(".zip") + ".zip"
        curRomInfo[6].value = romInfo.filesize.toString()
        curRomInfo[7].value = if (romInfo.bigversion.contains("816")) romInfo.bigversion.replace("816", "HyperOS 1.0") else "MIUI ${romInfo.bigversion}"
        curRomInfo[9].value = "https://cdnorg.d.miui.com/" + romInfo.version + "/" + romInfo.filename
        curRomInfo[10].value = "https://bkt-sgp-miui-ota-update-alisgp.oss-ap-southeast-1.aliyuncs.com/" + romInfo.version + "/" + romInfo.filename
        curRomInfo[11].value = log.toString().trimEnd()
    } else {
        clearRomInfo(curRomInfo)
    }
}

fun clearRomInfo(romInfo: List<MutableState<String>>) {
    romInfo.forEach { it.value = "" }
}