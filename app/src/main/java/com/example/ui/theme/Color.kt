package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Cyber Black & Blue Palette
val CyberVoid = Color(0xFF040711)
val CyberSurface = Color(0xFF0A1020)
val CyberSurfaceVariant = Color(0xFF101A30)
val CyberCardBorder = Color(0xFF1E3A66)

// Neon & Accent Colors
val CyberCyan = Color(0xFF00E5FF)
val CyberNeonBlue = Color(0xFF0066FF)
val CyberElectricBlue = Color(0xFF1E88E5)
val CyberViolet = Color(0xFF7C4DFF)
val CyberDeepPurple = Color(0xFF4A148C)
val CyberMatrixGreen = Color(0xFF00E676)
val CyberWarning = Color(0xFFFFB300)
val CyberDanger = Color(0xFFFF3D71)

// Text & Foreground
val CyberTextPrimary = Color(0xFFF0F6FC)
val CyberTextSecondary = Color(0xFF8B9CB5)
val CyberTextMuted = Color(0xFF536785)

// Animated Gradient Brushes
val CyberGradientBrush = Brush.linearGradient(
    colors = listOf(CyberCyan, CyberNeonBlue, CyberViolet)
)

val CyberShieldGlowBrush = Brush.radialGradient(
    colors = listOf(CyberCyan.copy(alpha = 0.45f), CyberNeonBlue.copy(alpha = 0.15f), Color.Transparent)
)

val CyberBarGradient = Brush.horizontalGradient(
    colors = listOf(CyberSurfaceVariant, Color(0xFF0D172A), CyberSurfaceVariant)
)
