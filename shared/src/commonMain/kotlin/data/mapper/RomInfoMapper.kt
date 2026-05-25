package data.mapper

import data.DataHelper
import data.OtaMetadataPb
import data.RomInfoHelper
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import utils.isWeb
import kotlin.time.ExperimentalTime

object RomInfoMapper {

    fun applyMetadata(curRomData: DataHelper.RomInfoData, ota: OtaMetadataPb?): DataHelper.RomInfoData {
        if (ota == null) return curRomData
        val post = ota.postcondition
        val postPartitions = post?.partitionState.orEmpty()
        val postFingerprint = postPartitions.firstOrNull { it.partitionName == "odm" }
            ?.build?.firstOrNull().orEmpty()
            .ifEmpty { post?.build?.firstOrNull().orEmpty() }
        return curRomData.copy(
            fingerprint = postFingerprint,
            securityPatchLevel = post?.securityPatchLevel.orEmpty(),
            timestamp = post?.timestamp?.takeIf { ts -> ts > 0 }
                ?.let { ts -> convertTimestampToDateTime(ts.toString()) }.orEmpty(),
            sdkLevel = post?.sdkLevel.orEmpty(),
        )
    }

    @OptIn(ExperimentalTime::class)
    private fun convertTimestampToDateTime(timestamp: String): String {
        val epochSeconds = timestamp.toLongOrNull() ?: return ""
        val instant = kotlin.time.Instant.fromEpochSeconds(epochSeconds)
        val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return "${dateTime.year}/${dateTime.month.number}/${dateTime.day} " +
                dateTime.hour.toString().padStart(2, '0') +
                ":${dateTime.minute.toString().padStart(2, '0')}" +
                ":${dateTime.second.toString().padStart(2, '0')}"
    }


    fun mapRom(
        recoveryRomInfo: RomInfoHelper.RomInfo,
        romInfo: RomInfoHelper.Rom?,
        officialDownload: String? = null,
        noUltimateLink: Boolean = false,
    ): Triple<DataHelper.RomInfoData, List<DataHelper.IconInfoData>, List<DataHelper.ImageInfoData>> {
        if (romInfo?.bigversion == null) return Triple(DataHelper.RomInfoData(), emptyList(), emptyList())

        val log = StringBuilder()
        romInfo.changelog?.forEach { (category, items) ->
            if (category.isNotEmpty()) log.append(category).append("\n")
            items.forEach { item ->
                val text = item.txt.trimEnd()
                if (text.isNotEmpty()) log.append(text).append("\n")
            }
            log.append("\n")
        }
        val changelogGroups = log.toString().trimEnd().split("\n\n")
        val changelog = changelogGroups.map { group -> group.lines().drop(1).joinToString("\n") }

        val gentle = StringBuilder()
        val formattedGentleNotice = recoveryRomInfo.gentleNotice?.text?.replace("<li>", "\n· ")
            ?.replace("</li>", "")?.replace("<p>", "\n")?.replace("</p>", "")?.replace("&nbsp;", " ")
            ?.replace("&#160;", "")?.replace(Regex("<[^>]*>"), "")?.trim()
        formattedGentleNotice?.forEach { gentle.append(it) }
        val gentleNotice = gentle.toString().trimEnd().split("\n").drop(1).joinToString("\n")

        var imageInfoData = emptyList<DataHelper.ImageInfoData>()
        var iconInfoData = emptyList<DataHelper.IconInfoData>()

        if (!romInfo.osbigversion.isNullOrEmpty() && romInfo.osbigversion.toFloat() >= 3.0f) {
            val imageMainLink = recoveryRomInfo.fileMirror?.image ?: ""
            imageInfoData = romInfo.changelog?.flatMap { (categoryTitle, items) ->
                items.map { item ->
                    val image = item.image?.firstOrNull()
                    DataHelper.ImageInfoData(
                        title = categoryTitle,
                        changelog = item.txt,
                        imageUrl = imageLink(imageMainLink, image?.path),
                        imageWidth = image?.w?.toIntOrNull(),
                        imageHeight = image?.h?.toIntOrNull()
                    )
                }
            } ?: emptyList()
        } else {
            val iconNames = changelogGroups.map { it.split("\n").first() }
            val iconMainLink = recoveryRomInfo.fileMirror?.icon ?: ""
            val iconNameLink = recoveryRomInfo.icon ?: mapOf()
            val iconLinks = iconLink(iconNames, iconMainLink, iconNameLink)
            iconInfoData = iconNames.mapIndexed { index, iconName ->
                DataHelper.IconInfoData(
                    iconName = iconName,
                    iconLink = iconLinks[iconName] ?: "",
                    changelog = changelog[index]
                )
            }
        }

        val bigVersion = when {
            !romInfo.osbigversion.isNullOrEmpty() && romInfo.osbigversion != ".0" && romInfo.osbigversion != "0.0" -> "HyperOS " + romInfo.osbigversion
            romInfo.bigversion.contains("816") -> romInfo.bigversion.replace("816", "HyperOS 1.0")
            else -> "MIUI ${romInfo.bigversion}"
        }

        val official1Download = if (noUltimateLink) "" else {
            "https://ultimateota.d.miui.com" + (officialDownload ?: downloadUrl(romInfo.version, romInfo.filename))
        }
        val official2Download = if (noUltimateLink) "" else {
            "https://superota.d.miui.com" + (officialDownload ?: downloadUrl(romInfo.version, romInfo.filename))
        }
        val cdn1Download =
            "https://bkt-sgp-miui-ota-update-alisgp.oss-ap-southeast-1.aliyuncs.com" + downloadUrl(romInfo.version, romInfo.filename)
        val cdn2Download = "https://cdnorg.d.miui.com" + downloadUrl(romInfo.version, romInfo.filename)

        val romInfoData = DataHelper.RomInfoData(
            type = romInfo.type.toString(),
            device = romInfo.device.toString(),
            version = romInfo.version.toString(),
            codebase = romInfo.codebase.toString(),
            branch = romInfo.branch.toString(),
            bigVersion = bigVersion,
            fileName = romInfo.filename.toString().substringBefore(".zip") + ".zip",
            fileSize = romInfo.filesize.toString(),
            md5 = romInfo.md5.toString(),
            isBeta = romInfo.isBeta == 1,
            isGov = romInfo.isGov == 1,
            official1Download = official1Download,
            official2Download = official2Download,
            cdn1Download = cdn1Download,
            cdn2Download = cdn2Download,
            changelog = log.toString().trimEnd(),
            gentleNotice = gentleNotice,
        )

        return Triple(romInfoData, iconInfoData, imageInfoData)
    }

    fun mapXmsInfo(
        info: RomInfoHelper.XmsUpdateInfo?,
        imageMirror: String,
        apps: List<DataHelper.XmsAppInfo> = emptyList(),
    ): DataHelper.XmsInfoData {
        if (info == null) return DataHelper.XmsInfoData(apps = apps)
        val cleanedGentle = info.gentleNotice?.text?.replace("<li>", "\n· ")
            ?.replace("</li>", "")?.replace("<p>", "\n")?.replace("</p>", "")?.replace("&nbsp;", " ")
            ?.replace("&#160;", "")?.replace(Regex("<[^>]*>"), "")?.trim()
            ?.split("\n")?.drop(1)?.joinToString("\n").orEmpty()

        val changelogItems = mutableListOf<DataHelper.ImageInfoData>()
        val flatLog = StringBuilder()
        info.changeLog?.forEach { (category, items) ->
            val joined = items.joinToString("\n") { it.txt.trimEnd() }.trim()
            if (category.isNotEmpty()) flatLog.append(category).append("\n")
            if (joined.isNotEmpty()) flatLog.append(joined).append("\n")
            flatLog.append("\n")
            items.forEach { item ->
                val image = item.image?.firstOrNull()
                changelogItems.add(
                    DataHelper.ImageInfoData(
                        title = category,
                        changelog = item.txt,
                        imageUrl = imageLink(imageMirror, image?.path),
                        imageWidth = image?.w?.toIntOrNull(),
                        imageHeight = image?.h?.toIntOrNull(),
                    )
                )
            }
        }

        return DataHelper.XmsInfoData(
            hasUpdate = info.hasXmsUpdate == 1,
            curVer = info.curVer.orEmpty(),
            lstVer = info.lstVer.orEmpty(),
            pkgCnt = info.pkgCnt,
            prio = info.prio ?: 0,
            apps = apps,
            gentleNotice = cleanedGentle,
            changelogItems = changelogItems,
            changelogText = flatLog.toString().trimEnd(),
        )
    }

    fun mapXmsApps(dto: RomInfoHelper.XmsDto): List<DataHelper.XmsAppInfo> {
        val mirrors = dto.mirrorList.orEmpty()
        return dto.apkLists.orEmpty().map { apk ->
            val resolvedUrls = apk.downloadUrls.orEmpty().mapNotNull { url ->
                resolveXmsDownloadUrl(url, mirrors)
            }
            DataHelper.XmsAppInfo(
                name = apk.name.orEmpty(),
                packName = apk.packName.orEmpty(),
                versionCode = apk.lastVerCode.orEmpty(),
                fileName = apk.fileName.orEmpty(),
                fileSize = apk.size?.toString().orEmpty(),
                md5 = apk.md5.orEmpty(),
                downloadUrls = resolvedUrls,
            )
        }
    }

    fun downloadUrl(romVersion: String?, romFilename: String?): String = "/$romVersion/$romFilename"

    private fun resolveXmsDownloadUrl(raw: String, mirrors: List<String>): String? {
        if (raw.isEmpty()) return null
        if (raw.startsWith("http://") || raw.startsWith("https://")) return raw
        val mirror = mirrors.firstOrNull()?.trimEnd('/') ?: return null
        return mirror + (if (raw.startsWith("/")) raw else "/$raw")
    }

    private fun iconLink(iconNames: List<String>, iconMainLink: String, iconNameLink: Map<String, String>): MutableMap<String, String> {
        if (isWeb()) return mutableMapOf()
        val iconMap = mutableMapOf<String, String>()
        val safeIconMainLink = iconMainLink.replace("http://", "https://")
        if (safeIconMainLink.isNotEmpty() && iconNameLink.isNotEmpty()) {
            for (name in iconNames) {
                iconNameLink[name]?.let { iconMap[name] = safeIconMainLink + it }
            }
        }
        return iconMap
    }

    private fun imageLink(mirrorImage: String, path: String?): String {
        if (isWeb()) return ""
        val base = mirrorImage.replace("http://", "https://")
        return base + (path ?: "")
    }
}
