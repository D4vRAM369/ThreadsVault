package com.d4vram.threadsvault.utils

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

data class ThreadsLinkPreview(
    val content: String,
    val imageUrls: List<String>,
    val videoUrls: List<String>,
    val authorAvatarUrl: String? = null,
    val threadPostUrls: List<String> = emptyList()
)

object ThreadsLinkPreviewExtractor {

    fun extract(url: String): ThreadsLinkPreview {
        return runCatching {
            val document = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Android) ThreadsVault/1.0")
                .timeout(8_000)
                .get()

            val html = document.html()

            val content = firstNonBlank(
                document.select("meta[property=og:description]").attr("content"),
                document.select("meta[name=description]").attr("content"),
                document.select("meta[name=twitter:description]").attr("content")
            ).orEmpty()

            val rawImages = collectMetaUrls(
                document.select("meta[property=og:image]").eachAttr("content"),
                document.select("meta[name=twitter:image]").eachAttr("content")
            )

            val videos = collectMetaUrls(
                document.select("meta[property=og:video]").eachAttr("content"),
                document.select("meta[property=og:video:url]").eachAttr("content"),
                document.select("meta[property=og:video:secure_url]").eachAttr("content"),
                document.select("meta[name=twitter:player:stream]").eachAttr("content")
            )

            // Extract avatar separately and remove it from media images
            val authorAvatarUrl = extractAuthorAvatarUrl(document, html, rawImages)
            val images = if (authorAvatarUrl != null) rawImages.filter { it != authorAvatarUrl } else rawImages

            // Extract thread post URLs using the existing parser
            val threadPostUrls = ThreadsThreadParser.extractThreadPostUrls(url, html)

            ThreadsLinkPreview(
                content = content,
                imageUrls = images,
                videoUrls = videos,
                authorAvatarUrl = authorAvatarUrl,
                threadPostUrls = threadPostUrls
            )
        }.getOrDefault(
            ThreadsLinkPreview(
                content = "",
                imageUrls = emptyList(),
                videoUrls = emptyList(),
                authorAvatarUrl = null,
                threadPostUrls = emptyList()
            )
        )
    }

    /**
     * Extracts the author avatar URL from the page.
     * Tries JSON-LD structured data first, then CDN URL heuristics.
     */
    private fun extractAuthorAvatarUrl(
        doc: Document,
        html: String,
        allImages: List<String>
    ): String? {
        // Strategy 1: JSON-LD structured data (most reliable)
        doc.select("script[type='application/ld+json']").forEach { script ->
            val json = script.data()
            // Pattern: "author": { ..., "image": { "url": "https://..." } }
            val match = Regex(
                """"author"[^}]{0,200}"image"[^}]{0,200}"url"\s*:\s*"(https://[^"]+)"""",
                setOf(RegexOption.DOT_MATCHES_ALL)
            ).find(json)
            if (match != null) return match.groupValues[1]

            // Fallback: "image": "https://..." near author type Person
            val match2 = Regex(
                """"@type"\s*:\s*"Person"[^}]{0,300}"image"\s*:\s*"(https://[^"]+)"""",
                setOf(RegexOption.DOT_MATCHES_ALL)
            ).find(json)
            if (match2 != null) return match2.groupValues[1]
        }

        // Strategy 2: Instagram/Threads CDN profile picture path patterns
        // Profile pics use path "t51.2885-19/" on Instagram's CDN
        val profilePathPattern = Regex(
            """https://[a-z0-9.-]+\.(?:cdninstagram|fbcdn)\.com/v/[^"'\s]*t51\.2885-19/[^"'\s]*"""
        )
        val cdnMatch = profilePathPattern.find(html)
        if (cdnMatch != null) return cdnMatch.value

        // Strategy 3: Heuristic - filter images that look like profile pictures
        val profileKeywords = listOf("profile_image", "profile_pic", "s150x150", "s320x320")
        allImages.forEach { imageUrl ->
            if (profileKeywords.any { imageUrl.contains(it, ignoreCase = true) }) {
                return imageUrl
            }
        }

        return null
    }

    private fun firstNonBlank(vararg values: String?): String? {
        return values.firstOrNull { !it.isNullOrBlank() }?.trim()
    }

    private fun collectMetaUrls(vararg groups: List<String>): List<String> {
        return groups
            .flatMap { it }
            .mapNotNull { it?.trim() }
            .flatMap { it.split(",") }
            .map { it.trim() }
            .filter { it.startsWith("http://") || it.startsWith("https://") }
            .distinct()
    }
}
