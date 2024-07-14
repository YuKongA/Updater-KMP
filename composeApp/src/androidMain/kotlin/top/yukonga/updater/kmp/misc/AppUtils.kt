package top.yukonga.updater.kmp.misc

import android.content.res.Configuration
import android.content.res.Resources.getSystem
import android.os.Build

object AppUtils {
    fun atLeast(version: Int): Boolean = Build.VERSION.SDK_INT >= version

    fun isLandscape(): Boolean = getSystem().configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
}