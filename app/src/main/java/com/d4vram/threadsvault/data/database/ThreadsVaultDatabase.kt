package com.d4vram.threadsvault.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.d4vram.threadsvault.data.database.dao.CategoryDao
import com.d4vram.threadsvault.data.database.dao.PostDao
import com.d4vram.threadsvault.data.database.entity.CategoryEntity
import com.d4vram.threadsvault.data.database.entity.PostEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Database(
    entities = [PostEntity::class, CategoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ThreadsVaultDatabase : RoomDatabase() {

    abstract fun postDao(): PostDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: ThreadsVaultDatabase? = null

        fun getDatabase(context: Context): ThreadsVaultDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ThreadsVaultDatabase::class.java,
                    "threadsvault_database"
                ).addCallback(SeedCategoriesCallback).build()
                INSTANCE = instance
                instance
            }
        }

        private val SeedCategoriesCallback = object : Callback() {
            private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                scope.launch {
                    INSTANCE?.categoryDao()?.let { dao ->
                        seedDefaultCategories(dao)
                    }
                }
            }
        }

        private suspend fun seedDefaultCategories(dao: CategoryDao) {
            val defaults = listOf(
                CategoryEntity(
                    nombre = "Inteligencia Artificial",
                    emoji = "\uD83E\uDD16",
                    color = "#6200EE"
                ),
                CategoryEntity(
                    nombre = "Programaci\u00F3n",
                    emoji = "\uD83D\uDCBB",
                    color = "#0288D1"
                ),
                CategoryEntity(
                    nombre = "Herramientas Dev",
                    emoji = "\uD83D\uDEE0\uFE0F",
                    color = "#388E3C"
                ),
                CategoryEntity(
                    nombre = "Prompts",
                    emoji = "\u270D\uFE0F",
                    color = "#F57C00"
                ),
                CategoryEntity(
                    nombre = "Recursos",
                    emoji = "\uD83D\uDCD6",
                    color = "#C2185B"
                ),
                CategoryEntity(
                    nombre = "Sin categor\u00EDa",
                    color = "#757575"
                )
            )

            defaults.forEach { category ->
                dao.insertar(category)
            }
        }
    }
}
