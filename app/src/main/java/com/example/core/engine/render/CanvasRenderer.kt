package com.example.core.engine.render

import android.graphics.Bitmap
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.NativePaint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import com.example.core.engine.model.BezierLayer
import com.example.core.engine.model.BlendModeDef
import com.example.core.engine.model.CanvasProject
import com.example.core.engine.model.ColorFill
import com.example.core.engine.model.DrawingLayer
import com.example.core.engine.model.GradientDef
import com.example.core.engine.model.GradientType
import com.example.core.engine.model.ImageLayer
import com.example.core.engine.model.Layer
import com.example.core.engine.model.ShapeLayer
import com.example.core.engine.model.TextLayer
import com.example.core.engine.path.PathEngine
import kotlin.math.cos
import kotlin.math.sin

object CanvasRenderer {

    fun createBrush(fill: ColorFill, width: Float, height: Float): Brush {
        if (!fill.isGradient || fill.gradient == null) {
            return Brush.linearGradient(listOf(Color(fill.solidColor), Color(fill.solidColor)))
        }
        val grad = fill.gradient
        val colors = grad.colors.map { Color(it) }
        return when (grad.type) {
            GradientType.LINEAR -> {
                val angleRad = Math.toRadians(grad.angle.toDouble())
                val endX = width * cos(angleRad).toFloat()
                val endY = height * sin(angleRad).toFloat()
                Brush.linearGradient(
                    colors = colors,
                    start = Offset(0f, 0f),
                    end = Offset(endX.coerceAtLeast(1f), endY.coerceAtLeast(1f))
                )
            }
            GradientType.RADIAL -> {
                val radius = (width.coerceAtLeast(height) / 2f).coerceAtLeast(1f)
                Brush.radialGradient(
                    colors = colors,
                    center = Offset(width / 2f, height / 2f),
                    radius = radius
                )
            }
        }
    }

    fun mapBlendMode(def: BlendModeDef): BlendMode {
        return when (def) {
            BlendModeDef.SRC_OVER -> BlendMode.SrcOver
            BlendModeDef.MULTIPLY -> BlendMode.Multiply
            BlendModeDef.SCREEN -> BlendMode.Screen
            BlendModeDef.OVERLAY -> BlendMode.Overlay
            BlendModeDef.DARKEN -> BlendMode.Darken
            BlendModeDef.LIGHTEN -> BlendMode.Lighten
            BlendModeDef.COLOR_DODGE -> BlendMode.ColorDodge
            BlendModeDef.COLOR_BURN -> BlendMode.ColorBurn
        }
    }

    fun renderBackground(
        drawScope: DrawScope,
        project: CanvasProject,
        canvasSize: Size
    ) {
        if (project.isTransparentBg) {
            // Draw luxury dark checkerboard pattern
            val checkSize = 20f
            val cols = (canvasSize.width / checkSize).toInt() + 1
            val rows = (canvasSize.height / checkSize).toInt() + 1
            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    val isEven = (r + c) % 2 == 0
                    val color = if (isEven) Color(0xFF1E1E22) else Color(0xFF141416)
                    drawScope.drawRect(
                        color = color,
                        topLeft = Offset(c * checkSize, r * checkSize),
                        size = Size(checkSize, checkSize)
                    )
                }
            }
        } else if (project.backgroundGradient != null) {
            val brush = createBrush(
                ColorFill(gradient = project.backgroundGradient, isGradient = true),
                canvasSize.width,
                canvasSize.height
            )
            drawScope.drawRect(brush = brush, size = canvasSize)
        } else {
            drawScope.drawRect(
                color = Color(project.backgroundColor),
                size = canvasSize
            )
        }
    }

    fun renderLayer(
        drawScope: DrawScope,
        layer: Layer,
        bitmapCache: Map<String, Bitmap> = emptyMap()
    ) {
        if (!layer.isVisible) return

        val t = layer.transform
        val blendMode = mapBlendMode(layer.blendMode)

        drawScope.translate(left = t.x, top = t.y) {
            drawScope.rotate(
                degrees = t.rotation,
                pivot = Offset(t.width / 2f, t.height / 2f)
            ) {
                drawScope.scale(
                    scaleX = t.scaleX,
                    scaleY = t.scaleY,
                    pivot = Offset(t.width / 2f, t.height / 2f)
                ) {
                    when (layer) {
                        is TextLayer -> renderTextLayer(this, layer)
                        is ShapeLayer -> renderShapeLayer(this, layer)
                        is BezierLayer -> renderBezierLayer(this, layer)
                        is DrawingLayer -> renderDrawingLayer(this, layer)
                        is ImageLayer -> renderImageLayer(this, layer, bitmapCache)
                    }
                }
            }
        }
    }

    private fun renderShapeLayer(drawScope: DrawScope, layer: ShapeLayer) {
        val t = layer.transform
        val path = PathEngine.createShapePath(
            shapeType = layer.shapeType,
            width = t.width,
            height = t.height,
            cornerRadius = layer.cornerRadius,
            polygonSides = layer.polygonSides,
            starPoints = layer.starPoints,
            starInnerRatio = layer.starInnerRadiusRatio
        )

        // Drop shadow
        if (layer.shadow.isEnabled) {
            drawScope.drawIntoCanvas { canvas ->
                val shadowPaint = Paint().apply {
                    color = Color(layer.shadow.color).copy(alpha = Color(layer.shadow.color).alpha * layer.opacity).toArgb()
                    setShadowLayer(
                        layer.shadow.radius,
                        layer.shadow.dx,
                        layer.shadow.dy,
                        Color(layer.shadow.color).toArgb()
                    )
                }
                canvas.nativeCanvas.drawPath(path.asAndroidPath(), shadowPaint)
            }
        }

        // Fill
        val fillBrush = createBrush(layer.fill, t.width, t.height)
        drawScope.drawPath(
            path = path,
            brush = fillBrush,
            alpha = layer.opacity,
            style = Fill
        )

        // Stroke
        if (layer.stroke.isEnabled && layer.stroke.width > 0) {
            drawScope.drawPath(
                path = path,
                color = Color(layer.stroke.color),
                alpha = layer.opacity,
                style = Stroke(width = layer.stroke.width)
            )
        }
    }

    private fun renderBezierLayer(drawScope: DrawScope, layer: BezierLayer) {
        val path = PathEngine.createBezierPath(layer.anchors, layer.isClosed)

        if (layer.isClosed && Color(layer.fill.solidColor).alpha > 0f) {
            drawScope.drawPath(
                path = path,
                color = Color(layer.fill.solidColor),
                alpha = layer.opacity,
                style = Fill
            )
        }

        if (layer.stroke.isEnabled && layer.stroke.width > 0) {
            drawScope.drawPath(
                path = path,
                color = Color(layer.stroke.color),
                alpha = layer.opacity,
                style = Stroke(width = layer.stroke.width)
            )
        }
    }

    private fun renderDrawingLayer(drawScope: DrawScope, layer: DrawingLayer) {
        for (stroke in layer.strokes) {
            if (stroke.points.isEmpty()) continue
            val strokePath = PathEngine.createSmoothStrokePath(stroke.points)
            val strokeColor = if (stroke.isEraser) Color.Transparent else Color(stroke.color)
            val blend = if (stroke.isEraser) BlendMode.Clear else BlendMode.SrcOver

            drawScope.drawPath(
                path = strokePath,
                color = strokeColor,
                alpha = stroke.opacity * layer.opacity,
                style = Stroke(width = stroke.strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round),
                blendMode = blend
            )
        }
    }

    private fun renderTextLayer(drawScope: DrawScope, layer: TextLayer) {
        val t = layer.transform
        val text = layer.text

        drawScope.drawIntoCanvas { canvas ->
            val nativeCanvas = canvas.nativeCanvas
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = layer.fontSize
                typeface = when {
                    layer.isBold && layer.isItalic -> Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
                    layer.isBold -> Typeface.DEFAULT_BOLD
                    layer.isItalic -> Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                    else -> Typeface.DEFAULT
                }
                letterSpacing = layer.letterSpacing * 0.05f
                isUnderlineText = layer.isUnderline
                textAlign = when (layer.textAlign) {
                    "LEFT" -> Paint.Align.LEFT
                    "RIGHT" -> Paint.Align.RIGHT
                    else -> Paint.Align.CENTER
                }
            }

            val xPos = when (layer.textAlign) {
                "LEFT" -> 0f
                "RIGHT" -> t.width
                else -> t.width / 2f
            }
            val yPos = t.height / 2f + (paint.textSize / 3f)

            // 3D Extrusion Effect
            if (layer.effect3D.isEnabled && layer.effect3D.depth > 0) {
                val depth = layer.effect3D.depth.toInt()
                val angleRad = Math.toRadians(layer.effect3D.lightAngle.toDouble())
                val dx = cos(angleRad).toFloat()
                val dy = sin(angleRad).toFloat()

                val extrudePaint = Paint(paint).apply {
                    color = Color(layer.effect3D.color).toArgb()
                    alpha = (layer.opacity * 255).toInt()
                }

                for (step in depth downTo 1) {
                    val stepOffset = step * 1f
                    nativeCanvas.drawText(
                        text,
                        xPos + dx * stepOffset,
                        yPos + dy * stepOffset,
                        extrudePaint
                    )
                }
            }

            // Drop Shadow
            if (layer.shadow.isEnabled) {
                val shadowPaint = Paint(paint).apply {
                    color = Color(layer.shadow.color).toArgb()
                    alpha = ((Color(layer.shadow.color).alpha * layer.opacity) * 255).toInt()
                    setShadowLayer(
                        layer.shadow.radius,
                        layer.shadow.dx,
                        layer.shadow.dy,
                        Color(layer.shadow.color).toArgb()
                    )
                }
                nativeCanvas.drawText(text, xPos, yPos, shadowPaint)
            }

            // Outer Stroke
            if (layer.stroke.isEnabled && layer.stroke.width > 0) {
                val strokePaint = Paint(paint).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = layer.stroke.width * 2f
                    color = Color(layer.stroke.color).toArgb()
                    alpha = (layer.opacity * 255).toInt()
                }
                nativeCanvas.drawText(text, xPos, yPos, strokePaint)
            }

            // Main Text Fill
            val fillPaint = Paint(paint).apply {
                style = Paint.Style.FILL
                if (layer.fill.isGradient && layer.fill.gradient != null) {
                    val colors = layer.fill.gradient.colors.map { Color(it).toArgb() }.toIntArray()
                    val positions = layer.fill.gradient.stops.toFloatArray()
                    val shader = android.graphics.LinearGradient(
                        0f, 0f, t.width, t.height,
                        colors,
                        positions,
                        android.graphics.Shader.TileMode.CLAMP
                    )
                    this.shader = shader
                } else {
                    color = Color(layer.fill.solidColor).toArgb()
                }
                alpha = (layer.opacity * 255).toInt()
            }

            nativeCanvas.drawText(text, xPos, yPos, fillPaint)

            // Reflection Effect
            if (layer.reflection.isEnabled) {
                val reflectionPaint = Paint(fillPaint).apply {
                    alpha = ((layer.reflection.opacity * layer.opacity) * 255).toInt()
                }
                nativeCanvas.save()
                nativeCanvas.translate(0f, yPos * 2 + layer.reflection.distance)
                nativeCanvas.scale(1f, -0.7f, xPos, yPos)
                nativeCanvas.drawText(text, xPos, yPos, reflectionPaint)
                nativeCanvas.restore()
            }
        }
    }

    private fun renderImageLayer(
        drawScope: DrawScope,
        layer: ImageLayer,
        bitmapCache: Map<String, Bitmap>
    ) {
        val t = layer.transform
        val bitmap = layer.imageUri?.let { bitmapCache[it] }

        if (bitmap != null) {
            drawScope.drawIntoCanvas { canvas ->
                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    alpha = (layer.opacity * 255).toInt()
                    // Color matrix for brightness, contrast, saturation
                    val cm = ColorMatrix()
                    cm.setSaturation(layer.saturation)
                    if (layer.brightness != 1f || layer.contrast != 1f) {
                        val scale = layer.contrast
                        val translate = (layer.brightness - 1f) * 255f
                        val contrastMatrix = ColorMatrix(
                            floatArrayOf(
                                scale, 0f, 0f, 0f, translate,
                                0f, scale, 0f, 0f, translate,
                                0f, 0f, scale, 0f, translate,
                                0f, 0f, 0f, 1f, 0f
                            )
                        )
                        cm.postConcat(contrastMatrix)
                    }
                    colorFilter = ColorMatrixColorFilter(cm)
                }

                val srcRect = android.graphics.Rect(0, 0, bitmap.width, bitmap.height)
                val dstRect = RectF(0f, 0f, t.width, t.height)
                canvas.nativeCanvas.drawBitmap(bitmap, srcRect, dstRect, paint)
            }
        } else {
            // Built-in luxury sticker placeholder or procedural sticker rendering
            drawScope.drawRoundRect(
                brush = Brush.linearGradient(listOf(Color(0xFFD4AF37), Color(0xFF997A15))),
                size = Size(t.width, t.height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f),
                alpha = layer.opacity * 0.8f
            )
            drawScope.drawIntoCanvas { canvas ->
                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = android.graphics.Color.WHITE
                    textSize = t.height * 0.22f
                    textAlign = Paint.Align.CENTER
                    typeface = Typeface.DEFAULT_BOLD
                }
                val label = if (layer.isSticker && layer.stickerName.isNotBlank()) layer.stickerName else "PIXEL"
                canvas.nativeCanvas.drawText(label, t.width / 2f, t.height / 2f + paint.textSize / 3f, paint)
            }
        }
    }
}
