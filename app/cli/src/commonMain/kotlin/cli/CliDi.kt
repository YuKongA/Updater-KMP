package cli

import data.repository.DeviceListRepository
import data.repository.LoginService
import data.repository.SessionRepository
import data.storage.CredentialsStorage
import data.storage.LoginFlowStorage
import data.usecase.FetchRomInfoUseCase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** Koin-backed accessor for the data layer used by CLI commands. */
object CliDi : KoinComponent {
    val session: SessionRepository by inject()
    val credentials: CredentialsStorage by inject()
    val loginFlow: LoginFlowStorage by inject()
    val loginService: LoginService by inject()
    val deviceListRepository: DeviceListRepository by inject()
    val fetchRomInfoUseCase: FetchRomInfoUseCase by inject()
}
