package utils

object LinkUtils {
    private val urlPattern = Regex(
        pattern = """https?://[^\s\u4e00-\u9fa5]+""",
        options = setOf(RegexOption.IGNORE_CASE)
    )

    fun extractLinks(text: String): List<Pair<String, IntRange>> {
        return urlPattern.findAll(text).map {
            it.value to it.range
        }.toList()
    }
}