package com.security.apkanalyzer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Define color palette
private val md_theme_light_primary = Color(0xFF6750A4)
private val md_theme_light_onPrimary = Color(0xFFFFFFFF)
private val md_theme_light_primaryContainer = Color(0xFFEADDFF)
private val md_theme_light_secondary = Color(0xFF625B71)
private val md_theme_light_error = Color(0xFFB3261E)
private val md_theme_light_background = Color(0xFFFFFBFE)
private val md_theme_light_surface = Color(0xFFFFFBFE)

private val md_theme_dark_primary = Color(0xFFD0BCFF)
private val md_theme_dark_onPrimary = Color(0xFF381E72)
private val md_theme_dark_primaryContainer = Color(0xFF4F378B)
private val md_theme_dark_secondary = Color(0xFFCCC2DC)
private val md_theme_dark_error = Color(0xFFF2B8B5)
private val md_theme_dark_background = Color(0xFF1C1B1F)
private val md_theme_dark_surface = Color(0xFF1C1B1F)

private val LightColors = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    secondary = md_theme_light_secondary,
    error = md_theme_light_error,
    background = md_theme_light_background,
    surface = md_theme_light_surface,
)

private val DarkColors = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    secondary = md_theme_dark_secondary,
    error = md_theme_dark_error,
    background = md_theme_dark_background,
    surface = md_theme_dark_surface,
)

@Composable
fun ApkAnalyzerTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
