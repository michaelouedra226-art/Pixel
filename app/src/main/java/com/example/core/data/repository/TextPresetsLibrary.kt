package com.example.core.data.repository

import com.example.core.engine.model.ColorFill
import com.example.core.engine.model.EmbossDef
import com.example.core.engine.model.GlowDef
import com.example.core.engine.model.GradientDef
import com.example.core.engine.model.GradientType
import com.example.core.engine.model.InnerShadowDef
import com.example.core.engine.model.Layer3DEffect
import com.example.core.engine.model.ShadowDef
import com.example.core.engine.model.StrokeDef
import com.example.core.engine.model.TextLayer

data class TextStylePreset(
    val id: String,
    val name: String,
    val description: String,
    val applyToLayer: (TextLayer) -> TextLayer
)

object TextPresetsLibrary {
    val presets = listOf(
        TextStylePreset(
            id = "gold_3d",
            name = "Or Impérial 3D",
            description = "Dégradé doré royal avec relief et biseau",
            applyToLayer = { layer ->
                layer.copy(
                    fill = ColorFill(
                        solidColor = 0xFFFFD700,
                        gradient = GradientDef(
                            type = GradientType.LINEAR,
                            colors = listOf(0xFFFFF6D1, 0xFFFFD700, 0xFFAA800A),
                            angle = 45f
                        ),
                        isGradient = true
                    ),
                    stroke = StrokeDef(isEnabled = true, color = 0xFF553D00, width = 2.5f),
                    effect3D = Layer3DEffect(isEnabled = true, depth = 14f, color = 0xFF422E00, lightAngle = 60f),
                    shadow = ShadowDef(isEnabled = true, color = 0xAA000000, radius = 10f, dx = 4f, dy = 8f),
                    emboss = EmbossDef(isEnabled = true, lightAngle = 60f, intensity = 70f, specularHardness = 30f)
                )
            }
        ),
        TextStylePreset(
            id = "chrome_metal",
            name = "Chrome Métallique 3D",
            description = "Reflet miroir argenté et contour haute précision",
            applyToLayer = { layer ->
                layer.copy(
                    fill = ColorFill(
                        solidColor = 0xFFECEFF1,
                        gradient = GradientDef(
                            type = GradientType.LINEAR,
                            colors = listOf(0xFFFFFFFF, 0xFFCFD8DC, 0xFF455A64, 0xFFECEFF1),
                            angle = 90f
                        ),
                        isGradient = true
                    ),
                    stroke = StrokeDef(isEnabled = true, color = 0xFF263238, width = 2.5f),
                    effect3D = Layer3DEffect(isEnabled = true, depth = 12f, color = 0xFF1C2529, lightAngle = 45f),
                    shadow = ShadowDef(isEnabled = true, color = 0x88000000, radius = 8f, dx = 2f, dy = 6f),
                    innerShadow = InnerShadowDef(isEnabled = true, color = 0x88000000, radius = 6f, dx = 1f, dy = 2f)
                )
            }
        ),
        TextStylePreset(
            id = "cyber_neon",
            name = "Néon Cyberpunk",
            description = "Lueur rose cyan vibrante avec ombre portée fluo",
            applyToLayer = { layer ->
                layer.copy(
                    fill = ColorFill(
                        solidColor = 0xFF00E5FF,
                        gradient = GradientDef(
                            type = GradientType.LINEAR,
                            colors = listOf(0xFF00E5FF, 0xFFFF007F),
                            angle = 45f
                        ),
                        isGradient = true
                    ),
                    stroke = StrokeDef(isEnabled = true, color = 0xFFFFFFFF, width = 1.5f),
                    glow = GlowDef(isEnabled = true, color = 0xFFFF007F, radius = 24f, opacity = 0.9f),
                    shadow = ShadowDef(isEnabled = true, color = 0xDD00E5FF, radius = 18f, dx = 0f, dy = 0f),
                    effect3D = Layer3DEffect(isEnabled = false)
                )
            }
        ),
        TextStylePreset(
            id = "retro_80s",
            name = "Synthwave 80s",
            description = "Dégradé coucher de soleil et extrusion inclinée",
            applyToLayer = { layer ->
                layer.copy(
                    fill = ColorFill(
                        solidColor = 0xFFFF5252,
                        gradient = GradientDef(
                            type = GradientType.LINEAR,
                            colors = listOf(0xFFFFEA00, 0xFFFF3D00, 0xFF9C27B0),
                            angle = 90f
                        ),
                        isGradient = true
                    ),
                    stroke = StrokeDef(isEnabled = true, color = 0xFF0D0D12, width = 3f),
                    effect3D = Layer3DEffect(isEnabled = true, depth = 16f, color = 0xFF311B92, lightAngle = 45f),
                    shadow = ShadowDef(isEnabled = true, color = 0x99000000, radius = 12f, dx = 6f, dy = 10f)
                )
            }
        ),
        TextStylePreset(
            id = "royal_marble",
            name = "Marbre & Or Noir",
            description = "Typographie luxe sombre cerclée d'or champagne",
            applyToLayer = { layer ->
                layer.copy(
                    fill = ColorFill(
                        solidColor = 0xFF1E1E24,
                        gradient = GradientDef(
                            type = GradientType.LINEAR,
                            colors = listOf(0xFF2E2E38, 0xFF141418),
                            angle = 45f
                        ),
                        isGradient = true
                    ),
                    stroke = StrokeDef(isEnabled = true, color = 0xFFD4AF37, width = 3f),
                    shadow = ShadowDef(isEnabled = true, color = 0xAA000000, radius = 14f, dx = 0f, dy = 8f),
                    emboss = EmbossDef(isEnabled = true, lightAngle = 45f, intensity = 60f)
                )
            }
        ),
        TextStylePreset(
            id = "emerald_glow",
            name = "Émeraude Cristalline",
            description = "Vert émeraude précieux avec lueur étincelante",
            applyToLayer = { layer ->
                layer.copy(
                    fill = ColorFill(
                        solidColor = 0xFF00E676,
                        gradient = GradientDef(
                            type = GradientType.LINEAR,
                            colors = listOf(0xFFB9F6CA, 0xFF00E676, 0xFF004D40),
                            angle = 45f
                        ),
                        isGradient = true
                    ),
                    stroke = StrokeDef(isEnabled = true, color = 0xFF00251A, width = 2f),
                    glow = GlowDef(isEnabled = true, color = 0xFF00E676, radius = 20f, opacity = 0.7f),
                    shadow = ShadowDef(isEnabled = true, color = 0x88000000, radius = 10f, dx = 3f, dy = 6f),
                    effect3D = Layer3DEffect(isEnabled = true, depth = 8f, color = 0xFF00332C, lightAngle = 45f)
                )
            }
        ),
        TextStylePreset(
            id = "crimson_fire",
            name = "Flamme Carmin 3D",
            description = "Rouge rubis incandescent avec ombre puissante",
            applyToLayer = { layer ->
                layer.copy(
                    fill = ColorFill(
                        solidColor = 0xFFFF1744,
                        gradient = GradientDef(
                            type = GradientType.LINEAR,
                            colors = listOf(0xFFFF8A80, 0xFFFF1744, 0xFF880E4F),
                            angle = 90f
                        ),
                        isGradient = true
                    ),
                    stroke = StrokeDef(isEnabled = true, color = 0xFF3E0415, width = 2.5f),
                    effect3D = Layer3DEffect(isEnabled = true, depth = 12f, color = 0xFF2A000A, lightAngle = 45f),
                    shadow = ShadowDef(isEnabled = true, color = 0xAAFF1744, radius = 16f, dx = 0f, dy = 4f)
                )
            }
        )
    )
}
