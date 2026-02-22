package com.d4vram.threadsvault.ui.detail

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d4vram.threadsvault.data.database.ThreadsVaultDatabase
import com.d4vram.threadsvault.data.database.entity.PostEntity
import com.d4vram.threadsvault.data.repository.PostRepository
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
                }
            }.onFailure { throwable ->
                _uiState.value = PostDetailUiState.Error(throwable.message ?: "Error")
            }
        }
    }
}
