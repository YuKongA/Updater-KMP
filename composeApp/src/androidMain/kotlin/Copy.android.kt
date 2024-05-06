import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import top.yukonga.updater.kmm.AndroidAppContext

actual fun copyToClipboard(text: String) {
    val context = AndroidAppContext.getApplicationContext()
    val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("label", text)
    clipboard.setPrimaryClip(clip)
}