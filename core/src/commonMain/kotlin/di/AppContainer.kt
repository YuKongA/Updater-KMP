package di

import data.repository.DeviceListRepository
import data.repository.LoginService
import data.repository.OtaMetadataFetcher
import data.repository.RomInfoRepository
import data.repository.SessionRepository
import data.storage.CredentialsStorage
import data.storage.LoginFlowStorage
import data.storage.PreferencesStorage
import data.usecase.FetchRomInfoUseCase
import io.ktor.client.HttpClient
import platform.httpClientPlatform

object AppContainer {
    val httpClient: HttpClient by lazy { httpClientPlatform() }
    val preferences: PreferencesStorage by lazy { PreferencesStorage() }
    val session: SessionRepository by lazy { SessionRepository(preferences) }
    val credentials: CredentialsStorage by lazy { CredentialsStorage(preferences) }
    val loginFlow: LoginFlowStorage by lazy { LoginFlowStorage(preferences) }
    val loginService: LoginService by lazy { LoginService(credentials, loginFlow, httpClient) }
    val deviceListRepository: DeviceListRepository by lazy { DeviceListRepository(preferences, client = httpClient) }
    val fetchRomInfoUseCase: FetchRomInfoUseCase by lazy {
        FetchRomInfoUseCase(romInfoRepository, loginService, deviceListRepository, metadataFetcher)
    }
    private val romInfoRepository: RomInfoRepository by lazy { RomInfoRepository(httpClient) }
    private val metadataFetcher: OtaMetadataFetcher by lazy { OtaMetadataFetcher(httpClient) }
}
