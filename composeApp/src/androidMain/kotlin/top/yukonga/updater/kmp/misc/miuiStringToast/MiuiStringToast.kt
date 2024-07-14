package top.yukonga.updater.kmp.misc.miuiStringToast

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import kotlinx.serialization.json.Json
import top.yukonga.updater.kmp.misc.miuiStringToast.data.IconParams
import top.yukonga.updater.kmp.misc.miuiStringToast.data.Left
import top.yukonga.updater.kmp.misc.miuiStringToast.data.Right
import top.yukonga.updater.kmp.misc.miuiStringToast.data.StringToastBean
import top.yukonga.updater.kmp.misc.miuiStringToast.data.TextParams

object MiuiStringToast {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("WrongConstant")
    fun showStringToast(context: Context, text: String) {
        val textParams = TextParams(text, Color.parseColor("#FFFFFFFF"))
        val left = Left(textParams = textParams)
        val iconParams = IconParams(Category.DRAWABLE, FileType.SVG, "ic_app_icon", 1)
        val right = Right(iconParams = iconParams)
        val stringToastBean = StringToastBean(left, right)
        val jsonStr = Json.encodeToString(StringToastBean.serializer(), stringToastBean)
        val bundle = StringToastBundle.Builder()
            .setPackageName("top.yukonga.updater.kmp")
            .setStrongToastCategory(StrongToastCategory.TEXT_BITMAP)
            .setParam(jsonStr)
            .onCreate()
        val service = context.getSystemService(Context.STATUS_BAR_SERVICE)
        service.javaClass.getMethod(
            "setStatus", Int::class.javaPrimitiveType, String::class.java, Bundle::class.java
        ).invoke(service, 1, "strong_toast_action", bundle)
    }

    object Category {
        const val RAW = "raw"
        const val DRAWABLE = "drawable"
        const val FILE = "file"
        const val MIPMAP = "mipmap"
    }

    object FileType {
        const val MP4 = "mp4"
        const val PNG = "png"
        const val SVG = "svg"
    }

    object StrongToastCategory {
        const val VIDEO_TEXT = "video_text"
        const val VIDEO_BITMAP_INTENT = "video_bitmap_intent"
        const val TEXT_BITMAP = "text_bitmap"
        const val TEXT_BITMAP_INTENT = "text_bitmap_intent"
        const val VIDEO_TEXT_TEXT_VIDEO = "video_text_text_video"
    }
}