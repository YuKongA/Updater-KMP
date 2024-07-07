package misc

import androidx.compose.runtime.MutableState
import data.IconInfoHelper
import data.RomInfoHelper
import data.RomInfoStateHelper
import iconLink
import kotlinx.serialization.json.Json

val json = Json { ignoreUnknownKeys = true }

fun handleRomInfo(
    recoveryRomInfo: RomInfoHelper.RomInfo,
    romInfo: RomInfoHelper.Rom?,
    romInfoState: MutableState<RomInfoStateHelper>,
    iconInfo: MutableState<List<IconInfoHelper>>,
    officialDownload: String
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
            IconInfoHelper(
                iconName = iconName,
                iconLink = iconLinks[iconName] ?: "",
                changelog = changelog[index]
            )
        }
        romInfoState.value = RomInfoStateHelper(
            type = romInfo.type.toString(),
            device = romInfo.device.toString(),
            version = romInfo.version.toString(),
            codebase = romInfo.codebase.toString(),
            branch = romInfo.branch.toString(),
            bigVersion = when {
                romInfo.osbigversion != "0.0" -> "HyperOS " + romInfo.osbigversion
                romInfo.bigversion.contains("816") -> romInfo.bigversion.replace("816", "HyperOS 1.0")
                else -> "MIUI ${romInfo.bigversion}"
            },
            fileName = romInfo.filename.toString().substringBefore(".zip") + ".zip",
            fileSize = romInfo.filesize.toString(),
            officialDownload = officialDownload,
            cdn1Download = "https://cdnorg.d.miui.com/" + romInfo.version + "/" + romInfo.filename,
            cdn2Download = "https://bkt-sgp-miui-ota-update-alisgp.oss-ap-southeast-1.aliyuncs.com/" + romInfo.version + "/" + romInfo.filename,
            changelog = log.toString().trimEnd()
        )
    } else {
        clearRomInfo(romInfoState)
    }
}

fun clearRomInfo(romInfoState: MutableState<RomInfoStateHelper>) {
    romInfoState.value = RomInfoStateHelper()
}