package com.example.core.engine.render

import android.graphics.Bitmap
import android.graphics.Camera
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path as AndroidPath
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
import com.example.core.engine.model.ColorEraserDef
import com.example.core.engine.model.ColorFill
import com.example.core.engine.model.DrawingLayer
import com.example.core.engine.model.GradientDef
import com.example.core.engine.model.GradientType
import com.example.core.engine.model.ImageLayer
import com.example.core.engine.model.Layer
import com.example.core.engine.model.ShapeLayer
import com.example.core.engine.model.TextLayer
import com.example.core.engine.path.PathEngine
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

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
            val checkSize = 24f
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
                    // Apply 3D Rotation (X and Y tilt) using Android Camera
                    if (t.rotationX != 0f || t.rotationY != 0f) {
                        drawScope.drawIntoCanvas { canvas ->
                            val nativeCanvas = canvas.nativeCanvas
                            nativeCanvas.save()
                            val camera = Camera()
                            val matrix = Matrix()
                            camera.save()
                            camera.rotateX(-t.rotationX)
                            camera.rotateY(t.rotationY)
                            camera.getMatrix(matrix)
                            camera.restore()

                            val centerX = t.width / 2f
                            val centerY = t.height / 2f
                            matrix.preTranslate(-centerX, -centerY)
                            matrix.postTranslate(centerX, centerY)

                            nativeCanvas.concat(matrix)
                        }
                    }

                    when (layer) {
                        is TextLayer -> renderTextLayer(this, layer, bitmapCache)
                        is ShapeLayer -> renderShapeLayer(this, layer, bitmapCache)
                        is BezierLayer -> renderBezierLayer(this, layer)
                        is DrawingLayer -> renderDrawingLayer(this, layer)
                        is ImageLayer -> renderImageLayer(this, layer, bitmapCache)
                    }

                    if (t.rotationX != 0f || t.rotationY != 0f) {
                        drawScope.drawIntoCanvas { canvas ->
                            canvas.nativeCanvas.restore()
                        }
                    }
                }
            }
        }
    }

    private fun renderShapeLayer(
        drawScope: DrawScope,
        layer: ShapeLayer,
        bitmapCache: Map<String, Bitmap>
    ) {
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

        // 3D Effect for Shape
        if (layer.effect3D.isEnabled && layer.effect3D.depth > 0) {
            val depth = layer.effect3D.depth.toInt()
            val angleRad = Math.toRadians(layer.effect3D.lightAngle.toDouble())
            val dx = cos(angleRad).toFloat()
            val dy = sin(angleRad).toFloat()
            drawScope.drawIntoCanvas { canvas ->
                val extrudePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color(layer.effect3D.color).toArgb()
                    alpha = (layer.opacity * 255).toInt()
                }
                for (step in depth downTo 1) {
                    val aPath = AndroidPath(path.asAndroidPath())
                    val m = Matrix().apply { setTranslate(dx * step, dy * step) }
                    aPath.transform(m)
                    canvas.nativeCanvas.drawPath(aPath, extrudePaint)
                }
            }
        }

        // Glow
        if (layer.glow.isEnabled && layer.glow.radius > 0) {
            drawScope.drawIntoCanvas { canvas ->
                val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color(layer.glow.color).toArgb()
                    alpha = ((layer.glow.opacity * layer.opacity) * 255).toInt()
                    setShadowLayer(
                        layer.glow.radius,
                        0f,
                        0f,
                        Color(layer.glow.color).toArgb()
                    )
                }
                canvas.nativeCanvas.drawPath(path.asAndroidPath(), glowPaint)
            }
        }

        // Drop shadow
        if (layer.shadow.isEnabled) {
            drawScope.drawIntoCanvas { canvas ->
                val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color(layer.shadow.color).toArgb()
                    alpha = ((Color(layer.shadow.color).alpha * layer.opacity) * 255).toInt()
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

        // Fill (Texture / Gradient / Solid)
        val textureBmp = layer.fill.textureUri?.let { bitmapCache[it] }
        if (layer.fill.isTexture && textureBmp != null) {
            drawScope.drawIntoCanvas { canvas ->
                val bmpPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    val shader = android.graphics.BitmapShader(
                        textureBmp,
                        Shader.TileMode.REPEAT,
                        Shader.TileMode.REPEAT
                    )
                    val matrix = Matrix()
                    matrix.postScale(
                        layer.fill.textureScale * (t.width / textureBmp.width.coerceAtLeast(1)),
                        layer.fill.textureScale * (t.height / textureBmp.height.coerceAtLeast(1))
                    )
                    shader.setLocalMatrix(matrix)
                    this.shader = shader
                    alpha = (layer.opacity * 255).toInt()
                }
                canvas.nativeCanvas.drawPath(path.asAndroidPath(), bmpPaint)
            }
        } else {
            val fillBrush = createBrush(layer.fill, t.width, t.height)
            drawScope.drawPath(
                path = path,
                brush = fillBrush,
                alpha = layer.opacity,
                style = Fill
            )
        }

        // Emboss / Bevel
        if (layer.emboss.isEnabled) {
            drawScope.drawIntoCanvas { canvas ->
                val angleRad = Math.toRadians(layer.emboss.lightAngle.toDouble())
                val dx = cos(angleRad).toFloat() * 2f
                val dy = sin(angleRad).toFloat() * 2f
                val embossPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 2.5f
                    color = android.graphics.Color.WHITE
                    alpha = ((layer.emboss.intensity / 100f) * 180 * layer.opacity).toInt()
                }
                val m = Matrix().apply { setTranslate(dx, dy) }
                val highlightPath = AndroidPath(path.asAndroidPath())
                highlightPath.transform(m)
                canvas.nativeCanvas.drawPath(highlightPath, embossPaint)
            }
        }

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

    private fun renderTextLayer(
        drawScope: DrawScope,
        layer: TextLayer,
        bitmapCache: Map<String, Bitmap>
    ) {
        val t = layer.transform
        val text = layer.text

        drawScope.drawIntoCanvas { canvas ->
            val nativeCanvas = canvas.nativeCanvas
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = layer.fontSize
                typeface = when (layer.fontFamily) {
                    "Serif", "Cinzel", "Playfair" -> Typeface.SERIF
                    "Monospace" -> Typeface.MONOSPACE
                    else -> when {
                        layer.isBold && layer.isItalic -> Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
                        layer.isBold -> Typeface.DEFAULT_BOLD
                        layer.isItalic -> Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                        else -> Typeface.DEFAULT
                    }
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

            val isCurved = layer.curvature.isEnabled && layer.curvature.bend != 0f

            // Create arc path if text curvature is enabled
            var curvePath: AndroidPath? = null
            if (isCurved) {
                curvePath = AndroidPath()
                val bend = layer.curvature.bend // -100 to 100
                val radius = (t.width * 1.5f) / (abs(bend) / 50f).coerceAtLeast(0.1f)
                val rectF = if (bend > 0) {
                    RectF(t.width / 2f - radius, yPos - radius + 20f, t.width / 2f + radius, yPos + radius + 20f)
                } else {
                    RectF(t.width / 2f - radius, yPos - radius - 20f, t.width / 2f + radius, yPos + radius - 20f)
                }
                val startAngle = if (bend > 0) 180f + 90f - (t.width / radius * 25f) else 90f - (t.width / radius * 25f)
                val sweepAngle = (t.width / radius * 50f) * (if (bend > 0) 1f else -1f)
                curvePath.addArc(rectF, startAngle, sweepAngle)
            }

            // Glow Effect
            if (layer.glow.isEnabled && layer.glow.radius > 0) {
                val glowPaint = Paint(paint).apply {
                    color = Color(layer.glow.color).toArgb()
                    alpha = ((layer.glow.opacity * layer.opacity) * 255).toInt()
                    setShadowLayer(
                        layer.glow.radius,
                        0f,
                        0f,
                        Color(layer.glow.color).toArgb()
                    )
                }
                if (isCurved && curvePath != null) {
                    nativeCanvas.drawTextOnPath(text, curvePath, 0f, 0f, glowPaint)
                } else {
                    nativeCanvas.drawText(text, xPos, yPos, glowPaint)
                }
            }

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
                    if (isCurved && curvePath != null) {
                        val shiftedPath = AndroidPath(curvePath)
                        val m = Matrix().apply { setTranslate(dx * stepOffset, dy * stepOffset) }
                        shiftedPath.transform(m)
                        nativeCanvas.drawTextOnPath(text, shiftedPath, 0f, 0f, extrudePaint)
                    } else {
                        nativeCanvas.drawText(
                            text,
                            xPos + dx * stepOffset,
                            yPos + dy * stepOffset,
                            extrudePaint
                        )
                    }
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
                if (isCurved && curvePath != null) {
                    nativeCanvas.drawTextOnPath(text, curvePath, 0f, 0f, shadowPaint)
                } else {
                    nativeCanvas.drawText(text, xPos, yPos, shadowPaint)
                }
            }

            // Outer Stroke
            if (layer.stroke.isEnabled && layer.stroke.width > 0) {
                val strokePaint = Paint(paint).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = layer.stroke.width * 2f
                    color = Color(layer.stroke.color).toArgb()
                    alpha = (layer.opacity * 255).toInt()
                }
                if (isCurved && curvePath != null) {
                    nativeCanvas.drawTextOnPath(text, curvePath, 0f, 0f, strokePaint)
                } else {
                    nativeCanvas.drawText(text, xPos, yPos, strokePaint)
                }
            }

            // Main Text Fill (with gradient or texture shader)
            val fillPaint = Paint(paint).apply {
                style = Paint.Style.FILL
                val textureBmp = layer.fill.textureUri?.let { bitmapCache[it] }
                if (layer.fill.isTexture && textureBmp != null) {
                    val shader = android.graphics.BitmapShader(
                        textureBmp,
                        Shader.TileMode.REPEAT,
                        Shader.TileMode.REPEAT
                    )
                    val matrix = Matrix()
                    matrix.postScale(
                        layer.fill.textureScale * (t.width / textureBmp.width.coerceAtLeast(1)),
                        layer.fill.textureScale * (t.height / textureBmp.height.coerceAtLeast(1))
                    )
                    shader.setLocalMatrix(matrix)
                    this.shader = shader
                } else if (layer.fill.isGradient && layer.fill.gradient != null) {
                    val colors = layer.fill.gradient.colors.map { Color(it).toArgb() }.toIntArray()
                    val positions = layer.fill.gradient.stops.toFloatArray()
                    val angleRad = Math.toRadians(layer.fill.gradient.angle.toDouble())
                    val endX = t.width * cos(angleRad).toFloat()
                    val endY = t.height * sin(angleRad).toFloat()
                    val shader = LinearGradient(
                        0f, 0f, endX.coerceAtLeast(1f), endY.coerceAtLeast(1f),
                        colors,
                        positions,
                        Shader.TileMode.CLAMP
                    )
                    this.shader = shader
                } else {
                    color = Color(layer.fill.solidColor).toArgb()
                }
                alpha = (layer.opacity * 255).toInt()
            }

            if (isCurved && curvePath != null) {
                nativeCanvas.drawTextOnPath(text, curvePath, 0f, 0f, fillPaint)
            } else {
                nativeCanvas.drawText(text, xPos, yPos, fillPaint)
            }

            // Emboss Highlight
            if (layer.emboss.isEnabled) {
                val angleRad = Math.toRadians(layer.emboss.lightAngle.toDouble())
                val dx = cos(angleRad).toFloat() * 1.5f
                val dy = sin(angleRad).toFloat() * 1.5f
                val embossPaint = Paint(paint).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 1.2f
                    color = android.graphics.Color.WHITE
                    alpha = ((layer.emboss.intensity / 100f) * 200 * layer.opacity).toInt()
                }
                if (isCurved && curvePath != null) {
                    val shiftedPath = AndroidPath(curvePath)
                    val m = Matrix().apply { setTranslate(dx, dy) }
                    shiftedPath.transform(m)
                    nativeCanvas.drawTextOnPath(text, shiftedPath, 0f, 0f, embossPaint)
                } else {
                    nativeCanvas.drawText(text, xPos + dx, yPos + dy, embossPaint)
                }
            }

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
        var bitmap = layer.imageUri?.let { bitmapCache[it] }

        if (bitmap != null) {
            // Apply Color Eraser (Chroma Key) if enabled
            val finalBitmap = if (layer.colorEraser.isEnabled) {
                applyChromaKeyFilter(bitmap, layer.colorEraser)
            } else {
                bitmap
            }

            drawScope.drawIntoCanvas { canvas ->
                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    alpha = (layer.opacity * 255).toInt()
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

                // Shadow
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
                    canvas.nativeCanvas.drawRect(0f, 0f, t.width, t.height, shadowPaint)
                }

                val srcRect = android.graphics.Rect(0, 0, finalBitmap.width, finalBitmap.height)
                val dstRect = RectF(0f, 0f, t.width, t.height)
                canvas.nativeCanvas.drawBitmap(finalBitmap, srcRect, dstRect, paint)

                // Optional Stroke for Image/Sticker
                if (layer.stroke.isEnabled && layer.stroke.width > 0) {
                    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        style = Paint.Style.STROKE
                        strokeWidth = layer.stroke.width
                        color = Color(layer.stroke.color).toArgb()
                        alpha = (layer.opacity * 255).toInt()
                    }
                    canvas.nativeCanvas.drawRect(dstRect, strokePaint)
                }
            }
        } else {
            // Built-in sticker placeholder
            drawScope.drawRoundRect(
                brush = Brush.linearGradient(listOf(Color(0xFFD4AF37), Color(0xFF997A15))),
                size = Size(t.width, t.height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f),
                alpha = layer.opacity * 0.85f
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

    private fun applyChromaKeyFilter(src: Bitmap, eraser: ColorEraserDef): Bitmap {
        val width = src.width
        val height = src.height
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(width * height)
        src.getPixels(pixels, 0, width, 0, 0, width, height)

        val targetR = (eraser.targetColor shr 16 and 0xFF).toInt()
        val targetG = (eraser.targetColor shr 8 and 0xFF).toInt()
        val targetB = (eraser.targetColor and 0xFF).toInt()
        val tolSq = (eraser.tolerance * 2.55f) * (eraser.tolerance * 2.55f)

        for (i in pixels.indices) {
            val p = pixels[i]
            val r = (p shr 16) and 0xFF
            val g = (p shr 8) and 0xFF
            val b = p and 0xFF
            val distSq = (r - targetR) * (r - targetR) + (g - targetG) * (g - targetG) + (b - targetB) * (b - targetB)
            if (distSq <= tolSq) {
                pixels[i] = 0x00000000 // Transparent
            }
        }
        output.setPixels(pixels, 0, width, 0, 0, width, height)
        return output
    }
}
