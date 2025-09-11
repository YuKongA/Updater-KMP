package data

import kotlinx.serialization.Serializable

object DataHelper {
    @Serializable
    data class AuthorizeData(
        val description: String? = null,
        val location: String? = null,
        val code: Int? = null,
        val result: String? = null,
        val ssecurity: String? = null,
        val userId: Long? = null,
        val notificationUrl: String? = null,
        val captchaUrl: String? = null,
    )

    @Serializable
    data class IconInfoData(
        val changelog: String,
        val iconName: String,
        val iconLink: String,
    )


    @Serializable
    data class LoginData(
        val accountType: String? = null,
        var authResult: String? = null,
        val description: String? = null,
        val ssecurity: String? = null,
        val serviceToken: String? = null,
        val userId: String? = null,
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

    @Serializable
    data class IdentityListData(
        val flag: Int? = null,
        val options: List<Int>? = null
    )

    @Serializable
    data class VerifyTicketData(
        val code: Int? = null,
        val location: String? = null,
    )
}