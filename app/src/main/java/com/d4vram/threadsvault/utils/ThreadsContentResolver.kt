package com.d4vram.threadsvault.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ResolvedThreadsContent(
    val content: String,
    val mediaUrl: String?
)

object ThreadsContentResolver {

    suspend fun resolve(url: String): ResolvedThreadsContent {
        val preview = withContext(Dispatchers.IO) {
            ThreadsLinkPreviewExtractor.extract(url)
        }

        val content = if (preview.content.isNotBlank()) {
            preview.content
        } else {
            preview.imageUrl
                ?.takeIf { it.isNotBlank() }
                ?.let { ThreadsOcrExtractor.extractTextFromImageUrl(it) }
                .orEmpty()
        }

        return ResolvedThreadsContent(
            content = ThreadsContentSanitizer.sanitize(content),
            mediaUrl = preview.imageUrl ?: preview.videoUrl
        )
    }
}
