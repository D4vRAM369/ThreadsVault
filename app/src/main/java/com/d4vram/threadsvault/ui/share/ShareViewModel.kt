package com.d4vram.threadsvault.ui.share

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d4vram.threadsvault.data.database.ThreadsVaultDatabase
import com.d4vram.threadsvault.data.database.entity.CategoryEntity
import com.d4vram.threadsvault.data.database.entity.PostEntity
import com.d4vram.threadsvault.data.preferences.AppPreferences
import com.d4vram.threadsvault.data.repository.PostRepository
import com.d4vram.threadsvault.utils.CategoryInputParser
import com.d4vram.threadsvault.utils.ThreadsContentResolver
import com.d4vram.threadsvault.utils.applyCategoryOrder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface ShareSaveState {
    data object Idle : ShareSaveState
    data object Saving : ShareSaveState
    data object Saved : ShareSaveState
    data class Error(val message: String) : ShareSaveState
}

class ShareViewModel(context: Context) : ViewModel() {

    private val db = ThreadsVaultDatabase.getDatabase(context)
    private val preferences = AppPreferences(context.applicationContext)
    private val postRepository = PostRepository(db.postDao())

    val categories: StateFlow<List<CategoryEntity>> = combine(
        db.categoryDao().obtenerTodas(),
        preferences.categoryOrderFlow
    ) { categories, orderedIds ->
        applyCategoryOrder(categories, orderedIds)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _saveState = MutableStateFlow<ShareSaveState>(ShareSaveState.Idle)
    val saveState: StateFlow<ShareSaveState> = _saveState.asStateFlow()

    fun addCategory(nombre: String, emoji: String) {
        val parsed = CategoryInputParser.parse(nombre, emoji) ?: return
        viewModelScope.launch {
            val id = db.categoryDao().insertar(
                CategoryEntity(nombre = parsed.nombre, emoji = parsed.emoji)
            )
            if (id > 0L) {
                val currentOrder = preferences.categoryOrderFlow.first()
                preferences.setCategoryOrder(currentOrder + id)
            }
        }
    }

    fun guardarSharedUrl(url: String, notas: String, categoria: String?) {
        viewModelScope.launch {
            _saveState.value = ShareSaveState.Saving
            runCatching {
                val preview = ThreadsContentResolver.resolve(url)
                val author = postRepository.parsearUrl(url)
                postRepository.insertar(
                    PostEntity(
                        url = url,
                        autor = author,
                        contenido = preview.content,
                        imagenPath = preview.mediaUrl,
                        notas = notas.trim(),
                        categorias = categoria.orEmpty()
                    )
                )
            }.onSuccess {
                _saveState.value = ShareSaveState.Saved
            }.onFailure { throwable ->
                _saveState.value = ShareSaveState.Error(
                    throwable.message ?: "Unknown save error"
                )
            }
        }
    }
}
