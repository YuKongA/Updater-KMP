import kotlin.collections.set

fun iconLink(link: String, iconNameLink: Map<String, String>, iconName: List<String>): MutableMap<String, String> {
    val iconMap = mutableMapOf<String, String>()
    if (iconNameLink.isNotEmpty()) {
        for (name in iconName) {
            val icon = iconNameLink[name]
            if (icon != null) {
                val realLink = link + icon
                iconMap[name] = realLink
            }
        }
    }
    return iconMap
}