# Design: "Cómo usar" ModalBottomSheet

**Date:** 2026-02-23
**Status:** Approved
**Feature:** How-to tutorial accessible from Empty State and Settings

---

## Context

ThreadsVault already has a 4-page onboarding flow shown once on first install. After dismissing onboarding, new users land on an empty vault with no further guidance. This feature adds a persistent, re-accessible tutorial so users always have a reference on how to use the app.

---

## Goals

- Help new users reach the "aha moment" (first saved post) faster
- Provide a permanent re-accessible tutorial (not just first-run)
- Keep it minimal: no new ViewModel, no DataStore, no DB changes

---

## Decisions

| Question | Decision | Rationale |
|---|---|---|
| Entry points | Empty State chip + Settings ListItem | Two natural discovery points; no TopBar clutter |
| Format | ModalBottomSheet (M3) with scrollable steps | Native Android pattern, no gesture conflicts, most screen real estate |
| Number of steps | 3 | Concise, not overwhelming |
| State management | UI-only `remember { mutableStateOf(false) }` | No persistence needed |

---

## Architecture

```
EmptyVaultState
  └── SuggestionChip "¿Cómo funciona?"  ──── onShowHowToUse()
                                                    │
SettingsScreen                                      ▼
  └── ListItem "Guía de uso"  ─────────► HowToUseBottomSheet
                                          (ModalBottomSheet M3)
```

State (`showHowToUse: Boolean`) lives in `VaultScreen` via `remember`.
Settings screen receives `onShowHowToUse: () -> Unit` callback from `MainActivity`.

---

## UI Spec

### HowToUseBottomSheet

- `skipPartiallyExpanded = true` — expands fully on open
- Background: `surfaceContainerLow`
- Handle: default M3 drag handle

**Header:**
- App icon via `AsyncImage(model = R.mipmap.ic_launcher_round)` (32dp, Coil)
- Title: `"Cómo usar ThreadsVault"` — `titleLarge`, `onSurface`

**3 Step Cards (vertical list, no pager):**

Each step row:
- Number badge: filled circle `primary`, size 28dp, number `labelMedium` `onPrimary`
- Icon: Material3 outlined icon, `primary` tint, 24dp
- Title: `titleSmall`, `onSurface`
- Description: `bodyMedium`, `onSurfaceVariant`
- Separator: `HorizontalDivider` with `alpha = 0.3f`

| Step | Icon | Title | Description |
|---|---|---|---|
| 1 | `Icons.Outlined.Share` | Guarda desde Threads | Abre cualquier post en Threads, toca el botón Compartir y selecciona ThreadsVault. El contenido se extrae automáticamente. |
| 2 | `Icons.Outlined.EditNote` | Añade manualmente | Pulsa el botón "+ Agregar manualmente" e introduce la URL del post directamente. |
| 3 | `Icons.Outlined.Category` | Organiza y filtra | Crea categorías con emoji y color. Usa #hashtags en notas para clasificar y buscar al instante. |

**Footer:**
- `FilledButton`: `"¡Entendido!"` → `onDismiss()`
- Width: `fillMaxWidth`, shape: `RoundedCornerShape(14.dp)`

---

### Trigger in EmptyVaultState

```
SuggestionChip(
    onClick = onShowHowToUse,
    label = { Text("¿Cómo funciona?") },
    icon = { Icon(Icons.Outlined.HelpOutline, ...) }
)
```

Placed below the description text, centered. Color: `secondary` tinted border.

---

### Settings Entry

```
ListItem(
    headlineContent = { Text("Guía de uso") },
    supportingContent = { Text("Aprende a sacar el máximo a ThreadsVault") },
    leadingContent = { Icon(Icons.Outlined.MenuBook, tint = primary) },
    trailingContent = { Icon(Icons.Default.ChevronRight) },
    modifier = Modifier.clickable { onShowHowToUse() }
)
```

---

## Files to Touch

| File | Change |
|---|---|
| `ui/vault/VaultScreen.kt` | Add `HowToUseBottomSheet` composable, `SuggestionChip` in `EmptyVaultState`, `onShowHowToUse` param, state in `VaultScreen` |
| `ui/settings/SettingsScreen.kt` | Add `ListItem` with `onShowHowToUse` callback |
| `MainActivity.kt` | Wire callback from VaultScreen state into SettingsScreen |
| `res/values/strings.xml` | Spanish strings for all tutorial text |
| `res/values-en/strings.xml` | English strings |

**Estimated new code:** ~160–180 lines of Compose
**New files:** 0
**New dependencies:** 0

---

## Constraints & Notes

- `ModalBottomSheet` requires `@OptIn(ExperimentalMaterial3Api::class)` — already present in VaultScreen
- App icon MUST use `AsyncImage(model = R.mipmap.ic_launcher_round)` via Coil (not `painterResource` — crashes on adaptive icons)
- `HowToUseBottomSheet` is a private composable within `VaultScreen.kt` to keep file colocation
