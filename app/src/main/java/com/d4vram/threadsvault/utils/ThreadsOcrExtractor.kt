package com.d4vram.threadsvault.utils

import android.graphics.BitmapFactory
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.net.URL

object ThreadsOcrExtractor {

    suspend fun extractTextFromImageUrl(imageUrl: String): String {
        return runCatching {
            val bitmap = withContext(Dispatchers.IO) {
                URL(imageUrl).openStream().use { input ->
                    BitmapFactory.decodeStream(input)
                }
            } ?: return ""

            val image = InputImage.fromBitmap(bitmap, 0)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val result = recognizer.process(image).await()
            result.text.orEmpty().trim()
        }.getOrDefault("")
    }
}
