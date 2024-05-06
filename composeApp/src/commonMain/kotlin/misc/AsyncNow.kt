package misc

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlin.reflect.KProperty

class AsyncNow<T> @OptIn(DelicateCoroutinesApi::class) constructor(
    initializer: suspend () -> T, scope: CoroutineScope = GlobalScope
) {
    private val value: AsyncLazy<T> = scope.asyncLazy(initializer).apply { start() }

    fun onCompleted(callback: (Result<T>) -> Unit) = value.onCompleted(callback)

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T? = value.getValue(thisRef, property)
}

fun <T> CoroutineScope.asyncNow(initializer: suspend () -> T): AsyncNow<T> = AsyncNow(initializer, this)

fun <T> asyncNow(initializer: suspend () -> T): AsyncNow<T> = AsyncNow(initializer)
