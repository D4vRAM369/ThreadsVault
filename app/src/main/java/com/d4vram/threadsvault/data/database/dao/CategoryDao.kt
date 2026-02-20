package com.d4vram.threadsvault.data.database.dao

import androidx.room.*
import com.d4vram.threadsvault.data.database.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(categoria: CategoryEntity): Long

    @Query("SELECT * FROM categories ORDER BY nombre ASC")
    fun obtenerTodas(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY nombre ASC")
    suspend fun obtenerTodasDirecto(): List<CategoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodas(categorias: List<CategoryEntity>)

    @Delete
    suspend fun borrar(categoria: CategoryEntity)

    @Update
    suspend fun actualizar(categoria: CategoryEntity)

    @Query("DELETE FROM categories")
    suspend fun borrarTodas()
}
