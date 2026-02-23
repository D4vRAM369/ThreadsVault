# HowToUse BottomSheet — Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add a "¿Cómo funciona?" chip to the empty state and a "Guía de uso" ListItem in Settings, both opening a `ModalBottomSheet` with 3 how-to steps.

**Architecture:** `HowToUseBottomSheet` lives in its own file (`ui/components/HowToUseBottomSheet.kt`) and is imported by both `VaultScreen.kt` and `SettingsScreen.kt`. Each screen manages its own `showHowToUse: Boolean` state locally via `remember { mutableStateOf(false) }` — no ViewModel, no DataStore. `EmptyVaultState` receives a new `onShowHowToUse: () -> Unit` callback and renders a `SuggestionChip`. `SettingsScreen` receives a new `onShowHowToUse: () -> Unit` param wired from `MainActivity`.

**Tech Stack:** Jetpack Compose, Material 3 (`ModalBottomSheet`, `SuggestionChip`, `ListItem`), Coil (`AsyncImage`), `@OptIn(ExperimentalMaterial3Api::class)`

---

## Task 1: Add string resources

**Files:**
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-en/strings.xml`

No tests for string resources — visual verification in Task 5.

**Step 1: Add Spanish strings**

Open `app/src/main/res/values/strings.xml` and add these entries before the closing `</resources>` tag:

```xml
<!-- HowToUse BottomSheet -->
<string name="howto_chip_label">¿Cómo funciona?</string>
<string name="howto_title">Cómo usar ThreadsVault</string>
<string name="howto_step1_title">Guarda desde Threads</string>
<string name="howto_step1_desc">Abre cualquier post en Threads, toca el botón Compartir y selecciona ThreadsVault. El contenido se extrae automáticamente.</string>
<string name="howto_step2_title">Añade manualmente</string>
<string name="howto_step2_desc">Pulsa el botón "+ Agregar manualmente" e introduce la URL del post directamente.</string>
<string name="howto_step3_title">Organiza y filtra</string>
<string name="howto_step3_desc">Crea categorías con emoji y color. Usa #hashtags en tus notas para clasificar y buscar al instante.</string>
<string name="howto_cta">¡Entendido!</string>
<string name="howto_settings_label">Guía de uso</string>
<string name="howto_settings_hint">Aprende a sacar el máximo a ThreadsVault</string>
```

**Step 2: Add English strings**

Open `app/src/main/res/values-en/strings.xml` and add before `</resources>`:

```xml
<!-- HowToUse BottomSheet -->
<string name="howto_chip_label">How does it work?</string>
<string name="howto_title">How to use ThreadsVault</string>
<string name="howto_step1_title">Save from Threads</string>
<string name="howto_step1_desc">Open any post in Threads, tap the Share button and select ThreadsVault. Content is extracted automatically.</string>
<string name="howto_step2_title">Add manually</string>
<string name="howto_step2_desc">Tap the "+ Add manually" button and paste the post URL directly.</string>
<string name="howto_step3_title">Organize and filter</string>
<string name="howto_step3_desc">Create categories with emoji and color. Use #hashtags in your notes to tag and search instantly.</string>
<string name="howto_cta">Got it!</string>
<string name="howto_settings_label">Usage guide</string>
<string name="howto_settings_hint">Learn how to get the most out of ThreadsVault</string>
```

**Step 3: Commit**

```bash
git add app/src/main/res/values/strings.xml app/src/main/res/values-en/strings.xml
git commit -m "feat: add string resources for HowToUse BottomSheet"
```

---

## Task 2: Create `HowToUseBottomSheet` composable

**Files:**
- Create: `app/src/main/java/com/d4vram/threadsvault/ui/components/HowToUseBottomSheet.kt`

This composable is shared between VaultScreen and SettingsScreen. It is self-contained.

**Step 1: Create the file with complete implementation**

Create `app/src/main/java/com/d4vram/threadsvault/ui/components/HowToUseBottomSheet.kt`:

```kotlin
package com.d4vram.threadsvault.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.d4vram.threadsvault.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HowToUseBottomSheet(onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Header: app icon + title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AsyncImage(
                    model = R.mipmap.ic_launcher_round,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = stringResource(R.string.howto_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Steps data
            data class HowToStep(val icon: ImageVector, val titleRes: Int, val descRes: Int)
            val steps = listOf(
                HowToStep(Icons.Outlined.Share,     R.string.howto_step1_title, R.string.howto_step1_desc),
                HowToStep(Icons.Outlined.EditNote,  R.string.howto_step2_title, R.string.howto_step2_desc),
                HowToStep(Icons.Outlined.Category,  R.string.howto_step3_title, R.string.howto_step3_desc)
            )

            steps.forEachIndexed { index, step ->
                HowToStepRow(
                    stepNumber = index + 1,
                    icon = step.icon,
                    title = stringResource(step.titleRes),
                    description = stringResource(step.descRes)
                )
                if (index < steps.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 2.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // CTA button
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = stringResource(R.string.howto_cta),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun HowToStepRow(
    stepNumber: Int,
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Numbered circle badge
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        // Step icon
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(24.dp)
                .padding(top = 2.dp)
        )

        // Title + description
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
        }
    }
}
```

**Step 2: Build check**

In Android Studio: Build → Make Project (or `Ctrl+F9`).
Expected: no compile errors in the new file.

**Step 3: Commit**

```bash
git add app/src/main/java/com/d4vram/threadsvault/ui/components/HowToUseBottomSheet.kt
git commit -m "feat: add HowToUseBottomSheet composable"
```

---

## Task 3: Wire `HowToUseBottomSheet` into `VaultScreen`

**Files:**
- Modify: `app/src/main/java/com/d4vram/threadsvault/ui/vault/VaultScreen.kt`

There are two changes:
1. Add `onShowHowToUse` param + `SuggestionChip` to `EmptyVaultState`
2. Add `showHowToUse` state + `BottomSheet` render to the main `VaultScreen` body

**Step 1: Add import for `HowToUseBottomSheet`**

In VaultScreen.kt, find the import block at the top. Add:

```kotlin
import com.d4vram.threadsvault.ui.components.HowToUseBottomSheet
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
```

**Step 2: Update `EmptyVaultState` signature**

Find:
```kotlin
@Composable
private fun EmptyVaultState(
    modifier: Modifier = Modifier,
    accentColor: androidx.compose.ui.graphics.Color? = null
)
```

Replace with:
```kotlin
@Composable
private fun EmptyVaultState(
    modifier: Modifier = Modifier,
    accentColor: androidx.compose.ui.graphics.Color? = null,
    onShowHowToUse: () -> Unit = {}
)
```

**Step 3: Add `SuggestionChip` inside `EmptyVaultState`**

Inside `EmptyVaultState`, find the `Spacer(modifier = Modifier.height(2.dp))` at the bottom of the Column. Add the chip **before** the Spacer:

```kotlin
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
```

**Step 4: Add `showHowToUse` state in `VaultScreen`**

In the body of the main `VaultScreen` composable, find where other `remember` state variables are declared (e.g., `var editNotesPost`, `var showCategoryDialog`, etc.). Add:

```kotlin
var showHowToUse by remember { mutableStateOf(false) }
```

**Step 5: Pass callback to `EmptyVaultState`**

Find the call site of `EmptyVaultState` inside the `VaultUiState.Empty` branch of the `Crossfade`. It currently looks like:

```kotlin
VaultUiState.Empty -> EmptyVaultState(accentColor = ...)
```

Replace with:

```kotlin
VaultUiState.Empty -> EmptyVaultState(
    accentColor = ...,
    onShowHowToUse = { showHowToUse = true }
)
```

**Step 6: Render the BottomSheet**

After all the existing `if (showXxxDialog)` blocks at the end of the `VaultScreen` composable body (after the `Scaffold`), add:

```kotlin
if (showHowToUse) {
    HowToUseBottomSheet(onDismiss = { showHowToUse = false })
}
```

**Step 7: Build check**

Build → Make Project. Expected: no errors.

**Step 8: Commit**

```bash
git add app/src/main/java/com/d4vram/threadsvault/ui/vault/VaultScreen.kt
git commit -m "feat: wire HowToUse chip and BottomSheet in VaultScreen"
```

---

## Task 4: Wire `HowToUseBottomSheet` into `SettingsScreen`

**Files:**
- Modify: `app/src/main/java/com/d4vram/threadsvault/ui/settings/SettingsScreen.kt`
- Modify: `app/src/main/java/com/d4vram/threadsvault/MainActivity.kt`

**Step 1: Add imports to SettingsScreen.kt**

```kotlin
import com.d4vram.threadsvault.ui.components.HowToUseBottomSheet
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.ListItem
import androidx.compose.foundation.clickable
```

**Step 2: Add `onShowHowToUse` param to `SettingsScreen`**

Find the `SettingsScreen` composable signature. The last parameter is `onOpenAboutDev: () -> Unit`. Add after it:

```kotlin
onShowHowToUse: () -> Unit,
```

**Step 3: Add `showHowToUse` internal state in SettingsScreen**

In the `SettingsScreen` body, near the top where other state vars live, add:

```kotlin
var showHowToUse by remember { mutableStateOf(false) }
```

**Step 4: Add "Guía de uso" ListItem**

Inside `SettingsScreen`, find the `SectionCard` that contains the "About dev" item (look for `onOpenAboutDev` usage or `about_dev_title` string). Add a new `ListItem` **before** the About Dev entry:

```kotlin
ListItem(
    headlineContent = {
        Text(
            text = stringResource(R.string.howto_settings_label),
            style = MaterialTheme.typography.bodyLarge
        )
    },
    supportingContent = {
        Text(
            text = stringResource(R.string.howto_settings_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    },
    leadingContent = {
        Icon(
            imageVector = Icons.Outlined.MenuBook,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
    },
    trailingContent = {
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    },
    modifier = Modifier.clickable { showHowToUse = true }
)
HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
```

**Step 5: Render BottomSheet in SettingsScreen**

At the end of the `SettingsScreen` composable body (after the `Scaffold` or at the same level as other dialogs), add:

```kotlin
if (showHowToUse) {
    HowToUseBottomSheet(onDismiss = { showHowToUse = false })
}
```

**Step 6: Wire `onShowHowToUse` in `MainActivity.kt`**

Open `MainActivity.kt`. Find the `composable(AppRoute.SETTINGS)` block where `SettingsScreen(...)` is called. Add the new parameter:

```kotlin
onShowHowToUse = { /* no-op: handled internally by SettingsScreen */ },
```

> **Note:** `SettingsScreen` manages its own `showHowToUse` state internally (Task 4 Step 3). The `onShowHowToUse` parameter here is wired but not needed — state lives inside SettingsScreen, not lifted to MainActivity. This matches the existing pattern of other dialog states in SettingsScreen.
>
> **Alternative (simpler):** If `onShowHowToUse` is not needed as an external param at all, remove it from the SettingsScreen signature and just keep `var showHowToUse by remember { mutableStateOf(false) }` + `HowToUseBottomSheet` fully self-contained. **Choose this simpler path.**

**Revised Step 2 (simpler):** Do NOT add `onShowHowToUse` to `SettingsScreen` signature. State is fully internal. No MainActivity change needed.

**Step 7: Build check**

Build → Make Project. Expected: no errors.

**Step 8: Commit**

```bash
git add app/src/main/java/com/d4vram/threadsvault/ui/settings/SettingsScreen.kt
git commit -m "feat: add HowToUse ListItem and BottomSheet to SettingsScreen"
```

---

## Task 5: Manual verification checklist

Run the app on device/emulator.

**Empty State flow:**
- [ ] Navigate to a category with no posts → empty state shows
- [ ] "¿Cómo funciona?" chip is visible below the description text
- [ ] Tap chip → `HowToUseBottomSheet` slides up from bottom
- [ ] Header shows app icon + "Cómo usar ThreadsVault"
- [ ] 3 steps visible with numbered circles, icons, title, description
- [ ] Tap "¡Entendido!" → sheet dismisses
- [ ] Drag down on sheet → sheet dismisses
- [ ] Tap outside sheet → sheet dismisses

**Settings flow:**
- [ ] Open Settings → "Guía de uso" ListItem visible with book icon
- [ ] Tap it → `HowToUseBottomSheet` slides up
- [ ] Same 3 steps, same CTA button
- [ ] Dismisses correctly

**Visual checks:**
- [ ] Number circles are `primary` colored
- [ ] Icons are `primary` tinted
- [ ] Descriptions are `onSurfaceVariant`
- [ ] Sheet background is `surfaceContainerLow`
- [ ] Sheet opens fully expanded (not half-expanded first)
- [ ] No clipping or overflow on small screens (test 360dp width)

---

## Task 6: Final commit and push

```bash
git log --oneline -5
git push origin main
```
