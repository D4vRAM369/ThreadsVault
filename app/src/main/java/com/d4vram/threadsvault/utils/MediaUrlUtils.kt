package com.d4vram.threadsvault.utils

import java.net.HttpURLConnection
import java.net.URL

object MediaUrlUtils {
    enum class MediaKind {
        Video,
        Image,
        Unknown
    }

    fun isVideoUrl(url: String): Boolean {
        val lower = url.lowercase()
        return lower.contains(".mp4") ||
            lower.contains(".webm") ||
            lower.contains(".m3u8") ||
            lower.contains("video") ||
            lower.contains("/v/t") ||
            lower.contains("bytestream")
    }

    suspend fun resolveMediaKind(url: String): MediaKind {
        if (url.isBlank()) return MediaKind.Unknown

        if (isVideoUrl(url)) return MediaKind.Video
        if (looksLikeImageUrl(url)) return MediaKind.Image

        return probeMediaKind(url)
    }

    private fun looksLikeImageUrl(url: String): Boolean {
        val lower = url.lowercase()
        return lower.contains(".jpg") ||
            lower.contains(".jpeg") ||
            lower.contains(".png") ||
            lower.contains(".webp") ||
            lower.contains("image")
    }

    private fun probeMediaKind(url: String): MediaKind {
        return runCatching {
            val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                instanceFollowRedirects = true
                connectTimeout = 8_000
                readTimeout = 8_000
                requestMethod = "HEAD"
                setRequestProperty("User-Agent", "Mozilla/5.0 (Android) ThreadsVault/1.0")
            }
            connection.connect()
            val contentType = connection.contentType.orEmpty().lowercase()
            connection.disconnect()
            when {
                contentType.startsWith("video/") -> MediaKind.Video
                contentType.startsWith("image/") -> MediaKind.Image
                else -> MediaKind.Unknown
            }
        }.getOrDefault(MediaKind.Unknown)
    }
}
