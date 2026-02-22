package com.d4vram.threadsvault.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Color
import com.d4vram.threadsvault.R
import java.util.Locale

@Composable
fun CategoryColorPickerDialog(
    initialHex: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var value by remember(initialHex) { mutableStateOf(initialHex) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.color_label)) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                label = { Text(stringResource(id = R.string.hex_color_label)) },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(normalizeHex(value)) }) {
                Text(stringResource(id = R.string.ok_action))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.cancel_action))
            }
        }
    )
}

fun parseHexColor(hex: String): Color = runCatching {
    Color(android.graphics.Color.parseColor(normalizeHex(hex)))
}.getOrDefault(Color(0xFF6D44E5))

private fun normalizeHex(value: String): String {
    val clean = value.trim().uppercase(Locale.ROOT)
    return if (Regex("^#[0-9A-F]{6}$").matches(clean)) clean else "#6D44E5"
}
