package com.example.kidtubelock.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = BrandYellow,
    onPrimary = BrandNavyDeep,
    primaryContainer = BrandCardAlt,
    onPrimaryContainer = Color.White,
    secondary = BrandSky,
    onSecondary = BrandNavyDeep,
    secondaryContainer = BrandGreen,
    onSecondaryContainer = Color.White,
    tertiary = BrandBlue,
    onTertiary = Color.White,
    tertiaryContainer = BrandBlueDark,
    onTertiaryContainer = Color.White,
    background = BrandNavyDeep,
    onBackground = Color.White,
    surface = BrandCard,
    onSurface = Color.White,
    surfaceVariant = BrandCardAlt,
    onSurfaceVariant = BrandTextMuted,
    outline = BrandOutline,
    outlineVariant = BrandOutline.copy(alpha = 0.55f),
    error = Color(0xFFFF8A80),
    onError = BrandNavyDeep,
)

private val LightColors = lightColorScheme(
    primary = BrandYellow,
    onPrimary = BrandNavyDeep,
    primaryContainer = BrandCardAlt,
    onPrimaryContainer = Color.White,
    secondary = BrandSky,
    onSecondary = BrandNavyDeep,
    secondaryContainer = BrandGreen,
    onSecondaryContainer = Color.White,
    tertiary = BrandBlue,
    onTertiary = Color.White,
    tertiaryContainer = BrandBlueDark,
    onTertiaryContainer = Color.White,
    background = BrandNavyDeep,
    onBackground = Color.White,
    surface = BrandCard,
    onSurface = Color.White,
    surfaceVariant = BrandCardAlt,
    onSurfaceVariant = BrandTextMuted,
    outline = BrandOutline,
    outlineVariant = BrandOutline.copy(alpha = 0.55f),
    error = Color(0xFFFF8A80),
    onError = BrandNavyDeep,
)

@Composable
fun KidTubeLockTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content,
    )
}
