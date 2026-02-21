package com.d4vram.threadsvault.utils

object MediaUrlsCodec {
    fun encode(urls: List<String>): String? {
        val normalized = normalize(urls)
        if (normalized.isEmpty()) return null
        return normalized.joinToString("\n")
    }

    fun decode(raw: String?): List<String> {
        if (raw.isNullOrBlank()) return emptyList()
        if (raw.trim().startsWith("[") && !raw.contains("http://") && !raw.contains("https://")) {
            return emptyList()
        }
        if (raw.contains("\n")) return normalize(raw.split("\n"))
        return normalize(raw.split(","))
    }

    fun mergeWithPrimary(raw: String?, primary: String?): List<String> {
        return normalize(decode(raw) + listOfNotNull(primary))
    }

    private fun normalize(urls: List<String>): List<String> {
        return urls
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
    }
}
