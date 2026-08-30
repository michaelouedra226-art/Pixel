package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Core Luxury Palette
val ObsidianBg = Color(0xFF0A0A0C)
val SurfaceDark = Color(0xFF141416)
val SurfaceElevated = Color(0xFF1A1A1D)
val SurfaceCard = Color(0xFF222226)
val SurfaceGlass = Color(0xCC141416)
val BorderGlass = Color(0x26FFFFFF)
val BorderGold = Color(0x4DD4AF37)

// Accents
val ChampagneGold = Color(0xFFD4AF37)
val ChampagneGoldLight = Color(0xFFF3E5AB)
val ChampagneGoldDark = Color(0xFF997A15)
val SoftPlatinum = Color(0xFFE5E4E2)

// Typography Colors
val TextPrimary = Color(0xFFF5F5F7)
val TextSecondary = Color(0xFFA1A1A6)
val TextMuted = Color(0xFF6B6B70)
val TextGold = Color(0xFFDFBA45)

// Status & Indicators
val EmeraldSuccess = Color(0xFF00E676)
val CrimsonAlert = Color(0xFFFF3366)
val CyanAccent = Color(0xFF00E5FF)
val ElectricPurple = Color(0xFF9D4EDD)

// Luxury Gradients
val GoldGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFF3E5AB), Color(0xFFD4AF37), Color(0xFFAA820A))
)

val DarkSurfaceGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF1A1A1D), Color(0xFF0E0E10))
)

val PlatinumGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFFFFFFF), Color(0xFFE5E4E2), Color(0xFFB0B0B5))
)

val NeonGlowGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF00E5FF), Color(0xFF9D4EDD))
)
