package com.d4vram.threadsvault.ui.vault

import android.text.format.DateUtils
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.d4vram.threadsvault.R
import com.d4vram.threadsvault.data.database.entity.CategoryEntity
import com.d4vram.threadsvault.data.database.entity.PostEntity
import com.d4vram.threadsvault.utils.MediaUrlUtils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun VaultScreen(
    title: String,
    searchText: String,
    categories: List<CategoryEntity>,
    selectedCategory: String?,
    uiState: VaultUiState,
    onSearchTextChange: (String) -> Unit,
    onSelectCategory: (String?) -> Unit,
    onToggleFavorito: (PostEntity) -> Unit,
    onDeletePost: (PostEntity) -> Unit,
    onRestorePost: (PostEntity) -> Unit,
    onEditNotes: (PostEntity, String) -> Unit,
    onEditCategories: (PostEntity, List<String>) -> Unit,
    onRetryExtraction: (PostEntity) -> Unit,
    onAddCategory: (String, String) -> Unit,
    onDeleteCategory: (CategoryEntity) -> Unit,
    onOpenPost: (Long) -> Unit,
    onSearchAction: () -> Unit,
    onManualAdd: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingDeleted by remember { mutableStateOf<PostEntity?>(null) }
    var editNotesPost by remember { mutableStateOf<PostEntity?>(null) }
    var editCategoriesPost by remember { mutableStateOf<PostEntity?>(null) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var pendingCategoryDelete by remember { mutableStateOf<CategoryEntity?>(null) }
    val deletedMessage = stringResource(id = R.string.post_deleted_message)
    val undoLabel = stringResource(id = R.string.undo_action)

    LaunchedEffect(pendingDeleted) {
        val deleted = pendingDeleted ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = deletedMessage,
            actionLabel = undoLabel,
            duration = SnackbarDuration.Short
        )
        if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
            onRestorePost(deleted)
        }
        pendingDeleted = null
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.navigationBars,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = title, style = MaterialTheme.typography.titleLarge) },
                actions = {
                    IconButton(onClick = onSearchAction) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(id = R.string.search_label)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onManualAdd,
                text = { Text(text = stringResource(id = R.string.manual_add_placeholder)) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(id = R.string.manual_add_placeholder)
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = searchText,
                onValueChange = onSearchTextChange,
                label = { Text(text = stringResource(id = R.string.search_label)) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) }
            )
            Spacer(modifier = Modifier.size(12.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterCategoryChip(
                        label = stringResource(id = R.string.category_all),
                        selected = selectedCategory == null,
                        onClick = { onSelectCategory(null) }
                    )
                }
                items(categories, key = { it.id }) { category ->
                    FilterCategoryChip(
                        label = listOf(category.emoji, category.nombre).filter { it.isNotBlank() }.joinToString(" "),
                        selected = selectedCategory == category.nombre,
                        onClick = { onSelectCategory(category.nombre) },
                        onLongClick = {
                            if (!category.nombre.equals("Sin categoria", ignoreCase = true) &&
                                !category.nombre.equals("Sin categoría", ignoreCase = true)
                            ) {
                                pendingCategoryDelete = category
                            }
                        }
                    )
                }
                item {
                    FilterCategoryChip(
                        label = stringResource(id = R.string.add_category_short),
                        selected = false,
                        onClick = { showAddCategoryDialog = true }
                    )
                }
            }
            Spacer(modifier = Modifier.size(12.dp))

            when (uiState) {
                is VaultUiState.Loading -> {
                    Text(text = stringResource(id = R.string.state_loading))
                }
                is VaultUiState.Empty -> {
                    Text(text = stringResource(id = R.string.state_empty))
                }
                is VaultUiState.Error -> {
                    Text(text = uiState.message)
                }
                is VaultUiState.Success -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(uiState.posts, key = { it.id }) { post ->
                            PostCard(
                                post = post,
                                onToggleFavorito = { onToggleFavorito(post) },
                                onDelete = {
                                    onDeletePost(post)
                                    pendingDeleted = post
                                },
                                onEditNotes = { editNotesPost = post },
                                onEditCategories = { editCategoriesPost = post },
                                onRetryExtraction = { onRetryExtraction(post) },
                                onOpen = { onOpenPost(post.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    editNotesPost?.let { target ->
        EditNotesDialog(
            currentNotes = target.notas,
            onDismiss = { editNotesPost = null },
            onSave = { newNotes ->
                onEditNotes(target, newNotes)
                editNotesPost = null
            }
        )
    }

    editCategoriesPost?.let { target ->
        EditCategoriesDialog(
            availableCategories = categories,
            currentCsv = target.categorias,
            onDismiss = { editCategoriesPost = null },
            onSave = { selected ->
                onEditCategories(target, selected)
                editCategoriesPost = null
            }
        )
    }

    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            onAdd = { name, emoji ->
                onAddCategory(name, emoji)
                showAddCategoryDialog = false
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
                TextButton(onClick = {
                    onDeleteCategory(target)
                    pendingCategoryDelete = null
                }) {
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FilterCategoryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        tonalElevation = if (selected) 3.dp else 0.dp,
        color = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick
        )
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PostCard(
    post: PostEntity,
    onToggleFavorito: () -> Unit,
    onDelete: () -> Unit,
    onEditNotes: () -> Unit,
    onEditCategories: () -> Unit,
    onRetryExtraction: () -> Unit,
    onOpen: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = post.autor.ifBlank { "@unknown" },
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = formatRelativeDate(post.fechaGuardado),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = post.contenido.ifBlank { stringResource(id = R.string.no_content_text) },
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge
            )
            Surface(
                tonalElevation = 1.dp,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = post.url,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (!post.imagenPath.isNullOrBlank()) {
                Box {
                    AsyncImage(
                        model = post.imagenPath,
                        contentDescription = stringResource(id = R.string.preview_image_content_desc),
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                    if (MediaUrlUtils.isVideoUrl(post.imagenPath)) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(8.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null
                            )
                        }
                    }
                }
            }
            if (post.categorias.isNotBlank()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    post.categorias.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach {
                        AssistChip(onClick = {}, label = { Text(it) })
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onToggleFavorito) {
                    Icon(
                        imageVector = if (post.esFavorito) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = stringResource(id = R.string.favorite_toggle),
                        tint = if (post.esFavorito) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(id = R.string.more_actions)
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(text = stringResource(id = R.string.edit_notes_action)) },
                            onClick = {
                                menuExpanded = false
                                onEditNotes()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(text = stringResource(id = R.string.assign_categories_action)) },
                            onClick = {
                                menuExpanded = false
                                onEditCategories()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(text = stringResource(id = R.string.retry_extraction_action)) },
                            onClick = {
                                menuExpanded = false
                                onRetryExtraction()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(text = stringResource(id = R.string.delete_action)) },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EditCategoriesDialog(
    availableCategories: List<CategoryEntity>,
    currentCsv: String,
    onDismiss: () -> Unit,
    onSave: (List<String>) -> Unit
) {
    val initial = remember(currentCsv) {
        currentCsv
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .toMutableSet()
    }
    var selected by remember { mutableStateOf(initial) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(id = R.string.assign_categories_action)) },
        text = {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                availableCategories.forEach { category ->
                    val name = category.nombre
                    val label = listOf(category.emoji, name).filter { it.isNotBlank() }.joinToString(" ")
                    FilterChip(
                        selected = selected.contains(name),
                        onClick = {
                            selected = selected.toMutableSet().apply {
                                if (contains(name)) remove(name) else add(name)
                            }
                        },
                        label = { Text(text = label) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(selected.toList()) }) {
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
private fun EditNotesDialog(
    currentNotes: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var notes by remember(currentNotes) { mutableStateOf(currentNotes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(id = R.string.edit_notes_action)) },
        text = {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = notes,
                onValueChange = { notes = it },
                label = { Text(text = stringResource(id = R.string.quick_notes_label)) }
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(notes) }) {
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
private fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(id = R.string.add_category_action)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(text = stringResource(id = R.string.new_category_label)) }
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = emoji,
                    onValueChange = { emoji = it },
                    label = { Text(text = stringResource(id = R.string.new_category_emoji_label)) }
                )
                Text(text = stringResource(id = R.string.emoji_hint))
            }
        },
        confirmButton = {
            TextButton(onClick = { onAdd(name, emoji) }) {
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

private fun formatRelativeDate(timestamp: Long): String {
    return DateUtils.getRelativeTimeSpanString(
        timestamp,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS
    ).toString()
}
