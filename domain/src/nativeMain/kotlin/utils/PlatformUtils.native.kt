package utils

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValuesOf
import kotlinx.cinterop.pointed
import platform.posix.localtime

actual fun isWeb(): Boolean = false

@OptIn(ExperimentalForeignApi::class)
actual fun formatEpochSeconds(epochSeconds: Long): String {
    val tm = platform.posix.localtime(kotlinx.cinterop.cValuesOf(epochSeconds))?.pointed
        ?: return epochSeconds.toString()
    return "${tm.tm_year + 1900}/${tm.tm_mon + 1}/${tm.tm_mday} " +
            tm.tm_hour.toString().padStart(2, '0') +
            ":${tm.tm_min.toString().padStart(2, '0')}" +
            ":${tm.tm_sec.toString().padStart(2, '0')}"
}
