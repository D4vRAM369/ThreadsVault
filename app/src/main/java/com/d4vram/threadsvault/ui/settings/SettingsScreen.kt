package com.d4vram.threadsvault.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.Composable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
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
    themeMode: ThemeMode,
    categories: List<CategoryEntity>,
    onThemeModeChange: (ThemeMode) -> Unit,
    onAddCategory: (String, String) -> Unit,
    onDeleteCategory: (CategoryEntity) -> Unit,
    onExportCsv: () -> Unit,
    onExportPdf: () -> Unit,
    onBackupJson: () -> Unit,
    onRestoreJson: () -> Unit
) {
    var newCategory by remember { mutableStateOf("") }
    var newCategoryEmoji by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text(text = title) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(text = stringResource(id = R.string.theme_mode_title))
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

            Text(text = stringResource(id = R.string.categories_title))
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

            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(categories, key = { it.id }) { category ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = listOf(category.emoji, category.nombre).filter { it.isNotBlank() }.joinToString(" "))
                        IconButton(onClick = { onDeleteCategory(category) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(id = R.string.delete_action)
                            )
                        }
                    }
                }
            }

            Text(text = stringResource(id = R.string.export_title))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onExportCsv) {
                    Text(text = stringResource(id = R.string.export_csv_action))
                }
                Button(onClick = onExportPdf) {
                    Text(text = stringResource(id = R.string.export_pdf_action))
                }
            }

            Text(text = stringResource(id = R.string.backup_title))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onBackupJson) {
                    Text(text = stringResource(id = R.string.backup_json_action))
                }
                Button(onClick = onRestoreJson) {
                    Text(text = stringResource(id = R.string.restore_json_action))
                }
            }
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
        androidx.compose.foundation.layout.Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RadioButton(selected = selected, onClick = onClick)
            Text(text = label)
        }
    }
}
