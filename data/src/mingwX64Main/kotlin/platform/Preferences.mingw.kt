package platform

import data.storage.PropertiesFileStore
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UShortVar
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKStringFromUtf16
import okio.Path.Companion.toPath
import platform.windows.GetEnvironmentVariableW

// posix getenv returns ANSI-codepage bytes that break non-ASCII user names
// (e.g. C:\Users\用户\AppData\...); the wide-char API is codepage-safe.
@OptIn(ExperimentalForeignApi::class)
private fun windowsEnv(name: String): String? = memScoped {
    val size = GetEnvironmentVariableW(name, null, 0u)
    if (size == 0u) return@memScoped null
    val buffer = allocArray<UShortVar>(size.toInt())
    val written = GetEnvironmentVariableW(name, buffer, size)
    if (written == 0u || written >= size) null else buffer.toKStringFromUtf16()
}

private val store = PropertiesFileStore(
    (windowsEnv("APPDATA") ?: ".").toPath() / "updater-kmp" / "config.properties"
)

actual fun prefSet(key: String, value: String) = store.set(key, value)

actual fun prefGet(key: String): String? = store.get(key)

actual fun prefRemove(key: String) = store.remove(key)
