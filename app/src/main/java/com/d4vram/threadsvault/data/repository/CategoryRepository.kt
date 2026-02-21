package com.d4vram.threadsvault.data.repository

import com.d4vram.threadsvault.data.database.dao.CategoryDao
import com.d4vram.threadsvault.data.database.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

class CategoryRepository(
    private val categoryDao: CategoryDao
) {
    fun observeAll(): Flow<List<CategoryEntity>> = categoryDao.obtenerTodas()

    suspend fun insert(category: CategoryEntity): Long = categoryDao.insertar(category)

    suspend fun reorderFromIds(orderedIds: List<Long>) {
        // Current schema stores ordering in preferences, not Room.
        // Keep method for compatibility with current ViewModel usage.
    }

    suspend fun normalizeSortOrder() {
        // No-op until Room-based sortOrder migration is applied.
    }
}
