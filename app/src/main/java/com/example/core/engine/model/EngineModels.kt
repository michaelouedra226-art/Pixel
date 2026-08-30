package com.example.core.engine.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import java.util.UUID

enum class LayerType {
    TEXT,
    SHAPE,
    BEZIER,
    DRAWING,
    IMAGE
}

enum class ShapeType {
    RECTANGLE,
    ROUNDED_RECT,
    CIRCLE,
    OVAL,
    LINE,
    POLYGON,
    STAR,
    ARROW,
    HEART,
    BEZIER_CUSTOM
}

enum class GradientType {
    LINEAR,
    RADIAL
}

enum class BlendModeDef {
    SRC_OVER,
    MULTIPLY,
    SCREEN,
    OVERLAY,
    DARKEN,
    LIGHTEN,
    COLOR_DODGE,
    COLOR_BURN
}

data class GradientDef(
    val type: GradientType = GradientType.LINEAR,
    val colors: List<Long> = listOf(0xFFF3E5AB, 0xFFD4AF37, 0xFFAA820A),
    val angle: Float = 0f,
    val stops: List<Float> = listOf(0f, 0.5f, 1f)
)

data class ColorFill(
    val solidColor: Long = 0xFFFFFFFF,
    val gradient: GradientDef? = null,
    val isGradient: Boolean = false,
    val textureUri: String? = null,
    val textureName: String? = null,
    val textureScale: Float = 1f,
    val isTexture: Boolean = false
) {
    companion object {
        val White = ColorFill(solidColor = 0xFFFFFFFF)
        val Gold = ColorFill(
            solidColor = 0xFFD4AF37,
            gradient = GradientDef(),
            isGradient = true
        )
        val Black = ColorFill(solidColor = 0xFF0A0A0C)
        val Cyan = ColorFill(solidColor = 0xFF00E5FF)
        val Crimson = ColorFill(solidColor = 0xFFFF2A55)
        val Chrome = ColorFill(
            solidColor = 0xFFE0E0E0,
            gradient = GradientDef(
                colors = listOf(0xFFFFFFFF, 0xFFB0BEC5, 0xFF37474F, 0xFFECEFF1),
                angle = 45f
            ),
            isGradient = true
        )
    }
}

data class ShadowDef(
    val isEnabled: Boolean = false,
    val color: Long = 0x99000000,
    val radius: Float = 10f,
    val dx: Float = 4f,
    val dy: Float = 6f
)

data class InnerShadowDef(
    val isEnabled: Boolean = false,
    val color: Long = 0xAA000000,
    val radius: Float = 8f,
    val dx: Float = 2f,
    val dy: Float = 2f
)

data class GlowDef(
    val isEnabled: Boolean = false,
    val color: Long = 0xFFFFD700,
    val radius: Float = 16f,
    val opacity: Float = 0.8f
)

data class EmbossDef(
    val isEnabled: Boolean = false,
    val lightAngle: Float = 90f,
    val intensity: Float = 50f,
    val ambientLight: Float = 50f,
    val specularHardness: Float = 20f,
    val bevel: Float = 10f
)

data class StrokeDef(
    val isEnabled: Boolean = false,
    val color: Long = 0xFFD4AF37,
    val width: Float = 3f
)

data class Layer3DEffect(
    val isEnabled: Boolean = false,
    val depth: Float = 12f,
    val color: Long = 0xFF6D5618,
    val lightAngle: Float = 45f,
    val darken: Float = 0.35f,
    val isOblique: Boolean = false,
    val obliqueAngle: Float = 45f
)

data class TextCurvature(
    val isEnabled: Boolean = false,
    val bend: Float = 0f // -100 to 100 percentage
)

data class TextReflection(
    val isEnabled: Boolean = false,
    val opacity: Float = 0.35f,
    val distance: Float = 4f
)

data class ColorEraserDef(
    val isEnabled: Boolean = false,
    val targetColor: Long = 0xFFFFFFFF,
    val tolerance: Float = 25f,
    val smoothness: Float = 5f
)

data class MaskDef(
    val isEnabled: Boolean = false,
    val isInside: Boolean = true,
    val points: List<Offset> = emptyList()
)

data class AnchorPoint(
    val id: String = UUID.randomUUID().toString(),
    val position: Offset = Offset.Zero,
    val handleIn: Offset = Offset.Zero,
    val handleOut: Offset = Offset.Zero,
    val isSmooth: Boolean = true
)

data class DrawingPoint(
    val x: Float,
    val y: Float,
    val pressure: Float = 1f
)

data class DrawingStroke(
    val id: String = UUID.randomUUID().toString(),
    val points: List<DrawingPoint> = emptyList(),
    val color: Long = 0xFFD4AF37,
    val strokeWidth: Float = 8f,
    val opacity: Float = 1f,
    val isEraser: Boolean = false
)

data class Transform(
    val x: Float = 100f,
    val y: Float = 100f,
    val width: Float = 200f,
    val height: Float = 200f,
    val rotation: Float = 0f,
    val scaleX: Float = 1f,
    val scaleY: Float = 1f,
    val rotationX: Float = 0f, // 3D tilt X (-180 to 180)
    val rotationY: Float = 0f, // 3D tilt Y (-180 to 180)
    val perspective: Float = 0f // Perspective depth factor
) {
    val center: Offset get() = Offset(x + width / 2f, y + height / 2f)
}

sealed class Layer {
    abstract val id: String
    abstract val name: String
    abstract val isVisible: Boolean
    abstract val isLocked: Boolean
    abstract val opacity: Float
    abstract val blendMode: BlendModeDef
    abstract val transform: Transform
    abstract val zIndex: Int

    abstract fun copyWithTransform(transform: Transform): Layer
    abstract fun copyWithVisibility(isVisible: Boolean): Layer
    abstract fun copyWithLock(isLocked: Boolean): Layer
    abstract fun copyWithOpacity(opacity: Float): Layer
    abstract fun copyWithZIndex(zIndex: Int): Layer
    abstract fun copyWithName(name: String): Layer
    abstract fun copyWithBlendMode(blendMode: BlendModeDef): Layer
}

data class TextLayer(
    override val id: String = UUID.randomUUID().toString(),
    override val name: String = "Texte",
    override val isVisible: Boolean = true,
    override val isLocked: Boolean = false,
    override val opacity: Float = 1f,
    override val blendMode: BlendModeDef = BlendModeDef.SRC_OVER,
    override val transform: Transform = Transform(x = 100f, y = 200f, width = 320f, height = 110f),
    override val zIndex: Int = 0,
    val text: String = "PIXELFORGE",
    val fontSize: Float = 42f,
    val fontFamily: String = "Cinzel",
    val isBold: Boolean = true,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false,
    val letterSpacing: Float = 2f,
    val lineHeight: Float = 1.2f,
    val textAlign: String = "CENTER", // LEFT, CENTER, RIGHT
    val fill: ColorFill = ColorFill.Gold,
    val stroke: StrokeDef = StrokeDef(isEnabled = true, color = 0xFF0A0A0C, width = 2f),
    val shadow: ShadowDef = ShadowDef(isEnabled = true, color = 0xAA000000, radius = 8f, dx = 3f, dy = 5f),
    val innerShadow: InnerShadowDef = InnerShadowDef(),
    val glow: GlowDef = GlowDef(),
    val emboss: EmbossDef = EmbossDef(),
    val effect3D: Layer3DEffect = Layer3DEffect(),
    val curvature: TextCurvature = TextCurvature(),
    val reflection: TextReflection = TextReflection(),
    val mask: MaskDef = MaskDef()
) : Layer() {
    override fun copyWithTransform(transform: Transform) = copy(transform = transform)
    override fun copyWithVisibility(isVisible: Boolean) = copy(isVisible = isVisible)
    override fun copyWithLock(isLocked: Boolean) = copy(isLocked = isLocked)
    override fun copyWithOpacity(opacity: Float) = copy(opacity = opacity)
    override fun copyWithZIndex(zIndex: Int) = copy(zIndex = zIndex)
    override fun copyWithName(name: String) = copy(name = name)
    override fun copyWithBlendMode(blendMode: BlendModeDef) = copy(blendMode = blendMode)
}

data class ShapeLayer(
    override val id: String = UUID.randomUUID().toString(),
    override val name: String = "Forme",
    override val isVisible: Boolean = true,
    override val isLocked: Boolean = false,
    override val opacity: Float = 1f,
    override val blendMode: BlendModeDef = BlendModeDef.SRC_OVER,
    override val transform: Transform = Transform(x = 120f, y = 120f, width = 220f, height = 220f),
    override val zIndex: Int = 0,
    val shapeType: ShapeType = ShapeType.ROUNDED_RECT,
    val cornerRadius: Float = 24f,
    val polygonSides: Int = 5,
    val starPoints: Int = 5,
    val starInnerRadiusRatio: Float = 0.5f,
    val fill: ColorFill = ColorFill(solidColor = 0xFF1A1A1D, gradient = GradientDef(), isGradient = false),
    val stroke: StrokeDef = StrokeDef(isEnabled = true, color = 0xFFD4AF37, width = 3f),
    val shadow: ShadowDef = ShadowDef(isEnabled = true, color = 0x66000000, radius = 12f, dx = 0f, dy = 6f),
    val innerShadow: InnerShadowDef = InnerShadowDef(),
    val glow: GlowDef = GlowDef(),
    val emboss: EmbossDef = EmbossDef(),
    val effect3D: Layer3DEffect = Layer3DEffect(),
    val mask: MaskDef = MaskDef()
) : Layer() {
    override fun copyWithTransform(transform: Transform) = copy(transform = transform)
    override fun copyWithVisibility(isVisible: Boolean) = copy(isVisible = isVisible)
    override fun copyWithLock(isLocked: Boolean) = copy(isLocked = isLocked)
    override fun copyWithOpacity(opacity: Float) = copy(opacity = opacity)
    override fun copyWithZIndex(zIndex: Int) = copy(zIndex = zIndex)
    override fun copyWithName(name: String) = copy(name = name)
    override fun copyWithBlendMode(blendMode: BlendModeDef) = copy(blendMode = blendMode)
}

data class BezierLayer(
    override val id: String = UUID.randomUUID().toString(),
    override val name: String = "Courbe Bézier",
    override val isVisible: Boolean = true,
    override val isLocked: Boolean = false,
    override val opacity: Float = 1f,
    override val blendMode: BlendModeDef = BlendModeDef.SRC_OVER,
    override val transform: Transform = Transform(x = 80f, y = 80f, width = 300f, height = 300f),
    override val zIndex: Int = 0,
    val anchors: List<AnchorPoint> = listOf(
        AnchorPoint(position = Offset(20f, 150f), handleIn = Offset(-20f, 0f), handleOut = Offset(40f, -60f)),
        AnchorPoint(position = Offset(150f, 40f), handleIn = Offset(-50f, 0f), handleOut = Offset(50f, 0f)),
        AnchorPoint(position = Offset(280f, 150f), handleIn = Offset(-40f, -60f), handleOut = Offset(20f, 0f))
    ),
    val isClosed: Boolean = false,
    val fill: ColorFill = ColorFill(solidColor = 0x33D4AF37),
    val stroke: StrokeDef = StrokeDef(isEnabled = true, color = 0xFFD4AF37, width = 4f),
    val shadow: ShadowDef = ShadowDef()
) : Layer() {
    override fun copyWithTransform(transform: Transform) = copy(transform = transform)
    override fun copyWithVisibility(isVisible: Boolean) = copy(isVisible = isVisible)
    override fun copyWithLock(isLocked: Boolean) = copy(isLocked = isLocked)
    override fun copyWithOpacity(opacity: Float) = copy(opacity = opacity)
    override fun copyWithZIndex(zIndex: Int) = copy(zIndex = zIndex)
    override fun copyWithName(name: String) = copy(name = name)
    override fun copyWithBlendMode(blendMode: BlendModeDef) = copy(blendMode = blendMode)
}

data class DrawingLayer(
    override val id: String = UUID.randomUUID().toString(),
    override val name: String = "Dessin",
    override val isVisible: Boolean = true,
    override val isLocked: Boolean = false,
    override val opacity: Float = 1f,
    override val blendMode: BlendModeDef = BlendModeDef.SRC_OVER,
    override val transform: Transform = Transform(x = 0f, y = 0f, width = 500f, height = 500f),
    override val zIndex: Int = 0,
    val strokes: List<DrawingStroke> = emptyList()
) : Layer() {
    override fun copyWithTransform(transform: Transform) = copy(transform = transform)
    override fun copyWithVisibility(isVisible: Boolean) = copy(isVisible = isVisible)
    override fun copyWithLock(isLocked: Boolean) = copy(isLocked = isLocked)
    override fun copyWithOpacity(opacity: Float) = copy(opacity = opacity)
    override fun copyWithZIndex(zIndex: Int) = copy(zIndex = zIndex)
    override fun copyWithName(name: String) = copy(name = name)
    override fun copyWithBlendMode(blendMode: BlendModeDef) = copy(blendMode = blendMode)
}

data class ImageLayer(
    override val id: String = UUID.randomUUID().toString(),
    override val name: String = "Image / Sticker",
    override val isVisible: Boolean = true,
    override val isLocked: Boolean = false,
    override val opacity: Float = 1f,
    override val blendMode: BlendModeDef = BlendModeDef.SRC_OVER,
    override val transform: Transform = Transform(x = 100f, y = 100f, width = 240f, height = 240f),
    override val zIndex: Int = 0,
    val imageUri: String? = null,
    val isSticker: Boolean = false,
    val stickerName: String = "",
    val brightness: Float = 1f, // 0 to 2
    val contrast: Float = 1f,   // 0 to 2
    val saturation: Float = 1f, // 0 to 2
    val tintColor: Long? = null,
    val stroke: StrokeDef = StrokeDef(isEnabled = false, color = 0xFFFFFFFF, width = 3f),
    val shadow: ShadowDef = ShadowDef(isEnabled = true, color = 0x66000000, radius = 10f, dx = 2f, dy = 6f),
    val glow: GlowDef = GlowDef(),
    val colorEraser: ColorEraserDef = ColorEraserDef(),
    val mask: MaskDef = MaskDef()
) : Layer() {
    override fun copyWithTransform(transform: Transform) = copy(transform = transform)
    override fun copyWithVisibility(isVisible: Boolean) = copy(isVisible = isVisible)
    override fun copyWithLock(isLocked: Boolean) = copy(isLocked = isLocked)
    override fun copyWithOpacity(opacity: Float) = copy(opacity = opacity)
    override fun copyWithZIndex(zIndex: Int) = copy(zIndex = zIndex)
    override fun copyWithName(name: String) = copy(name = name)
    override fun copyWithBlendMode(blendMode: BlendModeDef) = copy(blendMode = blendMode)
}

data class CanvasProject(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "Nouveau Projet",
    val width: Int = 1080,
    val height: Int = 1080,
    val backgroundColor: Long = 0xFF0A0A0C,
    val backgroundGradient: GradientDef? = null,
    val isTransparentBg: Boolean = false,
    val layers: List<Layer> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
