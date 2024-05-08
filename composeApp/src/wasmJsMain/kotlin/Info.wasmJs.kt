import kotlinx.browser.window
import kotlinx.coroutines.await
import org.w3c.fetch.DEFAULT
import org.w3c.fetch.FOLLOW
import org.w3c.fetch.Headers
import org.w3c.fetch.NO_CORS
import org.w3c.fetch.RequestCache
import org.w3c.fetch.RequestCredentials
import org.w3c.fetch.RequestInit
import org.w3c.fetch.RequestMode
import org.w3c.fetch.RequestRedirect
import org.w3c.fetch.SAME_ORIGIN

actual suspend fun getRecoveryRomInfo(codeNameExt: String, regionCode: String, romVersion: String, androidVersion: String): String {
    val jsonData = generateJson(codeNameExt, regionCode, romVersion, androidVersion, userId)
    val encryptedText = miuiEncrypt(jsonData, securityKey)
    val postData = "q=${encryptedText}&t=${serviceToken}&s=${port}"
    println(postData)
    val recoveryUrl = if (accountType == "GL") INTL_RECOVERY_URL else CN_RECOVERY_URL
    val init = RequestInit(
        method = "POST",
        headers = Headers(),
        body = "".toJsString(),
        referrer = "about:client",
        referrerPolicy = "".toJsString(),
        mode = RequestMode.NO_CORS,
        credentials = RequestCredentials.SAME_ORIGIN,
        cache = RequestCache.DEFAULT,
        redirect = RequestRedirect.FOLLOW,
        integrity = "",
        keepalive = true,
    )
    val postUrl = "$recoveryUrl?$postData"
    val requestedEncryptedText = window.fetch(postUrl, init).then { it.text() }.await() as JsString
    println(requestedEncryptedText.toString())
    return miuiDecrypt(requestedEncryptedText.toString(), securityKey)
}