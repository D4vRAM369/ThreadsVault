package com.d4vram.threadsvault.utils

object ThreadsContentSanitizer {

    fun sanitize(raw: String): String {
        if (raw.isBlank()) return ""

        val cleaned = raw
            .lineSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .filterNot { looksLikeUrlNoise(it) }
            .distinct()
            .joinToString(separator = "\n")
            .trim()

        return cleaned
    }

    private fun looksLikeUrlNoise(line: String): Boolean {
        val lower = line.lowercase()
        return lower.startsWith("http") ||
            lower.contains("threads.net/") ||
            lower.contains("threads.com/") ||
            lower.contains("?xmt=") ||
            lower.contains("&slof=") ||
            lower.contains("post/")
    }
}
