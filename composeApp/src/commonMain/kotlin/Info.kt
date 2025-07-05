import androidx.compose.runtime.MutableState
import data.DataHelper
import io.ktor.client.call.body
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.post
import io.ktor.http.Parameters
import io.ktor.utils.io.InternalAPI
import kotlinx.serialization.json.Json
import misc.json
import platform.httpClientPlatform
import platform.miuiDecrypt
import platform.miuiEncrypt
import platform.prefGet
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

val CN_RECOVERY_URL = if (isWeb()) "https://updater.yukonga.top/updates/miotaV3.php" else "https://update.miui.com/updates/miotaV3.php"
val INTL_RECOVERY_URL = if (isWeb()) "https://updater.yukonga.top/intl-updates/miotaV3.php" else "https://update.intl.miui.com/updates/miotaV3.php"
var accountType = "CN"
var port = "1"
var security = ""
var securityKey = "miuiotavalided11".encodeToByteArray()
var serviceToken = ""
var userId = ""

/**
 * Generate JSON data for recovery ROM info request.
 *
 * @param branch: Branch name
 * @param codeNameExt: CodeName with region extension
 * @param regionCode: Region code
 * @param romVersion: ROM version
 * @param androidVersion: Android version
 * @param userId: Xiaomi ID
 * @param security: Security key
 * @param token: Service token
 *
 * @return JSON data
 */
fun generateJson(
    branch: String,
    codeNameExt: String,
    regionCode: String,
    romVersion: String,
    androidVersion: String,
    userId: String,
    security: String,
    token: String
): String {
    val data = DataHelper.RequestData(
        b = branch,
        c = androidVersion,
        d = codeNameExt,
        f = "1",
        id = userId,
        l = if (!codeNameExt.contains("_global")) "zh_CN" else "en_US",
        ov = romVersion,
        p = codeNameExt,
        pn = codeNameExt,
        r = regionCode,
        security = security,
        token = token,
        unlock = "0",
        v = "MIUI-$romVersion"
    )
    return Json.encodeToString(data)
}

/**
 * Get recovery ROM info form xiaomi server.
 *
 * @param branch: Branch name
 * @param codeNameExt: CodeName with region extension
 * @param regionCode: Region code
 * @param romVersion: ROM version
 * @param androidVersion: Android version
 * @param isLogin: Xiaomi account login status
 *
 * @return Recovery ROM info
 */
@OptIn(ExperimentalEncodingApi::class, InternalAPI::class)
suspend fun getRecoveryRomInfo(
    branch: String,
    codeNameExt: String,
    regionCode: String,
    romVersion: String,
    androidVersion: String,
    isLogin: MutableState<Int>
): String {
    if (prefGet("loginInfo") != null && isLogin.value == 1) {
        val loginInfo = prefGet("loginInfo")?.let { json.decodeFromString<DataHelper.LoginData>(it) }
        val authResult = loginInfo?.authResult
        if (authResult != "3") {
            accountType = loginInfo?.accountType.toString().ifEmpty { "CN" }
            port = "2"
            security = loginInfo?.ssecurity.toString()
            securityKey = Base64.Mime.decode(security)
            serviceToken = loginInfo?.serviceToken.toString()
            userId = loginInfo?.userId.toString()
        } else setDefaultRequestInfo()
    } else setDefaultRequestInfo()

    val jsonData = generateJson(branch, codeNameExt, regionCode, romVersion, androidVersion, userId, security, serviceToken)
    val encryptedText = miuiEncrypt(jsonData, securityKey)
    val client = httpClientPlatform()
    val parameters = Parameters.build {
        append("q", encryptedText)
        append("t", serviceToken)
        append("s", port)
    }
    val recoveryUrl = if (accountType != "CN") INTL_RECOVERY_URL else CN_RECOVERY_URL
    try {
        val response = client.post(recoveryUrl) {
            body = FormDataContent(parameters)
        }
        val requestedEncryptedText = response.body<String>()
        client.close()
        return miuiDecrypt(requestedEncryptedText, securityKey)
    } catch (e: Exception) {
        e.printStackTrace()
        return ""
    }
}

/**
 * Set default request info.
 */
fun setDefaultRequestInfo() {
    accountType = "CN"
    port = "1"
    security = ""
    securityKey = "miuiotavalided11".encodeToByteArray()
    serviceToken = ""
    userId = ""
}