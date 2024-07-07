import androidx.compose.runtime.MutableState
import data.DataHelper
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.statement.request
import io.ktor.http.ContentType
import io.ktor.http.content.TextContent
import io.ktor.util.InternalAPI
import kotlinx.serialization.encodeToString
import misc.json

private const val testUrl = "https://hyperos.mi.com/"
private const val loginUrl = "https://account.xiaomi.com/pass/serviceLogin"
private const val loginAuth2Url = "https://account.xiaomi.com/pass/serviceLoginAuth2"

expect fun httpClientPlatform(): HttpClient

expect fun md5Hash(input: String): String

@OptIn(InternalAPI::class)
suspend fun login(
    account: String,
    password: String,
    global: Boolean,
    savePassword: String,
    isLogin: MutableState<Int>
): Int {
    if (account.isEmpty() || password.isEmpty()) return 1

    if (savePassword != "1") deletePassword()

    val client = httpClientPlatform()

    try {
        client.get(testUrl)
    } catch (e: Exception) {
        return 5
    }

    val response1 = client.get(loginUrl)

    val md5Hash = md5Hash(password)
    val sign = response1.request.url.parameters["_sign"]?.replace("2&V1_passport&", "") ?: return 2
    val sid = if (global) "miuiota_intl" else "miuiromota"
    val locale = if (global) "en_US" else "zh_CN"
    val data = "_json=true&bizDeviceType=&user=$account&hash=$md5Hash&sid=$sid&_sign=$sign&_locale=$locale"
    val response2 = client.post(loginAuth2Url) {
        body = TextContent(data, ContentType.Application.FormUrlEncoded)
    }

    val authStr = response2.body<String>().replace("&&&START&&&", "")
    val authJson = json.decodeFromString<DataHelper.AuthorizeData>(authStr)
    val description = authJson.description
    val ssecurity = authJson.ssecurity
    val location = authJson.location
    val userId = authJson.userId.toString()
    val accountType = if (global) "GL" else "CN"
    val authResult = if (authJson.result == "ok") "1" else "0"

    if (description == "成功") {
        if (savePassword == "1") {
            perfSet("savePassword", "1")
            savePassword(account, password)
        }
    } else return 3

    if (ssecurity == null || location == null || userId.isEmpty()) return 4

    val response3 = client.get(location) { parameter("_userIdNeedEncrypt", true) }

    val cookies = response3.headers["Set-Cookie"].toString().split("; ")[0].split("; ")[0]
    val serviceToken = cookies.split("serviceToken=")[1].split(";")[0]

    val loginInfo = DataHelper.LoginData(accountType, authResult, description, ssecurity, serviceToken, userId)
    perfSet("loginInfo", json.encodeToString(loginInfo))
    isLogin.value = 1
    return 0
}

fun logout(isLogin: MutableState<Int>): Boolean {
    perfRemove("loginInfo")
    isLogin.value = 0
    return true
}

fun savePassword(account: String, password: String) {
    generateKey()
    val encryptedAccount = ownEncrypt(account)
    val encryptedPassword = ownEncrypt(password)
    perfSet("account", encryptedAccount.first)
    perfSet("accountIv", encryptedAccount.second)
    perfSet("password", encryptedPassword.first)
    perfSet("passwordIv", encryptedPassword.second)
}

fun deletePassword() {
    perfRemove("account")
    perfRemove("accountIv")
    perfRemove("password")
    perfRemove("passwordIv")
}

fun getPassword(): Pair<String, String> {
    if (perfGet("account") != null && perfGet("password") != null && perfGet("accountIv") != null && perfGet("passwordIv") != null) {
        val encryptedAccount = perfGet("account").toString()
        val encodedAccountKey = perfGet("accountIv").toString()
        val encryptedPassword = perfGet("password").toString()
        val encodedPasswordKey = perfGet("passwordIv").toString()
        val account = ownDecrypt(encryptedAccount, encodedAccountKey)
        val password = ownDecrypt(encryptedPassword, encodedPasswordKey)
        return Pair(account, password)
    } else return Pair("", "")
}