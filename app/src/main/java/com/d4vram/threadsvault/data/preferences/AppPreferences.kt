package com.d4vram.threadsvault.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

private val Context.dataStore by preferencesDataStore(name = "threadsvault_preferences")

class AppPreferences(private val context: Context) {

    val themeModeFlow: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        val raw = preferences[Keys.THEME_MODE] ?: ThemeMode.SYSTEM.name
        ThemeMode.entries.firstOrNull { it.name == raw } ?: ThemeMode.SYSTEM
    }
    val autoBackupFolderUriFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[Keys.AUTO_BACKUP_FOLDER_URI]
    }
    val autoBackupIntervalHoursFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[Keys.AUTO_BACKUP_INTERVAL_HOURS] ?: 24
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[Keys.THEME_MODE] = mode.name
        }
    }

    suspend fun setAutoBackupFolderUri(uri: String?) {
        context.dataStore.edit { preferences ->
            if (uri.isNullOrBlank()) {
                preferences.remove(Keys.AUTO_BACKUP_FOLDER_URI)
            } else {
                preferences[Keys.AUTO_BACKUP_FOLDER_URI] = uri
            }
        }
    }

    suspend fun setAutoBackupIntervalHours(hours: Int) {
        val normalized = if (hours <= 12) 12 else 24
        context.dataStore.edit { preferences ->
            preferences[Keys.AUTO_BACKUP_INTERVAL_HOURS] = normalized
        }
    }

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val AUTO_BACKUP_FOLDER_URI = stringPreferencesKey("auto_backup_folder_uri")
        val AUTO_BACKUP_INTERVAL_HOURS = intPreferencesKey("auto_backup_interval_hours")
    }
}
