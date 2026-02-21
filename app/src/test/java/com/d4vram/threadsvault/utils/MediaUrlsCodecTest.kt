package com.d4vram.threadsvault.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaUrlsCodecTest {

    @Test
    fun `encode and decode keeps order and removes blanks`() {
        val raw = listOf(
            "https://img.example/a.jpg",
            "  ",
            "https://img.example/b.jpg",
            "https://img.example/a.jpg"
        )

        val encoded = MediaUrlsCodec.encode(raw)
        val decoded = MediaUrlsCodec.decode(encoded)

        assertEquals(
            listOf("https://img.example/a.jpg", "https://img.example/b.jpg"),
            decoded
        )
    }

    @Test
    fun `decode fallback supports csv and malformed json`() {
        val decodedCsv = MediaUrlsCodec.decode("https://a.jpg, https://b.jpg")
        val decodedMalformed = MediaUrlsCodec.decode("[not-json")

        assertEquals(listOf("https://a.jpg", "https://b.jpg"), decodedCsv)
        assertTrue(decodedMalformed.isEmpty())
    }
}
