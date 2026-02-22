package com.d4vram.threadsvault.utils

import android.util.Log
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

data class ThreadsLinkPreview(
    val content: String,
    val imageUrls: List<String>,
    val videoUrls: List<String>,
    val authorAvatarUrl: String? = null,
    val threadPostUrls: List<String> = emptyList()
)

/**
 * Extracts metadata from a Threads post URL using Jsoup (SSR scraping).
 *
 * IMPORTANT — Threads architecture constraints:
 * Threads is a React/Relay SPA (Meta stack, not Next.js). The SSR response for
 * non-browser UAs includes only Open Graph meta tags. Carousel images beyond the
 * first and the author avatar are exclusively client-side rendered and are NOT
 * available in the raw HTML response.
 *
 * What we CAN reliably extract via SSR:
 *   - Post text      → og:description / meta[name=description]
 *   - First image    → og:image (only the 1st carousel frame is included)
 *   - Video URL      → og:video (when the post is a video)
 *   - Thread URLs    → parsed from HTML by ThreadsThreadParser
 *
 * What is NOT available without JavaScript execution:
 *   - Carousel images 2, 3, … N (client-side only)
 *   - Author avatar URL (client-side only)
 */
object ThreadsLinkPreviewExtractor {

    // Non-browser UA so Threads serves the SSR version (with og:description + og:image).
    // Real browser UAs trigger the SPA shell (React-only, no SSR meta tags) which Jsoup can't render.
    private const val USER_AGENT = "Mozilla/5.0 (Android) ThreadsVault/1.0"

    fun extract(url: String): ThreadsLinkPreview {
        return runCatching {
            val document = Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .timeout(10_000)
                .get()
            extractFromDocument(url = url, document = document)
        }.onFailure { e ->
            Log.e("ThreadsExtractor", "Failed to extract from $url: ${e.message}", e)
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

    fun extractFromHtml(url: String, html: String): ThreadsLinkPreview {
        return runCatching {
            extractFromDocument(url = url, document = Jsoup.parse(html, url))
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

    private fun extractFromDocument(url: String, document: Document): ThreadsLinkPreview {
        val html = document.html()

        val content = firstNonBlank(
            document.select("meta[property=og:description]").attr("content"),
            document.select("meta[name=description]").attr("content"),
            document.select("meta[name=twitter:description]").attr("content")
        ).orEmpty()

        // Only og:image is available in the SSR response.
        // Carousel images 2+ are client-side only and cannot be extracted here.
        val images = collectMetaUrls(
            document.select("meta[property=og:image]").eachAttr("content"),
            document.select("meta[name=twitter:image]").eachAttr("content")
        )

        val videos = collectMetaUrls(
            document.select("meta[property=og:video]").eachAttr("content"),
            document.select("meta[property=og:video:url]").eachAttr("content"),
            document.select("meta[property=og:video:secure_url]").eachAttr("content"),
            document.select("meta[name=twitter:player:stream]").eachAttr("content")
        )

        // Author avatar is not present in the SSR response (client-side only).
        // The UI falls back to the author's initials when authorAvatarUrl is null.
        val authorAvatarUrl: String? = null

        val threadPostUrls = ThreadsThreadParser.extractThreadPostUrls(url, html)

        return ThreadsLinkPreview(
            content = content,
            imageUrls = images,
            videoUrls = videos,
            authorAvatarUrl = authorAvatarUrl,
            threadPostUrls = threadPostUrls
        )
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
