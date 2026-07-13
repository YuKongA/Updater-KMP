package di

import data.repository.DeviceListRepository
import data.repository.DeviceListRepositoryImpl
import data.repository.LoginService
import data.repository.LoginServiceImpl
import data.repository.OtaMetadataFetcher
import data.repository.OtaMetadataFetcherImpl
import data.repository.RomInfoRepository
import data.repository.RomInfoRepositoryImpl
import data.repository.SessionRepository
import data.repository.SessionRepositoryImpl
import data.storage.CredentialsStorage
import data.storage.LoginFlowStorage
import data.storage.PreferencesStorage
import data.usecase.FetchRomInfoUseCase
import io.ktor.client.HttpClient
import org.koin.dsl.module
import platform.httpClientPlatform

/**
 * Koin bindings for the data layer: shared HTTP client, persistent storages,
 * repository/service implementations bound to their interfaces, and use cases.
 */
val dataModule = module {
    single<HttpClient> { httpClientPlatform() }

    single { PreferencesStorage() }
    single { CredentialsStorage(get()) }
    single { LoginFlowStorage(get()) }

    single<SessionRepository> { SessionRepositoryImpl(get()) }
    single<LoginService> { LoginServiceImpl(get(), get(), get()) }
    single<RomInfoRepository> { RomInfoRepositoryImpl(get()) }
    single<OtaMetadataFetcher> { OtaMetadataFetcherImpl(get()) }
    single<DeviceListRepository> { DeviceListRepositoryImpl(get(), client = get()) }

    single { FetchRomInfoUseCase(get(), get(), get(), get()) }
}
