package com.d4vram.threadsvault.ui.vault

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d4vram.threadsvault.data.database.ThreadsVaultDatabase
import com.d4vram.threadsvault.data.database.entity.CategoryEntity
import com.d4vram.threadsvault.data.database.entity.PostEntity
import com.d4vram.threadsvault.data.preferences.AppPreferences
import com.d4vram.threadsvault.data.repository.PostRepository
import com.d4vram.threadsvault.R
import com.d4vram.threadsvault.utils.applyCategoryOrder
import com.d4vram.threadsvault.utils.CategoryInputParser
import com.d4vram.threadsvault.utils.MediaUrlsCodec
import com.d4vram.threadsvault.utils.ThreadsContentResolver
import com.d4vram.threadsvault.utils.ThreadsThreadParser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface VaultUiState {
    data object Loading : VaultUiState
    data class Success(val postGroups: List<List<PostEntity>>) : VaultUiState
    data object Empty : VaultUiState
    data class Error(val message: String) : VaultUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
class VaultViewModel(context: Context) : ViewModel() {

    private val db = ThreadsVaultDatabase.getDatabase(context)
    private val appContext = context.applicationContext
    private val preferences = AppPreferences(context.applicationContext)
    private val repository = PostRepository(db.postDao())

    private val searchText = MutableStateFlow("")
    val searchQuery: StateFlow<String> = searchText
    private val selectedCategory = MutableStateFlow<String?>(null)
    val currentCategory: StateFlow<String?> = selectedCategory
    private val _showFavoritesOnly = MutableStateFlow(false)
    val showFavoritesOnly: StateFlow<Boolean> = _showFavoritesOnly
    private val _messageEvents = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val messageEvents: SharedFlow<String> = _messageEvents

    val categories: StateFlow<List<CategoryEntity>> = combine(
        db.categoryDao().obtenerTodas(),
        preferences.categoryOrderFlow
    ) { categories, orderedIds ->
        applyCategoryOrder(categories, orderedIds)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

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
            val grouped = filtered.groupBy { it.threadGroupId ?: it.id.toString() }
                .values
                .map { group -> group.sortedBy { it.threadPosition } }
                .sortedByDescending { group -> group.first().fechaGuardado }
            grouped
        }
    }.map { postGroups ->
        if (postGroups.isEmpty()) VaultUiState.Empty else VaultUiState.Success(postGroups)
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
            val id = db.categoryDao().insertar(
                CategoryEntity(
                    nombre = parsed.nombre,
                    emoji = parsed.emoji
                )
            )
            if (id > 0L) {
                val currentOrder = preferences.categoryOrderFlow.first()
                preferences.setCategoryOrder(currentOrder + id)
            }
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
                        contenido = preview.content.ifBlank { post.contenido },
                        imagenPath = preview.mediaUrl ?: post.imagenPath,
                        mediaUrls = MediaUrlsCodec.encode(preview.mediaUrls).takeUnless { it.isNullOrBlank() }
                            ?: post.mediaUrls,
                        authorAvatarUrl = preview.authorAvatarUrl ?: post.authorAvatarUrl
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
                val normalizedUrl = ThreadsThreadParser.canonicalizePostUrl(url) ?: url.trim()
                val rootPreview = ThreadsContentResolver.resolve(normalizedUrl)
                val threadUrls = rootPreview.threadPostUrls
                    .mapNotNull(ThreadsThreadParser::canonicalizePostUrl)
                    .ifEmpty { listOf(normalizedUrl) }
                    .take(12)
                val threadGroupId = ThreadsThreadParser.buildThreadGroupId(threadUrls).takeIf { threadUrls.size > 1 }

                var insertedCount = 0
                threadUrls.forEachIndexed { index, threadUrl ->
                    if (repository.obtenerPorUrl(threadUrl) != null) return@forEachIndexed

                    val preview = if (threadUrl == normalizedUrl) {
                        rootPreview
                    } else {
                        ThreadsContentResolver.resolve(threadUrl, includeThreadPostUrls = false)
                    }

                    repository.insertar(
                        PostEntity(
                            url = threadUrl,
                            autor = repository.parsearUrl(threadUrl),
                            contenido = preview.content,
                            imagenPath = preview.mediaUrl,
                            mediaUrls = MediaUrlsCodec.encode(preview.mediaUrls),
                            authorAvatarUrl = preview.authorAvatarUrl,
                            threadGroupId = threadGroupId,
                            threadPosition = index
                        )
                    )
                    insertedCount++
                }

                if (insertedCount == 0) {
                    _messageEvents.tryEmit(appContext.getString(R.string.post_already_saved_message))
                } else {
                    _messageEvents.tryEmit(appContext.getString(R.string.manual_add_saved_message))
                }
            }.onFailure {
                _messageEvents.tryEmit(it.message ?: appContext.getString(R.string.save_error_generic))
            }
        }
    }
}
