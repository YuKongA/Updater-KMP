import data.RequestParamHelper
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okio.internal.commonAsUtf8ToByteArray
import kotlin.io.encoding.ExperimentalEncodingApi

private const val CN_RECOVERY_URL = "https://update.miui.com/updates/miotaV3.php"
private const val INTL_RECOVERY_URL = "https://update.intl.miui.com/updates/miotaV3.php"
private var securityKey = "miuiotavalided11".commonAsUtf8ToByteArray()
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

//@OptIn(ExperimentalEncodingApi::class)
//suspend fun getRecoveryRomInfo(codeNameExt: String, regionCode: String, romVersion: String, androidVersion: String): String {
//    if (FileUtils.isCookiesFileExists(context)) {
//        val cookiesFile = FileUtils.readCookiesFile(context)
//        val cookies = json.decodeFromString<LoginHelper>(cookiesFile)
//        val authResult = cookies.authResult
//        if (authResult != "-1") {
//            userId = cookies.userId.toString()
//            accountType = cookies.accountType.toString().ifEmpty { "CN" }
//            securityKey = Base64.decode((cookies.ssecurity.toString()))
//            serviceToken = cookies.serviceToken.toString()
//            port = "2"
//        }
//    }
//    val jsonData = generateJson(codeNameExt, regionCode, romVersion, androidVersion, userId)
//    val encryptedText = miuiEncrypt(jsonData, securityKey)
//    val formBodyBuilder = FormBody.Builder().add("q", encryptedText).add("t", serviceToken).add("s", port).build()
//    val recoveryUrl = if (accountType == "GL") INTL_RECOVERY_URL else CN_RECOVERY_URL
//    val postRequest = postRequest(recoveryUrl, formBodyBuilder)
//    val requestedEncryptedText = postRequest.body?.string() ?: ""
//    return miuiDecrypt(requestedEncryptedText, securityKey)
//}