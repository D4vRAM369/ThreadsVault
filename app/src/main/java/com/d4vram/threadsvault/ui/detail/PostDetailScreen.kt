package com.d4vram.threadsvault.ui.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import coil.compose.AsyncImage
import com.d4vram.threadsvault.R
import com.d4vram.threadsvault.utils.MediaSaveUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(uiState: PostDetailUiState) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(id = R.string.post_detail_placeholder)) }
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
                    Text(text = stringResource(id = R.string.state_loading))
                }
            }
            is PostDetailUiState.Empty -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = stringResource(id = R.string.post_detail_empty))
                }
            }
            is PostDetailUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = uiState.message)
                }
            }
            is PostDetailUiState.Success -> {
                val mediaUrl = uiState.post.imagenPath.orEmpty()
                var showImageViewer by remember(mediaUrl) { mutableStateOf(false) }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = uiState.post.autor.ifBlank { "@unknown" },
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = stringResource(id = R.string.post_content_title),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Surface(
                        tonalElevation = 1.dp,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = uiState.post.url,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        )
                    }
                    Text(
                        text = stringResource(id = R.string.extracted_content_title),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = uiState.post.contenido.ifBlank { stringResource(id = R.string.no_content_text) },
                        style = MaterialTheme.typography.bodyLarge
                    )

                    if (mediaUrl.isNotBlank()) {
                        AsyncImage(
                            model = mediaUrl,
                            contentDescription = stringResource(id = R.string.preview_image_content_desc),
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 140.dp, max = 420.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { showImageViewer = true }
                        )
                    }

                    if (uiState.post.categorias.isNotBlank()) {
                        AssistChip(
                            onClick = {},
                            label = { Text(text = uiState.post.categorias) }
                        )
                    }
                    if (uiState.post.notas.isNotBlank()) {
                        Text(text = uiState.post.notas, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                if (showImageViewer && mediaUrl.isNotBlank()) {
                    ImageViewerDialog(
                        imageUrl = mediaUrl,
                        onDismiss = { showImageViewer = false }
                    )
                }
            }
        }
    }
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
