package misc

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlin.reflect.KProperty

class AsyncLazy<T> @OptIn(DelicateCoroutinesApi::class) constructor(
    private val initializer: suspend () -> T, private val scope: CoroutineScope = GlobalScope
) {
    private val deferred: Deferred<T> = scope.async(start = CoroutineStart.LAZY) { initializer() }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun onCompleted(callback: (Result<T>) -> Unit) {
        deferred.invokeOnCompletion { throwable ->
            if (deferred.isActive) return@invokeOnCompletion
            if (throwable == null) {
                callback(Result.success(deferred.getCompleted()))
            } else {
                callback(Result.failure(throwable))
            }
        }
    }

    internal fun start() {
        if (!deferred.isActive && !deferred.isCompleted && !deferred.isCancelled) deferred.start()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        start()
        return if (deferred.isCompleted) deferred.getCompleted() else null
    }
}

fun <T> CoroutineScope.asyncLazy(initializer: suspend () -> T): AsyncLazy<T> = AsyncLazy(initializer, this)

fun <T> asyncLazy(initializer: suspend () -> T): AsyncLazy<T> = AsyncLazy(initializer)
