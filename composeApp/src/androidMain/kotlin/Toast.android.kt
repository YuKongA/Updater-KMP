import android.widget.Toast
import top.yukonga.updater.kmp.AndroidAppContext

private var lastToast: Toast? = null

actual fun useToast(): Boolean = true

actual fun showToast(message: String, duration: Long) {
    val context = AndroidAppContext.getApplicationContext()
    lastToast?.cancel()
    lastToast = Toast.makeText(context, message, duration.toInt()).apply { show() }
}