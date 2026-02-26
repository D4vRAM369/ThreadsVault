package com.d4vram.threadsvault.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ThreadsLinkPreviewExtractorTest {

    @Test
    fun `extractFromHtml extracts og image and content, authorAvatarUrl is always null`() {
        val imageUrl = "https://scontent.cdninstagram.com/v/t51.2885-15/111_a.jpg?stp=dst-jpg_e35"

        val html = """
            <html>
              <head>
                <meta property="og:description" content="Hello from Threads" />
                <meta property="og:image" content="$imageUrl" />
              </head>
              <body></body>
            </html>
        """.trimIndent()

        val preview = ThreadsLinkPreviewExtractor.extractFromHtml(
            url = "https://www.threads.net/@jurre_jan/post/ABC123",
            html = html
        )

        assertEquals("Hello from Threads", preview.content)
        assertEquals(listOf(imageUrl), preview.imageUrls)
        assertNull(preview.authorAvatarUrl)
    }
}
