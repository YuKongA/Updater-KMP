package data.storage

import okio.FileSystem
import okio.Path

/**
 * Minimal key=value store for the native CLI targets. Values are escaped so
 * user-controlled content (e.g. passwords containing newlines) cannot break
 * the line structure or inject other keys.
 *
 * [prepareFile] runs before the first write when the file does not exist yet,
 * so the platform can create it with restrictive permissions (Linux: 0600)
 * before any secret is written.
 */
internal class PropertiesFileStore(
    private val path: Path,
    private val prepareFile: (Path) -> Unit = {},
) {
    private val fs = FileSystem.SYSTEM

    private val cache: MutableMap<String, String> by lazy {
        val map = mutableMapOf<String, String>()
        if (fs.exists(path)) {
            fs.read(path) {
                while (true) {
                    val line = readUtf8Line() ?: break
                    if (line.isBlank() || line.startsWith("#")) continue
                    val idx = line.indexOf('=')
                    if (idx > 0) map[line.substring(0, idx)] = unescape(line.substring(idx + 1))
                }
            }
        }
        map
    }

    private fun store() {
        path.parent?.let { fs.createDirectories(it) }
        if (!fs.exists(path)) prepareFile(path)
        fs.write(path) {
            writeUtf8("# Updater Configuration\n")
            cache.forEach { (k, v) -> writeUtf8("$k=${escape(v)}\n") }
        }
    }

    fun get(key: String): String? = cache[key]

    fun set(key: String, value: String) {
        cache[key] = value
        store()
    }

    fun remove(key: String) {
        if (cache.remove(key) != null) store()
    }

    private fun escape(value: String): String = buildString {
        for (c in value) when (c) {
            '\\' -> append("\\\\")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            else -> append(c)
        }
    }

    private fun unescape(value: String): String = buildString {
        var i = 0
        while (i < value.length) {
            val c = value[i]
            if (c == '\\' && i + 1 < value.length) {
                when (val next = value[i + 1]) {
                    'n' -> append('\n')
                    'r' -> append('\r')
                    '\\' -> append('\\')
                    else -> {
                        append(c)
                        append(next)
                    }
                }
                i += 2
            } else {
                append(c)
                i++
            }
        }
    }
}
