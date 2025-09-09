import androidx.compose.runtime.MutableState
import androidx.compose.ui.focus.FocusManager
import data.DataHelper
import io.ktor.client.call.body
import io.ktor.client.request.cookie
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.request
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
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

private const val accountUrl = "https://account.xiaomi.com"
private const val serviceLoginUrl = "$accountUrl/pass/serviceLogin"
private const val serviceLoginAuth2Url = "$accountUrl/pass/serviceLoginAuth2"
private const val accountApiUrl = "https://api.account.xiaomi.com/sts"


private val client = httpClientPlatform()
private var _sign: String? = ""

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
    isLogin: MutableState<Int>,
    captcha: String? = null,
): Int {
    if (account.isEmpty() || password.isEmpty()) return 1
    if (savePassword == "1") {
        prefSet("savePassword", "1")
        savePassword(account, password)
    } else {
        deletePassword()
    }
    try {
        val serviceLogin = serviceLogin(
            account = account,
            isLogin = isLogin,
            global = global
        )
        if (serviceLogin == 0) {
            return serviceLogin
        } else {
            val serviceLoginAuth2 = serviceLoginAuth2(
                account = account,
                password = password,
                global = global,
                isLogin = isLogin,
                _sign = _sign!!,
                captcha = captcha,
            )
            return serviceLoginAuth2
        }
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
 * Service login Xiaomi account without password.
 *
 * @param account: Xiaomi account
 * @param isLogin: Login status
 * @param global: Global or China account
 *
 * @return Login status
 */
suspend fun serviceLogin(
    account: String,
    isLogin: MutableState<Int>,
    global: Boolean
): Int {
    val sid = if (global) "miuiota_intl" else "miuiromota"
    val response1 = client.get("$serviceLoginUrl?sid=$sid&_json=true") {
        cookie("userId", account)
        header("Content-Type", "application/x-www-form-urlencoded")
    }
    val body1 = response1.body<String>().replace("&&&START&&&", "")
    println("Login: body1: $body1")
    val elem = json.parseToJsonElement(body1)
    println("Login: elem: $elem")
    var ssecurity: String? = null
    var userId: String? = null
    var location: String? = null
    if (elem is JsonObject) {
        _sign = elem["_sign"]?.jsonPrimitive?.contentOrNull
        ssecurity = elem["ssecurity"]?.jsonPrimitive?.contentOrNull
        userId = elem["userId"]?.jsonPrimitive?.contentOrNull
        location = elem["location"]?.jsonPrimitive?.contentOrNull
    }

    // 无需密码登录
    if (!ssecurity.isNullOrEmpty() && !userId.isNullOrEmpty() && !location.isNullOrEmpty()) {
        println("Login: ssecurity: $ssecurity, userId: $userId, location: $location")
        val serviceToken = getServiceToken(location)
        println("Login: serviceToken: $serviceToken")
        val loginInfo = DataHelper.LoginData(
            accountType = if (global) "GL" else "CN",
            authResult = "1",
            description = "成功",
            ssecurity = ssecurity,
            serviceToken = serviceToken,
            userId = userId
        )
        println("Login: loginInfo: $loginInfo")
        prefSet("loginInfo", json.encodeToString(loginInfo))
        isLogin.value = 1
        return 0
    }
    println("Login: sign: $_sign")
    _sign?.let { if (it.isNotEmpty()) return 1 }

    // 回退 _sign 获取方案
    _sign?.let {
        if (it.isEmpty()) {
            _sign = response1.request.url.parameters["_sign"]?.removePrefix("2&V1_passport&")
            _sign?.let { it1 -> if (it1.isNotEmpty()) return 1 }
        }
    }
    println("Login: sign: $_sign")
    return 2
}

/**
 * Service login Xiaomi account with password.
 *
 * @param account: Xiaomi account
 * @param password: Password
 * @param global: Global or China account
 * @param isLogin: Login status
 * @param _sign: _sign from serviceLogin
 * @param captcha: Captcha if needed
 *
 * @return Login status
 */
suspend fun serviceLoginAuth2(
    account: String,
    password: String,
    global: Boolean,
    isLogin: MutableState<Int>,
    _sign: String,
    captcha: String? = null,
): Int {
    val md5Hash = md5Hash(password)
    val sid = if (global) "miuiota_intl" else "miuiromota"

    val response = client.post(serviceLoginAuth2Url) {
        parameter("user", account)
        parameter("hash", md5Hash)
        parameter("sid", sid)
        parameter("callback", "$accountApiUrl?sid=$sid")
        parameter("qs", "%3Fsid%3D$sid%26_json%3Dtrue")
        parameter("_json", "true")
        parameter("_sign", _sign)
        parameter("_locale", if (global) "en_US" else "zh_CN")
        captcha?.let { parameter("captcha", it) }
        header("Content-Type", "application/x-www-form-urlencoded")
    }
    val authStr = response.body<String>().replace("&&&START&&&", "")
    println("Login: authStr: $authStr")
    val authJson = json.decodeFromString<DataHelper.AuthorizeData>(authStr)
    println("Login: authJson: $authJson")

    // 处理验证码
    if (authJson.captchaUrl != null) {
        prefSet("captchaUrl", authJson.captchaUrl)
        return 6 // 6: 需要验证码
    }
    // 处理 2FA
    if (authJson.notificationUrl != null) {
        prefSet("notificationUrl", authJson.notificationUrl)
        return 5 // 5: 需要二次验证
    }
    // 处理登录失败
    if (authJson.result != "ok") {
        return 3 // 3: 登录失败
    }
    // 处理缺少字段
    if (authJson.location.isNullOrEmpty()) return 4 // 4: 未返回 location
    val serviceToken = getServiceToken(authJson.location)
    println("Login: serviceToken: $serviceToken")

    val loginInfo = DataHelper.LoginData(
        accountType = if (global) "GL" else "CN",
        authResult = "1",
        description = "成功",
        ssecurity = authJson.ssecurity,
        serviceToken = serviceToken,
        userId = authJson.userId.toString()
    )
    println("Login: loginInfo: $loginInfo")
    prefSet("loginInfo", json.encodeToString(loginInfo))
    isLogin.value = 1
    return 0
}

/** Get serviceToken from location URL.
 *
 * @param location: Location URL
 *
 * @return serviceToken
 */
suspend fun getServiceToken(location: String): String? {
    val response = client.get(location) {
        parameter("_userIdNeedEncrypt", true)
        header("Content-Type", "application/x-www-form-urlencoded")
    }
    return response.headers["Set-Cookie"]
        ?.split(";")
        ?.map { it.trim() }
        ?.firstOrNull { it.startsWith("serviceToken=") }
        ?.removePrefix("serviceToken=")
}

@OptIn(ExperimentalTime::class)
suspend fun verifyTicket(verifyUrl: String, ticket: String): DataHelper.VerifyTicketData? {
    val path = "identity/authStart"
    if (!verifyUrl.contains(path)) return null
    println("Login: verifyUrl: $verifyUrl, ticket: $ticket")

    val listUrl = verifyUrl.replace(path, "identity/list")
    println("Login: listUrl: $listUrl")
    val resp = client.get(listUrl)
    val identitySession = resp.headers["Set-Cookie"]
        ?.split(";")
        ?.map { it.trim() }
        ?.firstOrNull { it.startsWith("identity_session=") }
        ?.removePrefix("identity_session=")
    println("Login: identitySession: $identitySession")

    val data = json.decodeFromString<DataHelper.IdentityListData>(resp.body<String>().replace("&&&START&&&", ""))

    println("Login: data: $data")
    val flag = data.flag ?: 4
    val options = data.options ?: listOf(flag)

    for (f in options) {
        val api = when (f) {
            4 -> "/identity/auth/verifyPhone"
            8 -> "/identity/auth/verifyEmail"
            else -> continue
        }
        val formBody = "_flag=$f&ticket=$ticket&trust=true&_json=true"
        val verifyResp = client.post("$accountUrl$api") {
            parameter("_dc", Clock.System.now().toEpochMilliseconds())
            setBody(formBody)
            identitySession?.let { cookie("identity_session", it) }
            header("Content-Type", "application/x-www-form-urlencoded")
        }
        println("Login: verifyResp: ${verifyResp.body<String>()}")
        val rawResp = verifyResp.body<String>().replace("&&&START&&&", "")
        println("Login: rawResp: $rawResp")
        val verifyData = try {
            json.decodeFromString<DataHelper.VerifyTicketData>(rawResp)
        } catch (_: Exception) {
            null
        }
        println("Login: verifyData: $verifyData")
        return if (verifyData != null && verifyData.code == 0) {
            verifyData
        } else {
            null
        }
    }
    return null
}

suspend fun handle2FATicket(
    ticket: String,
    account: String,
    password: String,
    global: Boolean,
    savePassword: String,
    isLogin: MutableState<Int>,
): Int {
    val notificationUrl = prefGet("notificationUrl")
    if (notificationUrl != null && ticket.isNotBlank()) {
        val verifyData = verifyTicket(notificationUrl, ticket)
        println("Login: verifyData: $verifyData")
        if (verifyData != null && verifyData.location != null) {
            println("Login: 2FA verify success, location: ${verifyData.location}")
            val login = login(
                account = account,
                password = password,
                global = global,
                savePassword = savePassword,
                isLogin = isLogin
            )
            return login
        }
    }
    return 7
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
