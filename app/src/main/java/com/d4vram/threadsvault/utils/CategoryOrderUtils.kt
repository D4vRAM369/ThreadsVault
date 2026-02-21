package com.d4vram.threadsvault.utils

import com.d4vram.threadsvault.data.database.entity.CategoryEntity

fun applyCategoryOrder(
    categories: List<CategoryEntity>,
    orderedIds: List<Long>
): List<CategoryEntity> {
    if (categories.isEmpty()) return emptyList()
    if (orderedIds.isEmpty()) {
        return categories.sortedBy { it.nombre.lowercase() }
    }

    val position = orderedIds.withIndex().associate { (index, id) -> id to index }
    return categories.sortedWith(
        compareBy<CategoryEntity> { position[it.id] ?: Int.MAX_VALUE }
            .thenBy { it.nombre.lowercase() }
    )
}
