package com.d4vram.threadsvault.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ThreadsLinkPreviewExtractorTest {

    @Test
    fun `extractFromHtml gets multiple carousel images from escaped script urls and filters avatar`() {
        val avatarUrl = "https://scontent.cdninstagram.com/v/t51.2885-19/999_avatar.jpg?stp=dst-jpg_s150x150"
        val image1 = "https://scontent.cdninstagram.com/v/t51.2885-15/111_a.jpg?stp=dst-jpg_e35"
        val image2 = "https://scontent.cdninstagram.com/v/t51.2885-15/222_b.jpg?stp=dst-jpg_e35"

        val html = """
            <html>
              <head>
                <meta property="og:image" content="$avatarUrl" />
                <script type="application/ld+json">
                  {
                    "@type":"SocialMediaPosting",
                    "author":{"@type":"Person","name":"jurre_jan","image":{"url":"$avatarUrl"}}
                  }
                </script>
                <script>
                  window.__DATA__ = {
                    "carousel":[
                      "${image1.replace("/", "\\/")}",
                      "${image2.replace("/", "\\/")}"
                    ]
                  };
                </script>
              </head>
              <body></body>
            </html>
        """.trimIndent()

        val preview = ThreadsLinkPreviewExtractor.extractFromHtml(
            url = "https://www.threads.net/@jurre_jan/post/ABC123",
            html = html
        )

        assertEquals(avatarUrl, preview.authorAvatarUrl)
        assertEquals(listOf(image1, image2), preview.imageUrls)
        assertTrue(avatarUrl !in preview.imageUrls)
    }
}
