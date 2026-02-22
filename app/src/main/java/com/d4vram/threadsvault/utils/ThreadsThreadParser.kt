package com.d4vram.threadsvault.utils

import org.jsoup.Jsoup
import java.net.URI
import java.security.MessageDigest

object ThreadsThreadParser {
    private val embeddedPostUrlRegex = Regex(
        """https?://(?:www\.)?threads(?:\.net|\.com)/@[A-Za-z0-9._]+/post/[A-Za-z0-9_-]+(?:[?#][^"'\\\s<>]*)?""",
        RegexOption.IGNORE_CASE
    )

    fun extractThreadPostUrls(baseUrl: String, html: String): List<String> {
        val normalizedBase = canonicalizePostUrl(baseUrl) ?: return emptyList()
        val baseAuthor = extractAuthorHandle(normalizedBase)

        val doc = Jsoup.parse(html, normalizedBase)
        val urls = buildList {
            add(normalizedBase)
            doc.select("a[href]").forEach { element ->
                val resolved = element.absUrl("href").ifBlank { element.attr("href") }
                canonicalizePostUrl(resolved)?.let { candidate ->
                    val sameAuthor = baseAuthor == null || extractAuthorHandle(candidate).equals(baseAuthor, ignoreCase = true)
                    if (sameAuthor) add(candidate)
                }
            }
            embeddedPostUrlRegex.findAll(html).forEach { match ->
                canonicalizePostUrl(match.value)?.let { candidate ->
                    val sameAuthor = baseAuthor == null || extractAuthorHandle(candidate).equals(baseAuthor, ignoreCase = true)
                    if (sameAuthor) add(candidate)
                }
            }
        }

        return urls.distinct()
    }

    fun buildThreadGroupId(urls: List<String>): String {
        val normalized = urls
            .mapNotNull(::canonicalizePostUrl)
            .distinct()
            .sorted()
        if (normalized.isEmpty()) return ""

        val payload = normalized.joinToString("|")
        val digest = MessageDigest.getInstance("SHA-256").digest(payload.toByteArray())
        val shortHex = digest.take(8).joinToString("") { "%02x".format(it) }
        return "thread_$shortHex"
    }

    fun canonicalizePostUrl(rawUrl: String): String? {
        val trimmed = rawUrl.trim()
        if (trimmed.isBlank()) return null

        val uri = runCatching { URI(trimmed) }.getOrNull() ?: return null
        val host = uri.host?.lowercase() ?: return null
        val path = uri.path?.trimEnd('/').orEmpty()

        if (!(host.endsWith("threads.net") || host.endsWith("threads.com"))) return null
        if (!path.contains("/post/")) return null

        return URI(
            uri.scheme?.lowercase() ?: "https",
            uri.userInfo,
            host,
            if (uri.port == 80 || uri.port == 443) -1 else uri.port,
            path,
            null,
            null
        ).toString()
    }

    private fun extractAuthorHandle(url: String): String? {
        val match = Regex("""threads(?:\.net|\.com)/@([A-Za-z0-9._]+)""", RegexOption.IGNORE_CASE)
            .find(url)
        return match?.groupValues?.getOrNull(1)
    }
}
