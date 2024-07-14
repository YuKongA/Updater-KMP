import android.widget.Toast
import top.yukonga.updater.kmp.AndroidAppContext

private var lastToast: Toast? = null

actual fun useToast(): Boolean = true
actual fun showToast(message: String, duration: Int) {
    lastToast?.cancel()
    val context = AndroidAppContext.getApplicationContext()
    lastToast = Toast.makeText(context, message, duration).apply { show() }
}