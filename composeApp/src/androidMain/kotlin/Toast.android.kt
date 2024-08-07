import android.os.Build
import android.widget.Toast
import top.yukonga.updater.kmp.AndroidAppContext
import top.yukonga.updater.kmp.misc.AppUtils.atLeast
import top.yukonga.updater.kmp.misc.AppUtils.isLandscape
import top.yukonga.updater.kmp.misc.XiaomiUtils.isHyperOS
import top.yukonga.updater.kmp.misc.XiaomiUtils.isMiPad
import top.yukonga.updater.kmp.misc.miuiStrongToast.MiuiStrongToast

private var lastToast: Toast? = null

actual fun useToast(): Boolean = true

actual fun isSupportMiuiStrongToast(): Boolean = atLeast(Build.VERSION_CODES.TIRAMISU) && isHyperOS

actual fun showToast(message: String, duration: Long) {
    val context = AndroidAppContext.getApplicationContext()
    lastToast?.cancel()
    lastToast = Toast.makeText(context, message, duration.toInt()).apply { show() }
}

actual fun showExtToast(message: String, duration: Long) {
    val context = AndroidAppContext.getApplicationContext()
    if (!isSupportMiuiStrongToast() || (!isMiPad && isLandscape())) {
        lastToast?.cancel()
        lastToast = Toast.makeText(context, message, duration.toInt()).apply { show() }
    } else {
        MiuiStrongToast.showStrongToast(context!!, message, duration)
    }
}