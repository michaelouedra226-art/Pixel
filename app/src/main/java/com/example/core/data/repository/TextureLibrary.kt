package com.example.core.data.repository

import com.example.core.engine.model.GradientDef
import com.example.core.engine.model.GradientType

data class TexturePreset(
    val id: String,
    val name: String,
    val description: String,
    val primaryColor: Long,
    val gradient: GradientDef
)

object TextureLibrary {
    val textures = listOf(
        TexturePreset(
            id = "gold_foil",
            name = "Feuille d'Or",
            description = "Reflets dorés métalliques riches",
            primaryColor = 0xFFD4AF37,
            gradient = GradientDef(
                type = GradientType.LINEAR,
                colors = listOf(0xFFFFF9C4, 0xFFFFD54F, 0xFFFFB300, 0xFFFFE082, 0xFFFF8F00),
                angle = 45f,
                stops = listOf(0f, 0.25f, 0.5f, 0.75f, 1f)
            )
        ),
        TexturePreset(
            id = "liquid_chrome",
            name = "Chrome Liquide",
            description = "Acier inoxydable miroir et reflets argent",
            primaryColor = 0xFFECEFF1,
            gradient = GradientDef(
                type = GradientType.LINEAR,
                colors = listOf(0xFFFFFFFF, 0xFFCFD8DC, 0xFF78909C, 0xFFECEFF1, 0xFF37474F),
                angle = 135f,
                stops = listOf(0f, 0.2f, 0.5f, 0.8f, 1f)
            )
        ),
        TexturePreset(
            id = "carbon_dark",
            name = "Carbone Forgé",
            description = "Structure composite sombre sport",
            primaryColor = 0xFF212121,
            gradient = GradientDef(
                type = GradientType.LINEAR,
                colors = listOf(0xFF323238, 0xFF18181C, 0xFF2B2B30, 0xFF0F0F12),
                angle = 45f,
                stops = listOf(0f, 0.33f, 0.66f, 1f)
            )
        ),
        TexturePreset(
            id = "rose_gold",
            name = "Or Rose Royal",
            description = "Reflets cuivre rose et champagne",
            primaryColor = 0xFFF48FB1,
            gradient = GradientDef(
                type = GradientType.LINEAR,
                colors = listOf(0xFFFFCDD2, 0xFFF48FB1, 0xFFC2185B, 0xFFFF80AB),
                angle = 60f,
                stops = listOf(0f, 0.35f, 0.7f, 1f)
            )
        ),
        TexturePreset(
            id = "neon_hologram",
            name = "Hologramme Néon",
            description = "Prisme iridescent violet cyan",
            primaryColor = 0xFF80D8FF,
            gradient = GradientDef(
                type = GradientType.LINEAR,
                colors = listOf(0xFF00E5FF, 0xFF7C4DFF, 0xFFFF4081, 0xFF69F0AE),
                angle = 90f,
                stops = listOf(0f, 0.33f, 0.66f, 1f)
            )
        ),
        TexturePreset(
            id = "dark_marble",
            name = "Marbre Noir & Or",
            description = "Roche sédimentaire noire aux veines or",
            primaryColor = 0xFF141416,
            gradient = GradientDef(
                type = GradientType.LINEAR,
                colors = listOf(0xFF0A0A0C, 0xFF1F1F26, 0xFFD4AF37, 0xFF121215),
                angle = 120f,
                stops = listOf(0f, 0.45f, 0.5f, 1f)
            )
        ),
        TexturePreset(
            id = "emerald_mineral",
            name = "Émeraude Brute",
            description = "Cristal minéral vert profond",
            primaryColor = 0xFF00E676,
            gradient = GradientDef(
                type = GradientType.LINEAR,
                colors = listOf(0xFFA7FFEB, 0xFF00BFA5, 0xFF004D40, 0xFF64FFDA),
                angle = 45f,
                stops = listOf(0f, 0.3f, 0.7f, 1f)
            )
        )
    )
}
