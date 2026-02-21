package com.d4vram.threadsvault.ui.vault

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d4vram.threadsvault.data.database.ThreadsVaultDatabase
import com.d4vram.threadsvault.data.database.entity.CategoryEntity
import com.d4vram.threadsvault.data.database.entity.PostEntity
import com.d4vram.threadsvault.data.repository.PostRepository
import com.d4vram.threadsvault.utils.CategoryInputParser
import com.d4vram.threadsvault.utils.ThreadsContentResolver
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface VaultUiState {
    data object Loading : VaultUiState
    data class Success(val posts: List<PostEntity>) : VaultUiState
    data object Empty : VaultUiState
    data class Error(val message: String) : VaultUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
class VaultViewModel(context: Context) : ViewModel() {

    private val db = ThreadsVaultDatabase.getDatabase(context)
    private val repository = PostRepository(db.postDao())

    private val searchText = MutableStateFlow("")
    val searchQuery: StateFlow<String> = searchText
    private val selectedCategory = MutableStateFlow<String?>(null)
    val currentCategory: StateFlow<String?> = selectedCategory
    private val _showFavoritesOnly = MutableStateFlow(false)
    val showFavoritesOnly: StateFlow<Boolean> = _showFavoritesOnly

    val categories: StateFlow<List<CategoryEntity>> = db.categoryDao()
        .obtenerTodas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val postCountsByCategory: StateFlow<Map<String, Int>> = repository.obtenerTodos()
        .map { posts ->
            val counts = mutableMapOf<String, Int>()
            posts.forEach { post ->
                if (post.categorias.isBlank()) return@forEach
                post.categorias.split(",")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .forEach { cat -> counts[cat] = (counts[cat] ?: 0) + 1 }
            }
            counts
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val uiState: StateFlow<VaultUiState> = combine(
        searchText, selectedCategory, _showFavoritesOnly
    ) { query, category, favOnly ->
        Triple(query, category, favOnly)
    }.flatMapLatest { (query, category, favOnly) ->
        val baseFlow = if (query.isBlank()) repository.obtenerTodos() else repository.buscar(query)
        baseFlow.map { posts ->
            var filtered = posts
            if (favOnly) {
                filtered = filtered.filter { it.esFavorito }
            }
            if (!category.isNullOrBlank()) {
                filtered = filtered.filter { post ->
                    post.categorias.split(",")
                        .map { it.trim() }
                        .contains(category)
                }
            }
            filtered
        }
    }.map { posts ->
        if (posts.isEmpty()) VaultUiState.Empty else VaultUiState.Success(posts)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = VaultUiState.Loading
    )

    fun onSearchTextChange(value: String) {
        searchText.value = value
    }

    fun onCategorySelected(category: String?) {
        selectedCategory.value = category
    }

    fun toggleFavoritesFilter() {
        _showFavoritesOnly.value = !_showFavoritesOnly.value
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
        val name = category.nombre
        if (name.equals("Sin categoria", ignoreCase = true) ||
            name.equals("Sin categoría", ignoreCase = true)
        ) {
            return
        }
        viewModelScope.launch {
            db.categoryDao().borrar(category)
            if (selectedCategory.value == category.nombre) {
                selectedCategory.value = null
            }
        }
    }

    fun toggleFavorito(post: PostEntity) {
        viewModelScope.launch {
            runCatching {
                repository.actualizar(post.copy(esFavorito = !post.esFavorito))
            }
        }
    }

    fun borrarPost(post: PostEntity) {
        viewModelScope.launch {
            runCatching { repository.borrar(post) }
        }
    }

    fun restaurarPost(post: PostEntity) {
        viewModelScope.launch {
            runCatching { repository.insertar(post) }
        }
    }

    fun actualizarNotas(post: PostEntity, notas: String) {
        viewModelScope.launch {
            runCatching {
                repository.actualizar(post.copy(notas = notas.trim()))
            }
        }
    }

    fun reextraerContenido(post: PostEntity) {
        viewModelScope.launch {
            runCatching {
                val preview = ThreadsContentResolver.resolve(post.url)
                repository.actualizar(
                    post.copy(
                        contenido = preview.content,
                        imagenPath = preview.mediaUrl ?: post.imagenPath
                    )
                )
            }
        }
    }

    fun actualizarCategorias(post: PostEntity, categories: List<String>) {
        val csv = categories
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .joinToString(",")
        viewModelScope.launch {
            runCatching {
                repository.actualizar(post.copy(categorias = csv))
            }
        }
    }

    fun guardarManualUrl(url: String) {
        if (url.isBlank()) return
        viewModelScope.launch {
            runCatching {
                repository.insertar(
                    PostEntity(
                        url = url.trim(),
                        autor = repository.parsearUrl(url),
                        contenido = url.trim()
                    )
                )
            }
        }
    }
}
