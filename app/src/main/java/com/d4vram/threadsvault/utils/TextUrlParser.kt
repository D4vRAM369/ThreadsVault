package com.d4vram.threadsvault.utils

data class TextUrlMatch(
    val url: String,
    val start: Int,
    val endExclusive: Int
)

object TextUrlParser {
    private val urlRegex = Regex("""https?://\S+""", RegexOption.IGNORE_CASE)
    private val trailingPunctuation = setOf(',', '.', ';', ':', '!', '?', ')', ']', '}')

    fun findUrls(text: String): List<TextUrlMatch> {
        if (text.isBlank()) return emptyList()

        return urlRegex.findAll(text).mapNotNull { match ->
            var end = match.value.length
            while (end > 0 && match.value[end - 1] in trailingPunctuation) {
                end--
            }

            if (end <= 0) return@mapNotNull null

            val cleanUrl = match.value.substring(0, end)
            val start = match.range.first
            val endExclusive = start + end

            TextUrlMatch(
                url = cleanUrl,
                start = start,
                endExclusive = endExclusive
            )
        }.toList()
    }
}
