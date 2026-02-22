package com.d4vram.threadsvault.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TextUrlParserTest {

    @Test
    fun `findUrls extracts multiple urls and trims trailing punctuation`() {
        val text = "Links: https://example.com/test, and (https://openai.com/docs)."

        val urls = TextUrlParser.findUrls(text)

        assertEquals(2, urls.size)
        assertEquals("https://example.com/test", urls[0].url)
        assertEquals("https://openai.com/docs", urls[1].url)
        assertEquals(urls[0].url, text.substring(urls[0].start, urls[0].endExclusive))
        assertEquals(urls[1].url, text.substring(urls[1].start, urls[1].endExclusive))
    }

    @Test
    fun `findUrls returns empty when there are no urls`() {
        assertTrue(TextUrlParser.findUrls("No links here").isEmpty())
    }
}
