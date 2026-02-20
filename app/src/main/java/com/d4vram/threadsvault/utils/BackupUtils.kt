package com.d4vram.threadsvault.utils

import android.content.Context
import com.d4vram.threadsvault.data.database.entity.CategoryEntity
import com.d4vram.threadsvault.data.database.entity.PostEntity
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val BACKUP_SCHEMA_VERSION = 1

@Serializable
data class BackupPayload(
    val schemaVersion: Int = BACKUP_SCHEMA_VERSION,
    val exportedAtMillis: Long = System.currentTimeMillis(),
    val posts: List<PostEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList()
)

data class RestoreSummary(
    val importedPosts: Int,
    val importedCategories: Int
)

object BackupUtils {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        encodeDefaults = true
    }

    fun exportBackupJson(
        context: Context,
        posts: List<PostEntity>,
        categories: List<CategoryEntity>
    ): File {
        val file = File(
            context.cacheDir,
            "threadsvault_backup_${System.currentTimeMillis()}.json"
        )
        val normalizedCategories = ensureFallbackCategory(categories)
        val payload = BackupPayload(
            posts = posts,
            categories = normalizedCategories
        )
        file.writeText(json.encodeToString(BackupPayload.serializer(), payload))
        return file
    }

    fun parseBackup(inputStream: InputStream): BackupPayload {
        val content = inputStream.bufferedReader().use { it.readText() }
        val payload = json.decodeFromString(BackupPayload.serializer(), content)
        require(payload.schemaVersion <= BACKUP_SCHEMA_VERSION) {
            "Backup con schemaVersion no soportado: ${payload.schemaVersion}"
        }
        return payload.copy(
            categories = ensureFallbackCategory(payload.categories)
        )
    }

    fun exportBackupCsv(
        context: Context,
        posts: List<PostEntity>
    ): File {
        val file = File(
            context.cacheDir,
            "threadsvault_backup_${System.currentTimeMillis()}.csv"
        )
        val header = listOf(
            "url",
            "autor",
            "contenido",
            "imagen",
            "fecha_guardado_iso",
            "fecha_guardado_ms",
            "fecha_post_ms",
            "categorias",
            "etiquetas",
            "notas",
            "favorito",
            "fuente_pwa"
        ).joinToString(",")

        val body = posts.joinToString("\n") { post ->
            listOf(
                post.url,
                post.autor,
                normalizeMultiline(post.contenido),
                post.imagenPath.orEmpty(),
                formatDate(post.fechaGuardado),
                post.fechaGuardado.toString(),
                post.fechaPost?.toString().orEmpty(),
                post.categorias,
                post.etiquetas,
                normalizeMultiline(post.notas),
                post.esFavorito.toString(),
                post.fuentePWA.toString()
            ).joinToString(",") { csvEscape(it) }
        }

        file.writeText("$header\n$body")
        return file
    }

    fun parseBackupCsv(inputStream: InputStream): List<PostEntity> {
        val lines = inputStream.bufferedReader().use { it.readLines() }
        if (lines.isEmpty()) return emptyList()

        val header = splitCsvLine(lines.first()).map { it.trim().lowercase(Locale.ROOT) }
        val index = header.withIndex().associate { it.value to it.index }

        fun value(row: List<String>, key: String): String {
            val i = index[key] ?: return ""
            return row.getOrElse(i) { "" }
        }

        return lines.drop(1)
            .filter { it.isNotBlank() }
            .map { splitCsvLine(it) }
            .map { row ->
                PostEntity(
                    id = 0,
                    url = value(row, "url"),
                    autor = value(row, "autor"),
                    contenido = denormalizeMultiline(value(row, "contenido")),
                    imagenPath = value(row, "imagen").ifBlank { null },
                    fechaGuardado = value(row, "fecha_guardado_ms").toLongOrNull()
                        ?: System.currentTimeMillis(),
                    fechaPost = value(row, "fecha_post_ms").toLongOrNull(),
                    categorias = value(row, "categorias"),
                    etiquetas = value(row, "etiquetas"),
                    notas = denormalizeMultiline(value(row, "notas")),
                    esFavorito = value(row, "favorito").toBooleanStrictOrNull() ?: false,
                    fuentePWA = value(row, "fuente_pwa").toBooleanStrictOrNull() ?: false
                )
            }
            .filter { it.url.isNotBlank() }
    }

    private fun ensureFallbackCategory(categories: List<CategoryEntity>): List<CategoryEntity> {
        val hasFallback = categories.any { it.nombre.equals("Sin categoria", ignoreCase = true) } ||
            categories.any { it.nombre.equals("Sin categoría", ignoreCase = true) }
        return if (hasFallback) {
            categories
        } else {
            categories + CategoryEntity(
                nombre = "Sin categoría",
                color = "#757575"
            )
        }
    }

    private fun csvEscape(value: String): String {
        val escaped = value.replace("\"", "\"\"")
        return "\"$escaped\""
    }

    private fun splitCsvLine(line: String): List<String> {
        val out = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                c == '"' && i + 1 < line.length && line[i + 1] == '"' && inQuotes -> {
                    current.append('"')
                    i++
                }
                c == '"' -> inQuotes = !inQuotes
                c == ',' && !inQuotes -> {
                    out.add(current.toString())
                    current.clear()
                }
                else -> current.append(c)
            }
            i++
        }
        out.add(current.toString())
        return out
    }

    private fun normalizeMultiline(value: String): String =
        value.replace("\r\n", "\\n").replace("\n", "\\n")

    private fun denormalizeMultiline(value: String): String =
        value.replace("\\n", "\n")

    private fun formatDate(timestamp: Long): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(timestamp))
}
