package platform

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import top.yukonga.updater.kmp.AndroidAppContext

@SuppressLint("StaticFieldLeak")
private val context = AndroidAppContext.getApplicationContext()
private val sharedPreferences: SharedPreferences? = context?.getSharedPreferences("UpdaterKMP", Context.MODE_PRIVATE)

actual fun prefSet(key: String, value: String) {
    sharedPreferences?.edit()?.putString(key, value)?.apply()
}

actual fun prefGet(key: String): String? {
    return sharedPreferences?.getString(key, null)
}

actual fun prefRemove(key: String) {
    sharedPreferences?.edit()?.remove(key)?.apply()
}