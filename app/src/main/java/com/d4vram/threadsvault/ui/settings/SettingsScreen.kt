package com.d4vram.threadsvault.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.d4vram.threadsvault.R
import com.d4vram.threadsvault.data.database.entity.CategoryEntity
import com.d4vram.threadsvault.data.preferences.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    title: String,
    onBack: () -> Unit,
    themeMode: ThemeMode,
    autoBackupFolderUri: String?,
    autoBackupIntervalHours: Int,
    categories: List<CategoryEntity>,
    onThemeModeChange: (ThemeMode) -> Unit,
    onAddCategory: (String, String) -> Unit,
    onDeleteCategory: (CategoryEntity) -> Unit,
    onExportCsv: () -> Unit,
    onExportPdf: () -> Unit,
    onBackupJson: () -> Unit,
    onRestoreJson: () -> Unit,
    onBackupCsv: () -> Unit,
    onRestoreCsv: () -> Unit,
    onPickAutoBackupFolder: () -> Unit,
    onAutoBackupIntervalChange: (Int) -> Unit,
    onClearAutoBackupFolder: () -> Unit
) {
    var newCategory by remember { mutableStateOf("") }
    var newCategoryEmoji by remember { mutableStateOf("") }
    var showRestoreConfirm by remember { mutableStateOf(false) }
    var pendingCategoryDelete by remember { mutableStateOf<CategoryEntity?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back_action)
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                SectionCard(title = stringResource(id = R.string.theme_mode_title)) {
                    ThemeModeItem(
                        label = stringResource(id = R.string.theme_system),
                        selected = themeMode == ThemeMode.SYSTEM,
                        onClick = { onThemeModeChange(ThemeMode.SYSTEM) }
                    )
                    ThemeModeItem(
                        label = stringResource(id = R.string.theme_light),
                        selected = themeMode == ThemeMode.LIGHT,
                        onClick = { onThemeModeChange(ThemeMode.LIGHT) }
                    )
                    ThemeModeItem(
                        label = stringResource(id = R.string.theme_dark),
                        selected = themeMode == ThemeMode.DARK,
                        onClick = { onThemeModeChange(ThemeMode.DARK) }
                    )
                }
            }

            item {
                SectionCard(title = stringResource(id = R.string.categories_title)) {
                    Text(text = stringResource(id = R.string.emoji_hint))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            modifier = Modifier.weight(1f),
                            value = newCategory,
                            onValueChange = { newCategory = it },
                            label = { Text(text = stringResource(id = R.string.new_category_label)) }
                        )
                        OutlinedTextField(
                            modifier = Modifier.weight(0.5f),
                            value = newCategoryEmoji,
                            onValueChange = { newCategoryEmoji = it },
                            label = { Text(text = stringResource(id = R.string.new_category_emoji_label)) }
                        )
                    }
                    Button(
                        onClick = {
                            onAddCategory(newCategory, newCategoryEmoji)
                            newCategory = ""
                            newCategoryEmoji = ""
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Text(text = stringResource(id = R.string.add_category_action))
                    }
                }
            }

            items(categories, key = { it.id }) { category ->
                ElevatedCard(
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = listOf(category.emoji, category.nombre).filter { it.isNotBlank() }.joinToString(" "))
                        IconButton(onClick = { pendingCategoryDelete = category }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(id = R.string.delete_action)
                            )
                        }
                    }
                }
            }

            item {
                SectionCard(title = stringResource(id = R.string.export_title)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(modifier = Modifier.weight(1f), onClick = onExportCsv) {
                            Text(text = stringResource(id = R.string.export_csv_action))
                        }
                        Button(modifier = Modifier.weight(1f), onClick = onExportPdf) {
                            Text(text = stringResource(id = R.string.export_pdf_action))
                        }
                    }
                }
            }

            item {
                SectionCard(title = stringResource(id = R.string.backup_title)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(modifier = Modifier.weight(1f), onClick = onBackupJson) {
                            Text(text = stringResource(id = R.string.backup_json_action))
                        }
                        Button(modifier = Modifier.weight(1f), onClick = { showRestoreConfirm = true }) {
                            Text(text = stringResource(id = R.string.restore_json_action))
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(modifier = Modifier.weight(1f), onClick = onBackupCsv) {
                            Text(text = stringResource(id = R.string.backup_csv_action))
                        }
                        Button(modifier = Modifier.weight(1f), onClick = onRestoreCsv) {
                            Text(text = stringResource(id = R.string.restore_csv_action))
                        }
                    }
                }
            }

            item {
                SectionCard(title = stringResource(id = R.string.auto_backup_title)) {
                    Text(
                        text = if (autoBackupFolderUri.isNullOrBlank()) {
                            stringResource(id = R.string.auto_backup_folder_missing)
                        } else {
                            stringResource(id = R.string.auto_backup_folder_configured)
                        }
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(modifier = Modifier.weight(1f), onClick = onPickAutoBackupFolder) {
                            Text(text = stringResource(id = R.string.select_saf_folder_action))
                        }
                        Button(modifier = Modifier.weight(1f), onClick = onClearAutoBackupFolder) {
                            Text(text = stringResource(id = R.string.clear_saf_folder_action))
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        RadioButton(
                            selected = autoBackupIntervalHours <= 12,
                            onClick = { onAutoBackupIntervalChange(12) }
                        )
                        Text(text = stringResource(id = R.string.auto_backup_12h))
                        RadioButton(
                            selected = autoBackupIntervalHours > 12,
                            onClick = { onAutoBackupIntervalChange(24) }
                        )
                        Text(text = stringResource(id = R.string.auto_backup_24h))
                    }
                }
            }

            item {
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    if (showRestoreConfirm) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirm = false },
            title = { Text(text = stringResource(id = R.string.restore_confirm_title)) },
            text = { Text(text = stringResource(id = R.string.restore_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRestoreConfirm = false
                        onRestoreJson()
                    }
                ) {
                    Text(text = stringResource(id = R.string.restore_json_action))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreConfirm = false }) {
                    Text(text = stringResource(id = R.string.cancel_action))
                }
            }
        )
    }

    pendingCategoryDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { pendingCategoryDelete = null },
            title = { Text(text = stringResource(id = R.string.delete_category_title)) },
            text = {
                Text(
                    text = stringResource(
                        id = R.string.delete_category_message,
                        target.nombre
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteCategory(target)
                        pendingCategoryDelete = null
                    }
                ) {
                    Text(text = stringResource(id = R.string.delete_action))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingCategoryDelete = null }) {
                    Text(text = stringResource(id = R.string.cancel_action))
                }
            }
        )
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@Composable
private fun ThemeModeItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick
            )
            .padding(vertical = 6.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RadioButton(selected = selected, onClick = onClick)
            Text(text = label)
        }
    }
}
