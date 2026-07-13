package di

import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatform

/**
 * Start Koin for the Compose application (data + presentation modules).
 * Guarded so repeated calls (e.g. Android activity re-creation) are no-ops.
 */
fun initKoin() {
    if (KoinPlatform.getKoinOrNull() == null) {
        startKoin {
            modules(dataModule, appModule)
        }
    }
}
