package data.usecase

import data.DataHelper
import data.DeviceInfoHelper
import data.RomInfoHelper
import data.mapper.RomInfoMapper
import data.repository.DeviceListRepository
import data.repository.LoginService
import data.repository.OtaMetadataFetcher
import data.repository.RomInfoRepository
import data.repository.SessionRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import utils.isWeb

data class RomInfoQuery(
    val deviceName: String,
    val codeName: String,
    val deviceRegion: String,
    val deviceCarrier: String,
    val androidVersion: String,
    val systemVersion: String,
    val rustVersion: String,
    val loginData: DataHelper.LoginData?,
)

sealed interface RomInfoResult {
    data object NetworkError : RomInfoResult
    data object NoData : RomInfoResult

    data class Found(
        val curRomInfo: DataHelper.RomInfoData,
        val curIconInfo: List<DataHelper.IconInfoData>,
        val curImageInfo: List<DataHelper.ImageInfoData>,
        val incRomInfo: DataHelper.RomInfoData,
        val incIconInfo: List<DataHelper.IconInfoData>,
        val incImageInfo: List<DataHelper.ImageInfoData>,
        val xmsInfo: DataHelper.XmsInfoData,
        val noUltimateLink: Boolean,
        val isFallback: Boolean,
    ) : RomInfoResult
}

sealed interface SessionUpdate {
    data class Refreshed(val loginData: DataHelper.LoginData) : SessionUpdate
    data class Expired(val loginData: DataHelper.LoginData) : SessionUpdate
}

/** Single write-site for how a fetch outcome updates the persisted session. */
suspend fun SessionUpdate?.persistTo(session: SessionRepository) {
    when (this) {
        is SessionUpdate.Refreshed -> session.save(loginData)
        is SessionUpdate.Expired -> session.save(loginData.copy(authResult = "3"))
        null -> Unit
    }
}

data class FetchOutcome(
    val result: RomInfoResult,
    val sessionUpdate: SessionUpdate? = null,
)

class FetchRomInfoUseCase(
    private val repository: RomInfoRepository,
    private val loginService: LoginService,
    private val deviceListRepository: DeviceListRepository,
    private val metadataFetcher: OtaMetadataFetcher = OtaMetadataFetcher(),
) {
    suspend fun fetch(request: RomInfoQuery): FetchOutcome {
        val params = buildRequestParams(request)

        val initialInfo = repository.getRecoveryRomInfo(
            params.branchExt, params.codeNameExt, params.regionCode,
            params.systemVersionExt, request.androidVersion, request.loginData
        ) ?: return FetchOutcome(RomInfoResult.NetworkError)

        val refreshOutcome = maybeRefreshAndRetry(initialInfo, params, request)
        val recoveryRomInfo = refreshOutcome.romInfo
        val currentLoginData = refreshOutcome.loginData

        val (xmsForBuild, xmsApps) = fetchXmsBlock(recoveryRomInfo, params, request.androidVersion, request.rustVersion, currentLoginData)
        val xmsInfo = RomInfoMapper.mapXmsInfo(xmsForBuild, recoveryRomInfo.fileMirror?.image ?: "", xmsApps)

        val incrementRom = recoveryRomInfo.incrementRom
        val crossRom = recoveryRomInfo.crossRom
        val result = when {
            recoveryRomInfo.currentRom?.bigversion != null ->
                buildCurrentRomResult(recoveryRomInfo, params, request.androidVersion, currentLoginData, xmsInfo)

            incrementRom?.bigversion != null ->
                buildFallbackResult(recoveryRomInfo, incrementRom, xmsInfo)

            crossRom?.bigversion != null ->
                buildFallbackResult(recoveryRomInfo, crossRom, xmsInfo)

            else -> RomInfoResult.NoData
        }

        return FetchOutcome(result, refreshOutcome.sessionUpdate)
    }

    private data class RefreshOutcome(
        val romInfo: RomInfoHelper.RomInfo,
        val loginData: DataHelper.LoginData?,
        val sessionUpdate: SessionUpdate?,
    )

    private suspend fun maybeRefreshAndRetry(
        initialInfo: RomInfoHelper.RomInfo,
        params: RequestParams,
        request: RomInfoQuery,
    ): RefreshOutcome {
        val loginData = request.loginData ?: return RefreshOutcome(initialInfo, null, null)
        if (initialInfo.authResult == 1) return RefreshOutcome(initialInfo, loginData, null)

        val refreshed =
            loginService.refreshServiceToken(loginData) ?: return RefreshOutcome(initialInfo, loginData, SessionUpdate.Expired(loginData))

        val retried = repository.getRecoveryRomInfo(
            params.branchExt, params.codeNameExt, params.regionCode,
            params.systemVersionExt, request.androidVersion, refreshed
        )
        return RefreshOutcome(retried ?: initialInfo, refreshed, SessionUpdate.Refreshed(refreshed))
    }

    private suspend fun fetchXmsBlock(
        recoveryRomInfo: RomInfoHelper.RomInfo,
        params: RequestParams,
        androidVersion: String,
        rustVersion: String,
        loginData: DataHelper.LoginData?,
    ): Pair<RomInfoHelper.XmsUpdateInfo?, List<DataHelper.XmsAppInfo>> {
        val xmsRaw = recoveryRomInfo.xmsUpdateInfo
        val lstVer = xmsRaw?.lstVer
        if (xmsRaw?.hasXmsUpdate != 1 || lstVer.isNullOrEmpty()) return xmsRaw to emptyList()

        val pkgList = xmsRaw.pkgs.orEmpty()
        return coroutineScope {
            val followUpDeferred = async {
                repository.getRecoveryRomInfo(
                    params.branchExt, params.codeNameExt, params.regionCode,
                    params.systemVersionExt, androidVersion, loginData, lstVer
                )?.xmsUpdateInfo?.changeLog
            }
            val xmsVerDeferred = async {
                if (pkgList.isEmpty()) emptyList()
                else repository.getXmsVerInfo(
                    params.codeNameExt, params.regionCode, params.systemVersionExt,
                    androidVersion, pkgList, lstVer, rustVersion, loginData
                )?.let { RomInfoMapper.mapXmsApps(it) } ?: emptyList()
            }
            val followUpChangeLog = followUpDeferred.await()
            val apps = xmsVerDeferred.await()
            val updated = if (followUpChangeLog != null) xmsRaw.copy(changeLog = followUpChangeLog) else xmsRaw
            updated to apps
        }
    }

    private suspend fun buildCurrentRomResult(
        recoveryRomInfo: RomInfoHelper.RomInfo,
        params: RequestParams,
        androidVersion: String,
        loginData: DataHelper.LoginData?,
        xmsInfo: DataHelper.XmsInfoData,
    ): RomInfoResult.Found {
        val (curRomDownload, noUltimateLink) =
            fetchCurrentRomDownloadUrl(recoveryRomInfo, params, androidVersion, loginData)

        val (curRomData, curIcons, curImages) =
            RomInfoMapper.mapRom(recoveryRomInfo, recoveryRomInfo.currentRom, curRomDownload, noUltimateLink)

        val incRom = recoveryRomInfo.incrementRom ?: recoveryRomInfo.crossRom
        val (incRomData, incIcons, incImages) = if (incRom?.bigversion != null) {
            RomInfoMapper.mapRom(recoveryRomInfo, incRom)
        } else {
            Triple(DataHelper.RomInfoData(), emptyList(), emptyList())
        }

        val ota = if (!isWeb()) {
            val url = if (noUltimateLink) curRomData.cdn1Download else curRomData.official1Download
            if (url.isNotEmpty()) metadataFetcher.getOtaMetadata(url) else null
        } else null
        val enrichedCurRom = RomInfoMapper.applyMetadata(curRomData, ota)

        return RomInfoResult.Found(
            curRomInfo = enrichedCurRom,
            curIconInfo = curIcons,
            curImageInfo = curImages,
            incRomInfo = incRomData,
            incIconInfo = incIcons,
            incImageInfo = incImages,
            xmsInfo = xmsInfo,
            noUltimateLink = noUltimateLink,
            isFallback = false,
        )
    }

    private fun buildFallbackResult(
        recoveryRomInfo: RomInfoHelper.RomInfo,
        rom: RomInfoHelper.Rom,
        xmsInfo: DataHelper.XmsInfoData,
    ): RomInfoResult.Found {
        val (curRomData, curIcons, curImages) = RomInfoMapper.mapRom(recoveryRomInfo, rom)
        return RomInfoResult.Found(
            curRomInfo = curRomData,
            curIconInfo = curIcons,
            curImageInfo = curImages,
            incRomInfo = DataHelper.RomInfoData(),
            incIconInfo = emptyList(),
            incImageInfo = emptyList(),
            xmsInfo = xmsInfo,
            noUltimateLink = false,
            isFallback = true,
        )
    }

    private suspend fun fetchCurrentRomDownloadUrl(
        recoveryRomInfo: RomInfoHelper.RomInfo,
        params: RequestParams,
        androidVersion: String,
        loginData: DataHelper.LoginData?,
    ): Pair<String, Boolean> {
        if (recoveryRomInfo.currentRom?.md5 == recoveryRomInfo.latestRom?.md5) {
            return RomInfoMapper.downloadUrl(
                recoveryRomInfo.currentRom!!.version!!,
                recoveryRomInfo.latestRom?.filename!!
            ) to false
        }

        val recoveryRomInfoCurrent = repository.getRecoveryRomInfo(
            "", params.codeNameExt, params.regionCode,
            params.systemVersionExt, androidVersion, loginData
        ) ?: recoveryRomInfo

        val latestFilename = recoveryRomInfoCurrent.latestRom?.filename
        val currentVersion = recoveryRomInfoCurrent.currentRom?.version ?: recoveryRomInfo.currentRom?.version
        return if (latestFilename != null && currentVersion != null) {
            RomInfoMapper.downloadUrl(
                currentVersion,
                latestFilename
            ) to false
        } else {
            val currentRom = recoveryRomInfo.currentRom!!
            RomInfoMapper.downloadUrl(
                currentRom.version!!,
                currentRom.filename!!
            ) to true
        }
    }

    private fun buildRequestParams(request: RomInfoQuery): RequestParams {
        val regionCode = DeviceInfoHelper.regionCode(request.deviceRegion)
        val carrierCode = DeviceInfoHelper.carrierCode(request.deviceCarrier)
        val deviceCode = deviceListRepository.deviceCodeOf(request.androidVersion, request.codeName, regionCode, carrierCode)
        val regionCodeName = DeviceInfoHelper.regionCodeName(request.deviceRegion)
        val carrierCodeName = DeviceInfoHelper.carrierCodeName(request.deviceCarrier)

        val codeNameExt = if (regionCodeName.isNotEmpty()) {
            request.codeName + regionCodeName.replace("_global", "") + carrierCodeName + "_global"
        } else {
            if (regionCode == "CN" && carrierCode == "DM") {
                request.codeName + "_demo"
            } else {
                request.codeName + carrierCodeName
            }
        }
        val systemVersionExt = request.systemVersion.uppercase()
            .replace("^OS1".toRegex(), "V816")
            .replace("AUTO$".toRegex(), deviceCode)
        val branchExt = if (request.systemVersion.uppercase().endsWith(".DEV")) "X" else "F"

        return RequestParams(regionCode, carrierCode, codeNameExt, systemVersionExt, branchExt)
    }

    private data class RequestParams(
        val regionCode: String,
        val carrierCode: String,
        val codeNameExt: String,
        val systemVersionExt: String,
        val branchExt: String,
    )
}
