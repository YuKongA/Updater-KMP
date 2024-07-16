package top.yukonga.updater.kmp.misc

import android.annotation.SuppressLint

@SuppressLint("PrivateApi")
object XiaomiUtils {

    val isMiui by lazy {
        try {
            Class.forName("miui.os.Build").getDeclaredField("IS_MIUI").get(null) as Boolean
        } catch (e: Exception) {
            false
        }
    }

    val isMiPad by lazy {
        try {
            Class.forName("miui.os.Build").getDeclaredField("IS_TABLET").get(null) as Boolean
        } catch (e: Exception) {
            false
        }
    }

    private val getHyperOSVersion by lazy {
        try {
            Class.forName("android.os.SystemProperties").getDeclaredMethod("get", String::class.java)
                .invoke(null, "ro.mi.os.version.incremental") as String
        } catch (e: Exception) {
            ""
        }
    }

    val isHyperOS = isMiui && getHyperOSVersion != ""

}