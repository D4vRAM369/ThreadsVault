package com.d4vram.threadsvault.ui.settings

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.d4vram.threadsvault.data.database.ThreadsVaultDatabase
import com.d4vram.threadsvault.data.database.entity.CategoryEntity
import com.d4vram.threadsvault.data.preferences.AppPreferences
import com.d4vram.threadsvault.data.preferences.ThemeMode
import com.d4vram.threadsvault.data.repository.PostRepository
import com.d4vram.threadsvault.utils.BackupUtils
import com.d4vram.threadsvault.utils.CategoryInputParser
import com.d4vram.threadsvault.utils.ExportUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class ExportShareEvent(
    val uri: Uri,
    val mimeType: String,
    val title: String
)

class SettingsViewModel(context: Context) : ViewModel() {

    private val appContext = context.applicationContext
    private val preferences = AppPreferences(appContext)
    private val db = ThreadsVaultDatabase.getDatabase(appContext)
    private val postRepository = PostRepository(db.postDao())

    val themeMode: StateFlow<ThemeMode> = preferences.themeModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeMode.SYSTEM)

    val categories: StateFlow<List<CategoryEntity>> = db.categoryDao()
        .obtenerTodas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _exportEvents = MutableSharedFlow<ExportShareEvent>()
    val exportEvents = _exportEvents.asSharedFlow()
    private val _messageEvents = MutableSharedFlow<String>()
    val messageEvents = _messageEvents.asSharedFlow()

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            preferences.setThemeMode(mode)
        }
    }

    fun addCategory(nombre: String, emoji: String) {
        val parsed = CategoryInputParser.parse(nombre, emoji) ?: return
        viewModelScope.launch {
            db.categoryDao().insertar(
                CategoryEntity(
                    nombre = parsed.nombre,
                    emoji = parsed.emoji
                )
            )
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        if (category.nombre.equals("Sin categoria", ignoreCase = true) ||
            category.nombre.equals("Sin categoría", ignoreCase = true)
        ) {
            return
        }
        viewModelScope.launch {
            db.categoryDao().borrar(category)
        }
    }

    fun exportCsv() {
        viewModelScope.launch {
            val file = withContext(Dispatchers.IO) {
                val posts = postRepository.obtenerTodosDirecto()
                ExportUtils.exportPostsCsv(appContext, posts)
            }
            emitShareEvent(file, "text/csv", "Compartir CSV")
        }
    }

    fun exportPdf() {
        viewModelScope.launch {
            val file = withContext(Dispatchers.IO) {
                val posts = postRepository.obtenerTodosDirecto()
                ExportUtils.exportPostsPdf(appContext, posts)
            }
            emitShareEvent(file, "application/pdf", "Compartir PDF")
        }
    }

    fun backupJson() {
        viewModelScope.launch {
            val file = withContext(Dispatchers.IO) {
                val posts = postRepository.obtenerTodosDirecto()
                val categories = db.categoryDao().obtenerTodasDirecto()
                BackupUtils.exportBackupJson(
                    context = appContext,
                    posts = posts,
                    categories = categories
                )
            }
            emitShareEvent(file, "application/json", "Compartir backup JSON")
        }
    }

    fun restoreJson(uri: Uri) {
        viewModelScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    val payload = appContext.contentResolver.openInputStream(uri)?.use { input ->
                        BackupUtils.parseBackup(input)
                    } ?: error("No se pudo abrir el archivo seleccionado.")

                    db.withTransaction {
                        db.postDao().borrarTodos()
                        db.categoryDao().borrarTodas()
                        db.categoryDao().insertarTodas(payload.categories)
                        db.postDao().insertarTodos(payload.posts)
                    }

                    payload
                }
            }.onSuccess { payload ->
                _messageEvents.emit(
                    "Restore completado: ${payload.posts.size} posts, ${payload.categories.size} categorias."
                )
            }.onFailure { error ->
                _messageEvents.emit(
                    "Restore fallido: ${error.message ?: "archivo invalido"}"
                )
            }
        }
    }

    private suspend fun emitShareEvent(file: File, mimeType: String, title: String) {
        val uri = FileProvider.getUriForFile(
            appContext,
            "${appContext.packageName}.fileprovider",
            file
        )
        _exportEvents.emit(
            ExportShareEvent(
                uri = uri,
                mimeType = mimeType,
                title = title
            )
        )
    }
}
