package com.d4vram.threadsvault.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ThreadsThreadParserTest {

    @Test
    fun `extractThreadPostUrls keeps same-author posts in appearance order and normalizes urls`() {
        val baseUrl = "https://www.threads.net/@alice/post/123?x=1"
        val html = """
            <html>
              <body>
                <a href="/@alice/post/123">Root</a>
                <a href="https://www.threads.net/@alice/post/124/">Next</a>
                <a href="https://www.threads.net/@bob/post/999">Other author</a>
                <a href="/@alice/post/124?igshid=test">Duplicate with query</a>
                <a href="/@alice/post/125#fragment">Third</a>
              </body>
            </html>
        """.trimIndent()

        val urls = ThreadsThreadParser.extractThreadPostUrls(baseUrl = baseUrl, html = html)

        assertEquals(
            listOf(
                "https://www.threads.net/@alice/post/123",
                "https://www.threads.net/@alice/post/124",
                "https://www.threads.net/@alice/post/125"
            ),
            urls
        )
    }

    @Test
    fun `buildThreadGroupId is stable for same set of urls`() {
        val first = listOf(
            "https://www.threads.net/@alice/post/124",
            "https://www.threads.net/@alice/post/123"
        )
        val second = listOf(
            "https://www.threads.net/@alice/post/123?foo=1",
            "https://www.threads.net/@alice/post/124#x"
        )

        val id1 = ThreadsThreadParser.buildThreadGroupId(first)
        val id2 = ThreadsThreadParser.buildThreadGroupId(second)

        assertTrue(id1.isNotBlank())
        assertEquals(id1, id2)
    }

    @Test
    fun `extractThreadPostUrls also finds embedded urls in script html`() {
        val baseUrl = "https://www.threads.net/@alice/post/123"
        val html = """
            <script>
              window.__DATA__ = {
                "items":[
                  "https://www.threads.net/@alice/post/124?foo=1",
                  "https://www.threads.net/@alice/post/125#x"
                ]
              }
            </script>
        """.trimIndent()

        val urls = ThreadsThreadParser.extractThreadPostUrls(baseUrl, html)

        assertEquals(
            listOf(
                "https://www.threads.net/@alice/post/123",
                "https://www.threads.net/@alice/post/124",
                "https://www.threads.net/@alice/post/125"
            ),
            urls
        )
    }
}
