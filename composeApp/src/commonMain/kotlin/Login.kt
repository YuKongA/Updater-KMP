import androidx.compose.runtime.MutableState
import data.DataHelper
import io.ktor.client.call.body
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.request
import io.ktor.http.Parameters
import misc.json
import misc.md5Hash
import platform.generateKey
import platform.httpClientPlatform
import platform.ownDecrypt
import platform.ownEncrypt
import platform.prefGet
import platform.prefRemove
import platform.prefSet
import top.yukonga.miuix.kmp.utils.Platform
import top.yukonga.miuix.kmp.utils.platform
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

private const val loginUrl = "https://account.xiaomi.com/pass/serviceLogin"
private const val loginAuth2Url = "https://account.xiaomi.com/pass/serviceLoginAuth2"

fun isWeb(): Boolean = platform() == Platform.WasmJs || platform() == Platform.Js

/**
 * Login Xiaomi account.
 *
 * @param account: Xiaomi account
 * @param password: Password
 * @param global: Global or China account
 * @param savePassword: Save password or not
 * @param isLogin: Login status
 *
 * @return Login status
 */
suspend fun login(
    account: String,
    password: String,
    global: Boolean,
    savePassword: String,
    isLogin: MutableState<Int>
): Int {
    if (account.isEmpty() || password.isEmpty()) return 1
    if (savePassword != "1") deletePassword()

    if (savePassword == "1") {
        prefSet("savePassword", "1")
        savePassword(account, password)
    }

    val client = httpClientPlatform()
    val sid = if (global) "miuiota_intl" else "miuiromota"
    val md5Hash = md5Hash(password)

    try {
        val response1 = client.get(loginUrl)
        val sign = response1.request.url.parameters["_sign"]?.replace("2&V1_passport&", "") ?: return 2

        client.get(loginAuth2Url)
        val response = client.post(loginAuth2Url) {
            parameter("_json", "true")
            parameter("_sign", sign)
            parameter("user", account)
            parameter("hash", md5Hash)
            parameter("sid", sid)
        }

        val authStr = response.body<String>().replace("&&&START&&&", "")
        val authJson = json.decodeFromString<DataHelper.AuthorizeData>(authStr)
        val description = authJson.description
        val ssecurity = authJson.ssecurity
        val location = authJson.location
        val userId = authJson.userId.toString()
        val notificationUrl = authJson.notificationUrl
        val accountType = if (global) "GL" else "CN"
        val authResult = if (authJson.result == "ok") "1" else "0"

        println("authStr: $authStr")

        if (notificationUrl != null) {
            prefSet("notificationUrl", notificationUrl)
            return 5
        }

        if (description != "成功") return 3
        if (location == null) return 4

        val response2 = client.get(location) {
            parameter("_userIdNeedEncrypt", true)
            header("Content-Type", "application/x-www-form-urlencoded")
        }
        val setCookieHeader = response2.headers["Set-Cookie"]
        val serviceToken = setCookieHeader
            ?.split(";")
            ?.map { it.trim() }
            ?.firstOrNull { it.startsWith("serviceToken=") }

        val loginInfo = DataHelper.LoginData(accountType, authResult, description, ssecurity, serviceToken, userId)
        prefSet("loginInfo", json.encodeToString(loginInfo))

        isLogin.value = 1
        return 0
    } catch (_: Exception) {
        return 2
    }
}

/**
 * Logout Xiaomi account.
 *
 * @param isLogin: Login status
 *
 * @return Logout status
 */
fun logout(isLogin: MutableState<Int>): Boolean {
    prefRemove("loginInfo")
    isLogin.value = 0
    return true
}

/**
 * Save Xiaomi's account & password.
 *
 * @param account: Xiaomi account
 * @param password: Password
 */
fun savePassword(account: String, password: String) {
    generateKey()
    val encryptedAccount = ownEncrypt(account)
    val encryptedPassword = ownEncrypt(password)
    prefSet("account", encryptedAccount.first)
    prefSet("accountIv", encryptedAccount.second)
    prefSet("password", encryptedPassword.first)
    prefSet("passwordIv", encryptedPassword.second)
}

/**
 * Delete Xiaomi's account & password.
 */
fun deletePassword() {
    prefRemove("account")
    prefRemove("accountIv")
    prefRemove("password")
    prefRemove("passwordIv")
}

/**
 * Get Xiaomi's account & password.
 *
 * @return Pair of Xiaomi's account & password
 */
fun getPassword(): Pair<String, String> {
    if (prefGet("account") != null && prefGet("password") != null && prefGet("accountIv") != null && prefGet("passwordIv") != null) {
        val encryptedAccount = prefGet("account").toString()
        val encodedAccountKey = prefGet("accountIv").toString()
        val encryptedPassword = prefGet("password").toString()
        val encodedPasswordKey = prefGet("passwordIv").toString()
        val account = ownDecrypt(encryptedAccount, encodedAccountKey)
        val password = ownDecrypt(encryptedPassword, encodedPasswordKey)
        return Pair(account, password)
    } else return Pair("", "")
}

@OptIn(ExperimentalTime::class)
suspend fun verifyTicket(verifyUrl: String, ticket: String): DataHelper.VerifyTicketData? {
    val client = httpClientPlatform()
    val path = "identity/authStart"
    if (!verifyUrl.contains(path)) return null

    val listUrl = verifyUrl.replace(path, "identity/list")
    val resp = client.get(listUrl)
    val setCookieHeader = resp.headers["Set-Cookie"]
    val allCookiesString = setCookieHeader?.split(",")?.joinToString(";") {
        it.split(";")[0].trim()
    } ?: ""

    val data = try {
        json.decodeFromString<DataHelper.IdentityListData>(resp.body<String>())
    } catch (_: Exception) {
        DataHelper.IdentityListData()
    }
    val flag = data.flag ?: 4
    val options = data.options ?: listOf(flag)

    for (f in options) {
        val api = when (f) {
            4 -> "/identity/auth/verifyPhone"
            8 -> "/identity/auth/verifyEmail"
            else -> continue
        }
        val formParams = Parameters.build {
            append("_flag", f.toString())
            append("ticket", ticket)
            append("trust", "true")
            append("_json", "true")
        }
        val verifyResp = client.post("https://account.xiaomi.com$api") {
            parameter("_dc", Clock.System.now().toEpochMilliseconds())
            setBody(FormDataContent(formParams))
            header("Cookie", allCookiesString)
            header("User-Agent", "Mozilla/5.0 (KMP/Updater)")
            header("Referer", verifyUrl)
        }
        val rawResp = verifyResp.body<String>().replace("&&&START&&&", "")
        val verifyData = try {
            json.decodeFromString<DataHelper.VerifyTicketData>(rawResp)
        } catch (_: Exception) {
            null
        }
        if (verifyData != null && verifyData.code == 0) {
            return verifyData
        } else {
            return null
        }
    }
    return null
}

suspend fun handle2FATicket(
    ticketCode: String,
    isLogin: MutableState<Int>,
    setVerifyError: (String) -> Unit,
    setTicketCode: (String) -> Unit,
    setShowTicketInput: (Boolean) -> Unit,
    setShowDialog: (Boolean) -> Unit,
    setShowNotificationUrl: (Boolean) -> Unit,
    showMessage: (String) -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    setVerifyError("")
    val notificationUrl = prefGet("notificationUrl")
    if (notificationUrl != null && ticketCode.isNotBlank()) {
        val verifyData = verifyTicket(notificationUrl, ticketCode)
        if (verifyData != null && verifyData.location != null) {
            try {
                val client = httpClientPlatform()
                val response2 = client.get(verifyData.location) {
                    parameter("_userIdNeedEncrypt", true)
                    header("Content-Type", "application/x-www-form-urlencoded")
                }
                val setCookieHeader = response2.headers["Set-Cookie"]
                val serviceToken = setCookieHeader
                    ?.split(";")
                    ?.map { it.trim() }
                    ?.firstOrNull { it.startsWith("serviceToken=") }
                val loginInfo = DataHelper.LoginData(
                    authResult = "1",
                    description = "成功",
                    serviceToken = serviceToken
                )
                prefSet("loginInfo", json.encodeToString(loginInfo))
                prefRemove("notificationUrl")
                isLogin.value = 1
                showMessage("登录成功，二次验证已通过")
                setShowTicketInput(false)
                setShowDialog(false)
                setShowNotificationUrl(false)
                setTicketCode("")
            } catch (e: Exception) {
                setVerifyError("登录后续流程失败: ${e.message ?: "未知错误"}")
            }
        } else {
            setVerifyError("票据验证失败，请检查 ticket 是否正确或重新完成安全验证。")
            setTicketCode("")
            focusManager.clearFocus()
        }
    } else {
        setVerifyError("未检测到 notificationUrl 或票据为空。")
    }
}
