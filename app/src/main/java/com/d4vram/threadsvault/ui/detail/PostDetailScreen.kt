package com.d4vram.threadsvault.ui.detail

import android.text.format.DateUtils
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.d4vram.threadsvault.R
import com.d4vram.threadsvault.ui.components.LinkifiedText
import com.d4vram.threadsvault.utils.MediaUrlsCodec
import com.d4vram.threadsvault.utils.MediaSaveUtils
import com.d4vram.threadsvault.utils.MediaUrlUtils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun PostDetailScreen(
    uiState: PostDetailUiState,
    isRefreshing: Boolean = false,
    onBack: () -> Unit = {},
    onPreviousInThread: () -> Unit = {},
    onNextInThread: () -> Unit = {},
    onRetryExtraction: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.post_detail_placeholder),
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
                },
                actions = {
                    if (uiState is PostDetailUiState.Success) {
                        IconButton(onClick = onRetryExtraction, enabled = !isRefreshing) {
                            if (isRefreshing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = stringResource(id = R.string.retry_extraction_action),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        when (uiState) {
            is PostDetailUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.state_loading),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            is PostDetailUiState.Empty -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.post_detail_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            is PostDetailUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            is PostDetailUiState.Success -> {
                val mediaUrls = remember(uiState.post.mediaUrls, uiState.post.imagenPath) {
                    MediaUrlsCodec.mergeWithPrimary(uiState.post.mediaUrls, uiState.post.imagenPath)
                }
                val mediaPagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { mediaUrls.size })
                var viewerUrl by remember(mediaUrls) { mutableStateOf<String?>(null) }
                val hasThreadNavigation = uiState.threadPosts.size > 1

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Author header + avatar + date
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar circular
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            val initial = uiState.post.autor.removePrefix("@").firstOrNull()?.uppercase() ?: "?"
                            var avatarError by remember(uiState.post.authorAvatarUrl) { mutableStateOf(false) }
                            if (uiState.post.authorAvatarUrl != null && !avatarError) {
                                AsyncImage(
                                    model = uiState.post.authorAvatarUrl,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    onError = { avatarError = true }
                                )
                            } else {
                                Text(
                                    text = initial,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = uiState.post.autor.ifBlank { "@unknown" },
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(id = R.string.saved_on_label),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = formatRelativeDate(uiState.post.fechaGuardado),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (hasThreadNavigation) {
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.surfaceContainerLow,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = stringResource(
                                        id = R.string.thread_post_position_label,
                                        uiState.currentThreadIndex + 1,
                                        uiState.threadPosts.size
                                    ),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = onPreviousInThread,
                                        enabled = uiState.currentThreadIndex > 0,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(text = stringResource(id = R.string.previous_post_action))
                                    }
                                    FilledTonalButton(
                                        onClick = onNextInThread,
                                        enabled = uiState.currentThreadIndex < uiState.threadPosts.lastIndex,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(text = stringResource(id = R.string.next_post_action))
                                    }
                                }
                            }
                        }
                    }

                    // URL section (clickable + copy)
                    DetailSectionLabel(text = stringResource(id = R.string.post_content_title))
                    val uriHandler = LocalUriHandler.current
                    val clipboardManager = LocalClipboardManager.current
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 12.dp, top = 4.dp, bottom = 4.dp, end = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = uiState.post.url,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        runCatching { uriHandler.openUri(uiState.post.url) }
                                    }
                            )
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(uiState.post.url))
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = stringResource(id = R.string.copy_url_action),
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Content section
                    DetailSectionLabel(text = stringResource(id = R.string.extracted_content_title))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        LinkifiedText(
                            text = uiState.post.contenido.ifBlank { stringResource(id = R.string.no_content_text) },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        )
                    }

                    // Media carousel
                    if (mediaUrls.isNotEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            androidx.compose.foundation.pager.HorizontalPager(
                                state = mediaPagerState,
                                modifier = Modifier.fillMaxWidth()
                            ) { page ->
                                val mediaUrl = mediaUrls[page]
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 200.dp, max = 500.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = mediaUrl,
                                        contentDescription = stringResource(id = R.string.preview_image_content_desc),
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 200.dp, max = 500.dp)
                                            .clip(MaterialTheme.shapes.medium)
                                            .clickable { viewerUrl = mediaUrl }
                                    )
                                    if (MediaUrlUtils.isVideoUrl(mediaUrl)) {
                                        Surface(
                                            modifier = Modifier
                                                .align(Alignment.Center)
                                                .size(52.dp),
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
                            if (mediaUrls.size > 1) {
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(8.dp),
                                    shape = MaterialTheme.shapes.small,
                                    color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.55f)
                                ) {
                                    Text(
                                        text = "${mediaPagerState.currentPage + 1}/${mediaUrls.size}",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = androidx.compose.ui.graphics.Color.White
                                    )
                                }
                            }
                        }
                    }

                    // Categories as FlowRow chips
                    if (uiState.post.categorias.isNotBlank()) {
                        DetailSectionLabel(text = stringResource(id = R.string.categories_title))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            uiState.post.categorias.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach { cat ->
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text(text = cat, style = MaterialTheme.typography.labelMedium) },
                                    shape = MaterialTheme.shapes.small,
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                )
                            }
                        }
                    }

                    // Notes section
                    if (uiState.post.notas.isNotBlank()) {
                        DetailSectionLabel(text = stringResource(id = R.string.notes_section_title))
                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text(
                                text = uiState.post.notas,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }

                if (!viewerUrl.isNullOrBlank()) {
                    ImageViewerDialog(
                        imageUrl = viewerUrl.orEmpty(),
                        onDismiss = { viewerUrl = null }
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ImageViewerDialog(
    imageUrl: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var pendingDownloadUrl by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = androidx.compose.ui.graphics.Color.Black
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = stringResource(id = R.string.preview_image_content_desc),
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .combinedClickable(
                        onClick = onDismiss,
                        onLongClick = { pendingDownloadUrl = imageUrl }
                    )
            )
        }
    }

    pendingDownloadUrl?.let { url ->
        AlertDialog(
            onDismissRequest = { pendingDownloadUrl = null },
            title = { Text(text = stringResource(id = R.string.download_image_title)) },
            text = { Text(text = stringResource(id = R.string.download_image_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        MediaSaveUtils.saveToGallery(context, url)
                        pendingDownloadUrl = null
                    }
                ) {
                    Text(text = stringResource(id = R.string.download_action))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDownloadUrl = null }) {
                    Text(text = stringResource(id = R.string.cancel_action))
                }
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
