package com.d4vram.threadsvault.utils

import android.content.Context
import com.d4vram.threadsvault.data.database.entity.CategoryEntity
import com.d4vram.threadsvault.data.database.entity.PostEntity
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.io.InputStream

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
}
