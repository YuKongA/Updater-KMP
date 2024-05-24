import data.LoginHelper
import data.RequestParamHelper
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.Parameters
import io.ktor.http.content.TextContent
import io.ktor.http.formUrlEncode
import io.ktor.util.InternalAPI
import io.ktor.utils.io.core.toByteArray
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import misc.json
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

const val CN_RECOVERY_URL = "https://update.miui.com/updates/miotaV3.php"
const val INTL_RECOVERY_URL = "https://update.intl.miui.com/updates/miotaV3.php"
var securityKey = "miuiotavalided11".toByteArray()
var accountType = "CN"
var port = "1"
var userId = ""
var security = ""
var serviceToken = ""

fun generateJson(
    codeNameExt: String, regionCode: String, romVersion: String, androidVersion: String, userId: String, security: String, token: String
): String {
    val data = RequestParamHelper(
        security = security,
        token = token,
        id = userId,
        c = androidVersion,
        d = codeNameExt,
        f = "1",
        ov = romVersion,
        l = if (!codeNameExt.contains("_global")) "zh_CN" else "en_US",
        r = regionCode,
        v = "MIUI-$romVersion",
        unlock = "0"
    )
    return Json.encodeToString(data)
}

@OptIn(ExperimentalEncodingApi::class, InternalAPI::class)
suspend fun getRecoveryRomInfo(
    codeNameExt: String, regionCode: String, romVersion: String, androidVersion: String
): String {
    if (perfGet("loginInfo") != null) {
        val cookies = perfGet("loginInfo")?.let { json.decodeFromString<LoginHelper>(it) }
        val authResult = cookies?.authResult
        if (authResult != "3") {
            userId = cookies?.userId.toString()
            accountType = cookies?.accountType.toString().ifEmpty { "CN" }
            security = cookies?.ssecurity.toString()
            securityKey = Base64.Default.decode(security)
            serviceToken = cookies?.serviceToken.toString()
            port = "2"
        }
    }
    val jsonData = generateJson(codeNameExt, regionCode, romVersion, androidVersion, userId, security, serviceToken)
    val encryptedText = miuiEncrypt(jsonData, securityKey)
    val client = httpClientPlatform()
    val parameters = Parameters.build {
        append("q", encryptedText)
        append("t", serviceToken)
        append("s", port)
    }.formUrlEncode()
    val recoveryUrl = if (accountType == "GL") INTL_RECOVERY_URL else CN_RECOVERY_URL
    val response = client.post(recoveryUrl) {
        body = TextContent(parameters, ContentType.Application.FormUrlEncoded)
    }
    val requestedEncryptedText = response.body<String>()
    client.close()
    return miuiDecrypt(requestedEncryptedText, securityKey)
}