package com.d4vram.threadsvault.data.database.dao

import androidx.room.*
import com.d4vram.threadsvault.data.database.entity.PostEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {

    // ── Insertar ───────────────────────────────────────
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(post: PostEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(posts: List<PostEntity>)

    // ── Obtener todos (ordenados por fecha, más nuevo primero) ──
    @Query("SELECT * FROM posts ORDER BY fechaGuardado DESC")
    fun obtenerTodos(): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts ORDER BY fechaGuardado DESC")
    suspend fun obtenerTodosDirecto(): List<PostEntity>

    // ── Buscar por texto ───────────────────────────────
    @Query(
        """
        SELECT * FROM posts 
        WHERE contenido LIKE '%' || :texto || '%' 
           OR autor LIKE '%' || :texto || '%'
           OR etiquetas LIKE '%' || :texto || '%'
        ORDER BY fechaGuardado DESC
    """
    )
    fun buscar(texto: String): Flow<List<PostEntity>>

    // ── Filtrar por categoría ──────────────────────────
    @Query("SELECT * FROM posts WHERE categorias LIKE '%' || :categoria || '%' ORDER BY fechaGuardado DESC")
    fun filtrarPorCategoria(categoria: String): Flow<List<PostEntity>>

    // ── Solo favoritos ─────────────────────────────────
    @Query("SELECT * FROM posts WHERE esFavorito = 1 ORDER BY fechaGuardado DESC")
    fun obtenerFavoritos(): Flow<List<PostEntity>>

    // ── Obtener uno por ID ─────────────────────────────
    @Query("SELECT * FROM posts WHERE id = :id")
    suspend fun obtenerPorId(id: Long): PostEntity?

    // ── Actualizar ─────────────────────────────────────
    @Update
    suspend fun actualizar(post: PostEntity)

    // ── Borrar ─────────────────────────────────────────
    @Delete
    suspend fun borrar(post: PostEntity)

    @Query("DELETE FROM posts")
    suspend fun borrarTodos()

    // ── Contar posts (para estadísticas) ──────────────
    @Query("SELECT COUNT(*) FROM posts")
    suspend fun contarTodos(): Int
}
