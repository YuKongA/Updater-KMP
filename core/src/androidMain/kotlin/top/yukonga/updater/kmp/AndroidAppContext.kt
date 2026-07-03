package top.yukonga.updater.kmp

import android.annotation.SuppressLint
import android.content.Context

@SuppressLint("StaticFieldLeak")
object AndroidAppContext {
    private var context: Context? = null

    fun init(context: Context) {
        AndroidAppContext.context = context.applicationContext
    }

    fun getApplicationContext(): Context? {
        return context
    }
}