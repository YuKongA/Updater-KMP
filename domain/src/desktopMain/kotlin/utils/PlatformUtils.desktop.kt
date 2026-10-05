package utils

actual fun isWeb(): Boolean = false

actual fun formatEpochSeconds(epochSeconds: Long): String {
    val dt = java.time.Instant.ofEpochSecond(epochSeconds).atZone(java.time.ZoneId.systemDefault())
    return "${dt.year}/${dt.monthValue}/${dt.dayOfMonth} " +
            dt.hour.toString().padStart(2, '0') +
            ":${dt.minute.toString().padStart(2, '0')}" +
            ":${dt.second.toString().padStart(2, '0')}"
}
