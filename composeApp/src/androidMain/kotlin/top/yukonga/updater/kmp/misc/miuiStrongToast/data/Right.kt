package top.yukonga.updater.kmp.misc.miuiStrongToast.data

import kotlinx.serialization.Serializable

@Serializable
data class Right(
    var iconParams: IconParams? = null,
    var textParams: TextParams? = null
)