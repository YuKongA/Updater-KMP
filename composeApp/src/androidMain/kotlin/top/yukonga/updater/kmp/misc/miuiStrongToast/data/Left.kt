package top.yukonga.updater.kmp.misc.miuiStrongToast.data

import kotlinx.serialization.Serializable

@Serializable
data class Left(
    var iconParams: IconParams? = null,
    var textParams: TextParams? = null
)