package data

import kotlinx.serialization.Serializable

@Serializable
data class IconInfoHelper(
    val iconName: String,
    val iconLink: String,
    val changelog: String,
)