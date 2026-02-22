package com.d4vram.threadsvault.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.d4vram.threadsvault.data.preferences.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = VaultPrimary,
    onPrimary = Color.White,
    primaryContainer = VaultPrimaryDark,
    onPrimaryContainer = VaultPrimaryLight,
    secondary = VaultSecondary,
    onSecondary = Color.White,
    secondaryContainer = VaultSecondaryDark,
    onSecondaryContainer = VaultSecondaryLight,
    tertiary = VaultTertiary,
    onTertiary = Color.White,
    tertiaryContainer = VaultTertiaryDark,
    onTertiaryContainer = VaultTertiaryLight,
    error = VaultErrorDark,
    onError = Color.Black,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = VaultBackgroundDark,
    onBackground = VaultOnBackgroundDark,
    surface = VaultSurfaceDark,
    onSurface = VaultOnSurfaceDark,
    surfaceVariant = VaultSurfaceVariantDark,
    onSurfaceVariant = VaultOnSurfaceVariantDark,
    surfaceContainerLowest = VaultBackgroundDark,
    surfaceContainerLow = Color(0xFF151929),
    surfaceContainer = VaultSurfaceContainerDark,
    surfaceContainerHigh = VaultSurfaceContainerHighDark,
    surfaceContainerHighest = Color(0xFF30384D),
    outline = VaultOutlineDark,
    outlineVariant = VaultOutlineVariantDark,
    inverseSurface = VaultOnBackgroundDark,
    inverseOnSurface = VaultBackgroundDark,
    inversePrimary = VaultPrimaryDark
)

private val LightColorScheme = lightColorScheme(
    primary = VaultPrimaryDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFECE7FF),
    onPrimaryContainer = Color(0xFF2D1D6E),
    secondary = VaultSecondaryDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDDF3F8),
    onSecondaryContainer = VaultSecondaryDark,
    tertiary = VaultTertiaryDark,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF9DEEB),
    onTertiaryContainer = VaultTertiaryDark,
    error = VaultError,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF93000A),
    background = VaultBackgroundLight,
    onBackground = VaultOnBackgroundLight,
    surface = VaultSurfaceLight,
    onSurface = VaultOnSurfaceLight,
    surfaceVariant = VaultSurfaceVariantLight,
    onSurfaceVariant = VaultOnSurfaceVariantLight,
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF9FBFE),
    surfaceContainer = VaultSurfaceContainerLight,
    surfaceContainerHigh = VaultSurfaceContainerHighLight,
    surfaceContainerHighest = Color(0xFFE4EBF6),
    outline = VaultOutlineLight,
    outlineVariant = VaultOutlineVariantLight,
    inverseSurface = VaultOnBackgroundLight,
    inverseOnSurface = VaultBackgroundLight,
    inversePrimary = VaultPrimaryLight
)

val VaultShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun ThreadsVaultTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = VaultShapes,
        content = content
    )
}
