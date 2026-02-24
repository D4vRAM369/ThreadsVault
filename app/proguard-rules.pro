# ============================================================
# ThreadsVault ProGuard / R8 Rules
# ============================================================
# Estas reglas complementan proguard-android-optimize.txt.
# Solo protegen lo que realmente se usa en el proyecto.
# ============================================================


# ------------------------------------------------------------
# 1. KOTLIN SERIALIZATION
# ------------------------------------------------------------
# BackupPayload, PostEntity y CategoryEntity usan @Serializable.
# El runtime de kotlinx.serialization busca campos por nombre
# al serializar/deserializar backup JSON. Sin estas reglas,
# los campos serían renombrados a "a", "b", "c" y el parser
# no los encontraría → crash al restaurar un backup.

-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-dontwarn kotlinx.serialization.**

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Mantener toda clase anotada con @Serializable y sus miembros
-keep @kotlinx.serialization.Serializable class * {
    *;
}

# Mantener los descriptores generados por el compilador de serialización
-keepclassmembers class ** {
    @kotlinx.serialization.SerialName <fields>;
    kotlinx.serialization.KSerializer serializer(...);
}


# ------------------------------------------------------------
# 2. ROOM DATABASE
# ------------------------------------------------------------
# Room genera implementaciones en tiempo de compilación (KSP),
# pero también usa los nombres de clases para abrir la BD y
# referenciar migraciones. Sin estas reglas, la BD no abre.

# Base de la BD
-keep class * extends androidx.room.RoomDatabase { *; }

# Entities: Room lee nombres de tabla y columna por reflexión
-keep @androidx.room.Entity class * { *; }
-keepclassmembers @androidx.room.Entity class * {
    <fields>;
}

# DAOs: Room genera implementaciones que referencian estos métodos
-keep @androidx.room.Dao interface * { *; }

# ColumnInfo y otras anotaciones de Room en campos
-keepclassmembers class * {
    @androidx.room.ColumnInfo <fields>;
    @androidx.room.PrimaryKey <fields>;
    @androidx.room.Ignore <fields>;
}

# Migraciones dentro de ThreadsVaultDatabase
-keep class com.d4vram.threadsvault.data.database.ThreadsVaultDatabase {
    *;
}
-keep class com.d4vram.threadsvault.data.database.ThreadsVaultDatabase$* {
    *;
}


# ------------------------------------------------------------
# 3. WORKER (WorkManager)
# ------------------------------------------------------------
# WorkManager instancia AutoBackupWorker por reflexión usando
# el nombre completo de la clase. Si R8 lo renombra, el worker
# no se puede crear y el auto-backup silenciosamente no ocurre.

-keep class com.d4vram.threadsvault.utils.AutoBackupWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}


# ------------------------------------------------------------
# 4. JSOUP
# ------------------------------------------------------------
# Jsoup hace parsing de HTML con clases internas que R8
# puede eliminar o renombrar al considerarlas "no usadas".

-keep class org.jsoup.** { *; }
-dontwarn org.jsoup.**


# ------------------------------------------------------------
# 5. ML KIT (Text Recognition)
# ------------------------------------------------------------
# ML Kit carga su pipeline por reflexión. Si sus clases
# se renombran u ofuscan, el OCR falla silenciosamente
# o lanza una excepción al procesar la imagen.

-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.internal.mlkit_vision_text_latin.** { *; }
-dontwarn com.google.mlkit.**
-dontwarn com.google.android.gms.**


# ------------------------------------------------------------
# 6. ENUMS
# ------------------------------------------------------------
# ThemeMode usa `entries.firstOrNull { it.name == raw }` que
# lee el nombre del enum por reflexión. Sin esto, los names()
# quedarían ofuscados y el tema guardado no se restauraría.

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    public final java.lang.String name();
    public final int ordinal();
}


# ------------------------------------------------------------
# 7. STACK TRACES LEGIBLES (recomendado para debug de crashes)
# ------------------------------------------------------------
# Permite reconstruir stack traces reales a partir de los
# reportes de crash (si usas Firebase Crashlytics en el futuro
# o analizas el mapping.txt manualmente).

-keepattributes SourceFile, LineNumberTable
-renamesourcefileattribute SourceFile
