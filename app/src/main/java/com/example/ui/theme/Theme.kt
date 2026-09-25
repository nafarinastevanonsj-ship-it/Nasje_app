package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val UE5ColorScheme = darkColorScheme(
    primary = UE_AccentCyan,
    onPrimary = UE_Background,
    primaryContainer = UE_AccentBlue,
    onPrimaryContainer = UE_TextPrimary,
    secondary = UE_AccentGold,
    onSecondary = UE_Background,
    background = UE_Background,
    onBackground = UE_TextPrimary,
    surface = UE_Surface,
    onSurface = UE_TextPrimary,
    surfaceVariant = UE_SurfaceVariant,
    onSurfaceVariant = UE_TextSecondary,
    outline = UE_PanelBorder
)

@Composable
fun UE5Theme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = UE5ColorScheme,
        typography = Typography,
        content = content
    )
}
