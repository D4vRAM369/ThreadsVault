package com.d4vram.threadsvault.utils

import org.jsoup.Jsoup

data class ThreadsLinkPreview(
    val content: String,
    val imageUrls: List<String>,
    val videoUrls: List<String>
)

object ThreadsLinkPreviewExtractor {

    fun extract(url: String): ThreadsLinkPreview {
        return runCatching {
            val document = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Android) ThreadsVault/1.0")
                .timeout(8_000)
                .get()

            val content = firstNonBlank(
                document.select("meta[property=og:description]").attr("content"),
                document.select("meta[name=description]").attr("content"),
                document.select("meta[name=twitter:description]").attr("content")
            ).orEmpty()

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

            ThreadsLinkPreview(content = content, imageUrls = images, videoUrls = videos)
        }.getOrDefault(ThreadsLinkPreview(content = "", imageUrls = emptyList(), videoUrls = emptyList()))
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
