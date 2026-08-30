package com.example.feature.editor.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateRotation
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import com.example.core.common.MathUtil
import com.example.core.engine.model.AnchorPoint
import com.example.core.engine.model.BezierLayer
import com.example.core.engine.model.CanvasProject
import com.example.core.engine.model.DrawingPoint
import com.example.core.engine.model.DrawingStroke
import com.example.core.engine.model.Layer
import com.example.core.engine.model.Transform
import com.example.core.engine.render.CanvasRenderer
import com.example.core.engine.snap.MagneticSnapEngine
import com.example.core.engine.snap.SnapGuideLine
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.ObsidianBg
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

enum class ActiveTool {
    SELECT,
    TEXT,
    SHAPES,
    BEZIER,
    DRAW,
    STICKERS,
    LAYERS,
    BACKGROUND
}

enum class HandleType {
    NONE,
    BODY,
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT,
    TOP_CENTER,
    BOTTOM_CENTER,
    LEFT_CENTER,
    RIGHT_CENTER,
    ROTATE
}

@Composable
fun EditorCanvas(
    project: CanvasProject,
    selectedLayerId: String?,
    activeTool: ActiveTool,
    isGridEnabled: Boolean,
    isSnapEnabled: Boolean,
    brushColor: Long,
    brushSize: Float,
    isEraser: Boolean,
    selectedAnchorIndex: Int?,
    onSelectLayer: (String?) -> Unit,
    onSelectAnchor: (Int?) -> Unit,
    onUpdateLayerTransform: (String, Transform) -> Unit,
    onUpdateBezierAnchors: (String, List<AnchorPoint>) -> Unit,
    onAddDrawingStroke: (DrawingStroke) -> Unit,
    onDeleteSelectedLayer: () -> Unit,
    onDuplicateSelectedLayer: () -> Unit,
    modifier: Modifier = Modifier,
    bitmapCache: Map<String, Bitmap> = emptyMap()
) {
    var zoom by remember { mutableFloatStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    var activeGuideLines by remember { mutableStateOf<List<SnapGuideLine>>(emptyList()) }
    val currentDrawingPoints = remember { mutableStateListOf<DrawingPoint>() }

    val selectedLayer = project.layers.find { it.id == selectedLayerId }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .pointerInput(project, selectedLayerId, activeTool, isSnapEnabled, zoom, panOffset) {
                awaitEachGesture {
                    val firstDown = awaitFirstDown(requireUnconsumed = false)
                    var downTime = System.currentTimeMillis()
                    var hasMoved = false

                    // Convert first touch to canvas coordinates
                    val canvasW = project.width.toFloat()
                    val canvasH = project.height.toFloat()
                    val defaultOffsetX = (size.width - canvasW * zoom) / 2f
                    val defaultOffsetY = (size.height - canvasH * zoom) / 2f
                    val totalOffsetX = defaultOffsetX + panOffset.x
                    val totalOffsetY = defaultOffsetY + panOffset.y

                    fun screenToCanvas(screenPt: Offset): Offset {
                        return Offset(
                            (screenPt.x - totalOffsetX) / zoom,
                            (screenPt.y - totalOffsetY) / zoom
                        )
                    }

                    val initialCanvasTouch = screenToCanvas(firstDown.position)

                    if (activeTool == ActiveTool.DRAW) {
                        currentDrawingPoints.clear()
                        currentDrawingPoints.add(DrawingPoint(initialCanvasTouch.x, initialCanvasTouch.y))
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == firstDown.id } ?: break
                            if (!change.pressed) break
                            change.consume()
                            val cur = screenToCanvas(change.position)
                            currentDrawingPoints.add(DrawingPoint(cur.x, cur.y))
                        }
                        if (currentDrawingPoints.size > 1) {
                            val stroke = DrawingStroke(
                                points = currentDrawingPoints.toList(),
                                color = brushColor,
                                strokeWidth = brushSize,
                                isEraser = isEraser
                            )
                            onAddDrawingStroke(stroke)
                        }
                        currentDrawingPoints.clear()
                        return@awaitEachGesture
                    }

                    // Check which handle or layer is under the touch
                    var activeHandle = HandleType.NONE
                    var grabbedLayer = selectedLayer

                    if (selectedLayer != null && !selectedLayer.isLocked && selectedLayer.isVisible) {
                        val t = selectedLayer.transform
                        val center = t.center
                        val localTouch = canvasToLocal(initialCanvasTouch, center, t.rotation)
                        val halfW = (t.width * t.scaleX) / 2f
                        val halfH = (t.height * t.scaleY) / 2f
                        val touchRadius = 32f / zoom

                        // Test handles in local space
                        activeHandle = when {
                            // Rotate stalk
                            MathUtil.distance(localTouch, Offset(0f, -halfH - 40f / zoom)) < touchRadius -> HandleType.ROTATE
                            // 4 Corners
                            MathUtil.distance(localTouch, Offset(-halfW, -halfH)) < touchRadius -> HandleType.TOP_LEFT
                            MathUtil.distance(localTouch, Offset(halfW, -halfH)) < touchRadius -> HandleType.TOP_RIGHT
                            MathUtil.distance(localTouch, Offset(-halfW, halfH)) < touchRadius -> HandleType.BOTTOM_LEFT
                            MathUtil.distance(localTouch, Offset(halfW, halfH)) < touchRadius -> HandleType.BOTTOM_RIGHT
                            // 4 Edges
                            MathUtil.distance(localTouch, Offset(0f, -halfH)) < touchRadius -> HandleType.TOP_CENTER
                            MathUtil.distance(localTouch, Offset(0f, halfH)) < touchRadius -> HandleType.BOTTOM_CENTER
                            MathUtil.distance(localTouch, Offset(-halfW, 0f)) < touchRadius -> HandleType.LEFT_CENTER
                            MathUtil.distance(localTouch, Offset(halfW, 0f)) < touchRadius -> HandleType.RIGHT_CENTER
                            // Body
                            localTouch.x in -halfW..halfW && localTouch.y in -halfH..halfH -> HandleType.BODY
                            else -> HandleType.NONE
                        }
                    }

                    // If not on selected layer handle/body, check if we hit any other layer
                    if (activeHandle == HandleType.NONE) {
                        for (layer in project.layers.reversed()) {
                            if (!layer.isVisible || layer.isLocked) continue
                            val t = layer.transform
                            val center = t.center
                            val localTouch = canvasToLocal(initialCanvasTouch, center, t.rotation)
                            val halfW = (t.width * t.scaleX) / 2f
                            val halfH = (t.height * t.scaleY) / 2f
                            if (localTouch.x in -halfW..halfW && localTouch.y in -halfH..halfH) {
                                grabbedLayer = layer
                                activeHandle = HandleType.BODY
                                onSelectLayer(layer.id)
                                break
                            }
                        }
                    }

                    var lastTransform = grabbedLayer?.transform

                    while (true) {
                        val event = awaitPointerEvent()
                        val canceled = event.changes.any { it.isConsumed }
                        if (canceled) break

                        val activePointers = event.changes.filter { it.pressed }
                        if (activePointers.isEmpty()) break

                        if (activePointers.size >= 2) {
                            // Multi-touch gestures (Pinch zoom & rotate)
                            val zoomFactor = event.calculateZoom()
                            val pan = event.calculatePan()
                            val rotChange = event.calculateRotation()

                            if (activeHandle != HandleType.NONE && grabbedLayer != null && !grabbedLayer.isLocked) {
                                // Rotate & scale layer directly with 2 fingers
                                val curT = grabbedLayer.transform
                                val newScaleX = (curT.scaleX * zoomFactor).coerceIn(0.2f, 5f)
                                val newScaleY = (curT.scaleY * zoomFactor).coerceIn(0.2f, 5f)
                                val newRot = (curT.rotation + rotChange) % 360f
                                val newT = curT.copy(
                                    x = curT.x + pan.x / zoom,
                                    y = curT.y + pan.y / zoom,
                                    scaleX = newScaleX,
                                    scaleY = newScaleY,
                                    rotation = newRot
                                )
                                grabbedLayer = grabbedLayer.copyWithTransform(newT)
                                onUpdateLayerTransform(grabbedLayer.id, newT)
                            } else {
                                // Pan & Zoom canvas
                                zoom = (zoom * zoomFactor).coerceIn(0.3f, 6f)
                                panOffset += pan
                            }
                            hasMoved = true
                            event.changes.forEach { it.consume() }
                        } else if (activePointers.size == 1) {
                            val change = activePointers[0]
                            val dragAmount = change.positionChange()
                            if (dragAmount.getDistance() > 2f) {
                                hasMoved = true
                            }

                            if (hasMoved && grabbedLayer != null && !grabbedLayer.isLocked && activeHandle != HandleType.NONE) {
                                change.consume()
                                val curT = grabbedLayer.transform
                                val canvasDragX = dragAmount.x / zoom
                                val canvasDragY = dragAmount.y / zoom

                                when (activeHandle) {
                                    HandleType.BODY -> {
                                        val unSnapped = curT.copy(
                                            x = curT.x + canvasDragX,
                                            y = curT.y + canvasDragY
                                        )
                                        val snapRes = MagneticSnapEngine.snapTransform(
                                            current = unSnapped,
                                            canvasWidth = project.width.toFloat(),
                                            canvasHeight = project.height.toFloat(),
                                            otherLayers = project.layers.filter { it.id != grabbedLayer.id },
                                            snapEnabled = isSnapEnabled
                                        )
                                        activeGuideLines = snapRes.guideLines
                                        grabbedLayer = grabbedLayer.copyWithTransform(snapRes.snappedTransform)
                                        onUpdateLayerTransform(grabbedLayer.id, snapRes.snappedTransform)
                                    }
                                    HandleType.ROTATE -> {
                                        val currentCanvasTouch = screenToCanvas(change.position)
                                        val center = curT.center
                                        val angleRad = atan2(
                                            (currentCanvasTouch.y - center.y).toDouble(),
                                            (currentCanvasTouch.x - center.x).toDouble()
                                        )
                                        val newAngleDeg = (Math.toDegrees(angleRad).toFloat() + 90f + 360f) % 360f
                                        val updatedT = curT.copy(rotation = newAngleDeg)
                                        grabbedLayer = grabbedLayer.copyWithTransform(updatedT)
                                        onUpdateLayerTransform(grabbedLayer.id, updatedT)
                                    }
                                    HandleType.BOTTOM_RIGHT -> {
                                        // Diagonal Resize
                                        val angleRad = Math.toRadians(-curT.rotation.toDouble())
                                        val localDragX = (canvasDragX * cos(angleRad) - canvasDragY * sin(angleRad)).toFloat()
                                        val localDragY = (canvasDragX * sin(angleRad) + canvasDragY * cos(angleRad)).toFloat()
                                        val newW = max(30f, curT.width + localDragX * 2)
                                        val newH = max(30f, curT.height + localDragY * 2)
                                        val updatedT = curT.copy(
                                            width = newW,
                                            height = newH,
                                            x = curT.x - localDragX,
                                            y = curT.y - localDragY
                                        )
                                        grabbedLayer = grabbedLayer.copyWithTransform(updatedT)
                                        onUpdateLayerTransform(grabbedLayer.id, updatedT)
                                    }
                                    HandleType.TOP_LEFT -> {
                                        val angleRad = Math.toRadians(-curT.rotation.toDouble())
                                        val localDragX = (canvasDragX * cos(angleRad) - canvasDragY * sin(angleRad)).toFloat()
                                        val localDragY = (canvasDragX * sin(angleRad) + canvasDragY * cos(angleRad)).toFloat()
                                        val newW = max(30f, curT.width - localDragX * 2)
                                        val newH = max(30f, curT.height - localDragY * 2)
                                        val updatedT = curT.copy(
                                            width = newW,
                                            height = newH,
                                            x = curT.x + localDragX,
                                            y = curT.y + localDragY
                                        )
                                        grabbedLayer = grabbedLayer.copyWithTransform(updatedT)
                                        onUpdateLayerTransform(grabbedLayer.id, updatedT)
                                    }
                                    HandleType.TOP_RIGHT -> {
                                        val angleRad = Math.toRadians(-curT.rotation.toDouble())
                                        val localDragX = (canvasDragX * cos(angleRad) - canvasDragY * sin(angleRad)).toFloat()
                                        val localDragY = (canvasDragX * sin(angleRad) + canvasDragY * cos(angleRad)).toFloat()
                                        val newW = max(30f, curT.width + localDragX * 2)
                                        val newH = max(30f, curT.height - localDragY * 2)
                                        val updatedT = curT.copy(
                                            width = newW,
                                            height = newH,
                                            x = curT.x - localDragX,
                                            y = curT.y + localDragY
                                        )
                                        grabbedLayer = grabbedLayer.copyWithTransform(updatedT)
                                        onUpdateLayerTransform(grabbedLayer.id, updatedT)
                                    }
                                    HandleType.BOTTOM_LEFT -> {
                                        val angleRad = Math.toRadians(-curT.rotation.toDouble())
                                        val localDragX = (canvasDragX * cos(angleRad) - canvasDragY * sin(angleRad)).toFloat()
                                        val localDragY = (canvasDragX * sin(angleRad) + canvasDragY * cos(angleRad)).toFloat()
                                        val newW = max(30f, curT.width - localDragX * 2)
                                        val newH = max(30f, curT.height + localDragY * 2)
                                        val updatedT = curT.copy(
                                            width = newW,
                                            height = newH,
                                            x = curT.x + localDragX,
                                            y = curT.y - localDragY
                                        )
                                        grabbedLayer = grabbedLayer.copyWithTransform(updatedT)
                                        onUpdateLayerTransform(grabbedLayer.id, updatedT)
                                    }
                                    HandleType.RIGHT_CENTER, HandleType.LEFT_CENTER -> {
                                        val angleRad = Math.toRadians(-curT.rotation.toDouble())
                                        val localDragX = (canvasDragX * cos(angleRad) - canvasDragY * sin(angleRad)).toFloat()
                                        val newW = max(30f, curT.width + (if (activeHandle == HandleType.RIGHT_CENTER) localDragX else -localDragX) * 2)
                                        val updatedT = curT.copy(width = newW)
                                        grabbedLayer = grabbedLayer.copyWithTransform(updatedT)
                                        onUpdateLayerTransform(grabbedLayer.id, updatedT)
                                    }
                                    HandleType.BOTTOM_CENTER, HandleType.TOP_CENTER -> {
                                        val angleRad = Math.toRadians(-curT.rotation.toDouble())
                                        val localDragY = (canvasDragX * sin(angleRad) + canvasDragY * cos(angleRad)).toFloat()
                                        val newH = max(30f, curT.height + (if (activeHandle == HandleType.BOTTOM_CENTER) localDragY else -localDragY) * 2)
                                        val updatedT = curT.copy(height = newH)
                                        grabbedLayer = grabbedLayer.copyWithTransform(updatedT)
                                        onUpdateLayerTransform(grabbedLayer.id, updatedT)
                                    }
                                    else -> {}
                                }
                            } else if (activeHandle == HandleType.NONE) {
                                // Pan canvas
                                change.consume()
                                panOffset += dragAmount
                            }
                        }
                    }

                    activeGuideLines = emptyList()

                    // Handle quick Tap (selection or deselect)
                    val duration = System.currentTimeMillis() - downTime
                    if (!hasMoved && duration < 250) {
                        if (activeHandle == HandleType.BODY && grabbedLayer != null) {
                            onSelectLayer(grabbedLayer.id)
                        } else if (activeHandle == HandleType.NONE) {
                            onSelectLayer(null)
                        }
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = project.width.toFloat()
            val canvasH = project.height.toFloat()

            val defaultOffsetX = (size.width - canvasW * zoom) / 2f
            val defaultOffsetY = (size.height - canvasH * zoom) / 2f

            translate(left = defaultOffsetX + panOffset.x, top = defaultOffsetY + panOffset.y) {
                scale(scaleX = zoom, scaleY = zoom, pivot = Offset.Zero) {

                    // 1. Draw Project Background
                    CanvasRenderer.renderBackground(this, project, Size(canvasW, canvasH))

                    // 2. Optional Grid
                    if (isGridEnabled) {
                        drawGrid(canvasW, canvasH)
                    }

                    // 3. Render all layers in z-order
                    val sortedLayers = project.layers.sortedBy { it.zIndex }
                    for (layer in sortedLayers) {
                        CanvasRenderer.renderLayer(this, layer, bitmapCache)
                    }

                    // 4. Live Drawing stroke in progress
                    if (currentDrawingPoints.size > 1) {
                        val livePath = com.example.core.engine.path.PathEngine.createSmoothStrokePath(currentDrawingPoints)
                        drawPath(
                            path = livePath,
                            color = if (isEraser) Color.White.copy(alpha = 0.5f) else Color(brushColor),
                            style = Stroke(width = brushSize, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                        )
                    }

                    // 5. Magnetic Snap Guidelines (Champagne gold dashed lines)
                    for (guide in activeGuideLines) {
                        drawLine(
                            color = ChampagneGold,
                            start = guide.start,
                            end = guide.end,
                            strokeWidth = 1.5f / zoom,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                    }

                    // 6. 8-Point Rotated Transform Bounding Box for selected layer
                    if (selectedLayer != null && selectedLayer.isVisible && activeTool != ActiveTool.DRAW) {
                        drawRotatedBoundingBox(selectedLayer, zoom)
                    }
                }
            }
        }
    }
}

private fun canvasToLocal(canvasPt: Offset, center: Offset, rotationDeg: Float): Offset {
    val rad = Math.toRadians(-rotationDeg.toDouble())
    val dx = canvasPt.x - center.x
    val dy = canvasPt.y - center.y
    val lx = (dx * cos(rad) - dy * sin(rad)).toFloat()
    val ly = (dx * sin(rad) + dy * cos(rad)).toFloat()
    return Offset(lx, ly)
}

private fun DrawScope.drawGrid(width: Float, height: Float) {
    val step = 40f
    val gridColor = Color(0x33FFFFFF)
    var x = 0f
    while (x <= width) {
        drawLine(gridColor, Offset(x, 0f), Offset(x, height), strokeWidth = 0.8f)
        x += step
    }
    var y = 0f
    while (y <= height) {
        drawLine(gridColor, Offset(0f, y), Offset(width, y), strokeWidth = 0.8f)
        y += step
    }
}

private fun DrawScope.drawRotatedBoundingBox(layer: Layer, zoom: Float) {
    val t = layer.transform
    val handleRadius = 7f / zoom
    val boxColor = ChampagneGold
    val w = t.width * t.scaleX
    val h = t.height * t.scaleY
    val halfW = w / 2f
    val halfH = h / 2f

    translate(left = t.x + halfW, top = t.y + halfH) {
        rotate(degrees = t.rotation, pivot = Offset.Zero) {
            // Main Bounding Rectangle
            drawRect(
                color = boxColor,
                topLeft = Offset(-halfW, -halfH),
                size = Size(w, h),
                style = Stroke(width = 1.6f / zoom)
            )

            // 4 Corner Handles
            val cornerHandles = listOf(
                Offset(-halfW, -halfH),
                Offset(halfW, -halfH),
                Offset(-halfW, halfH),
                Offset(halfW, halfH)
            )

            for (pos in cornerHandles) {
                drawCircle(
                    color = Color(0xFF0A0A0C),
                    radius = handleRadius,
                    center = pos
                )
                drawCircle(
                    color = boxColor,
                    radius = handleRadius,
                    center = pos,
                    style = Stroke(width = 2f / zoom)
                )
            }

            // 4 Side Edge Handles
            val edgeHandles = listOf(
                Offset(0f, -halfH),
                Offset(0f, halfH),
                Offset(-halfW, 0f),
                Offset(halfW, 0f)
            )

            val pillW = 14f / zoom
            val pillH = 5f / zoom

            drawRect(
                color = boxColor,
                topLeft = Offset(-pillW / 2f, -halfH - pillH / 2f),
                size = Size(pillW, pillH)
            )
            drawRect(
                color = boxColor,
                topLeft = Offset(-pillW / 2f, halfH - pillH / 2f),
                size = Size(pillW, pillH)
            )
            drawRect(
                color = boxColor,
                topLeft = Offset(-halfW - pillH / 2f, -pillW / 2f),
                size = Size(pillH, pillW)
            )
            drawRect(
                color = boxColor,
                topLeft = Offset(halfW - pillH / 2f, -pillW / 2f),
                size = Size(pillH, pillW)
            )

            // Top Stalk Rotation Handle
            val rotCenter = Offset(0f, -halfH - 36f / zoom)
            drawLine(
                color = boxColor,
                start = Offset(0f, -halfH),
                end = rotCenter,
                strokeWidth = 1.5f / zoom
            )
            drawCircle(
                color = Color(0xFF0A0A0C),
                radius = handleRadius * 1.3f,
                center = rotCenter
            )
            drawCircle(
                color = boxColor,
                radius = handleRadius * 1.3f,
                center = rotCenter,
                style = Stroke(width = 2f / zoom)
            )
        }
    }
}
