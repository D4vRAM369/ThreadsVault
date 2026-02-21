package com.d4vram.threadsvault.utils

import android.net.Uri

data class ParsedCategoryInput(
    val nombre: String,
    val emoji: String,
    val color: String = "#6200EE"
)

object CategoryInputParser {

    fun parse(nombreInput: String, emojiInput: String): ParsedCategoryInput? {
        return parse(nombreInput, emojiInput, "#6200EE")
    }

    fun parse(nombreInput: String, emojiInput: String, colorInput: String): ParsedCategoryInput? {
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
        val normalizedColor = sanitizeColor(colorInput)

        return ParsedCategoryInput(
            nombre = normalizedName,
            emoji = normalizedEmoji,
            color = normalizedColor
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

    private fun sanitizeColor(input: String): String {
        val clean = input.trim()
        val hex6 = Regex("^#[0-9A-Fa-f]{6}$")
        return if (hex6.matches(clean)) clean.uppercase() else "#6200EE"
    }
}
