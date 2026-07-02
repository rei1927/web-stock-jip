package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = NavyPrimary,
    onPrimary = Color.White,
    secondary = GoldAccent,
    onSecondary = Color.Black,
    background = DeepSlate,
    surface = SurfaceDark,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = NavyPrimary,
    onPrimary = Color.White,
    secondary = GoldAccent,
    onSecondary = Color.White,
    background = SoftBackground,
    surface = Color.White,
    onBackground = ContentPrimary,
    onSurface = ContentPrimary,
    surfaceVariant = SoftBackground,
    onSurfaceVariant = ContentSecondary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
