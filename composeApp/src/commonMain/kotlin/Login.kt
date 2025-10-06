import androidx.compose.runtime.MutableState
import data.DataHelper
import dev.whyoleg.cryptography.DelicateCryptographyApi
import dev.whyoleg.cryptography.algorithms.MD5
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import io.ktor.http.parameters
import io.ktor.http.setCookie
import io.ktor.http.userAgent
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import platform.httpClientPlatform
import platform.prefGet
import platform.prefRemove
import platform.prefSet
import platform.provider
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class Login() {
    private val client = httpClientPlatform().config {
        install(HttpCookies) {
            storage = AcceptAllCookiesStorage()
        }
        defaultRequest {
            userAgent(generateUserAgent())
        }
    }

    private val accountUrl = "https://account.xiaomi.com"
    private val serviceLoginAuth2Url = "$accountUrl/pass/serviceLoginAuth2"

    /**
     * Login Xiaomi account.
     *
     * @param account: Xiaomi account
     * @param password: Xiaomi password
     * @param global: Global or China account
     * @param savePassword: Save password or not
     * @param isLogin: Login status
     * @param captcha: Captcha if needed
     * @param flag: 2FA flag if needed, 4 for phone, 8 for email
     * @param ticket: 2FA ticket if needed
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
        flag: Int? = null,
        ticket: String = "",
    ): Int {
        if (account.isEmpty() || password.isEmpty()) return 1 // 1: 输入为空
        if (savePassword == "1") {
            Password().savePassword(account, password)
        } else {
            Password().deletePassword()
        }
        try {
            if (flag != null && ticket.isEmpty()) {
                val send2FACode = send2FATicket()
                if (!send2FACode) return 3 // 3: 登录失败
            }
            if (flag != null && ticket.isNotEmpty()) {
                val verify2FATicket = verify2FATicket(flag = flag, ticket = ticket)
                if (!verify2FATicket) return 3 // 3: 登录失败
            }
            return serviceLoginAuth2(
                account = account,
                password = password,
                global = global,
                isLogin = isLogin,
                captcha = captcha,
            )
        } catch (_: Exception) {
            return 2 // 2: 网络错误
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
     * Service login Xiaomi account with password.
     *
     * @param account: Xiaomi account
     * @param password: Xiaomi password
     * @param global: Global or China account
     * @param isLogin: Login status
     * @param captcha: Captcha if needed
     *
     * @return Login status
     */
    suspend fun serviceLoginAuth2(
        account: String,
        password: String,
        global: Boolean,
        isLogin: MutableState<Int>,
        captcha: String = "",
    ): Int {
        val md5Hash = md5Hash(password).uppercase()
        val sid = if (global) "miuiota_intl" else "miuiromota"

        val parameters = parameters {
            append("sid", sid)
            append("hash", md5Hash)
            append("user", account)
            append("_json", "true")
            append("_locale", if (global) "en_US" else "zh_CN")
            if (captcha.isNotEmpty()) append("captCode", captcha)
        }
        val response = client.submitForm(serviceLoginAuth2Url, parameters)
        if (!response.status.isSuccess()) return 2 // 2: 网络错误

        val content = Json.decodeFromString<JsonObject>(removeResponsePrefix(response.bodyAsText()))
        val ssecurity = content["ssecurity"]?.jsonPrimitive?.content
        val captchaUrl = content["captchaUrl"]?.jsonPrimitive?.content
        val notificationUrl = content["notificationUrl"]?.jsonPrimitive?.content
        val result = content["result"]?.jsonPrimitive?.content

        if (captchaUrl != null && captchaUrl != "null") {
            prefSet("captchaUrl", captchaUrl)
            return 6 // 6: 需要验证码
        }

        if (!notificationUrl.isNullOrEmpty()) {
            val context = getQueryParam(notificationUrl, "context")
            if (context.isNullOrEmpty()) return 3 // 3: 登录失败
            prefSet("2FAContext", context)
            val response = client.get(notificationUrl.replace("identity/authStart", "identity/list"))
            val identitySession = requireNotNull(response.setCookie().find { it.name == "identity_session" }?.value)
            prefSet("identity_session", identitySession)
            val listJson = Json.decodeFromString<JsonObject>(removeResponsePrefix(response.bodyAsText()))
            val options = listJson["options"]?.jsonArray?.mapNotNull { it.jsonPrimitive.intOrNull } ?: emptyList()
            if (options.isEmpty()) return 3 // 3: 登录失败
            if (options.contains(4)) prefSet("notificationUrl", notificationUrl)
            prefSet("2FAOptions", Json.encodeToString(options))
            return 5 // 5: 需要二次验证
        }

        if ((result != null && result != "ok") || ssecurity.isNullOrBlank()) {
            return 3 // 3: 登录失败
        }

        val location = requireNotNull(content["location"]?.jsonPrimitive?.content)
        val finalResponse = client.get("$location&_userIdNeedEncrypt=true")
        if (!finalResponse.status.isSuccess()) return 2

        val userId = requireNotNull(content["userId"]?.jsonPrimitive?.content)
        val cUserId = requireNotNull(content["cUserId"]?.jsonPrimitive?.content)
        val serviceToken = requireNotNull(finalResponse.setCookie().find { it.name == "serviceToken" }?.value)
        if (serviceToken == "") return 4 // 4: 未返回 serviceToken

        val loginInfo = DataHelper.LoginData(
            accountType = if (global) "GL" else "CN",
            authResult = "1",
            description = "成功",
            ssecurity = ssecurity,
            serviceToken = serviceToken,
            userId = userId,
            cUserId = cUserId
        )
        prefSet("loginInfo", Json.encodeToString(loginInfo))
        isLogin.value = 1
        return 0 // 0: 登录成功
    }

    /**
     * Send 2FA ticket(email).
     *
     * @return Send status
     */
    @OptIn(ExperimentalTime::class)
    suspend fun send2FATicket(): Boolean {
        val sendTicketUrl = "https://account.xiaomi.com/identity/auth/sendEmailTicket"
        val parameters = parameters {
            append("retry", "0")
            append("icode", "")
            append("_json", "true")
        }
        val response = client.submitForm(sendTicketUrl, parameters) {
            parameter("_dc", Clock.System.now().toEpochMilliseconds())
            header("cookie", "identity_session=${prefGet("identity_session") ?: ""}")
        }
        val sendTicketText = response.bodyAsText()
        val sendTicketJson = Json.decodeFromString<JsonObject>(removeResponsePrefix(sendTicketText))
        return sendTicketJson["code"]?.jsonPrimitive?.intOrNull == 0
    }

    /**
     * Verify 2FA ticket(phone or email).
     *
     * @param flag: 4 for phone, 8 for email
     * @param ticket: 2FA ticket
     *
     * @return Handle 2FA ticket status
     */
    @OptIn(ExperimentalTime::class)
    suspend fun verify2FATicket(flag: Int, ticket: String): Boolean {
        val apiPath = if (flag == 4) "/identity/auth/verifyPhone" else "/identity/auth/verifyEmail"
        val apiUrl = "$accountUrl$apiPath"

        val parameters = parameters {
            append("_flag", flag.toString())
            append("ticket", ticket)
            append("trust", "true")
            append("_json", "true")
        }
        val verifyResponse = client.submitForm(apiUrl, parameters) {
            parameter("_dc", Clock.System.now().toEpochMilliseconds())
            header("cookie", "identity_session=${prefGet("identity_session") ?: ""}")
        }
        val verifyBody = Json.decodeFromString<JsonObject>(removeResponsePrefix(verifyResponse.bodyAsText()))
        if (verifyBody["code"]?.jsonPrimitive?.int == 0) {
            val location = requireNotNull(verifyBody["location"]?.jsonPrimitive?.content)
            client.get(location)
            return true
        }
        return false
    }

    /** Generate User-Agent(Xiaomi 17 Pro).
     *
     * @return User-Agent
     */
    fun generateUserAgent(): String {
        return "Dalvik/2.1.0 (Linux; U; Android 16; 25098PN5AC Build/BP2A.250605.031.A3)"
    }

    /**
     * Remove the prefix "&&&START&&&" from the response string.
     *
     * @param response: Response string
     *
     * @return Response string without the prefix
     */
    private fun removeResponsePrefix(response: String): String {
        return response.removePrefix("&&&START&&&")
    }

    /**
     * Get query parameter from URL.
     *
     * @param url: URL string
     * @param key: Query parameter key
     *
     * @return Query parameter value or null if not found
     */
    fun getQueryParam(url: String, key: String): String? {
        val query = url.substringAfter('?', "")
        return query.split('&')
            .map { it.split('=') }
            .firstOrNull { it.size == 2 && it[0] == key }
            ?.get(1)
    }

    /**
     * Generate MD5 hash.
     *
     * @param input: Input string
     *
     * @return MD5 hash
     */
    @OptIn(DelicateCryptographyApi::class)
    suspend fun md5Hash(input: String): String {
        val md = provider().get(MD5)
        return md.hasher().hash(input.encodeToByteArray()).joinToString("") {
            val hex = (it.toInt() and 0xFF).toString(16).uppercase()
            if (hex.length == 1) "0$hex" else hex
        }
    }
}
