package com.d4vram.threadsvault.ui.detail

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d4vram.threadsvault.data.database.ThreadsVaultDatabase
import com.d4vram.threadsvault.data.database.entity.PostEntity
import com.d4vram.threadsvault.data.repository.PostRepository
import com.d4vram.threadsvault.utils.MediaUrlsCodec
import com.d4vram.threadsvault.utils.ThreadsContentResolver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface PostDetailUiState {
    data object Loading : PostDetailUiState
    data class Success(
        val post: PostEntity,
        val threadPosts: List<PostEntity> = listOf(post),
        val currentThreadIndex: Int = 0
    ) : PostDetailUiState
    data object Empty : PostDetailUiState
    data class Error(val message: String) : PostDetailUiState
}

class PostDetailViewModel(context: Context, postId: Long) : ViewModel() {

    private val repository = PostRepository(
        ThreadsVaultDatabase.getDatabase(context).postDao()
    )

    private val _uiState = MutableStateFlow<PostDetailUiState>(PostDetailUiState.Loading)
    val uiState: StateFlow<PostDetailUiState> = _uiState.asStateFlow()
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
    private val attemptedAutoRefreshPostIds = mutableSetOf<Long>()

    init {
        cargarPost(postId)
    }

    fun irAlSiguienteEnHilo() {
        val current = _uiState.value as? PostDetailUiState.Success ?: return
        val next = current.threadPosts.getOrNull(current.currentThreadIndex + 1) ?: return
        cargarPost(next.id)
    }

    fun irAlAnteriorEnHilo() {
        val current = _uiState.value as? PostDetailUiState.Success ?: return
        val previous = current.threadPosts.getOrNull(current.currentThreadIndex - 1) ?: return
        cargarPost(previous.id)
    }

    private fun cargarPost(postId: Long) {
        viewModelScope.launch {
            runCatching {
                repository.obtenerPorId(postId)
            }.onSuccess { post ->
                _uiState.value = if (post == null) {
                    PostDetailUiState.Empty
                } else {
                    val threadPosts = post.threadGroupId
                        ?.takeIf { it.isNotBlank() }
                        ?.let { repository.obtenerPorThreadGroupId(it) }
                        ?.ifEmpty { null }
                        ?: listOf(post)
                    val currentIndex = threadPosts.indexOfFirst { it.id == post.id }.coerceAtLeast(0)
                    PostDetailUiState.Success(
                        post = threadPosts[currentIndex],
                        threadPosts = threadPosts,
                        currentThreadIndex = currentIndex
                    )
                        .also { success ->
                            maybeAutoRefreshPost(success.post)
                        }
                }
            }.onFailure { throwable ->
                _uiState.value = PostDetailUiState.Error(throwable.message ?: "Error")
            }
        }
    }

    fun reextraerContenido() {
        val post = (_uiState.value as? PostDetailUiState.Success)?.post ?: return
        viewModelScope.launch {
            _isRefreshing.value = true
            runCatching {
                val preview = ThreadsContentResolver.resolve(post.url, includeThreadPostUrls = false)
                val updated = post.copy(
                    contenido = preview.content.takeIf { it.isNotBlank() } ?: post.contenido,
                    imagenPath = preview.mediaUrl ?: post.imagenPath,
                    mediaUrls = MediaUrlsCodec.encode(preview.mediaUrls).takeUnless { it.isNullOrBlank() }
                        ?: post.mediaUrls,
                    authorAvatarUrl = preview.authorAvatarUrl ?: post.authorAvatarUrl
                )
                if (updated != post) repository.actualizar(updated)
                cargarPost(post.id)
            }
            _isRefreshing.value = false
        }
    }

    private fun maybeAutoRefreshPost(post: PostEntity) {
        if (!needsAutoRefresh(post)) return
        if (!attemptedAutoRefreshPostIds.add(post.id)) return

        viewModelScope.launch {
            runCatching {
                val preview = ThreadsContentResolver.resolve(post.url, includeThreadPostUrls = false)
                val mergedMediaUrls = MediaUrlsCodec.encode(preview.mediaUrls).takeUnless { it.isNullOrBlank() }
                val updated = post.copy(
                    contenido = preview.content.takeIf { it.isNotBlank() && !it.equals(post.url, ignoreCase = true) }
                        ?: post.contenido,
                    imagenPath = preview.mediaUrl ?: post.imagenPath,
                    mediaUrls = mergedMediaUrls ?: post.mediaUrls,
                    authorAvatarUrl = preview.authorAvatarUrl ?: post.authorAvatarUrl
                )

                if (updated != post) {
                    repository.actualizar(updated)
                    cargarPost(post.id)
                }
            }
        }
    }

    private fun needsAutoRefresh(post: PostEntity): Boolean {
        val contentTrimmed = post.contenido.trim()
        val hasOnlyRawUrl = contentTrimmed.isNotBlank() && contentTrimmed.equals(post.url.trim(), ignoreCase = true)
        val missingContent = contentTrimmed.isBlank() || hasOnlyRawUrl
        val missingAvatar = post.authorAvatarUrl.isNullOrBlank()
        val mergedMedia = MediaUrlsCodec.mergeWithPrimary(post.mediaUrls, post.imagenPath)
        val missingMedia = mergedMedia.isEmpty()
        val legacyMediaOnly = !post.imagenPath.isNullOrBlank() && post.mediaUrls.isNullOrBlank()

        return missingContent || missingAvatar || missingMedia || legacyMediaOnly
    }
}
