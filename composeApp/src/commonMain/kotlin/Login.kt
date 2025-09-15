import androidx.compose.runtime.MutableState
import data.DataHelper
import dev.whyoleg.cryptography.DelicateCryptographyApi
import dev.whyoleg.cryptography.algorithms.MD5
import io.ktor.client.plugins.cookies.AcceptAllCookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.cookies.get
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import io.ktor.http.parameters
import io.ktor.http.setCookie
import io.ktor.http.userAgent
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
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
            Password().savePassword(account, password)
        } else {
            Password().deletePassword()
        }
        try {
            if (ticket.isNotEmpty()) {
                val verify2FATicket = verify2FATicket(ticket = ticket)
                if (!verify2FATicket) return 3
            }
            val serviceLoginAuth2 = serviceLoginAuth2(
                account = account,
                password = password,
                global = global,
                isLogin = isLogin,
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
     * Service login Xiaomi account with password.
     *
     * @param password: Password
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
        if (!response.status.isSuccess()) return 2

        val content = Json.decodeFromString<JsonObject>(removeResponsePrefix(response.bodyAsText()))
        val ssecurity = content["ssecurity"]?.jsonPrimitive?.content
        val captchaUrl = content["captchaUrl"]?.jsonPrimitive?.content
        val notificationUrl = content["notificationUrl"]?.jsonPrimitive?.content
        val result = content["result"]?.jsonPrimitive?.content

        if (captchaUrl != null && captchaUrl != "null") {
            prefSet("captchaUrl", captchaUrl)
            return 6 // 6: 需要验证码
        }
        if (notificationUrl != null && notificationUrl != "null") {
            prefSet("notificationUrl", notificationUrl)
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
        val serviceToken = requireNotNull(
            finalResponse.setCookie().last { it.name == "serviceToken" && it.value.isNotBlank() }.value
        )
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
        return 0
    }


    /**
     * Verify 2FA ticket.
     *
     * @param ticket: 2FA ticket
     *
     * @return Handle 2FA ticket status
     */
    @OptIn(ExperimentalTime::class)
    suspend fun verify2FATicket(
        ticket: String
    ): Boolean {
        val notificationUrl = prefGet("notificationUrl")
        if (notificationUrl != null && ticket.isNotBlank()) {
            val path = "identity/authStart"
            val identitiesResponse = client.get(notificationUrl.replace(path, "identity/list"))
            val identitiesBody = Json.decodeFromString<JsonObject>(removeResponsePrefix(identitiesResponse.bodyAsText()))
            val flag = identitiesBody["flag"]?.jsonPrimitive?.int
            val options = identitiesBody["options"]?.jsonArray
            require(identitiesResponse.setCookie()["identity_session"]?.value.isNullOrBlank().not())
            require(options?.any { it.jsonPrimitive.int == flag } == true)

            val apiMap = mapOf(
                4 to "/identity/auth/verifyPhone",
                8 to "/identity/auth/verifyEmail"
            )

            for ((apiFlag, apiPath) in apiMap) {
                val apiUrl = "$accountUrl$apiPath"

                val parameters = parameters {
                    append("_flag", apiFlag.toString())
                    append("ticket", ticket)
                    append("trust", "true")
                    append("_json", "true")
                }
                val verifyResponse = client.submitForm(apiUrl, parameters) {
                    parameter("_dc", Clock.System.now().toEpochMilliseconds())
                }

                val verifyBody = Json.decodeFromString<JsonObject>(removeResponsePrefix(verifyResponse.bodyAsText()))
                if (verifyBody["code"]?.jsonPrimitive?.int == 0) {
                    val location = requireNotNull(verifyBody["location"]?.jsonPrimitive?.content)
                    client.get(location)
                    return true
                }
            }
        }
        return false
    }

    /** Generate random User-Agent.
     *
     * @return User-Agent
     */
    fun generateUserAgent(): String {
        val agentId = (1..13).map { ('A'..'E').random() }.joinToString("")
        val randomText = (1..18).map { ('a'..'z').random() }.joinToString("")
        return "$randomText-$agentId APP/com.android.updater APPV/9.1.0"
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
