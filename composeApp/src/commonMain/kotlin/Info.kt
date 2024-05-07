import data.RequestParamHelper
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.utils.io.InternalAPI
import io.ktor.utils.io.core.toByteArray
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val CN_RECOVERY_URL = "https://update.miui.com/updates/miotaV3.php"
private const val INTL_RECOVERY_URL = "https://update.intl.miui.com/updates/miotaV3.php"
private var securityKey = "miuiotavalided11".toByteArray()
private var userId = ""
private var accountType = "CN"
private var serviceToken = ""
private var port = "1"

private fun generateJson(codeNameExt: String, regionCode: String, romVersion: String, androidVersion: String, userId: String): String {
    val data = RequestParamHelper(
        id = userId,
        c = androidVersion,
        d = codeNameExt,
        f = "1",
        ov = romVersion,
        l = if (!codeNameExt.contains("_global")) "zh_CN" else "en_US",
        r = regionCode,
        v = "miui-$romVersion",
        unlock = "0"
    )
    return Json.encodeToString(data)
}

@OptIn(InternalAPI::class)
suspend fun getRecoveryRomInfo(codeNameExt: String, regionCode: String, romVersion: String, androidVersion: String): String {
    val jsonData = generateJson(codeNameExt, regionCode, romVersion, androidVersion, userId)
    val encryptedText = miuiEncrypt(jsonData, securityKey)
    val client = HttpClient()
    val postData = "q=${encryptedText}&t=${serviceToken}&s=${port}"
    val recoveryUrl = if (accountType == "GL") INTL_RECOVERY_URL else CN_RECOVERY_URL
    val response = client.post {
        url(recoveryUrl)
        this.body = postData
        contentType(ContentType.Application.FormUrlEncoded)
    }
    val requestedEncryptedText = response.body<String>()
    client.close()
    return miuiDecrypt(requestedEncryptedText, securityKey)
}