import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import top.yukonga.updater.kmp.AndroidAppContext

@SuppressLint("StaticFieldLeak")
private val context = AndroidAppContext.getApplicationContext()
private val sharedPreferences: SharedPreferences? = context?.getSharedPreferences("UpdaterKMP", Context.MODE_PRIVATE)

actual fun perfSet(key: String, value: String) {
    sharedPreferences?.edit()?.putString(key, value)?.apply()
}

actual fun perfGet(key: String): String? {
    return sharedPreferences?.getString(key, null)
}

actual fun perfRemove(key: String) {
    sharedPreferences?.edit()?.remove(key)?.apply()
}