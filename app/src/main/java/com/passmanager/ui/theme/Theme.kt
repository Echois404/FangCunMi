package com.passmanager.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Pink,
    onPrimary = BgSecondary,
    primaryContainer = PinkSoft,
    secondary = Lavender,
    onSecondary = TextPrimary,
    secondaryContainer = LavenderSoft,
    background = BgPrimary,
    onBackground = TextPrimary,
    surface = BgSecondary,
    onSurface = TextPrimary,
    surfaceVariant = BgCard,
    onSurfaceVariant = TextSecondary,
    outline = GrayMedium,
    outlineVariant = GrayLight,
    error = Danger,
    onError = BgSecondary
)

@Composable
fun SecretAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = AppTypography,
        content = content
    )
}
