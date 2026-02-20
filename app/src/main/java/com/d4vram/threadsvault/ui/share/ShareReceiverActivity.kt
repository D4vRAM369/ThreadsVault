package com.d4vram.threadsvault.ui.share

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.d4vram.threadsvault.ui.theme.ThreadsVaultTheme

class ShareReceiverActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedUrl = extractSharedText(intent).orEmpty()
        if (sharedUrl.isBlank()) {
            finish()
            return
        }

        val viewModel = ShareViewModel(applicationContext)

        setContent {
            ThreadsVaultTheme {
                val categories by viewModel.categories.collectAsState()
                val saveState by viewModel.saveState.collectAsState()

                LaunchedEffect(saveState) {
                    if (saveState is ShareSaveState.Saved) {
                        finish()
                    }
                }

                ShareScreen(
                    sharedUrl = sharedUrl,
                    categories = categories,
                    saveState = saveState,
                    onSave = { notes, category ->
                        viewModel.guardarSharedUrl(
                            url = sharedUrl,
                            notas = notes,
                            categoria = category
                        )
                    },
                    onCancel = { finish() }
                )
            }
        }
    }

    private fun extractSharedText(intent: Intent?): String? {
        if (intent?.action != Intent.ACTION_SEND) return null
        if (intent.type != "text/plain") return null
        return intent.getStringExtra(Intent.EXTRA_TEXT)
    }
}
