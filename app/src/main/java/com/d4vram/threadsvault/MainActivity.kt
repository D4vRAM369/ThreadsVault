package com.d4vram.threadsvault

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.d4vram.threadsvault.ui.detail.PostDetailScreen
import com.d4vram.threadsvault.ui.detail.PostDetailViewModel
import com.d4vram.threadsvault.ui.settings.SettingsViewModel
import com.d4vram.threadsvault.ui.vault.VaultScreen
import com.d4vram.threadsvault.ui.vault.VaultViewModel
import com.d4vram.threadsvault.ui.settings.SettingsScreen
import com.d4vram.threadsvault.ui.theme.ThreadsVaultTheme
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val navController = rememberNavController()
            val vaultViewModel = remember { VaultViewModel(applicationContext) }
            val settingsViewModel = remember { SettingsViewModel(applicationContext) }
            val vaultUiState by vaultViewModel.uiState.collectAsState()
            val searchQuery by vaultViewModel.searchQuery.collectAsState()
            val categories by vaultViewModel.categories.collectAsState()
            val selectedCategory by vaultViewModel.currentCategory.collectAsState()
            val themeMode by settingsViewModel.themeMode.collectAsState()
            val settingsCategories by settingsViewModel.categories.collectAsState()
            var showManualAdd by remember { mutableStateOf(false) }
            val restoreLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.OpenDocument()
            ) { uri ->
                if (uri != null) {
                    settingsViewModel.restoreJson(uri)
                }
            }

            ThreadsVaultTheme(themeMode = themeMode) {
                LaunchedEffect(settingsViewModel) {
                    settingsViewModel.exportEvents.collectLatest { event ->
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = event.mimeType
                            putExtra(Intent.EXTRA_STREAM, event.uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        startActivity(Intent.createChooser(shareIntent, event.title))
                    }
                }
                LaunchedEffect(settingsViewModel) {
                    settingsViewModel.messageEvents.collectLatest { message ->
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    }
                }

                NavHost(startDestination = AppRoute.VAULT, navController = navController) {
                    composable(AppRoute.VAULT) {
                        VaultScreen(
                            title = stringResource(id = R.string.app_name),
                            searchText = searchQuery,
                            categories = categories,
                            selectedCategory = selectedCategory,
                            uiState = vaultUiState,
                            onSearchTextChange = vaultViewModel::onSearchTextChange,
                            onSelectCategory = vaultViewModel::onCategorySelected,
                            onToggleFavorito = vaultViewModel::toggleFavorito,
                            onDeletePost = vaultViewModel::borrarPost,
                            onRestorePost = vaultViewModel::restaurarPost,
                            onEditNotes = vaultViewModel::actualizarNotas,
                            onEditCategories = vaultViewModel::actualizarCategorias,
                            onRetryExtraction = vaultViewModel::reextraerContenido,
                            onAddCategory = vaultViewModel::addCategory,
                            onDeleteCategory = vaultViewModel::deleteCategory,
                            onOpenPost = { postId ->
                                navController.navigate("${AppRoute.DETAIL}/$postId")
                            },
                            onSearchAction = {},
                            onManualAdd = { showManualAdd = true }
                        )
                    }
                    composable(
                        route = "${AppRoute.DETAIL}/{postId}",
                        arguments = listOf(navArgument("postId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val postId = backStackEntry.arguments?.getLong("postId") ?: 0L
                        val detailViewModel = remember(postId) {
                            PostDetailViewModel(applicationContext, postId)
                        }
                        val detailUiState by detailViewModel.uiState.collectAsState()
                        PostDetailScreen(uiState = detailUiState)
                    }
                    composable(AppRoute.SETTINGS) {
                        SettingsScreen(
                            title = stringResource(id = R.string.settings_title),
                            themeMode = themeMode,
                            categories = settingsCategories,
                            onThemeModeChange = settingsViewModel::setThemeMode,
                            onAddCategory = settingsViewModel::addCategory,
                            onDeleteCategory = settingsViewModel::deleteCategory,
                            onExportCsv = settingsViewModel::exportCsv,
                            onExportPdf = settingsViewModel::exportPdf,
                            onBackupJson = settingsViewModel::backupJson,
                            onRestoreJson = {
                                restoreLauncher.launch(arrayOf("application/json", "text/plain"))
                            }
                        )
                    }
                }

                if (showManualAdd) {
                    ManualAddDialog(
                        onDismiss = { showManualAdd = false },
                        onConfirm = { url ->
                            vaultViewModel.guardarManualUrl(url)
                            showManualAdd = false
                        }
                    )
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun ManualAddDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var url by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(id = R.string.manual_add_placeholder)) },
        text = {
            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text(text = stringResource(id = R.string.url_label)) }
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(url) }) {
                Text(text = stringResource(id = R.string.save_in_threadsvault))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.cancel_action))
            }
        }
    )
}

private object AppRoute {
    const val VAULT = "vault"
    const val DETAIL = "detail"
    const val SETTINGS = "settings"
}
