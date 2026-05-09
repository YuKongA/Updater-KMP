package data.repository

import data.DataHelper
import io.ktor.client.call.body
import io.ktor.client.request.cookie
import io.ktor.client.request.forms.submitForm
import io.ktor.http.Parameters
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import platform.httpClientPlatform
import platform.miuiDecrypt
import platform.miuiEncrypt
import utils.isWeb
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class RomInfoRepository {
    private val CN_RECOVERY_URL = "https://update.miui.com/updates/miotaV3.php"
    private val INTL_RECOVERY_URL = "https://update.intl.miui.com/updates/miotaV3.php"
    private val CN_GETXMSVER_URL = "https://update.miui.com/api/v3/xms/getXmsVer"
    private val INTL_GETXMSVER_URL = "https://update.intl.miui.com/api/v3/xms/getXmsVer"

    private val client = httpClientPlatform()
    private val defaultSecurityKey = "miuiotavalided11".encodeToByteArray()

    fun generateJson(
        branch: String,
        codeNameExt: String,
        regionCode: String,
        romVersion: String,
        androidVersion: String,
        userId: String,
        security: String,
        token: String,
        xmsVersion: String,
    ): String {
        return buildJsonObject {
            if (branch.isNotEmpty()) put("b", branch)
            put("c", androidVersion)
            put("d", codeNameExt)
            put("f", "1")
            put("id", userId)
            put("l", if (!codeNameExt.contains("_global")) "zh_CN" else "en_US")
            put("ov", romVersion)
            put("p", codeNameExt)
            put("pn", codeNameExt)
            put("r", regionCode)
            put("security", security)
            put("token", token)
            put("unlock", "0")
            put("v", "MIUI-$romVersion")
            if (xmsVersion.isNotEmpty()) put("xv", xmsVersion)

            if ((androidVersion.toFloatOrNull() ?: 0f) >= 15.0f) {
                putJsonObject("options") {
                    put("av", "9.1.3")
                }
            }
        }.toString()
    }

    @OptIn(ExperimentalEncodingApi::class)
    suspend fun getRecoveryRomInfo(
        branch: String,
        codeNameExt: String,
        regionCode: String,
        romVersion: String,
        androidVersion: String,
        loginData: DataHelper.LoginData?,
        xmsVersion: String = "",
    ): String {
        val accountType: String
        val port: String
        val ssecurity: String
        val securityKey: ByteArray
        val serviceToken: String
        val userId: String
        val cUserId: String

        if (loginData != null && loginData.authResult != "3") {
            accountType = loginData.accountType?.ifEmpty { "CN" } ?: "CN"
            port = "2"
            ssecurity = loginData.ssecurity.orEmpty()
            securityKey = Base64.Mime.decode(ssecurity)
            serviceToken = loginData.serviceToken.orEmpty()
            userId = loginData.userId.orEmpty()
            cUserId = loginData.cUserId.orEmpty()
        } else {
            accountType = "CN"
            port = "1"
            ssecurity = ""
            securityKey = defaultSecurityKey
            serviceToken = ""
            userId = ""
            cUserId = ""
        }

        val jsonData = generateJson(branch, codeNameExt, regionCode, romVersion, androidVersion, userId, ssecurity, serviceToken, xmsVersion)
        val encryptedText = miuiEncrypt(jsonData, securityKey)
        val parameters = Parameters.build {
            append("q", encryptedText)
            append("t", serviceToken)
            append("s", port)
        }

        val recoveryUrl = if (isWeb()) {
            if (accountType != "CN") "https://updater.yukonga.top/intl-updates/miotaV3.php" else "https://updater.yukonga.top/updates/miotaV3.php"
        } else {
            if (accountType != "CN") INTL_RECOVERY_URL else CN_RECOVERY_URL
        }

        try {
            val response = client.submitForm(recoveryUrl, parameters) {
                if (serviceToken.isNotEmpty() && cUserId.isNotEmpty()) {
                    cookie("serviceToken", serviceToken)
                    cookie("uid", cUserId)
                    cookie("s", "1")
                }
            }
            val requestedEncryptedText = response.body<String>()
            val decrypted = miuiDecrypt(requestedEncryptedText, securityKey)
            return decrypted
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    @OptIn(ExperimentalEncodingApi::class, ExperimentalUuidApi::class)
    suspend fun getXmsVerInfo(
        codeNameExt: String,
        regionCode: String,
        romVersion: String,
        androidVersion: String,
        pkgs: List<String>,
        curVer: String,
        lstVer: String,
        loginData: DataHelper.LoginData?,
    ): String {
        if (isWeb()) return ""
        if (pkgs.isEmpty()) return ""

        val accountType: String
        val port: String
        val securityKey: ByteArray
        val serviceToken: String
        val userId: String
        val cUserId: String

        if (loginData != null && loginData.authResult != "3") {
            accountType = loginData.accountType?.ifEmpty { "CN" } ?: "CN"
            port = "2"
            securityKey = Base64.Mime.decode(loginData.ssecurity.orEmpty())
            serviceToken = loginData.serviceToken.orEmpty()
            userId = loginData.userId.orEmpty()
            cUserId = loginData.cUserId.orEmpty()
        } else {
            accountType = "CN"
            port = "1"
            securityKey = defaultSecurityKey
            serviceToken = ""
            userId = ""
            cUserId = ""
        }

        val jsonData = buildJsonObject {
            put("b", "F")
            put("c", androidVersion.removeSuffix(".0"))
            put("d", codeNameExt)
            if (romVersion.isNotEmpty()) put("rv", romVersion)
            put("f", "1")
            put("csv", curVer)
            put("l", if (!codeNameExt.contains("_global")) "zh_CN" else "en_US")
            put("lsv", lstVer)
            put("r", regionCode)
            put("id", userId)
            putJsonArray("pkgs") {
                pkgs.forEach { pkg ->
                    addJsonObject {
                        put("pkg", pkg)
                        put("pkgVer", "1")
                    }
                }
            }
        }.toString()

        val encryptedText = miuiEncrypt(jsonData, securityKey)
        val parameters = Parameters.build {
            append("n", Uuid.random().toString())
            append("q", encryptedText)
            append("s", port)
            append("t", serviceToken)
            append("ts", kotlin.time.Clock.System.now().toEpochMilliseconds().toString())
        }

        val getXmsVerUrl = if (accountType != "CN") INTL_GETXMSVER_URL else CN_GETXMSVER_URL

        try {
            val response = client.submitForm(getXmsVerUrl, parameters) {
                if (serviceToken.isNotEmpty() && cUserId.isNotEmpty()) {
                    cookie("serviceToken", serviceToken)
                    cookie("uid", cUserId)
                    cookie("s", "1")
                }
            }
            val requestedEncryptedText = response.body<String>()
            return miuiDecrypt(requestedEncryptedText, securityKey)
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }
}
