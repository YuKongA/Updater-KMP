import androidx.compose.runtime.MutableState
import data.DataHelper
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.cookie
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.statement.request
import io.ktor.http.isSuccess
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import misc.json
import misc.md5Hash
import misc.sha1Hash
import platform.generateKey
import platform.httpClientPlatform
import platform.ownDecrypt
import platform.ownEncrypt
import platform.prefGet
import platform.prefRemove
import platform.prefSet
import top.yukonga.miuix.kmp.utils.Platform
import top.yukonga.miuix.kmp.utils.platform
import kotlin.io.encoding.Base64
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

private const val accountUrl = "https://account.xiaomi.com"
private const val serviceLoginUrl = "$accountUrl/pass/serviceLogin"
private const val serviceLoginAuth2Url = "$accountUrl/pass/serviceLoginAuth2"

private var globalClient: HttpClient? = null
private val agent = generateAgent()
private val deviceId = generateDeviceId()
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
    captcha: String = "",
    ticket: String = "",
): Int {
    if (account.isEmpty() || password.isEmpty()) return 1
    if (savePassword == "1") {
        prefSet("savePassword", "1")
        savePassword(account, password)
    } else {
        deletePassword()
    }
    val client = globalClient ?: httpClientPlatform().also { globalClient = it }
    try {
        if (captcha.isNotEmpty()) {
            val serviceLoginAuth2 = serviceLoginAuth2(
                client = client,
                account = account,
                password = password,
                global = global,
                isLogin = isLogin,
                _sign = _sign!!,
                captcha = captcha,
            )
            return serviceLoginAuth2
        }
        if (ticket.isNotEmpty()) {
            val handle2FATicket = handle2FATicket(
                client = client,
                ticket = ticket
            )
            if (handle2FATicket == 3) return 3
        }
        val serviceLogin = serviceLogin(
            client = client,
            account = account,
            isLogin = isLogin,
            global = global
        )
        if (serviceLogin == 0) return serviceLogin
        val serviceLoginAuth2 = serviceLoginAuth2(
            client = client,
            account = account,
            password = password,
            global = global,
            isLogin = isLogin,
            _sign = _sign!!,
            captcha = captcha,
        )
        return serviceLoginAuth2
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
    client: HttpClient,
    account: String,
    isLogin: MutableState<Int>,
    global: Boolean
): Int {
    val sid = if (global) "miuiota_intl" else "miuiromota"

    println("Login: agent: $agent")
    println("Login: deviceId: $deviceId")

    val response = client.get(serviceLoginUrl) {
        parameter("sid", sid)
        parameter("_json", true)
        cookie("sdkVersion", "accountsdk-18.8.15")
        cookie("deviceId", deviceId)
        cookie("userId", account)
        header("User-Agent", agent)
        header("Content-Type", "application/x-www-form-urlencoded")
    }
    response.request.headers.entries().forEach { (key, values) ->
        println("Login: serviceLogin Header: $key = ${values.joinToString(", ")}")
    }
    val body = response.body<String>().replace("&&&START&&&", "")
    val elem = json.parseToJsonElement(body)
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
        val authorizeData = DataHelper.AuthorizeData(ssecurity = ssecurity, userId = userId.toLong(), location = location)
        println("Login: ssecurity: $ssecurity, userId: $userId, location: $location")
        val serviceToken = getServiceToken(client, authorizeData)
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
        closeHttpClient()
        isLogin.value = 1
        return 0
    }
    println("Login: sign: $_sign")
    _sign?.let { if (it.isNotEmpty()) return 1 }

    // 回退 _sign 获取方案
    _sign?.let {
        if (it.isEmpty()) {
            _sign = response.request.url.parameters["_sign"]?.removePrefix("2&V1_passport&")
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
    client: HttpClient,
    account: String,
    password: String,
    global: Boolean,
    isLogin: MutableState<Int>,
    _sign: String,
    captcha: String = "",
): Int {
    val md5Hash = md5Hash(password)
    val sid = if (global) "miuiota_intl" else "miuiromota"

    val response = client.post(serviceLoginAuth2Url) {
        cookie("sdkVersion", "accountsdk-18.8.15")
        cookie("deviceId", deviceId)
        header("User-Agent", agent)
        header("Content-Type", "application/x-www-form-urlencoded")
        parameter("sid", sid)
        parameter("_json", "true")
        parameter("_sign", _sign)
        parameter("user", account)
        parameter("hash", md5Hash)
        parameter("_locale", if (global) "en_US" else "zh_CN")
        if (captcha.isNotEmpty()) parameter("captcha", captcha)
    }
    response.request.headers.entries().forEach { (key, values) ->
        println("Login: serviceLoginAuth2 Header: $key = ${values.joinToString(", ")}")
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
    if (authJson.location.isNullOrEmpty()) {
        return 4 // 4: 未返回 location
    }
    println("Login: authJson.location: ${authJson.location}")
    val serviceToken = getServiceToken(client, authJson)
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
    closeHttpClient()
    isLogin.value = 1
    return 0
}

/** Get serviceToken from authorize data.
 *
 * @param authorizeData: Data from serviceLogin or serviceLoginAuth2
 *
 * @return serviceToken
 */
suspend fun getServiceToken(
    client: HttpClient,
    authorizeData: DataHelper.AuthorizeData
): String? {
    val locationUrl = authorizeData.location!!
    println("Login: locationUrl: $locationUrl")
    val code = authorizeData.code!!
    val ssecurity = authorizeData.ssecurity!!
    val clientSign = generateClientSign(code, ssecurity)
    println("Login: clientSign: $clientSign")
    val response = client.get(locationUrl) {
        cookie("sdkVersion", "accountsdk-18.8.15")
        cookie("deviceId", deviceId)
        header("User-Agent", agent)
        header("Content-Type", "application/x-www-form-urlencoded")
        parameter("_userIdNeedEncrypt", true)
        parameter("clientSign", clientSign)
    }
    response.request.headers.entries().forEach { (key, values) ->
        println("Login: getServiceToken Header: $key = ${values.joinToString(", ")}")
    }
    return response.headers["Set-Cookie"]
        ?.split(";")
        ?.map { it.trim() }
        ?.firstOrNull { it.startsWith("serviceToken=") }
        ?.removePrefix("serviceToken=")
}


fun generateAgent(): String {
    val agentId = (1..13)
        .map { Random.nextInt(65, 70).toChar() }
        .joinToString("")
    val randomText = (1..18)
        .map { Random.nextInt(97, 123).toChar() }
        .joinToString("")
    return "$randomText-$agentId APP/com.android.updater APPV/8.5.2"
}

fun generateDeviceId(): String {
    return (1..6)
        .map { Random.nextInt(97, 123).toChar() }
        .joinToString("")
}

suspend fun generateClientSign(code: Int, ssecurity: String): String {
    val input = "nonce=$code&$ssecurity"
    val sha1Digest = sha1Hash(input).encodeToByteArray()
    return Base64.UrlSafe.encode(sha1Digest)
}

@OptIn(ExperimentalTime::class)
suspend fun verifyTicket(
    client: HttpClient,
    verifyUrl: String,
    ticket: String
): DataHelper.VerifyTicketData? {
    val path = "identity/authStart"
    val listUrl = verifyUrl.replace(path, "identity/list")
    val resp = client.get(listUrl) {
        cookie("sdkVersion", "accountsdk-18.8.15")
        cookie("deviceId", deviceId)
        header("User-Agent", agent)
        header("Content-Type", "application/x-www-form-urlencoded")
    }
    if (!resp.status.isSuccess()) return null

    val setCookie = resp.headers["Set-Cookie"]
    val identitySession = setCookie
        ?.split(";")
        ?.map { it.trim() }
        ?.firstOrNull { it.startsWith("identity_session=") }?.substringAfter("=")
        ?: return null

    val data = json.decodeFromString<DataHelper.IdentityListData>(resp.body<String>().replace("&&&START&&&", ""))
    println("Login: data: $data")

    val apiMap = mapOf(
        4 to "/identity/auth/verifyPhone",
        8 to "/identity/auth/verifyEmail"
    )
    val filteredApiMap = apiMap.filterKeys { data.options?.contains(it) == true }

    for ((apiInt, apiPath) in filteredApiMap) {
        val apiUrl = "$accountUrl$apiPath"
        println("Login: apiUrl: $apiUrl")

        val response = client.post(apiUrl) {
            cookie("sdkVersion", "accountsdk-18.8.15")
            cookie("deviceId", deviceId)
            cookie("identity_session", identitySession)
            header("User-Agent", agent)
            header("Content-Type", "application/x-www-form-urlencoded")
            parameter("_dc", Clock.System.now().toEpochMilliseconds().toString())
            parameter("ticket", ticket)
            parameter("_flag", apiInt.toString())
            parameter("trust", "true")
            parameter("_json", "true")
        }
        response.request.headers.entries().forEach { (key, values) ->
            println("Login: getServiceToken Header: $key = ${values.joinToString(", ")}")
        }
        val respStr = response.body<String>().replace("&&&START&&&", "")
        println("Login: respStr: $respStr")
        val verifyData = json.decodeFromString<DataHelper.VerifyTicketData>(respStr)
        println("Login: verifyData: $verifyData")
        if (verifyData.code == 0 && !verifyData.location.isNullOrEmpty()) {
            return verifyData
        }
    }
    return null
}

/**
 * Handle 2FA ticket.
 *
 * @param ticket: Ticket from 2FA notification
 *
 * @return Login status
 */
suspend fun handle2FATicket(
    client: HttpClient,
    ticket: String
): Int {
    val notificationUrl = prefGet("notificationUrl")
    if (notificationUrl != null && ticket.isNotBlank()) {
        val verifyData = verifyTicket(client, notificationUrl, ticket)
        println("Login: verifyData: $verifyData")
        if (verifyData != null && verifyData.location != null) {
            println("Login: 2FA verify success, location: ${verifyData.location}")
            return 0
        }
    }
    return 3
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

/**
 * Close the global HttpClient.
 */
fun closeHttpClient() {
    globalClient?.close()
    globalClient = null
}