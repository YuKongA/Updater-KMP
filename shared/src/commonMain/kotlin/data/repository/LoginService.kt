package data.repository

import data.DataHelper
import data.LoginResult
import data.storage.CredentialsStorage
import data.storage.LoginFlowStorage
import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.DelicateCryptographyApi
import dev.whyoleg.cryptography.algorithms.MD5
import io.ktor.client.HttpClient
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import platform.httpClientPlatform
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class LoginService(
    private val credentials: CredentialsStorage,
    private val loginFlow: LoginFlowStorage,
    httpClient: HttpClient = httpClientPlatform(),
) {
    private val client = httpClient.config {
        install(HttpCookies) {
            storage = AcceptAllCookiesStorage()
        }
        defaultRequest {
            userAgent(USER_AGENT)
        }
    }

    private val refreshClient by lazy {
        httpClient.config {
            install(HttpCookies) {
                storage = AcceptAllCookiesStorage()
            }
            defaultRequest {
                userAgent(USER_AGENT)
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    suspend fun login(
        account: String,
        password: String,
        global: Boolean,
        savePassword: Boolean,
        captcha: String = "",
        flag: Int? = null,
        ticket: String = "",
    ): LoginResult = withContext(Dispatchers.Default) {
        if (account.isEmpty() || password.isEmpty()) return@withContext LoginResult.EmptyCredentials
        if (savePassword) credentials.save(account, password) else credentials.delete()
        try {
            if (flag != null && ticket.isNotEmpty()) {
                val verifyResult = verifyTwoFactorTicket(flag = flag, ticket = ticket)
                if (verifyResult == 70014) return@withContext LoginResult.VerificationCodeError
                if (verifyResult != 0) return@withContext LoginResult.LoginFailed
            }
            serviceLoginAuth2(
                account = account,
                password = password,
                global = global,
                captcha = captcha,
            )
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            LoginResult.NetworkError
        }
    }

    private suspend fun serviceLoginAuth2(
        account: String,
        password: String,
        global: Boolean,
        captcha: String = "",
    ): LoginResult {
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
        val response = client.submitForm(SERVICE_LOGIN_AUTH2_URL, parameters)
        if (!response.status.isSuccess()) return LoginResult.NetworkError

        val content = Json.decodeFromString<JsonObject>(removeResponsePrefix(response.bodyAsText()))
        val ssecurity = content["ssecurity"]?.jsonPrimitive?.content
        val passToken = content["passToken"]?.jsonPrimitive?.content
        val notificationUrl = content["notificationUrl"]?.jsonPrimitive?.content
        val result = content["result"]?.jsonPrimitive?.content

        if (notificationUrl != null && notificationUrl != "null") {
            val context = getQueryParam(notificationUrl, "context")
            if (context.isNullOrEmpty()) return LoginResult.LoginFailed
            loginFlow.saveTwoFactorContext(context)
            val listResponse = client.get(notificationUrl.replace("fe/service/identity/authStart", "identity/list"))
            val identitySession = requireNotNull(listResponse.setCookie().find { it.name == "identity_session" }?.value)
            loginFlow.saveIdentitySession(identitySession)
            val listJson = Json.decodeFromString<JsonObject>(removeResponsePrefix(listResponse.bodyAsText()))

            val twoFactorAuth = listJson["twoFactorAuth"]?.jsonPrimitive?.booleanOrNull ?: false
            if (twoFactorAuth) return LoginResult.TwoFactorUnsupported

            val options = listJson["options"]?.jsonArray?.mapNotNull { it.jsonPrimitive.intOrNull } ?: emptyList()
            if (options.isEmpty()) return LoginResult.LoginFailed
            loginFlow.saveNotificationUrl(notificationUrl)
            return LoginResult.TwoFactorRequired(options)
        }

        if ((result != null && result != "ok") || ssecurity.isNullOrBlank()) {
            return LoginResult.LoginFailed
        }

        val location = requireNotNull(content["location"]?.jsonPrimitive?.content)
        val response2 = client.get("$location&_userIdNeedEncrypt=true")
        if (!response2.status.isSuccess()) return LoginResult.NetworkError

        val userId = requireNotNull(content["userId"]?.jsonPrimitive?.content)
        val cUserId = requireNotNull(content["cUserId"]?.jsonPrimitive?.content)
        val serviceToken =
            requireNotNull(response2.setCookie().find { it.name == "serviceToken" && it.value.isNotBlank() }?.value)
        if (serviceToken == "") return LoginResult.SecurityError

        val loginInfo = DataHelper.LoginData(
            accountType = if (global) "GL" else "CN",
            authResult = "1",
            description = "成功",
            ssecurity = ssecurity,
            serviceToken = serviceToken,
            userId = userId,
            cUserId = cUserId,
            passToken = passToken,
        )
        return LoginResult.Success(loginInfo)
    }

    @OptIn(ExperimentalTime::class)
    suspend fun sendTicket(flag: Int): Boolean = withContext(Dispatchers.Default) {
        val apiPath = if (flag == 4) "/identity/auth/sendPhoneTicket" else "/identity/auth/sendEmailTicket"
        val params = parameters {
            append("_json", "true")
            append("retry", "0")
            append("icode", "")
        }
        val sessionCookie = "identity_session=${loginFlow.identitySession()}"
        try {
            client.submitForm("$ACCOUNT_URL$apiPath", params) {
                parameter("_dc", Clock.System.now().toEpochMilliseconds())
                header("cookie", sessionCookie)
            }
            true
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            false
        }
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun verifyTwoFactorTicket(flag: Int, ticket: String): Int {
        val apiPath = if (flag == 4) "/identity/auth/verifyPhone" else "/identity/auth/verifyEmail"
        val apiUrl = "$ACCOUNT_URL$apiPath"

        val parameters = parameters {
            append("_flag", flag.toString())
            append("ticket", ticket)
            append("trust", "true")
            append("_json", "true")
        }
        val sessionCookie = "identity_session=${loginFlow.identitySession()}"
        val verifyResponse = client.submitForm(apiUrl, parameters) {
            parameter("_dc", Clock.System.now().toEpochMilliseconds())
            header("cookie", sessionCookie)
        }
        val verifyBody = Json.decodeFromString<JsonObject>(removeResponsePrefix(verifyResponse.bodyAsText()))
        val code = verifyBody["code"]?.jsonPrimitive?.intOrNull
        if (code == 0) {
            val location = requireNotNull(verifyBody["location"]?.jsonPrimitive?.content)
            client.get(location)
            return 0
        }
        return code ?: -1
    }

    suspend fun refreshServiceToken(loginData: DataHelper.LoginData): DataHelper.LoginData? = withContext(Dispatchers.Default) {
        val passToken = loginData.passToken ?: return@withContext null
        val userId = loginData.userId ?: return@withContext null
        val sid = if (loginData.accountType == "GL") "miuiota_intl" else "miuiromota"

        try {
            val response = refreshClient.get("$ACCOUNT_URL/pass/serviceLogin") {
                parameter("sid", sid)
                parameter("_json", "true")
                header("Cookie", "passToken=$passToken;userId=$userId")
            }
            val content = Json.decodeFromString<JsonObject>(removeResponsePrefix(response.bodyAsText()))
            val ssecurity = content["ssecurity"]?.jsonPrimitive?.content
            val location = content["location"]?.jsonPrimitive?.content
            if (ssecurity.isNullOrBlank() || location.isNullOrBlank()) return@withContext null
            val newPassToken = content["passToken"]?.jsonPrimitive?.content

            val response2 = refreshClient.get("$location&_userIdNeedEncrypt=true")
            val serviceToken = response2.setCookie()
                .find { it.name == "serviceToken" && it.value.isNotBlank() }?.value ?: return@withContext null
            val cUserId = content["cUserId"]?.jsonPrimitive?.content ?: loginData.cUserId

            loginData.copy(
                authResult = "1",
                ssecurity = ssecurity,
                serviceToken = serviceToken,
                cUserId = cUserId,
                passToken = newPassToken ?: passToken,
            )
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            null
        }
    }

    @OptIn(DelicateCryptographyApi::class)
    private suspend fun md5Hash(input: String): String {
        val md = CryptographyProvider.Default.get(MD5)
        return md.hasher().hash(input.encodeToByteArray()).joinToString("") {
            val hex = (it.toInt() and 0xFF).toString(16).uppercase()
            if (hex.length == 1) "0$hex" else hex
        }
    }

    private fun removeResponsePrefix(response: String): String = response.removePrefix("&&&START&&&")

    private fun getQueryParam(url: String, key: String): String? {
        val query = url.substringAfter('?', "")
        return query.split('&')
            .map { it.split('=') }
            .firstOrNull { it.size == 2 && it[0] == key }
            ?.get(1)
    }

    companion object {
        private const val ACCOUNT_URL = "https://account.xiaomi.com"
        private const val SERVICE_LOGIN_AUTH2_URL = "$ACCOUNT_URL/pass/serviceLoginAuth2"
        private const val USER_AGENT = "Dalvik/2.1.0 (Linux; U; Android 16; 2509FPN0BC Build/BP2A.250605.031.A3)"
    }
}
