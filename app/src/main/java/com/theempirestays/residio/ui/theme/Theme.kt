package com.theempirestays.residio.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Ink = Color(0xFF2A2A2C)
val Paper = Color(0xFFE9E9EA)
val Gold = Color(0xFFB08D57)
val Cloud = Color(0xFFF6F6F7)

private val LightColors = lightColorScheme(
    primary = Ink, onPrimary = Color.White,
    secondary = Gold, onSecondary = Color.White,
    background = Paper, onBackground = Ink,
    surface = Color.White, onSurface = Ink
)

private val DarkColors = darkColorScheme(
    primary = Gold, onPrimary = Ink,
    secondary = Gold, background = Ink, surface = Color(0xFF1E1E20)
)

@Composable
fun ResidioTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}
