package com.d4vram.threadsvault.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.d4vram.threadsvault.data.database.entity.CategoryEntity
import com.d4vram.threadsvault.data.database.entity.PostEntity
import java.net.URL
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportUtils {

    fun exportPostsCsv(
        context: Context,
        posts: List<PostEntity>,
        categories: List<CategoryEntity>
    ): File {
        val file = File(context.cacheDir, "threadsvault_export_${System.currentTimeMillis()}.csv")
        val categoriesByName = categories.associateBy { it.nombre.trim().lowercase(Locale.ROOT) }
        val header = listOf(
            "id",
            "url",
            "autor",
            "contenido",
            "imagen",
            "media_urls",
            "fecha_guardado_iso",
            "fecha_guardado_ms",
            "fecha_post_iso",
            "fecha_post_ms",
            "categorias",
            "categorias_colores",
            "categorias_emojis",
            "etiquetas",
            "notas",
            "favorito",
            "fuente_pwa"
        ).joinToString(",")
        val body = posts.joinToString(separator = "\n") { post ->
            val categoryNames = post.categorias.split(",").map { it.trim() }.filter { it.isNotBlank() }
            val categoryColors = categoryNames.map { categoriesByName[it.lowercase(Locale.ROOT)]?.color ?: "#6200EE" }
                .joinToString("|")
            val categoryEmojis = categoryNames.map { categoriesByName[it.lowercase(Locale.ROOT)]?.emoji.orEmpty() }
                .joinToString("|")
            listOf(
                post.id.toString(),
                post.url,
                post.autor,
                post.contenido,
                post.imagenPath.orEmpty(),
                post.mediaUrls.orEmpty(),
                formatTimestamp(post.fechaGuardado),
                post.fechaGuardado.toString(),
                post.fechaPost?.let(::formatTimestamp).orEmpty(),
                post.fechaPost?.toString().orEmpty(),
                post.categorias,
                categoryColors,
                categoryEmojis,
                post.etiquetas,
                post.notas,
                post.esFavorito.toString(),
                post.fuentePWA.toString()
            ).joinToString(",") { csvEscape(it) }
        }
        file.writeText("$header\n$body")
        return file
    }

    fun exportPostsPdf(context: Context, posts: List<PostEntity>): File {
        val file = File(context.cacheDir, "threadsvault_export_${System.currentTimeMillis()}.pdf")
        val pdf = PdfDocument()
        val pageWidth = 1080
        val pageHeight = 1920
        val margin = 48

        val titlePaint = Paint().apply {
            textSize = 34f
            isFakeBoldText = true
        }
        val textPaint = Paint().apply {
            textSize = 24f
        }
        val metaPaint = Paint().apply {
            textSize = 20f
        }

        var pageNumber = 1
        var page = pdf.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
        var canvas = page.canvas
        var y = margin.toFloat()

        canvas.drawText("ThreadsVault Export", margin.toFloat(), y, titlePaint)
        y += 44f
        canvas.drawText(
            "Fecha: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}",
            margin.toFloat(),
            y,
            metaPaint
        )
        y += 50f

        posts.forEachIndexed { index, post ->
            val lines = buildList {
                add("${index + 1}. ${post.autor.ifBlank { "@unknown" }}")
                add("URL: ${post.url}")
                add("Contenido: ${post.contenido.ifBlank { "(sin contenido extraido)" }}")
                if (post.notas.isNotBlank()) add("Notas: ${post.notas}")
                if (post.categorias.isNotBlank()) add("Categorias: ${post.categorias}")
                if (post.etiquetas.isNotBlank()) add("Hashtags: ${post.etiquetas}")
                add("Guardado: ${formatTimestamp(post.fechaGuardado)}")
                add("")
            }

            lines.forEach { line ->
                if (y > pageHeight - margin) {
                    pdf.finishPage(page)
                    pageNumber += 1
                    page = pdf.startPage(
                        PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    )
                    canvas = page.canvas
                    y = margin.toFloat()
                }
                canvas.drawText(line.take(140), margin.toFloat(), y, textPaint)
                y += 30f
            }

            val mediaUrls = MediaUrlsCodec.mergeWithPrimary(post.mediaUrls, post.imagenPath)
            mediaUrls.forEach { mediaUrl ->
                if (y > pageHeight - margin - 220) {
                    pdf.finishPage(page)
                    pageNumber += 1
                    page = pdf.startPage(
                        PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    )
                    canvas = page.canvas
                    y = margin.toFloat()
                }
                val bitmap = runCatching {
                    URL(mediaUrl).openStream().use { BitmapFactory.decodeStream(it) }
                }.getOrNull()
                if (bitmap != null) {
                    val targetWidth = pageWidth - margin * 2
                    val ratio = bitmap.height.toFloat() / bitmap.width.toFloat().coerceAtLeast(1f)
                    val targetHeight = (targetWidth * ratio).toInt().coerceIn(120, 520)
                    val scaled = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true)
                    canvas.drawBitmap(scaled, margin.toFloat(), y, null)
                    y += targetHeight + 16f
                    if (scaled != bitmap) scaled.recycle()
                    bitmap.recycle()
                } else {
                    val label = if (MediaUrlUtils.isVideoUrl(mediaUrl)) "Video: $mediaUrl" else "Media: $mediaUrl"
                    canvas.drawText(label.take(140), margin.toFloat(), y, metaPaint)
                    y += 26f
                }
            }
        }

        pdf.finishPage(page)
        file.outputStream().use { output -> pdf.writeTo(output) }
        pdf.close()
        return file
    }

    private fun csvEscape(value: String): String {
        val escaped = value.replace("\"", "\"\"")
        return "\"$escaped\""
    }

    private fun formatTimestamp(timestamp: Long): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(timestamp))
    }
}
