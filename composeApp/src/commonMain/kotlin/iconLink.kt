import kotlin.collections.set

/**
 * Generate maps with links with corresponding names and icons.
 *
 * @param iconNames: Icon names included in the changelog
 * @param iconMainLink: Main link to get the icon
 * @param iconNameLink: Links that correspond to each icon name
 *
 * @return Links to icons with corresponding names
 */
fun iconLink(iconNames: List<String>, iconMainLink: String, iconNameLink: Map<String, String>): MutableMap<String, String> {
    val iconMap = mutableMapOf<String, String>()
    if (iconNameLink.isNotEmpty()) {
        for (name in iconNames) {
            val icon = iconNameLink[name]
            if (icon != null) {
                val realLink = iconMainLink + icon
                iconMap[name] = realLink
            }
        }
    }
    return iconMap
}