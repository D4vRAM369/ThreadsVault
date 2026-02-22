package com.d4vram.threadsvault.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "posts")
@Serializable
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val url: String,                        // URL del post de Threads
    val autor: String = "",                 // @handle del autor
    val contenido: String = "",             // Texto del post
    val imagenPath: String? = null,         // Ruta local de imagen guardada
    val mediaUrls: String? = null,          // JSON con todas las medias (imagenes/videos)
    val authorAvatarUrl: String? = null,    // Foto de perfil del autor

    val fechaGuardado: Long = System.currentTimeMillis(),  // Timestamp
    val fechaPost: Long? = null,            // Fecha original del post (si se extrae)

    val categorias: String = "",            // "IA,Programación" ← guardado como CSV
    val etiquetas: String = "",             // "kotlin,llm,prompts"
    val notas: String = "",                 // Tus apuntes sobre el post
    val threadGroupId: String? = null,      // ID lógico para navegar posts del mismo hilo
    @field:ColumnInfo(defaultValue = "0")
    val threadPosition: Int = 0,            // Orden dentro del hilo (0 = primero)

    val esFavorito: Boolean = false,
    val fuentePWA: Boolean = false          // true si vino de la PWA, false si vino del APK
)
