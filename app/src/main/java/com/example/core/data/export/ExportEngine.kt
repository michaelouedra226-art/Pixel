package com.example.core.data.export

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.toArgb
import com.example.core.engine.model.BezierLayer
import com.example.core.engine.model.CanvasProject
import com.example.core.engine.model.DrawingLayer
import com.example.core.engine.model.ImageLayer
import com.example.core.engine.model.Layer
import com.example.core.engine.model.ShapeLayer
import com.example.core.engine.model.ShapeType
import com.example.core.engine.model.TextLayer
import com.example.core.engine.path.PathEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import kotlin.math.cos
import kotlin.math.sin

enum class ExportFormat {
    PNG,
    JPG,
    WEBP,
    SVG
}

data class ExportConfig(
    val format: ExportFormat = ExportFormat.PNG,
    val scaleFactor: Float = 1f, // 1x, 2x, 4x
    val quality: Int = 100 // 1 to 100 for JPG/WEBP
)

object ExportEngine {

    suspend fun renderProjectToBitmap(
        project: CanvasProject,
        config: ExportConfig,
        bitmapCache: Map<String, Bitmap> = emptyMap()
    ): Bitmap = withContext(Dispatchers.Default) {
        val targetWidth = (project.width * config.scaleFactor).toInt().coerceAtLeast(100)
        val targetHeight = (project.height * config.scaleFactor).toInt().coerceAtLeast(100)

        val bitmap = Bitmap.createBitmap(
            targetWidth,
            targetHeight,
            if (config.format == ExportFormat.PNG) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565
        )
        val canvas = Canvas(bitmap)

        // Draw background
        if (!project.isTransparentBg) {
            val bgPaint = Paint().apply {
                color = Color(project.backgroundColor).toArgb()
                style = Paint.Style.FILL
            }
            if (project.backgroundGradient != null) {
                val grad = project.backgroundGradient
                val colors = grad.colors.map { Color(it).toArgb() }.toIntArray()
                val positions = grad.stops.toFloatArray()
                bgPaint.shader = android.graphics.LinearGradient(
                    0f, 0f, targetWidth.toFloat(), targetHeight.toFloat(),
                    colors, positions, android.graphics.Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 0f, targetWidth.toFloat(), targetHeight.toFloat(), bgPaint)
        }

        // Draw layers in order
        val sortedLayers = project.layers.filter { it.isVisible }.sortedBy { it.zIndex }
        for (layer in sortedLayers) {
            drawLayerOnAndroidCanvas(canvas, layer, config.scaleFactor, bitmapCache)
        }

        bitmap
    }

    private fun drawLayerOnAndroidCanvas(
        canvas: Canvas,
        layer: Layer,
        scale: Float,
        bitmapCache: Map<String, Bitmap>
    ) {
        val t = layer.transform
        val sx = t.x * scale
        val sy = t.y * scale
        val sw = t.width * scale * t.scaleX
        val sh = t.height * scale * t.scaleY

        canvas.save()
        canvas.translate(sx, sy)
        canvas.rotate(t.rotation, sw / 2f, sh / 2f)

        when (layer) {
            is TextLayer -> {
                val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    textSize = layer.fontSize * scale * t.scaleY
                    typeface = when {
                        layer.isBold && layer.isItalic -> Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
                        layer.isBold -> Typeface.DEFAULT_BOLD
                        layer.isItalic -> Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                        else -> Typeface.DEFAULT
                    }
                    letterSpacing = layer.letterSpacing * 0.05f
                    textAlign = when (layer.textAlign) {
                        "LEFT" -> Paint.Align.LEFT
                        "RIGHT" -> Paint.Align.RIGHT
                        else -> Paint.Align.CENTER
                    }
                    alpha = (layer.opacity * 255).toInt()
                }

                val xPos = when (layer.textAlign) {
                    "LEFT" -> 0f
                    "RIGHT" -> sw
                    else -> sw / 2f
                }
                val yPos = sh / 2f + (textPaint.textSize / 3f)

                // 3D extrusion
                if (layer.effect3D.isEnabled && layer.effect3D.depth > 0) {
                    val depth = (layer.effect3D.depth * scale).toInt()
                    val angleRad = Math.toRadians(layer.effect3D.lightAngle.toDouble())
                    val dx = cos(angleRad).toFloat()
                    val dy = sin(angleRad).toFloat()
                    val extrudePaint = Paint(textPaint).apply {
                        color = Color(layer.effect3D.color).toArgb()
                        alpha = (layer.opacity * 255).toInt()
                    }
                    for (step in depth downTo 1) {
                        canvas.drawText(layer.text, xPos + dx * step, yPos + dy * step, extrudePaint)
                    }
                }

                // Shadow
                if (layer.shadow.isEnabled) {
                    val shadowPaint = Paint(textPaint).apply {
                        color = Color(layer.shadow.color).toArgb()
                        setShadowLayer(
                            layer.shadow.radius * scale,
                            layer.shadow.dx * scale,
                            layer.shadow.dy * scale,
                            Color(layer.shadow.color).toArgb()
                        )
                    }
                    canvas.drawText(layer.text, xPos, yPos, shadowPaint)
                }

                // Stroke
                if (layer.stroke.isEnabled && layer.stroke.width > 0) {
                    val strokePaint = Paint(textPaint).apply {
                        style = Paint.Style.STROKE
                        strokeWidth = layer.stroke.width * 2f * scale
                        color = Color(layer.stroke.color).toArgb()
                    }
                    canvas.drawText(layer.text, xPos, yPos, strokePaint)
                }

                // Fill
                textPaint.style = Paint.Style.FILL
                if (layer.fill.isGradient && layer.fill.gradient != null) {
                    val colors = layer.fill.gradient.colors.map { Color(it).toArgb() }.toIntArray()
                    val positions = layer.fill.gradient.stops.toFloatArray()
                    textPaint.shader = android.graphics.LinearGradient(
                        0f, 0f, sw, sh, colors, positions, android.graphics.Shader.TileMode.CLAMP
                    )
                } else {
                    textPaint.color = Color(layer.fill.solidColor).toArgb()
                }
                canvas.drawText(layer.text, xPos, yPos, textPaint)
            }

            is ShapeLayer -> {
                val shapePath = PathEngine.createShapePath(
                    shapeType = layer.shapeType,
                    width = sw,
                    height = sh,
                    cornerRadius = layer.cornerRadius * scale,
                    polygonSides = layer.polygonSides,
                    starPoints = layer.starPoints,
                    starInnerRatio = layer.starInnerRadiusRatio
                ).asAndroidPath()

                val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.FILL
                    color = Color(layer.fill.solidColor).toArgb()
                    alpha = (layer.opacity * 255).toInt()
                }
                if (layer.fill.isGradient && layer.fill.gradient != null) {
                    val colors = layer.fill.gradient.colors.map { Color(it).toArgb() }.toIntArray()
                    fillPaint.shader = android.graphics.LinearGradient(
                        0f, 0f, sw, sh, colors, null, android.graphics.Shader.TileMode.CLAMP
                    )
                }
                canvas.drawPath(shapePath, fillPaint)

                if (layer.stroke.isEnabled && layer.stroke.width > 0) {
                    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        style = Paint.Style.STROKE
                        strokeWidth = layer.stroke.width * scale
                        color = Color(layer.stroke.color).toArgb()
                        alpha = (layer.opacity * 255).toInt()
                    }
                    canvas.drawPath(shapePath, strokePaint)
                }
            }

            is BezierLayer -> {
                val bezierPath = PathEngine.createBezierPath(
                    layer.anchors.map { it.copy(position = it.position * scale, handleIn = it.handleIn * scale, handleOut = it.handleOut * scale) },
                    layer.isClosed
                ).asAndroidPath()

                if (layer.isClosed) {
                    val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        style = Paint.Style.FILL
                        color = Color(layer.fill.solidColor).toArgb()
                        alpha = (layer.opacity * 255).toInt()
                    }
                    canvas.drawPath(bezierPath, fillPaint)
                }
                if (layer.stroke.isEnabled && layer.stroke.width > 0) {
                    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        style = Paint.Style.STROKE
                        strokeWidth = layer.stroke.width * scale
                        color = Color(layer.stroke.color).toArgb()
                        alpha = (layer.opacity * 255).toInt()
                    }
                    canvas.drawPath(bezierPath, strokePaint)
                }
            }

            is DrawingLayer -> {
                for (stroke in layer.strokes) {
                    if (stroke.points.isEmpty()) continue
                    val strokePath = PathEngine.createSmoothStrokePath(
                        stroke.points.map { it.copy(x = it.x * scale, y = it.y * scale) }
                    ).asAndroidPath()

                    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        style = Paint.Style.STROKE
                        strokeWidth = stroke.strokeWidth * scale
                        color = Color(stroke.color).toArgb()
                        alpha = (stroke.opacity * layer.opacity * 255).toInt()
                        strokeCap = Paint.Cap.ROUND
                        strokeJoin = Paint.Join.ROUND
                    }
                    canvas.drawPath(strokePath, strokePaint)
                }
            }

            is ImageLayer -> {
                val bitmap = layer.imageUri?.let { bitmapCache[it] }
                if (bitmap != null) {
                    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        alpha = (layer.opacity * 255).toInt()
                    }
                    val src = android.graphics.Rect(0, 0, bitmap.width, bitmap.height)
                    val dst = RectF(0f, 0f, sw, sh)
                    canvas.drawBitmap(bitmap, src, dst, paint)
                }
            }
        }

        canvas.restore()
    }

    suspend fun saveBitmapToGallery(
        context: Context,
        bitmap: Bitmap,
        title: String,
        format: ExportFormat,
        quality: Int
    ): Uri? = withContext(Dispatchers.IO) {
        val filename = "PixelForge_${System.currentTimeMillis()}.${if (format == ExportFormat.PNG) "png" else "jpg"}"
        val mimeType = if (format == ExportFormat.PNG) "image/png" else "image/jpeg"
        val compressFormat = if (format == ExportFormat.PNG) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/PixelForge")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                resolver.openOutputStream(uri)?.use { stream ->
                    bitmap.compress(compressFormat, quality, stream)
                }
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
                return@withContext uri
            }
        } else {
            val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val dir = File(picturesDir, "PixelForge")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, filename)
            FileOutputStream(file).use { stream ->
                bitmap.compress(compressFormat, quality, stream)
            }
            return@withContext Uri.fromFile(file)
        }
        null
    }

    fun generateProjectSvg(project: CanvasProject): String {
        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8"?>""").append("\n")
        sb.append("""<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 ${project.width} ${project.height}" width="${project.width}" height="${project.height}">""").append("\n")

        // Background
        if (!project.isTransparentBg) {
            val hexColor = String.format("#%06X", 0xFFFFFF and project.backgroundColor.toInt())
            sb.append("""  <rect width="100%" height="100%" fill="$hexColor" />""").append("\n")
        }

        // Layers
        val sortedLayers = project.layers.filter { it.isVisible }.sortedBy { it.zIndex }
        for (layer in sortedLayers) {
            val t = layer.transform
            val opacity = layer.opacity
            when (layer) {
                is TextLayer -> {
                    val fillHex = String.format("#%06X", 0xFFFFFF and layer.fill.solidColor.toInt())
                    val strokeAttr = if (layer.stroke.isEnabled) {
                        val strokeHex = String.format("#%06X", 0xFFFFFF and layer.stroke.color.toInt())
                        """stroke="$strokeHex" stroke-width="${layer.stroke.width}""""
                    } else ""
                    val fontWeight = if (layer.isBold) """font-weight="bold"""" else ""
                    val fontStyle = if (layer.isItalic) """font-style="italic"""" else ""
                    val textAnchor = when (layer.textAlign) {
                        "LEFT" -> "start"
                        "RIGHT" -> "end"
                        else -> "middle"
                    }
                    val textX = if (textAnchor == "middle") t.x + t.width / 2 else if (textAnchor == "end") t.x + t.width else t.x
                    val textY = t.y + t.height * 0.7f

                    sb.append("""  <text x="$textX" y="$textY" font-size="${layer.fontSize}" text-anchor="$textAnchor" fill="$fillHex" opacity="$opacity" $strokeAttr $fontWeight $fontStyle>${layer.text}</text>""").append("\n")
                }
                is ShapeLayer -> {
                    val fillHex = String.format("#%06X", 0xFFFFFF and layer.fill.solidColor.toInt())
                    val strokeAttr = if (layer.stroke.isEnabled) {
                        val strokeHex = String.format("#%06X", 0xFFFFFF and layer.stroke.color.toInt())
                        """stroke="$strokeHex" stroke-width="${layer.stroke.width}""""
                    } else ""
                    when (layer.shapeType) {
                        ShapeType.RECTANGLE -> {
                            sb.append("""  <rect x="${t.x}" y="${t.y}" width="${t.width}" height="${t.height}" fill="$fillHex" opacity="$opacity" $strokeAttr />""").append("\n")
                        }
                        ShapeType.ROUNDED_RECT -> {
                            sb.append("""  <rect x="${t.x}" y="${t.y}" width="${t.width}" height="${t.height}" rx="${layer.cornerRadius}" ry="${layer.cornerRadius}" fill="$fillHex" opacity="$opacity" $strokeAttr />""").append("\n")
                        }
                        ShapeType.CIRCLE -> {
                            val cx = t.x + t.width / 2
                            val cy = t.y + t.height / 2
                            val r = minOf(t.width, t.height) / 2
                            sb.append("""  <circle cx="$cx" cy="$cy" r="$r" fill="$fillHex" opacity="$opacity" $strokeAttr />""").append("\n")
                        }
                        else -> {
                            sb.append("""  <rect x="${t.x}" y="${t.y}" width="${t.width}" height="${t.height}" rx="8" fill="$fillHex" opacity="$opacity" $strokeAttr />""").append("\n")
                        }
                    }
                }
                is DrawingLayer -> {
                    for (stroke in layer.strokes) {
                        if (stroke.points.size < 2) continue
                        val strokeHex = String.format("#%06X", 0xFFFFFF and stroke.color.toInt())
                        val d = buildString {
                            append("M ${stroke.points[0].x} ${stroke.points[0].y} ")
                            for (i in 1 until stroke.points.size) {
                                append("L ${stroke.points[i].x} ${stroke.points[i].y} ")
                            }
                        }
                        sb.append("""  <path d="$d" fill="none" stroke="$strokeHex" stroke-width="${stroke.strokeWidth}" stroke-linecap="round" stroke-linejoin="round" opacity="${stroke.opacity * opacity}" />""").append("\n")
                    }
                }
                else -> {}
            }
        }

        sb.append("</svg>")
        return sb.toString()
    }

    suspend fun saveSvgToStorage(
        context: Context,
        svgContent: String,
        title: String
    ): Uri? = withContext(Dispatchers.IO) {
        val filename = "PixelForge_${System.currentTimeMillis()}.svg"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/svg+xml")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/PixelForge")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                resolver.openOutputStream(uri)?.use { stream ->
                    stream.write(svgContent.toByteArray(Charsets.UTF_8))
                }
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
                return@withContext uri
            }
        } else {
            val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "PixelForge")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, filename)
            FileOutputStream(file).use { stream ->
                stream.write(svgContent.toByteArray(Charsets.UTF_8))
            }
            return@withContext Uri.fromFile(file)
        }
        null
    }
}
