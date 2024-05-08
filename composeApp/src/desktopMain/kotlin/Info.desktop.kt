actual suspend fun getRecoveryRomInfo(
    codeNameExt: String, regionCode: String, romVersion: String, androidVersion: String
): String {
    return getRecoveryRomInfoDefault(codeNameExt, regionCode, romVersion, androidVersion)
}