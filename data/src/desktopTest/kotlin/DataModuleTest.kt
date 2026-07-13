import data.repository.DeviceListRepository
import data.repository.LoginService
import data.repository.OtaMetadataFetcher
import data.repository.RomInfoRepository
import data.repository.SessionRepository
import data.usecase.FetchRomInfoUseCase
import di.dataModule
import org.koin.dsl.koinApplication
import kotlin.test.Test

/**
 * Runtime check that the whole data-layer Koin graph resolves — a missing
 * binding is a compile-clean but run-time failure, so instantiate every
 * top-level dependency once.
 */
class DataModuleTest {
    @Test
    fun dataGraphResolves() {
        val app = koinApplication { modules(dataModule) }
        val koin = app.koin
        koin.get<SessionRepository>()
        koin.get<LoginService>()
        koin.get<RomInfoRepository>()
        koin.get<OtaMetadataFetcher>()
        koin.get<DeviceListRepository>()
        koin.get<FetchRomInfoUseCase>()
        app.close()
    }
}
