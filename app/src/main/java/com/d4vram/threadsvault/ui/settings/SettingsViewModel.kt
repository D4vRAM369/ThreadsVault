package com.d4vram.threadsvault.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.d4vram.threadsvault.data.database.ThreadsVaultDatabase
import com.d4vram.threadsvault.data.database.entity.CategoryEntity
import com.d4vram.threadsvault.data.preferences.AppPreferences
import com.d4vram.threadsvault.data.preferences.ThemeMode
import com.d4vram.threadsvault.data.repository.CategoryRepository
import com.d4vram.threadsvault.data.repository.PostRepository
import com.d4vram.threadsvault.utils.AutoBackupScheduler
import com.d4vram.threadsvault.utils.BackupUtils
import com.d4vram.threadsvault.utils.CategoryInputParser
import com.d4vram.threadsvault.utils.ExportUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File

data class SaveDocumentRequest(
    val sourcePath: String,
    val displayName: String,
    val mimeType: String,
    val successMessage: String
)

class SettingsViewModel(context: Context) : ViewModel() {

    private val appContext = context.applicationContext
    private val preferences = AppPreferences(appContext)
    private val db = ThreadsVaultDatabase.getDatabase(appContext)
    private val postRepository = PostRepository(db.postDao())
    private val categoryRepository = CategoryRepository(db.categoryDao())
    private val reorderMutex = Mutex()

    val themeMode: StateFlow<ThemeMode> = preferences.themeModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ThemeMode.SYSTEM)
    val autoBackupFolderUri: StateFlow<String?> = preferences.autoBackupFolderUriFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val autoBackupIntervalHours: StateFlow<Int> = preferences.autoBackupIntervalHoursFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 24)

    val categories: StateFlow<List<CategoryEntity>> = categoryRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _saveDocumentEvents = MutableSharedFlow<SaveDocumentRequest>()
    val saveDocumentEvents = _saveDocumentEvents.asSharedFlow()
    private val _messageEvents = MutableSharedFlow<String>()
    val messageEvents = _messageEvents.asSharedFlow()

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            preferences.setThemeMode(mode)
        }
    }

    fun setAutoBackupFolderUri(uri: String?) {
        viewModelScope.launch {
            preferences.setAutoBackupFolderUri(uri)
            configureAutoBackup()
            if (uri.isNullOrBlank()) {
                _messageEvents.emit("Autobackup desactivado: sin carpeta SAF.")
            } else {
                _messageEvents.emit("Carpeta SAF de autobackup configurada.")
            }
        }
    }

    fun setAutoBackupIntervalHours(hours: Int) {
        viewModelScope.launch {
            preferences.setAutoBackupIntervalHours(hours)
            configureAutoBackup()
            _messageEvents.emit("Frecuencia de autobackup actualizada a ${if (hours <= 12) 12 else 24}h.")
        }
    }

    fun addCategory(nombre: String, emoji: String, color: String) {
        val parsed = CategoryInputParser.parse(nombre, emoji, color) ?: return
        viewModelScope.launch {
            val id = categoryRepository.insert(
                CategoryEntity(
                    nombre = parsed.nombre,
                    emoji = parsed.emoji,
                    color = parsed.color
                )
            )
            if (id > 0L) categoryRepository.normalizeSortOrder()
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
            categoryRepository.normalizeSortOrder()
        }
    }

    fun editCategory(category: CategoryEntity, nombre: String, emoji: String, color: String) {
        val parsed = CategoryInputParser.parse(nombre, emoji, color) ?: return
        viewModelScope.launch {
            val oldName = category.nombre
            val newName = parsed.nombre
            db.withTransaction {
                db.categoryDao().actualizar(
                    category.copy(
                        nombre = newName,
                        emoji = parsed.emoji,
                        color = parsed.color
                    )
                )
                if (!oldName.equals(newName, ignoreCase = true)) {
                    val posts = postRepository.obtenerTodosDirecto()
                    posts.forEach { post ->
                        val updatedCsv = replaceCategoryName(post.categorias, oldName, newName)
                        if (updatedCsv != post.categorias) {
                            db.postDao().actualizar(post.copy(categorias = updatedCsv))
                        }
                    }
                }
            }
        }
    }

    fun exportCsv() {
        viewModelScope.launch {
            _messageEvents.emit("Generando export CSV...")
            val file = withContext(Dispatchers.IO) {
                val posts = postRepository.obtenerTodosDirecto()
                val categories = db.categoryDao().obtenerTodasDirecto()
                ExportUtils.exportPostsCsv(appContext, posts, categories)
            }
            _messageEvents.emit("Selecciona carpeta y nombre para guardar el CSV.")
            emitSaveRequest(
                file = file,
                mimeType = "text/csv",
                successMessage = "CSV guardado correctamente."
            )
        }
    }

    fun exportPdf() {
        viewModelScope.launch {
            _messageEvents.emit("Generando export PDF...")
            val file = withContext(Dispatchers.IO) {
                val posts = postRepository.obtenerTodosDirecto()
                ExportUtils.exportPostsPdf(appContext, posts)
            }
            _messageEvents.emit("Selecciona carpeta y nombre para guardar el PDF.")
            emitSaveRequest(
                file = file,
                mimeType = "application/pdf",
                successMessage = "PDF guardado correctamente."
            )
        }
    }

    fun backupJson() {
        viewModelScope.launch {
            _messageEvents.emit("Generando backup JSON...")
            val file = withContext(Dispatchers.IO) {
                val posts = postRepository.obtenerTodosDirecto()
                val categories = db.categoryDao().obtenerTodasDirecto()
                BackupUtils.exportBackupJson(
                    context = appContext,
                    posts = posts,
                    categories = categories
                )
            }
            _messageEvents.emit("Selecciona carpeta y nombre para guardar el backup JSON.")
            emitSaveRequest(
                file = file,
                mimeType = "application/json",
                successMessage = "Backup JSON guardado correctamente."
            )
        }
    }

    fun backupCsv() {
        viewModelScope.launch {
            _messageEvents.emit("Generando backup CSV...")
            val file = withContext(Dispatchers.IO) {
                val posts = postRepository.obtenerTodosDirecto()
                val categories = db.categoryDao().obtenerTodasDirecto()
                BackupUtils.exportBackupCsv(appContext, posts, categories)
            }
            _messageEvents.emit("Selecciona carpeta y nombre para guardar el backup CSV.")
            emitSaveRequest(
                file = file,
                mimeType = "text/csv",
                successMessage = "Backup CSV guardado correctamente."
            )
        }
    }

    fun restoreJson(uri: Uri) {
        viewModelScope.launch {
            _messageEvents.emit("Restaurando backup JSON...")
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
                        categoryRepository.normalizeSortOrder()
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

    fun restoreCsv(uri: Uri) {
        viewModelScope.launch {
            _messageEvents.emit("Restaurando backup CSV...")
            runCatching {
                withContext(Dispatchers.IO) {
                    val payload = appContext.contentResolver.openInputStream(uri)?.use { input ->
                        BackupUtils.parseBackupCsv(input)
                    } ?: error("No se pudo abrir el archivo CSV seleccionado.")
                    val posts = payload.posts
                    val categories = payload.categories

                    db.withTransaction {
                        db.postDao().borrarTodos()
                        db.categoryDao().borrarTodas()
                        db.categoryDao().insertarTodas(categories)
                        db.postDao().insertarTodos(posts)
                        categoryRepository.normalizeSortOrder()
                    }

                    posts.size to categories.size
                }
            }.onSuccess { (postsCount, categoriesCount) ->
                _messageEvents.emit(
                    "Restore CSV completado: $postsCount posts, $categoriesCount categorias."
                )
            }.onFailure { error ->
                _messageEvents.emit(
                    "Restore CSV fallido: ${error.message ?: "archivo invalido"}"
                )
            }
        }
    }

    fun saveDocumentToUri(request: SaveDocumentRequest, targetUri: Uri?) {
        viewModelScope.launch {
            if (targetUri == null) {
                _messageEvents.emit("Guardado cancelado.")
                return@launch
            }
            runCatching {
                _messageEvents.emit("Guardando archivo...")
                withContext(Dispatchers.IO) {
                    val sourceFile = File(request.sourcePath)
                    appContext.contentResolver.openOutputStream(targetUri)?.use { output ->
                        sourceFile.inputStream().use { input ->
                            input.copyTo(output)
                        }
                    } ?: error("No se pudo abrir destino de guardado.")
                    sourceFile.delete()
                }
            }.onSuccess {
                _messageEvents.emit(request.successMessage)
            }.onFailure { error ->
                _messageEvents.emit("Error al guardar: ${error.message ?: "desconocido"}")
            }
        }
    }

    fun updateCategoryOrder(orderedIds: List<Long>) {
        viewModelScope.launch {
            reorderMutex.withLock {
                db.withTransaction {
                    categoryRepository.reorderFromIds(orderedIds)
                }
            }
        }
    }

    fun reorderCategories(orderedIds: List<Long>) {
        updateCategoryOrder(orderedIds)
    }


    private suspend fun configureAutoBackup() {
        val folderUri = preferences.autoBackupFolderUriFlow.first()
        val hours = preferences.autoBackupIntervalHoursFlow.first()

        if (folderUri.isNullOrBlank()) {
            AutoBackupScheduler.cancel(appContext)
        } else {
            AutoBackupScheduler.schedule(appContext, hours)
        }
    }

    private suspend fun emitSaveRequest(
        file: File,
        mimeType: String,
        successMessage: String
    ) {
        _saveDocumentEvents.emit(
            SaveDocumentRequest(
                sourcePath = file.absolutePath,
                displayName = file.name,
                mimeType = mimeType,
                successMessage = successMessage
            )
        )
    }

    private fun replaceCategoryName(csv: String, oldName: String, newName: String): String {
        if (csv.isBlank()) return csv
        return csv.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .map { current ->
                if (current.equals(oldName, ignoreCase = true)) newName else current
            }
            .distinct()
            .joinToString(",")
    }
}
