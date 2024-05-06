package top.yukonga.updater.kmm

import android.annotation.SuppressLint
import android.content.Context

@SuppressLint("StaticFieldLeak")
object AndroidAppContext {
    private var context: Context? = null

    fun init(context: Context) {
        this.context = context.applicationContext
    }

    fun getApplicationContext(): Context? {
        return context
    }
}