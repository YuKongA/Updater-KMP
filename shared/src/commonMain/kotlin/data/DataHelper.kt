package data

import kotlinx.serialization.Serializable

object DataHelper {
    @Serializable
    data class IconInfoData(
        val changelog: String = "",
        val iconName: String = "",
        val iconLink: String = "",
    )

    @Serializable
    data class ImageInfoData(
        val title: String = "",
        val changelog: String = "",
        val imageUrl: String = "",
        val imageWidth: Int? = null,
        val imageHeight: Int? = null
    )

    @Serializable
    data class SearchHistoryEntry(
        val deviceName: String = "",
        val codeName: String = "",
        val deviceRegion: String = "Default (CN)",
        val deviceCarrier: String = "Default (Xiaomi)",
        val androidVersion: String = "16.0",
        val systemVersion: String = "",
    )

    @Serializable
    data class LoginData(
        val accountType: String? = null,
        val authResult: String? = null,
        val description: String? = null,
        val ssecurity: String? = null,
        val serviceToken: String? = null,
        val userId: String? = null,
        val cUserId: String? = null,
        val passToken: String? = null,
    )

    @Serializable
    data class RomInfoData(
        val type: String = "",
        val device: String = "",
        val version: String = "",
        val codebase: String = "",
        val branch: String = "",
        val bigVersion: String = "",
        val fileName: String = "",
        val fileSize: String = "",
        val md5: String = "",
        val isBeta: Boolean = false,
        val isGov: Boolean = false,
        val official1Download: String = "",
        val official2Download: String = "",
        val cdn1Download: String = "",
        val cdn2Download: String = "",
        val changelog: String = "",
        val gentleNotice: String = "",
        val fingerprint: String = "",
        val securityPatchLevel: String = "",
        val timestamp: String = "",
    )
}