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
    data class Success(val post: PostEntity) : PostDetailUiState
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

    private fun cargarPost(postId: Long) {
        viewModelScope.launch {
            runCatching {
                repository.obtenerPorId(postId)
            }.onSuccess { post ->
                _uiState.value = if (post == null) {
                    PostDetailUiState.Empty
                } else {
                    PostDetailUiState.Success(post)
                }
            }.onFailure { throwable ->
                _uiState.value = PostDetailUiState.Error(throwable.message ?: "Error")
            }
        }
    }
}
