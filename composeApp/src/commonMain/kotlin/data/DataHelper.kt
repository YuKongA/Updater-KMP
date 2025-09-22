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
        val changelog: String = "",
        val imageName: String = "",
        val imageLink: String = "",
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
    data class RequestData(
        val b: String,
        val c: String,
        val d: String,
        val f: String,
        val id: String,
        val l: String,
        val ov: String,
        val p: String,
        val pn: String,
        val r: String,
        val security: String,
        val token: String,
        val v: String,
        val unlock: String,
        val options: Options,
    )

    @Serializable
    data class Options(
        val av: String,
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