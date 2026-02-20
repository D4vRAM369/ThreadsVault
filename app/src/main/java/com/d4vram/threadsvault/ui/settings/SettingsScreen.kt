package com.d4vram.threadsvault.ui.settings

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.d4vram.threadsvault.R
import com.d4vram.threadsvault.data.database.entity.CategoryEntity
import com.d4vram.threadsvault.data.preferences.ThemeMode

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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
    onEditCategory: (CategoryEntity, String, String) -> Unit,
    onDeleteCategory: (CategoryEntity) -> Unit,
    onExportCsv: () -> Unit,
    onExportPdf: () -> Unit,
    onBackupJson: () -> Unit,
    onRestoreJson: () -> Unit,
    onBackupCsv: () -> Unit,
    onRestoreCsv: () -> Unit,
    onPickAutoBackupFolder: () -> Unit,
    onAutoBackupIntervalChange: (Int) -> Unit,
    onClearAutoBackupFolder: () -> Unit,
    onOpenAboutDev: () -> Unit
) {
    var newCategory by remember { mutableStateOf("") }
    var newCategoryEmoji by remember { mutableStateOf("") }
    var showRestoreConfirm by remember { mutableStateOf(false) }
    var pendingCategoryDelete by remember { mutableStateOf<CategoryEntity?>(null) }
    var pendingCategoryEdit by remember { mutableStateOf<CategoryEntity?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back_action),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── Apariencia ──
            item {
                SectionCard(
                    icon = Icons.Default.Palette,
                    title = stringResource(id = R.string.section_appearance)
                ) {
                    Text(
                        text = stringResource(id = R.string.theme_mode_title),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        val options = listOf(
                            ThemeMode.SYSTEM to stringResource(id = R.string.theme_system),
                            ThemeMode.LIGHT to stringResource(id = R.string.theme_light),
                            ThemeMode.DARK to stringResource(id = R.string.theme_dark)
                        )
                        options.forEachIndexed { index, (mode, label) ->
                            SegmentedButton(
                                selected = themeMode == mode,
                                onClick = { onThemeModeChange(mode) },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = options.size
                                )
                            ) {
                                Text(text = label, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }

            // ── Categorias ──
            item {
                SectionCard(
                    icon = Icons.AutoMirrored.Filled.Label,
                    title = stringResource(id = R.string.categories_title)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            modifier = Modifier.weight(1f),
                            value = newCategory,
                            onValueChange = { newCategory = it },
                            placeholder = { Text(text = stringResource(id = R.string.new_category_label)) },
                            shape = MaterialTheme.shapes.medium,
                            textStyle = MaterialTheme.typography.bodyMedium,
                            singleLine = true
                        )
                        OutlinedTextField(
                            modifier = Modifier.width(72.dp),
                            value = newCategoryEmoji,
                            onValueChange = { newCategoryEmoji = it },
                            placeholder = {
                                Text(
                                    text = stringResource(id = R.string.new_category_emoji_label),
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1
                                )
                            },
                            shape = MaterialTheme.shapes.medium,
                            textStyle = MaterialTheme.typography.bodyMedium,
                            singleLine = true
                        )
                    }
                    FilledTonalButton(
                        onClick = {
                            onAddCategory(newCategory, newCategoryEmoji)
                            newCategory = ""
                            newCategoryEmoji = ""
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = stringResource(id = R.string.add_category_action))
                    }
                }
            }

            items(categories, key = { it.id }) { category ->
                Surface(
                    modifier = Modifier.animateItemPlacement(),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = listOf(category.emoji, category.nombre).filter { it.isNotBlank() }.joinToString(" "),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { pendingCategoryEdit = category }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = stringResource(id = R.string.edit_category_action),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { pendingCategoryDelete = category }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = stringResource(id = R.string.delete_action),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            // ── Exportar ──
            item {
                SectionCard(
                    icon = Icons.Default.FileDownload,
                    title = stringResource(id = R.string.export_title)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(modifier = Modifier.weight(1f), onClick = onExportCsv) {
                            Text(text = stringResource(id = R.string.export_csv_action))
                        }
                        OutlinedButton(modifier = Modifier.weight(1f), onClick = onExportPdf) {
                            Text(text = stringResource(id = R.string.export_pdf_action))
                        }
                    }
                }
            }

            // ── Backup ──
            item {
                SectionCard(
                    icon = Icons.Default.CloudUpload,
                    title = stringResource(id = R.string.backup_title)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledTonalButton(modifier = Modifier.weight(1f), onClick = onBackupJson) {
                            Text(text = stringResource(id = R.string.backup_json_action))
                        }
                        OutlinedButton(modifier = Modifier.weight(1f), onClick = { showRestoreConfirm = true }) {
                            Text(text = stringResource(id = R.string.restore_json_action))
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledTonalButton(modifier = Modifier.weight(1f), onClick = onBackupCsv) {
                            Text(text = stringResource(id = R.string.backup_csv_action))
                        }
                        OutlinedButton(modifier = Modifier.weight(1f), onClick = onRestoreCsv) {
                            Text(text = stringResource(id = R.string.restore_csv_action))
                        }
                    }
                }
            }

            // ── Autobackup ──
            item {
                SectionCard(
                    icon = Icons.Default.Schedule,
                    title = stringResource(id = R.string.auto_backup_title)
                ) {
                    // Status chip
                    Surface(
                        color = if (autoBackupFolderUri.isNullOrBlank())
                            MaterialTheme.colorScheme.errorContainer
                        else
                            MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = if (autoBackupFolderUri.isNullOrBlank())
                                stringResource(id = R.string.auto_backup_folder_missing)
                            else
                                stringResource(id = R.string.auto_backup_folder_configured),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (autoBackupFolderUri.isNullOrBlank())
                                MaterialTheme.colorScheme.onErrorContainer
                            else
                                MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledTonalButton(modifier = Modifier.weight(1f), onClick = onPickAutoBackupFolder) {
                            Text(text = stringResource(id = R.string.select_saf_folder_action))
                        }
                        OutlinedButton(modifier = Modifier.weight(1f), onClick = onClearAutoBackupFolder) {
                            Text(text = stringResource(id = R.string.clear_saf_folder_action))
                        }
                    }

                    Text(
                        text = stringResource(id = R.string.auto_backup_interval_label),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = autoBackupIntervalHours <= 12,
                            onClick = { onAutoBackupIntervalChange(12) },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                        ) {
                            Text(text = stringResource(id = R.string.auto_backup_12h))
                        }
                        SegmentedButton(
                            selected = autoBackupIntervalHours > 12,
                            onClick = { onAutoBackupIntervalChange(24) },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                        ) {
                            Text(text = stringResource(id = R.string.auto_backup_24h))
                        }
                    }
                }
            }

            item {
                SectionCard(
                    icon = Icons.Default.Info,
                    title = stringResource(id = R.string.about_dev_title)
                ) {
                    Text(
                        text = stringResource(id = R.string.about_dev_settings_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FilledTonalButton(
                        onClick = onOpenAboutDev
                    ) {
                        Text(text = stringResource(id = R.string.about_dev_action))
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
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

    pendingCategoryEdit?.let { target ->
        EditCategoryDialog(
            category = target,
            onDismiss = { pendingCategoryEdit = null },
            onSave = { name, emoji ->
                onEditCategory(target, name, emoji)
                pendingCategoryEdit = null
            }
        )
    }
}

@Composable
private fun EditCategoryDialog(
    category: CategoryEntity,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember(category.id) { mutableStateOf(category.nombre) }
    var icon by remember(category.id) { mutableStateOf(category.emoji) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(id = R.string.edit_category_action)) },
        text = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text(text = stringResource(id = R.string.new_category_label)) },
                    shape = MaterialTheme.shapes.medium,
                    singleLine = true
                )
                OutlinedTextField(
                    modifier = Modifier.width(72.dp),
                    value = icon,
                    onValueChange = { icon = it },
                    placeholder = {
                        Text(
                            text = stringResource(id = R.string.new_category_emoji_label),
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1
                        )
                    },
                    shape = MaterialTheme.shapes.medium,
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(name, icon) }) {
                Text(text = stringResource(id = R.string.save_action))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.cancel_action))
            }
        }
    )
}

@Composable
private fun SectionCard(
    icon: ImageVector,
    title: String,
    content: @Composable () -> Unit
) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            content()
        }
    }
}
