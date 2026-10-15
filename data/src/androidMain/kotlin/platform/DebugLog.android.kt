package platform

import android.content.pm.ApplicationInfo
import android.util.Log
import top.yukonga.updater.kmp.AndroidAppContext

internal actual fun logDebugJson(json: String) {
    val context = AndroidAppContext.getApplicationContext() ?: return
    if (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE == 0) return

    // Keep each UTF-8 chunk below Logcat's limit without splitting surrogate pairs.
    var start = 0
    while (start < json.length) {
        var end = minOf(start + 1000, json.length)
        if (end < json.length && json[end - 1].isHighSurrogate()) end--
        Log.d("UpdaterJson", json.substring(start, end))
        start = end
    }
}
