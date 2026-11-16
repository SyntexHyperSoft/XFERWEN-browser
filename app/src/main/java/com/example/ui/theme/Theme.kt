package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val CyberColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = Color(0xFF001F28),
    primaryContainer = CyberNeonBlue,
    onPrimaryContainer = Color.White,
    secondary = CyberElectricBlue,
    onSecondary = Color.White,
    secondaryContainer = CyberSurfaceVariant,
    onSecondaryContainer = CyberTextPrimary,
    tertiary = CyberViolet,
    onTertiary = Color.White,
    background = CyberVoid,
    onBackground = CyberTextPrimary,
    surface = CyberSurface,
    onSurface = CyberTextPrimary,
    surfaceVariant = CyberSurfaceVariant,
    onSurfaceVariant = CyberTextSecondary,
    outline = CyberCardBorder,
    error = CyberDanger,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = CyberVoid.toArgb()
            window.navigationBarColor = CyberVoid.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = CyberColorScheme,
        typography = Typography,
        content = content
    )
}
