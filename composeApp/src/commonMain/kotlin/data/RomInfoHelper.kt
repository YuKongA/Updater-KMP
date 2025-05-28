package data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

object RomInfoHelper {
    @Serializable
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
    data class Rom(
        val bigversion: String? = null,
        val branch: String? = null,
        val changelog: HashMap<String, Changelog>? = null,
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
    data class Changelog(
        val txt: List<String>,
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