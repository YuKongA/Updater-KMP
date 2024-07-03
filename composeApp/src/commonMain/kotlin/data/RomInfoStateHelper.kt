package data

import kotlinx.serialization.Serializable

@Serializable
data class RomInfoStateHelper(
    var type: String = "",
    var device: String = "",
    var version: String = "",
    var codebase: String = "",
    var branch: String = "",
    var bigVersion: String = "",
    var fileName: String = "",
    var fileSize: String = "",
    var officialDownload: String = "",
    var cdn1Download: String = "",
    var cdn2Download: String = "",
    var changelog: String = ""
)