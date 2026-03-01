package utils

import top.yukonga.miuix.kmp.utils.Platform
import top.yukonga.miuix.kmp.utils.platform

fun isWeb(): Boolean = platform() == Platform.WasmJs || platform() == Platform.Js
