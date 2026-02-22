package com.d4vram.threadsvault.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
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
    version = 4,
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
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .addCallback(SeedCategoriesCallback)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE posts ADD COLUMN mediaUrls TEXT")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Tolerate schema drift from pre-release builds on test devices/emulators.
                ensurePostsColumns(db)
                ensureCategoriesColumns(db)
                rebuildPostsTableToV4(db)
                rebuildCategoriesTableToV3(db)
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                if (!hasColumn(db, "posts", "authorAvatarUrl")) {
                    db.execSQL("ALTER TABLE posts ADD COLUMN authorAvatarUrl TEXT")
                }
                rebuildPostsTableToV4(db)
            }
        }

        private fun ensurePostsColumns(db: SupportSQLiteDatabase) {
            if (!hasColumn(db, "posts", "mediaUrls")) {
                db.execSQL("ALTER TABLE posts ADD COLUMN mediaUrls TEXT")
            }
            if (!hasColumn(db, "posts", "threadGroupId")) {
                db.execSQL("ALTER TABLE posts ADD COLUMN threadGroupId TEXT")
            }
            if (!hasColumn(db, "posts", "threadPosition")) {
                db.execSQL("ALTER TABLE posts ADD COLUMN threadPosition INTEGER NOT NULL DEFAULT 0")
            }
        }

        private fun ensureCategoriesColumns(db: SupportSQLiteDatabase) {
            if (!hasColumn(db, "categories", "color")) {
                db.execSQL("ALTER TABLE categories ADD COLUMN color TEXT NOT NULL DEFAULT '#6200EE'")
            }
            if (!hasColumn(db, "categories", "descripcion")) {
                db.execSQL("ALTER TABLE categories ADD COLUMN descripcion TEXT NOT NULL DEFAULT ''")
            }
        }

        private fun rebuildPostsTableToV4(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS posts_new (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    url TEXT NOT NULL,
                    autor TEXT NOT NULL,
                    contenido TEXT NOT NULL,
                    imagenPath TEXT,
                    mediaUrls TEXT,
                    authorAvatarUrl TEXT,
                    fechaGuardado INTEGER NOT NULL,
                    fechaPost INTEGER,
                    categorias TEXT NOT NULL,
                    etiquetas TEXT NOT NULL,
                    notas TEXT NOT NULL,
                    threadGroupId TEXT,
                    threadPosition INTEGER NOT NULL DEFAULT 0,
                    esFavorito INTEGER NOT NULL,
                    fuentePWA INTEGER NOT NULL
                )
                """.trimIndent()
            )
            val hasAuthorAvatarUrl = hasColumn(db, "posts", "authorAvatarUrl")
            val selectAuthorAvatarUrl = if (hasAuthorAvatarUrl) "authorAvatarUrl" else "NULL"
            db.execSQL(
                """
                INSERT INTO posts_new (
                    id, url, autor, contenido, imagenPath, mediaUrls, authorAvatarUrl, fechaGuardado, fechaPost,
                    categorias, etiquetas, notas, threadGroupId, threadPosition, esFavorito, fuentePWA
                )
                SELECT
                    id, url, autor, contenido, imagenPath, mediaUrls, $selectAuthorAvatarUrl, fechaGuardado, fechaPost,
                    categorias, etiquetas, notas, threadGroupId, threadPosition, esFavorito, fuentePWA
                FROM posts
                """.trimIndent()
            )
            db.execSQL("DROP TABLE posts")
            db.execSQL("ALTER TABLE posts_new RENAME TO posts")
        }

        private fun rebuildCategoriesTableToV3(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS categories_new (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    nombre TEXT NOT NULL,
                    emoji TEXT NOT NULL,
                    color TEXT NOT NULL,
                    descripcion TEXT NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT INTO categories_new (id, nombre, emoji, color, descripcion)
                SELECT id, nombre, emoji, color, descripcion
                FROM categories
                """.trimIndent()
            )
            db.execSQL("DROP TABLE categories")
            db.execSQL("ALTER TABLE categories_new RENAME TO categories")
        }

        private fun hasColumn(
            db: SupportSQLiteDatabase,
            tableName: String,
            columnName: String
        ): Boolean {
            db.query("PRAGMA table_info($tableName)").use { cursor ->
                val nameIndex = cursor.getColumnIndex("name")
                while (cursor.moveToNext()) {
                    if (nameIndex >= 0 && cursor.getString(nameIndex) == columnName) {
                        return true
                    }
                }
            }
            return false
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
