package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = SunGold,
    onPrimary = SpaceBlack,
    primaryContainer = CosmicNavy,
    onPrimaryContainer = SunGold,
    secondary = IceWallCyan,
    onSecondary = SpaceBlack,
    secondaryContainer = SurfaceVariantDark,
    onSecondaryContainer = IceBlue,
    tertiary = AccentTeal,
    onTertiary = SpaceBlack,
    background = SpaceBlack,
    onBackground = TextPrimary,
    surface = DeepSpace,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceDark,
    onSurfaceVariant = TextSecondary,
    outline = GridCyan
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
