package misc

import androidx.compose.runtime.MutableState
import data.IconInfo
import data.RomInfoHelper
import iconLink
import kotlinx.serialization.json.Json

val json = Json { ignoreUnknownKeys = true }

fun handleRomInfo(
    recoveryRomInfo: RomInfoHelper.RomInfo,
    romInfo: RomInfoHelper.Rom?,
    curRomInfo: List<MutableState<String>>,
    iconInfo: MutableState<List<IconInfo>>
) {
    if (romInfo?.bigversion != null) {
        val log = StringBuilder()
        romInfo.changelog!!.forEach { log.append(it.key).append("\n·").append(it.value.txt.joinToString("\n·")).append("\n\n") }
        val changelogGroups = log.toString().trimEnd().split("\n\n")
        val iconNames = changelogGroups.map { it.split("\n").first() }
        val changelog = changelogGroups.map { it.split("\n").drop(1).joinToString("\n") }

        val iconLink = recoveryRomInfo.fileMirror!!.icon
        val iconNameLink = recoveryRomInfo.icon!!
        val iconLinks = iconLink(iconLink, iconNameLink, iconNames)
        iconInfo.value = iconNames.mapIndexed { index, iconName ->
            IconInfo(
                iconName = iconName,
                iconLink = iconLinks[iconName] ?: "",
                changelog = changelog[index]
            )
        }

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