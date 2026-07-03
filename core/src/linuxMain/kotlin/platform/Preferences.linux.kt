package platform

import data.storage.PropertiesFileStore
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import okio.Path
import okio.Path.Companion.toPath
import platform.posix.O_CREAT
import platform.posix.O_WRONLY
import platform.posix.S_IRUSR
import platform.posix.S_IWUSR
import platform.posix.close
import platform.posix.getenv
import platform.posix.open

// Create the file 0600 before any secret is written: the config holds session
// tokens and (plaintext) credentials, and okio preserves an existing file's mode.
@OptIn(ExperimentalForeignApi::class)
private fun createSecureFile(path: Path) {
    val fd = open(path.toString(), O_CREAT or O_WRONLY, (S_IRUSR or S_IWUSR).toUInt())
    if (fd >= 0) close(fd)
}

@OptIn(ExperimentalForeignApi::class)
private val store = PropertiesFileStore(
    (getenv("XDG_CONFIG_HOME")?.toKString()?.toPath()
        ?: ((getenv("HOME")?.toKString() ?: ".").toPath() / ".config"))
        / "updater-kmp" / "config.properties",
    prepareFile = ::createSecureFile,
)

actual fun prefSet(key: String, value: String) = store.set(key, value)

actual fun prefGet(key: String): String? = store.get(key)

actual fun prefRemove(key: String) = store.remove(key)
