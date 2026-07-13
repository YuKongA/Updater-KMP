package data.repository

import data.DataHelper
import data.RomInfoHelper

interface RomInfoRepository {
    suspend fun getRecoveryRomInfo(
        branch: String,
        codeNameExt: String,
        regionCode: String,
        romVersion: String,
        androidVersion: String,
        loginData: DataHelper.LoginData?,
        xmsVersion: String = "",
    ): RomInfoHelper.RomInfo?

    suspend fun getXmsVerInfo(
        codeNameExt: String,
        regionCode: String,
        romVersion: String,
        androidVersion: String,
        pkgs: List<String>,
        lstVer: String,
        rustVersion: String,
        loginData: DataHelper.LoginData?,
    ): RomInfoHelper.XmsDto?
}
