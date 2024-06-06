package data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

object RomInfoHelper {

    @Serializable
    data class RomInfo(
        @SerialName("AuthResult") val authResult: Int? = null,
        @SerialName("CurrentRom") val currentRom: CurrentRom? = null,
        @SerialName("LatestRom") val latestRom: LatestRom? = null,
        @SerialName("IncrementRom") val incrementRom: IncrementRom? = null,
        @SerialName("CrossRom") val crossRom: CrossRom? = null,
    )

    @Serializable
    data class CurrentRom(
        val type: String? = null,
        val bigversion: String? = null,
        val branch: String? = null,
        val codebase: String? = null,
        val device: String? = null,
        val filename: String? = null,
        val filesize: String? = null,
        val md5: String? = null,
        val name: String? = null,
        val version: String? = null,
        val changelog: HashMap<String, Changelog>? = null,
    )

    @Serializable
    data class LatestRom(
        val filename: String? = null,
        val md5: String? = null,
    )

    @Serializable
    data class IncrementRom(
        val type: String? = null,
        val bigversion: String? = null,
        val branch: String? = null,
        val codebase: String? = null,
        val device: String? = null,
        val filename: String? = null,
        val filesize: String? = null,
        val md5: String? = null,
        val name: String? = null,
        val version: String? = null,
        val versionForApply: String? = null,
        val changelog: HashMap<String, Changelog>? = null,
    )

    @Serializable
    data class CrossRom(
        val type: String? = null,
        val bigversion: String? = null,
        val branch: String? = null,
        val codebase: String? = null,
        val device: String? = null,
        val filename: String? = null,
        val filesize: String? = null,
        val md5: String? = null,
        val name: String? = null,
        val version: String? = null,
        val changelog: HashMap<String, Changelog>? = null,
    )

    @Serializable
    data class Changelog(
        val txt: List<String>
    )
}