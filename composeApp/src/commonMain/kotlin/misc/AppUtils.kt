package misc

import androidx.compose.runtime.MutableState
import data.RomInfoHelper
import kotlinx.serialization.json.Json

val json = Json { ignoreUnknownKeys = true }

fun handleRomInfo(
    romInfo: RomInfoHelper.Rom?,
    type: MutableState<String>,
    device: MutableState<String>,
    version: MutableState<String>,
    codebase: MutableState<String>,
    branch: MutableState<String>,
    fileName: MutableState<String>,
    fileSize: MutableState<String>,
    bigVersion: MutableState<String>,
    cdn1Download: MutableState<String>,
    cdn2Download: MutableState<String>,
    changeLog: MutableState<String>
) {
    if (romInfo?.bigversion != null) {
        val log = StringBuilder()
        romInfo.changelog!!.forEach { log.append(it.key).append("\n- ").append(it.value.txt.joinToString("\n- ")).append("\n\n") }
        type.value = romInfo.type.toString()
        device.value = romInfo.device.toString()
        version.value = romInfo.version.toString()
        codebase.value = romInfo.codebase.toString()
        branch.value = romInfo.branch.toString()
        fileName.value = romInfo.filename.toString().substringBefore(".zip") + ".zip"
        fileSize.value = romInfo.filesize.toString()
        bigVersion.value = if (romInfo.bigversion.contains("816")) romInfo.bigversion.replace("816", "HyperOS 1.0") else "MIUI ${romInfo.bigversion}"
        cdn1Download.value = "https://cdnorg.d.miui.com/" + romInfo.version + "/" + romInfo.filename
        cdn2Download.value = "https://bkt-sgp-miui-ota-update-alisgp.oss-ap-southeast-1.aliyuncs.com/" + romInfo.version + "/" + romInfo.filename
        changeLog.value = log.toString().trimEnd()
    } else {
        type.value = ""
        device.value = ""
        version.value = ""
        codebase.value = ""
        branch.value = ""
        fileName.value = ""
        fileSize.value = ""
        bigVersion.value = ""
        cdn1Download.value = ""
        cdn2Download.value = ""
        changeLog.value = ""
    }
}