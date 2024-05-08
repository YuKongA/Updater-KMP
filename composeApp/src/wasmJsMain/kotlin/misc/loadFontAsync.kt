package misc

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.readResourceBytes
import kotlin.reflect.KProperty

class LoadFontAsync
@OptIn(DelicateCoroutinesApi::class) constructor(
    path: String, weight: FontWeight, style: FontStyle, identifier: String? = null, lazy: Boolean = true, scope: CoroutineScope = GlobalScope
) {
    @OptIn(InternalResourceApi::class)
    private var value: AsyncLazy<Font> = scope.asyncLazy {
        Font(
            identifier ?: path, readResourceBytes(path), weight = weight, style = style
        )
    }.apply { if (!lazy) start() }

    fun onCompleted(callback: (Result<Font>) -> Unit) = value.onCompleted(callback)

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Font? = value.getValue(thisRef, property)
}

fun CoroutineScope.loadFontAsync(
    path: String,
    weight: FontWeight,
    style: FontStyle,
    identifier: String? = null,
    lazy: Boolean = true,
): LoadFontAsync = LoadFontAsync(path, weight, style, identifier, lazy, this)

fun loadFontAsync(
    path: String,
    weight: FontWeight,
    style: FontStyle,
    identifier: String? = null,
    lazy: Boolean = true,
): LoadFontAsync = LoadFontAsync(path, weight, style, identifier, lazy)