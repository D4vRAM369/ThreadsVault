package com.d4vram.threadsvault.ui.vault

import android.text.format.DateUtils
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.activity.compose.BackHandler
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.rounded.Tag
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.d4vram.threadsvault.R
import com.d4vram.threadsvault.data.database.entity.CategoryEntity
import com.d4vram.threadsvault.data.database.entity.PostEntity
import com.d4vram.threadsvault.ui.components.CategoryColorPickerDialog
import com.d4vram.threadsvault.ui.components.LinkifiedText
import com.d4vram.threadsvault.ui.components.HowToUseBottomSheet
import androidx.compose.material.icons.outlined.HelpOutline
import com.d4vram.threadsvault.ui.components.parseHexColor
import com.d4vram.threadsvault.ui.theme.VaultFavorite
import com.d4vram.threadsvault.utils.MediaUrlsCodec
import com.d4vram.threadsvault.utils.MediaUrlUtils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun VaultScreen(
    title: String,
    searchText: String,
    categories: List<CategoryEntity>,
    selectedCategory: String?,
    showFavoritesOnly: Boolean,
    uiState: VaultUiState,
    onSearchTextChange: (String) -> Unit,
    onSelectCategory: (String?) -> Unit,
    onToggleFavoritesFilter: () -> Unit,
    onToggleFavorito: (PostEntity) -> Unit,
    onDeletePost: (PostEntity) -> Unit,
    onRestorePost: (PostEntity) -> Unit,
    onEditNotes: (PostEntity, String) -> Unit,
    onEditCategories: (PostEntity, List<String>) -> Unit,
    onRetryExtraction: (PostEntity) -> Unit,
    onAddCategory: (String, String, String) -> Unit,
    onDeleteCategory: (CategoryEntity) -> Unit,
    onOpenPost: (Long) -> Unit,
    onSearchAction: () -> Unit,
    onOpenSettings: () -> Unit,
    onManualAdd: () -> Unit,
    postCountsByCategory: Map<String, Int> = emptyMap(),
    isSelectionMode: Boolean = false,
    selectedGroupKeys: Set<Long> = emptySet(),
    onLongPressCard: (Long) -> Unit = {},
    onToggleSeleccion: (Long) -> Unit = {},
    onAgrupar: () -> Unit = {},
    onSalirSeleccion: () -> Unit = {},
    hashtagFilter: String? = null,
    onHashtagClick: (String?) -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingDeleted by remember { mutableStateOf<PostEntity?>(null) }
    var editNotesPost by remember { mutableStateOf<PostEntity?>(null) }
    var editCategoriesPost by remember { mutableStateOf<PostEntity?>(null) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var pendingCategoryDelete by remember { mutableStateOf<CategoryEntity?>(null) }
    var pendingCopiedUrl by remember { mutableStateOf<String?>(null) }
    var showHowToUse by remember { mutableStateOf(false) }
    val deletedMessage = stringResource(id = R.string.post_deleted_message)
    val undoLabel = stringResource(id = R.string.undo_action)
    val copiedMessage = stringResource(id = R.string.url_copied_message)
    val postCount = when (uiState) {
        is VaultUiState.Success -> uiState.postGroups.size
        else -> 0
    }
    val topHashtags = when (uiState) {
        is VaultUiState.Success -> extractTopHashtags(uiState.postGroups.flatten())
        else -> emptyList()
    }

    BackHandler(enabled = isSelectionMode) { onSalirSeleccion() }

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
    LaunchedEffect(pendingCopiedUrl) {
        val copied = pendingCopiedUrl ?: return@LaunchedEffect
        if (copied.isNotBlank()) {
            snackbarHostState.showSnackbar(message = copiedMessage, duration = SnackbarDuration.Short)
        }
        pendingCopiedUrl = null
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.navigationBars,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    IconButton(onClick = onToggleFavoritesFilter) {
                        Icon(
                            imageVector = if (showFavoritesOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = stringResource(id = R.string.favorites_filter_label),
                            tint = if (showFavoritesOnly) VaultFavorite else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    IconButton(onClick = onSearchAction) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(id = R.string.search_label),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(id = R.string.open_settings_action),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = !isSelectionMode,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it }
            ) {
                ExtendedFloatingActionButton(
                    onClick = onManualAdd,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    text = { Text(text = stringResource(id = R.string.manual_add_placeholder)) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(id = R.string.manual_add_placeholder)
                        )
                    }
                )
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = isSelectionMode,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it }
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 4.dp,
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(onClick = onSalirSeleccion) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(id = R.string.exit_selection_mode)
                                )
                            }
                            Text(
                                text = stringResource(id = R.string.selection_count_label, selectedGroupKeys.size),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Button(
                            onClick = onAgrupar,
                            enabled = selectedGroupKeys.size >= 2
                        ) {
                            Text(text = stringResource(id = R.string.group_as_thread_action))
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surfaceContainerLowest
                        )
                    )
                )
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            ElevatedCard(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Tag,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = stringResource(id = R.string.vault_hashtags_label),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (topHashtags.isNotEmpty()) {
                            Text(
                                text = "${topHashtags.size}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (topHashtags.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(topHashtags, key = { it }) { tag ->
                                val isActiveFilter = hashtagFilter == tag
                                Surface(
                                    onClick = { onHashtagClick(if (isActiveFilter) null else tag) },
                                    shape = RoundedCornerShape(999.dp),
                                    color = if (isActiveFilter) MaterialTheme.colorScheme.secondary
                                            else MaterialTheme.colorScheme.secondaryContainer,
                                    border = BorderStroke(
                                        1.dp,
                                        MaterialTheme.colorScheme.secondary.copy(
                                            alpha = if (isActiveFilter) 1f else 0.32f
                                        )
                                    )
                                ) {
                                    Text(
                                        text = "#$tag",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isActiveFilter) MaterialTheme.colorScheme.onSecondary
                                                else MaterialTheme.colorScheme.onSecondaryContainer,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = stringResource(id = R.string.vault_hashtags_empty),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = searchText,
                        onValueChange = onSearchTextChange,
                        placeholder = { Text(text = stringResource(id = R.string.search_label)) },
                        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                        shape = MaterialTheme.shapes.large,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(id = R.string.vault_quick_filters_label),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "$postCount",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = MaterialTheme.shapes.large,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                item {
                                    FilterCategoryChip(
                                        label = stringResource(id = R.string.category_all),
                                        selected = selectedCategory == null && !showFavoritesOnly,
                                        onClick = { onSelectCategory(null) }
                                    )
                                }
                                items(categories, key = { it.id }) { category ->
                                    val count = postCountsByCategory[category.nombre]
                                    val chipLabel = buildString {
                                        listOf(category.emoji, category.nombre)
                                            .filter { it.isNotBlank() }
                                            .joinTo(this, " ")
                                        if (count != null && count > 0) append(" ($count)")
                                    }
                                    FilterCategoryChip(
                                        label = chipLabel,
                                        selected = selectedCategory == category.nombre,
                                        onClick = { onSelectCategory(category.nombre) },
                                        onLongClick = {
                                            if (!category.nombre.equals("Sin categoria", ignoreCase = true) &&
                                                !category.nombre.equals("Sin categoría", ignoreCase = true)
                                            ) {
                                                pendingCategoryDelete = category
                                            }
                                        },
                                        accentColor = runCatching { parseHexColor(category.color) }.getOrNull()
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
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.size(16.dp))

            Crossfade(
                targetState = uiState,
                animationSpec = tween(durationMillis = 220),
                label = "vault_state_crossfade"
            ) { state ->
                when (state) {
                    is VaultUiState.Loading -> {
                        ShimmerPostCardList()
                    }
                    is VaultUiState.Empty -> {
                        val emptyAccent = selectedCategory?.let { catName ->
                            categories.find { it.nombre == catName }
                                ?.color?.let { runCatching { parseHexColor(it) }.getOrNull() }
                        }
                        EmptyVaultState(
                            modifier = Modifier.padding(bottom = 104.dp),
                            accentColor = emptyAccent,
                            onShowHowToUse = { showHowToUse = true }
                        )
                    }
                    is VaultUiState.Error -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    is VaultUiState.Success -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 120.dp)
                        ) {
                            items(state.postGroups, key = { it.first().id }) { postGroup ->
                                val firstPost = postGroup.first()
                                val firstCat = firstPost.categorias.split(",")
                                    .map { it.trim() }.firstOrNull { it.isNotBlank() }
                                val accentColor = firstCat?.let { name ->
                                    categories.find { it.nombre == name }?.color?.let { hex ->
                                        runCatching {
                                            val c = android.graphics.Color.parseColor(hex)
                                            androidx.compose.ui.graphics.Color(
                                                red = android.graphics.Color.red(c) / 255f,
                                                green = android.graphics.Color.green(c) / 255f,
                                                blue = android.graphics.Color.blue(c) / 255f
                                            )
                                        }.getOrNull()
                                    }
                                }
                                var visible by remember { mutableStateOf(false) }
                                LaunchedEffect(firstPost.id) { visible = true }
                                val cardAlpha by animateFloatAsState(
                                    targetValue = if (visible) 1f else 0f,
                                    animationSpec = tween(250),
                                    label = "card_fade"
                                )
                                PostGroupCard(
                                    postGroup = postGroup,
                                    modifier = Modifier.animateItemPlacement().alpha(cardAlpha),
                                    accentColor = accentColor,
                                    isSelectionMode = isSelectionMode,
                                    isSelected = firstPost.id in selectedGroupKeys,
                                    onLongPress = { onLongPressCard(firstPost.id) },
                                    onToggleSeleccion = { onToggleSeleccion(firstPost.id) },
                                    onHashtagClick = { tag -> onHashtagClick(tag) },
                                    onToggleFavorito = { onToggleFavorito(it) },
                                    onDelete = {
                                        onDeletePost(it)
                                        pendingDeleted = it
                                    },
                                    onEditNotes = { editNotesPost = it },
                                    onEditCategories = { editCategoriesPost = it },
                                    onCopyUrl = { pendingCopiedUrl = it },
                                    onRetryExtraction = { onRetryExtraction(it) },
                                    onOpen = { onOpenPost(it.id) }
                                )
                            }
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
            onAdd = { name, emoji, color ->
                onAddCategory(name, emoji, color)
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

    if (showHowToUse) {
        HowToUseBottomSheet(onDismiss = { showHowToUse = false })
    }
}

@Composable
private fun EmptyVaultState(
    modifier: Modifier = Modifier,
    accentColor: androidx.compose.ui.graphics.Color? = null,
    onShowHowToUse: () -> Unit = {}
) {
    val container = accentColor ?: MaterialTheme.colorScheme.primaryContainer
    val iconTint = accentColor ?: MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(
                1.dp,
                (accentColor ?: MaterialTheme.colorScheme.outlineVariant).copy(alpha = 0.45f)
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                container.copy(alpha = 0.22f),
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = container.copy(alpha = 0.18f)
                    ) {
                        Text(
                            text = "Start your private vault",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = iconTint.copy(alpha = if (accentColor != null) 1f else 0.85f),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = container.copy(alpha = 0.18f)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Inventory2,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(16.dp)
                                .size(72.dp),
                            tint = iconTint.copy(alpha = 0.72f)
                        )
                    }
                    Text(
                        text = stringResource(id = R.string.state_empty_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                Text(
                    text = stringResource(id = R.string.state_empty_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
                Text(
                    text = stringResource(id = R.string.state_empty_hashtag_tip),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center
                )
                    SuggestionChip(
                        onClick = onShowHowToUse,
                        label = {
                            Text(
                                text = stringResource(R.string.howto_chip_label),
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.HelpOutline,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.10f),
                            labelColor = MaterialTheme.colorScheme.secondary,
                            iconContentColor = MaterialTheme.colorScheme.secondary
                        ),
                        border = SuggestionChipDefaults.suggestionChipBorder(
                            enabled = true,
                            borderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
            }
        }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FilterCategoryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    accentColor: androidx.compose.ui.graphics.Color? = null
) {
    val bgColor by animateColorAsState(
        targetValue = when {
            selected && accentColor != null -> accentColor
            selected -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = tween(durationMillis = 200),
        label = "chip_bg"
    )
    val textColor by animateColorAsState(
        targetValue = when {
            selected && accentColor != null -> {
                val lum = 0.299f * accentColor.red + 0.587f * accentColor.green + 0.114f * accentColor.blue
                if (lum > 0.55f) androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.87f)
                else androidx.compose.ui.graphics.Color.White
            }
            selected -> MaterialTheme.colorScheme.onPrimary
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(durationMillis = 200),
        label = "chip_text"
    )
    val borderColor by animateColorAsState(
        targetValue = when {
            selected && accentColor != null -> accentColor
            selected -> MaterialTheme.colorScheme.primary
            accentColor != null -> accentColor.copy(alpha = 0.45f)
            else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
        },
        animationSpec = tween(durationMillis = 200),
        label = "chip_border"
    )
    Surface(
        shape = MaterialTheme.shapes.medium,
        tonalElevation = if (selected) 3.dp else 0.dp,
        color = bgColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick
        )
    ) {
        Text(
            text = label,
            modifier = Modifier
                .heightIn(min = 34.dp)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun PostGroupCard(
    postGroup: List<PostEntity>,
    modifier: Modifier = Modifier,
    accentColor: androidx.compose.ui.graphics.Color? = null,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onLongPress: () -> Unit = {},
    onToggleSeleccion: () -> Unit = {},
    onHashtagClick: (String) -> Unit = {},
    onToggleFavorito: (PostEntity) -> Unit,
    onDelete: (PostEntity) -> Unit,
    onEditNotes: (PostEntity) -> Unit,
    onEditCategories: (PostEntity) -> Unit,
    onCopyUrl: (String) -> Unit,
    onRetryExtraction: (PostEntity) -> Unit,
    onOpen: (PostEntity) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var favPressed by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current

    val favScale by animateFloatAsState(
        targetValue = if (favPressed) 1.3f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        finishedListener = { favPressed = false },
        label = "fav_scale"
    )

    val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { postGroup.size })
    val currentPost = postGroup[pagerState.currentPage]
    val primaryColor = MaterialTheme.colorScheme.primary
    val cardShape = MaterialTheme.shapes.large

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
            .then(if (isSelected) Modifier.border(2.dp, primaryColor, cardShape) else Modifier)
            .combinedClickable(
                onClick = { if (isSelectionMode) onToggleSeleccion() else onOpen(currentPost) },
                onLongClick = { if (!isSelectionMode) onLongPress() }
            ),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = cardShape
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().then(
                if (accentColor != null)
                    Modifier.drawBehind {
                        drawRect(
                            color = accentColor,
                            size = size.copy(width = 4.dp.toPx())
                        )
                    }
                else Modifier
            )
        ) {
        androidx.compose.foundation.pager.HorizontalPager(state = pagerState) { page ->
            val post = postGroup[page]
        Column(
            modifier = Modifier.fillMaxWidth().padding(
                start = if (accentColor != null) 18.dp else 14.dp,
                top = 14.dp, end = 14.dp, bottom = 14.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Avatar + Author + Date + Post Counter
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar circular con imagen real o inicial
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    val initial = post.autor.removePrefix("@").firstOrNull()?.uppercase() ?: "?"
                    var avatarError by remember(post.authorAvatarUrl) { mutableStateOf(false) }
                    if (post.authorAvatarUrl != null && !avatarError) {
                        coil.compose.AsyncImage(
                            model = post.authorAvatarUrl,
                            contentDescription = "Avatar de ${post.autor}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            onError = { avatarError = true }
                        )
                    } else {
                        Text(
                            text = initial,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.autor.ifBlank { "@unknown" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = formatRelativeDate(post.fechaGuardado),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Indicador de Pager si hay más de 1 post en el grupo
                if (postGroup.size > 1) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = "${page + 1}/${postGroup.size}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Content
            val hasOnlyRawUrl = post.contenido.trim().equals(post.url.trim(), ignoreCase = true)
            val extractionNeedsRetry = (post.contenido.isBlank() || hasOnlyRawUrl) && post.imagenPath.isNullOrBlank()
            LinkifiedText(
                text = if (extractionNeedsRetry) {
                    stringResource(id = R.string.no_content_text)
                } else {
                    post.contenido
                },
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.92f),
                onHashtagClick = onHashtagClick
            )

            // URL pill (clickable hyperlink + copy)
            val uriHandler = LocalUriHandler.current
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 10.dp, top = 2.dp, bottom = 2.dp, end = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = post.url,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                runCatching { uriHandler.openUri(post.url) }
                            },
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(post.url))
                            onCopyUrl(post.url)
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = stringResource(id = R.string.copy_url_action),
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Media
            val mediaUrls = remember(post.mediaUrls, post.imagenPath) {
                MediaUrlsCodec.mergeWithPrimary(post.mediaUrls, post.imagenPath)
            }
            if (mediaUrls.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(mediaUrls, key = { it }) { mediaUrl ->
                        Box(
                            modifier = Modifier
                                .width(220.dp)
                                .clip(MaterialTheme.shapes.medium)
                        ) {
                            AsyncImage(
                                model = mediaUrl,
                                contentDescription = stringResource(id = R.string.preview_image_content_desc),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(16f / 9f)
                            )
                            if (MediaUrlUtils.isVideoUrl(mediaUrl)) {
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .size(42.dp),
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.86f),
                                    shape = CircleShape
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Category chips
            if (post.categorias.isNotBlank()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    post.categorias.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach { cat ->
                        SuggestionChip(
                            onClick = {},
                            label = {
                                Text(
                                    text = cat,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            shape = MaterialTheme.shapes.small,
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )
                    }
                }
            }

            if (post.notas.isNotBlank()) {
                val notePreview = post.notas
                    .lineSequence()
                    .map { it.trim() }
                    .firstOrNull { it.isNotBlank() }
                    .orEmpty()
                AssistChip(
                    onClick = { onEditNotes(post) },
                    leadingIcon = {
                        Text(
                            text = "\uD83D\uDCDD",
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    label = {
                        Text(
                            text = notePreview,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                        labelColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        leadingIconContentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    ),
                    border = AssistChipDefaults.assistChipBorder(
                        enabled = true,
                        borderColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.28f)
                    )
                )
            }

            // Footer: Notes indicator + Favorite + Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = {
                        favPressed = true
                        onToggleFavorito(post)
                    },
                    modifier = Modifier.graphicsLayer(
                        scaleX = favScale,
                        scaleY = favScale
                    )
                ) {
                    Icon(
                        imageVector = if (post.esFavorito) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = stringResource(id = R.string.favorite_toggle),
                        tint = if (post.esFavorito) VaultFavorite else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(id = R.string.more_actions),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.EditNote, contentDescription = null)
                            },
                            text = { Text(text = stringResource(id = R.string.edit_notes_action)) },
                            onClick = {
                                menuExpanded = false
                                onEditNotes(post)
                            }
                        )
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(imageVector = Icons.AutoMirrored.Filled.Label, contentDescription = null)
                            },
                            text = { Text(text = stringResource(id = R.string.assign_categories_action)) },
                            onClick = {
                                menuExpanded = false
                                onEditCategories(post)
                            }
                        )
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null)
                            },
                            text = { Text(text = stringResource(id = R.string.copy_url_action)) },
                            onClick = {
                                menuExpanded = false
                                clipboardManager.setText(AnnotatedString(post.url))
                                onCopyUrl(post.url)
                            }
                        )
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                            },
                            text = { Text(text = stringResource(id = R.string.retry_extraction_action)) },
                            onClick = {
                                menuExpanded = false
                                onRetryExtraction(post)
                            }
                        )
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            text = {
                                Text(
                                    text = stringResource(id = R.string.delete_action),
                                    color = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onDelete(post)
                            }
                        )
                    }
                }
            }
        }
        } // closes HorizontalPager
        if (isSelected) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 10.dp, end = 10.dp)
                    .size(24.dp),
                shape = CircleShape,
                color = primaryColor
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
        } // closes Box
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
                label = { Text(text = stringResource(id = R.string.quick_notes_label)) },
                shape = MaterialTheme.shapes.medium
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
    onAdd: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var icon by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("#6D44E5") }
    var showColorPicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(id = R.string.add_category_action)) },
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
                IconButton(onClick = { showColorPicker = true }) {
                    Surface(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape),
                        color = parseHexColor(color),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {}
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onAdd(name, icon, color) }) {
                Text(text = stringResource(id = R.string.save_action))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.cancel_action))
            }
        }
    )

    if (showColorPicker) {
        CategoryColorPickerDialog(
            initialHex = color,
            onDismiss = { showColorPicker = false },
            onConfirm = { selectedHex ->
                color = selectedHex
                showColorPicker = false
            }
        )
    }
}

private fun formatRelativeDate(timestamp: Long): String {
    return DateUtils.getRelativeTimeSpanString(
        timestamp,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS
    ).toString()
}

private fun extractTopHashtags(posts: List<PostEntity>): List<String> {
    val counts = linkedMapOf<String, Int>()
    val regex = Regex("""#([\p{L}\p{N}_]{2,40})""")

    fun collectFromText(text: String) {
        regex.findAll(text).forEach { match ->
            val tag = match.groupValues[1].lowercase()
            if (tag.isNotBlank()) {
                counts[tag] = (counts[tag] ?: 0) + 1
            }
        }
    }

    posts.forEach { post ->
        post.etiquetas
            .split(",")
            .map { it.trim().lowercase() }
            .filter { it.isNotBlank() }
            .forEach { tag -> counts[tag] = (counts[tag] ?: 0) + 1 }
        collectFromText(post.contenido)
        collectFromText(post.notas)
    }
    return counts.entries
        .sortedByDescending { it.value }
        .map { it.key }
        .take(8)
}

@Composable
private fun shimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val offset by transition.animateFloat(
        initialValue = -600f, targetValue = 600f,
        animationSpec = infiniteRepeatable(
            tween(1000, easing = LinearEasing),
            RepeatMode.Restart
        ),
        label = "shimmer_offset"
    )
    val base = MaterialTheme.colorScheme.surfaceContainerHigh
    val highlight = MaterialTheme.colorScheme.surfaceContainerHighest
    return Brush.horizontalGradient(
        colors = listOf(base, highlight, base),
        startX = offset,
        endX = offset + 600f
    )
}

@Composable
private fun ShimmerPostCard(brush: Brush) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(brush))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.height(12.dp).fillMaxWidth(0.35f).clip(MaterialTheme.shapes.small).background(brush))
                    Box(modifier = Modifier.height(10.dp).fillMaxWidth(0.2f).clip(MaterialTheme.shapes.small).background(brush))
                }
            }
            Box(modifier = Modifier.height(12.dp).fillMaxWidth(0.95f).clip(MaterialTheme.shapes.small).background(brush))
            Box(modifier = Modifier.height(12.dp).fillMaxWidth(0.8f).clip(MaterialTheme.shapes.small).background(brush))
            Box(modifier = Modifier.height(12.dp).fillMaxWidth(0.6f).clip(MaterialTheme.shapes.small).background(brush))
        }
    }
}

@Composable
private fun ShimmerPostCardList() {
    val brush = shimmerBrush()
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 120.dp),
        userScrollEnabled = false
    ) {
        items(5) { ShimmerPostCard(brush) }
    }
}
