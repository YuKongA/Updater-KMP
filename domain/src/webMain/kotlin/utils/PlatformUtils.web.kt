package utils

actual fun isWeb(): Boolean = true

actual fun formatEpochSeconds(epochSeconds: Long): String {
    val totalSecs = epochSeconds
    val sec = (totalSecs % 60).toInt().let { if (it < 0) it + 60 else it }
    val totalMins = (totalSecs - sec) / 60
    val min = (totalMins % 60).toInt().let { if (it < 0) it + 60 else it }
    val totalHours = (totalMins - min) / 60
    val hour = (totalHours % 24).toInt().let { if (it < 0) it + 24 else it }
    val totalDays = (totalHours - hour) / 24

    val z = totalDays + 719468
    val era = (if (z >= 0) z else z - 146096) / 146097
    val doe = (z - era * 146097).toInt()
    val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365
    var y = yoe + era * 400
    val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
    val mp = (5 * doy + 2) / 153
    val d = doy - (153 * mp + 2) / 5 + 1
    val m = if (mp < 10) mp + 3 else mp - 9
    if (m <= 2) y++
    return "$y/$m/$d ${hour.toString().padStart(2, '0')}:${min.toString().padStart(2, '0')}:${sec.toString().padStart(2, '0')}"
}
