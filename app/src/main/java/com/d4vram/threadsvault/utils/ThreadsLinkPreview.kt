package com.d4vram.threadsvault.utils

import org.jsoup.Jsoup

data class ThreadsLinkPreview(
    val content: String,
    val imageUrl: String?,
    val videoUrl: String?
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

            val image = firstNonBlank(
                document.select("meta[property=og:image]").attr("content"),
                document.select("meta[name=twitter:image]").attr("content")
            )

            val video = firstNonBlank(
                document.select("meta[property=og:video]").attr("content"),
                document.select("meta[property=og:video:url]").attr("content"),
                document.select("meta[property=og:video:secure_url]").attr("content"),
                document.select("meta[name=twitter:card]").attr("content")
                    .takeIf { it.equals("player", ignoreCase = true) }
                    ?.let { document.select("meta[name=twitter:player:stream]").attr("content") },
                document.select("meta[name=twitter:player:stream]").attr("content")
            )

            ThreadsLinkPreview(content = content, imageUrl = image, videoUrl = video)
        }.getOrDefault(ThreadsLinkPreview(content = "", imageUrl = null, videoUrl = null))
    }

    private fun firstNonBlank(vararg values: String?): String? {
        return values.firstOrNull { !it.isNullOrBlank() }?.trim()
    }
}
