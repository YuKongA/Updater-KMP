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
    data class LoginData(
        val accountType: String? = null,
        var authResult: String? = null,
        val description: String? = null,
        val ssecurity: String? = null,
        val serviceToken: String? = null,
        val userId: String? = null,
        val cUserId: String? = null,
    )

    @Serializable
    data class RomInfoData(
        var type: String = "",
        var device: String = "",
        var version: String = "",
        var codebase: String = "",
        var branch: String = "",
        var bigVersion: String = "",
        var fileName: String = "",
        var fileSize: String = "",
        var md5: String = "",
        var isBeta: Boolean = false,
        var isGov: Boolean = false,
        var official1Download: String = "",
        var official2Download: String = "",
        var cdn1Download: String = "",
        var cdn2Download: String = "",
        var changelog: String = "",
        var gentleNotice: String = "",
        var fingerprint: String = "",
        var securityPatchLevel: String = "",
        var timestamp: String = "",
    )
}