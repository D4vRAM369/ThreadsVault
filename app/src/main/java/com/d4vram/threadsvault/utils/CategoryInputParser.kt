package com.d4vram.threadsvault.utils

import android.net.Uri

data class ParsedCategoryInput(
    val nombre: String,
    val emoji: String
)

object CategoryInputParser {

    fun parse(nombreInput: String, emojiInput: String): ParsedCategoryInput? {
        val rawName = nombreInput.trim()
        if (rawName.isBlank()) return null

        val isUrl = rawName.startsWith("http://") || rawName.startsWith("https://")
        val normalizedName = if (isUrl) {
            fallbackNameFromUrl(rawName)
        } else {
            rawName
        }.take(40)

        if (normalizedName.isBlank()) return null

        val normalizedEmoji = sanitizeEmoji(emojiInput)

        return ParsedCategoryInput(
            nombre = normalizedName,
            emoji = normalizedEmoji
        )
    }

    private fun fallbackNameFromUrl(url: String): String {
        val uri = runCatching { Uri.parse(url) }.getOrNull() ?: return "Categoria"
        val host = uri.host.orEmpty().replace("www.", "")
        val lastSegment = uri.pathSegments.lastOrNull().orEmpty()
        val base = if (lastSegment.isNotBlank()) lastSegment else host
        val normalized = base.replace(Regex("[^a-zA-Z0-9-_]"), " ").trim()
        return normalized.ifBlank { "Categoria" }
    }

    private fun sanitizeEmoji(input: String): String {
        val clean = input.trim()
        if (clean.isBlank()) return ""
        if (clean.contains("<") || clean.contains(">")) return ""
        if (clean.startsWith("http://") || clean.startsWith("https://")) return ""
        return clean.take(4)
    }
}
