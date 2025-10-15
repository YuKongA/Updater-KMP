package data

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

object RomInfoHelper {
    @Serializable
    @JsonIgnoreUnknownKeys
    @OptIn(ExperimentalSerializationApi::class)
    data class RomInfo(
        @SerialName("AuthResult") val authResult: Int? = null,
        @SerialName("CurrentRom") val currentRom: Rom? = null,
        @SerialName("LatestRom") val latestRom: Rom? = null,
        @SerialName("IncrementRom") val incrementRom: Rom? = null,
        @SerialName("CrossRom") val crossRom: Rom? = null,
        @SerialName("Icon") val icon: Map<String, String>? = null,
        @SerialName("FileMirror") val fileMirror: FileMirror? = null,
        @SerialName("GentleNotice") val gentleNotice: GentleNotice? = null,
    )

    @Serializable
    @JsonIgnoreUnknownKeys
    @OptIn(ExperimentalSerializationApi::class)
    data class Rom(
        val bigversion: String? = null,
        val branch: String? = null,
        val changelog: HashMap<String, List<ChangelogItem>>? = null,
        val codebase: String? = null,
        val device: String? = null,
        val filename: String? = null,
        val filesize: String? = null,
        val md5: String? = null,
        val name: String? = null,
        val osbigversion: String? = null,
        val type: String? = null,
        val version: String? = null,
        val isBeta: Int = 0,
        val isGov: Int = 0,
    )

    @Serializable
    data class ChangelogItem(
        val txt: String,
        val image: List<ChangelogImage>? = null
    )

    @Serializable
    data class ChangelogImage(
        val path: String,
        val h: String,
        val w: String
    )

    @Serializable
    data class FileMirror(
        val icon: String,
        val image: String,
        val video: String,
        val headimage: String,
    )

    @Serializable
    data class GentleNotice(
        val text: String,
    )
}