package com.d4vram.threadsvault.utils

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.d4vram.threadsvault.data.database.entity.PostEntity
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportUtils {

    fun exportPostsCsv(context: Context, posts: List<PostEntity>): File {
        val file = File(context.cacheDir, "threadsvault_export_${System.currentTimeMillis()}.csv")
        val header = listOf(
            "id",
            "url",
            "autor",
            "contenido",
            "imagen",
            "fecha_guardado_iso",
            "fecha_guardado_ms",
            "fecha_post_iso",
            "fecha_post_ms",
            "categorias",
            "etiquetas",
            "notas",
            "favorito",
            "fuente_pwa"
        ).joinToString(",")
        val body = posts.joinToString(separator = "\n") { post ->
            listOf(
                post.id.toString(),
                post.url,
                post.autor,
                post.contenido,
                post.imagenPath.orEmpty(),
                formatTimestamp(post.fechaGuardado),
                post.fechaGuardado.toString(),
                post.fechaPost?.let(::formatTimestamp).orEmpty(),
                post.fechaPost?.toString().orEmpty(),
                post.categorias,
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
