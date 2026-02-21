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

data class CsvBackupPayload(
    val posts: List<PostEntity>,
    val categories: List<CategoryEntity>
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
        posts: List<PostEntity>,
        categories: List<CategoryEntity>
    ): File {
        val file = File(
            context.cacheDir,
            "threadsvault_backup_${System.currentTimeMillis()}.csv"
        )
        val categoriesByName = categories.associateBy { it.nombre.trim().lowercase(Locale.ROOT) }
        val header = listOf(
            "url",
            "autor",
            "contenido",
            "imagen",
            "media_urls",
            "fecha_guardado_iso",
            "fecha_guardado_ms",
            "fecha_post_ms",
            "categorias",
            "categorias_colores",
            "categorias_emojis",
            "etiquetas",
            "notas",
            "favorito",
            "fuente_pwa"
        ).joinToString(",")

        val body = posts.joinToString("\n") { post ->
            val categoryNames = post.categorias
                .split(",")
                .map { it.trim() }
                .filter { it.isNotBlank() }
            val categoryColors = categoryNames
                .map { name ->
                    categoriesByName[name.lowercase(Locale.ROOT)]?.color ?: "#6200EE"
                }
                .joinToString("|")
            val categoryEmojis = categoryNames
                .map { name ->
                    categoriesByName[name.lowercase(Locale.ROOT)]?.emoji.orEmpty()
                }
                .joinToString("|")
            listOf(
                post.url,
                post.autor,
                normalizeMultiline(post.contenido),
                post.imagenPath.orEmpty(),
                post.mediaUrls.orEmpty(),
                formatDate(post.fechaGuardado),
                post.fechaGuardado.toString(),
                post.fechaPost?.toString().orEmpty(),
                post.categorias,
                categoryColors,
                categoryEmojis,
                post.etiquetas,
                normalizeMultiline(post.notas),
                post.esFavorito.toString(),
                post.fuentePWA.toString()
            ).joinToString(",") { csvEscape(it) }
        }

        file.writeText("$header\n$body")
        return file
    }

    fun parseBackupCsv(inputStream: InputStream): CsvBackupPayload {
        val content = inputStream.bufferedReader().use { it.readText() }
        val rows = parseCsvRows(content)
        if (rows.isEmpty()) return CsvBackupPayload(emptyList(), emptyList())

        val header = rows.first().map { normalizeHeader(it) }
        val index = header.withIndex().associate { it.value to it.index }

        fun value(row: List<String>, vararg keys: String): String {
            val i = keys.asSequence().mapNotNull { key -> index[normalizeHeader(key)] }.firstOrNull() ?: return ""
            return row.getOrElse(i) { "" }
        }

        val categoryMap = linkedMapOf<String, CategoryEntity>()
        val posts = rows.drop(1)
            .filter { row -> row.any { it.isNotBlank() } }
            .map { row ->
                val categoriesCsv = value(row, "categorias")
                val colorTokens = value(row, "categorias_colores").split("|").map { it.trim() }
                val emojiTokens = value(row, "categorias_emojis").split("|").map { it.trim() }
                val names = categoriesCsv.split(",").map { it.trim() }.filter { it.isNotBlank() }
                names.forEachIndexed { idx, name ->
                    val key = name.lowercase(Locale.ROOT)
                    if (!categoryMap.containsKey(key)) {
                        categoryMap[key] = CategoryEntity(
                            nombre = name,
                            color = colorTokens.getOrNull(idx)?.takeIf { it.matches(Regex("^#[0-9A-Fa-f]{6}$")) }
                                ?: "#6200EE",
                            emoji = emojiTokens.getOrNull(idx).orEmpty()
                        )
                    }
                }
                PostEntity(
                    id = 0,
                    url = value(row, "url"),
                    autor = value(row, "autor"),
                    contenido = denormalizeMultiline(value(row, "contenido")),
                    imagenPath = value(row, "imagen", "imagenpath").ifBlank { null },
                    mediaUrls = value(row, "media_urls", "mediaurls").ifBlank { null },
                    fechaGuardado = value(row, "fecha_guardado_ms", "fechaguardado").toLongOrNull()
                        ?: System.currentTimeMillis(),
                    fechaPost = value(row, "fecha_post_ms", "fechapost").toLongOrNull(),
                    categorias = categoriesCsv,
                    etiquetas = value(row, "etiquetas"),
                    notas = denormalizeMultiline(value(row, "notas")),
                    esFavorito = value(row, "favorito", "esfavorito").toBooleanStrictOrNull() ?: false,
                    fuentePWA = value(row, "fuente_pwa", "fuentepwa").toBooleanStrictOrNull() ?: false
                )
            }
            .filter { it.url.isNotBlank() }
        return CsvBackupPayload(
            posts = posts,
            categories = ensureFallbackCategory(categoryMap.values.toList())
        )
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

    private fun parseCsvRows(content: String): List<List<String>> {
        if (content.isBlank()) return emptyList()
        val rows = mutableListOf<MutableList<String>>()
        var row = mutableListOf<String>()
        val cell = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < content.length) {
            val c = content[i]
            when {
                c == '"' && i + 1 < content.length && content[i + 1] == '"' && inQuotes -> {
                    cell.append('"')
                    i++
                }
                c == '"' -> inQuotes = !inQuotes
                c == ',' && !inQuotes -> {
                    row.add(cell.toString())
                    cell.clear()
                }
                (c == '\n' || c == '\r') && !inQuotes -> {
                    row.add(cell.toString())
                    cell.clear()
                    if (row.any { it.isNotBlank() }) {
                        rows.add(row)
                    }
                    row = mutableListOf()
                    if (c == '\r' && i + 1 < content.length && content[i + 1] == '\n') {
                        i++
                    }
                }
                else -> cell.append(c)
            }
            i++
        }
        if (cell.isNotEmpty() || row.isNotEmpty()) {
            row.add(cell.toString())
            if (row.any { it.isNotBlank() }) {
                rows.add(row)
            }
        }
        return rows
    }

    private fun normalizeHeader(value: String): String =
        value.trim().lowercase(Locale.ROOT).replace("_", "")

    private fun normalizeMultiline(value: String): String =
        value.replace("\r\n", "\\n").replace("\n", "\\n")

    private fun denormalizeMultiline(value: String): String =
        value.replace("\\n", "\n")

    private fun formatDate(timestamp: Long): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(timestamp))
}
