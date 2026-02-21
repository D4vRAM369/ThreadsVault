package com.d4vram.threadsvault.ui.share

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Link
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.d4vram.threadsvault.R
import com.d4vram.threadsvault.data.database.entity.CategoryEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareScreen(
    sharedUrl: String,
    categories: List<CategoryEntity>,
    saveState: ShareSaveState,
    onSave: (notas: String, categoria: String?) -> Unit,
    onCancel: () -> Unit,
    onAddCategory: (nombre: String, emoji: String) -> Unit = { _, _ -> }
) {
    var notes by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var showAddCategory by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onCancel,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 240.dp, max = 560.dp)
                .navigationBarsPadding()
                .imePadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.share_receiver_title),
                    style = MaterialTheme.typography.titleLarge
                )
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = null
                        )
                        Text(
                            text = sharedUrl,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(text = stringResource(id = R.string.quick_notes_label)) }
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { category ->
                        val label = listOf(category.emoji, category.nombre)
                            .filter { it.isNotBlank() }.joinToString(" ")
                        FilterChip(
                            selected = selectedCategory == category.nombre,
                            onClick = { selectedCategory = category.nombre },
                            label = { Text(text = label) },
                            leadingIcon = {
                                if (selectedCategory == category.nombre) {
                                    Text(text = "\u2713")
                                }
                            }
                        )
                    }
                    item {
                        FilterChip(
                            selected = false,
                            onClick = { showAddCategory = true },
                            label = { Text(text = "Nueva") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }

                if (showAddCategory) {
                    AddCategoryInlineDialog(
                        onDismiss = { showAddCategory = false },
                        onAdd = { nombre, emoji ->
                            onAddCategory(nombre, emoji)
                            selectedCategory = nombre
                            showAddCategory = false
                        }
                    )
                }
            }

            if (saveState is ShareSaveState.Error) {
                Text(text = saveState.message)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onCancel) {
                    Text(text = stringResource(id = R.string.cancel_action))
                }
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onSave(notes, selectedCategory) },
                enabled = saveState != ShareSaveState.Saving
            ) {
                Text(
                    text = if (saveState == ShareSaveState.Saving) {
                        stringResource(id = R.string.state_loading)
                    } else {
                        stringResource(id = R.string.save_in_threadsvault)
                    }
                )
            }
        }
    }
}
@Composable
private fun AddCategoryInlineDialog(
    onDismiss: () -> Unit,
    onAdd: (nombre: String, emoji: String) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva categoría") },
        text = {
            androidx.compose.foundation.layout.Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = androidx.compose.ui.Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = emoji,
                    onValueChange = { emoji = it },
                    label = { Text("Emoji (opcional)") },
                    singleLine = true,
                    modifier = androidx.compose.ui.Modifier.fillMaxWidth(0.4f)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (nombre.isNotBlank()) onAdd(nombre.trim(), emoji.trim()) },
                enabled = nombre.isNotBlank()
            ) { Text("Crear") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
