package com.d4vram.threadsvault.data.repository

import com.d4vram.threadsvault.data.database.dao.PostDao
import com.d4vram.threadsvault.data.database.entity.PostEntity
import kotlinx.coroutines.flow.Flow

class PostRepository(
    private val postDao: PostDao
) {
    fun obtenerTodos(): Flow<List<PostEntity>> = postDao.obtenerTodos()

    suspend fun obtenerTodosDirecto(): List<PostEntity> = postDao.obtenerTodosDirecto()
    suspend fun obtenerPorUrl(url: String): PostEntity? = postDao.obtenerPorUrl(url)

    fun buscar(texto: String): Flow<List<PostEntity>> = postDao.buscar(texto)

    fun filtrarPorCategoria(categoria: String): Flow<List<PostEntity>> =
        postDao.filtrarPorCategoria(categoria)

    fun obtenerFavoritos(): Flow<List<PostEntity>> = postDao.obtenerFavoritos()

    suspend fun insertar(post: PostEntity): Long = postDao.insertar(post)

    suspend fun obtenerPorId(id: Long): PostEntity? = postDao.obtenerPorId(id)
    suspend fun obtenerPorThreadGroupId(threadGroupId: String): List<PostEntity> =
        postDao.obtenerPorThreadGroupId(threadGroupId)

    suspend fun actualizar(post: PostEntity) = postDao.actualizar(post)

    suspend fun actualizarThreadGroup(postId: Long, groupId: String, position: Int) =
        postDao.actualizarThreadGroup(postId, groupId, position)

    suspend fun borrar(post: PostEntity) = postDao.borrar(post)

    suspend fun contarTodos(): Int = postDao.contarTodos()

    fun parsearUrl(url: String): String {
        val match = THREADS_HANDLE_REGEX.find(url.trim()) ?: return ""
        return "@${match.groupValues[1]}"
    }

    companion object {
        private val THREADS_HANDLE_REGEX = Regex(
            pattern = """threads(?:\.net|\.com)/@([A-Za-z0-9._]+)""",
            options = setOf(RegexOption.IGNORE_CASE)
        )
    }
}
