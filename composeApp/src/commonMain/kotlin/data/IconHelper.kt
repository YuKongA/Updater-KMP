package data

import kotlinx.serialization.Serializable

@Serializable
data class IconInfo(
    val iconName: String,
    val iconLink: String,
    val changelog: String,
)