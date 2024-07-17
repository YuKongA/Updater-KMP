package top.yukonga.updater.kmp.misc.miuiStrongToast.data

import kotlinx.serialization.Serializable

@Serializable
data class StrongToastBean(
    var left: Left? = null,
    var right: Right? = null
)