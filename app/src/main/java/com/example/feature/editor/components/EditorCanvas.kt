package com.example.feature.editor.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.core.common.MathUtil
import com.example.core.engine.model.AnchorPoint
import com.example.core.engine.model.BezierLayer
import com.example.core.engine.model.CanvasProject
import com.example.core.engine.model.DrawingLayer
import com.example.core.engine.model.DrawingPoint
import com.example.core.engine.model.DrawingStroke
import com.example.core.engine.model.Layer
import com.example.core.engine.model.Transform
import com.example.core.engine.render.CanvasRenderer
import com.example.core.engine.snap.MagneticSnapEngine
import com.example.core.engine.snap.SnapGuideLine
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.ObsidianBg
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

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

    var activeHandle by remember { mutableStateOf(HandleType.NONE) }
    var initialTouchPoint by remember { mutableStateOf(Offset.Zero) }
    var initialTransform by remember { mutableStateOf<Transform?>(null) }
    var activeGuideLines by remember { mutableStateOf<List<SnapGuideLine>>(emptyList()) }

    // Drawing in progress points
    val currentDrawingPoints = remember { mutableStateListOf<DrawingPoint>() }

    val selectedLayer = project.layers.find { it.id == selectedLayerId }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .pointerInput(activeTool) {
                if (activeTool == ActiveTool.SELECT || activeTool == ActiveTool.TEXT || activeTool == ActiveTool.SHAPES || activeTool == ActiveTool.BACKGROUND) {
                    detectTransformGestures { _, pan, gestureZoom, _ ->
                        zoom = (zoom * gestureZoom).coerceIn(0.2f, 6f)
                        panOffset += pan
                    }
                }
            }
            .pointerInput(activeTool, project.layers, selectedLayerId) {
                detectTapGestures(
                    onDoubleTap = {
                        // Double tap to fit / reset zoom
                        zoom = 1f
                        panOffset = Offset.Zero
                    },
                    onTap = { tapScreenOffset ->
                        if (activeTool == ActiveTool.DRAW) return@detectTapGestures

                        // Convert screen touch to canvas local coordinates
                        val canvasX = (tapScreenOffset.x - panOffset.x) / zoom
                        val canvasY = (tapScreenOffset.y - panOffset.y) / zoom
                        val localTouch = Offset(canvasX, canvasY)

                        // Check Bézier anchor tap if in Bézier mode
                        if (activeTool == ActiveTool.BEZIER && selectedLayer is BezierLayer) {
                            val anchorIdx = selectedLayer.anchors.indexOfFirst { anchor ->
                                val pos = selectedLayer.transform.x + anchor.position.x
                                val posY = selectedLayer.transform.y + anchor.position.y
                                MathUtil.distance(localTouch, Offset(pos, posY)) < 30f / zoom
                            }
                            if (anchorIdx != -1) {
                                onSelectAnchor(anchorIdx)
                                return@detectTapGestures
                            }
                        }

                        // Hit test layers in reverse z-order (topmost first)
                        var hitLayerId: String? = null
                        for (layer in project.layers.reversed()) {
                            if (!layer.isVisible || layer.isLocked) continue
                            val t = layer.transform
                            val layerRect = Rect(t.x, t.y, t.x + t.width * t.scaleX, t.y + t.height * t.scaleY)
                            if (layerRect.contains(localTouch)) {
                                hitLayerId = layer.id
                                break
                            }
                        }
                        onSelectLayer(hitLayerId)
                        onSelectAnchor(null)
                    }
                )
            }
            .pointerInput(activeTool, selectedLayerId, zoom, panOffset, isSnapEnabled) {
                detectDragGestures(
                    onDragStart = { startOffset ->
                        val canvasX = (startOffset.x - panOffset.x) / zoom
                        val canvasY = (startOffset.y - panOffset.y) / zoom
                        initialTouchPoint = Offset(canvasX, canvasY)

                        if (activeTool == ActiveTool.DRAW) {
                            currentDrawingPoints.clear()
                            currentDrawingPoints.add(DrawingPoint(canvasX, canvasY))
                            return@detectDragGestures
                        }

                        if (selectedLayer != null && !selectedLayer.isLocked) {
                            initialTransform = selectedLayer.transform
                            val t = selectedLayer.transform
                            val layerRect = Rect(t.x, t.y, t.x + t.width * t.scaleX, t.y + t.height * t.scaleY)
                            val rotCenter = Offset(t.x + (t.width * t.scaleX) / 2f, t.y - 40f)

                            activeHandle = when {
                                MathUtil.distance(initialTouchPoint, rotCenter) < 28f / zoom -> HandleType.ROTATE
                                MathUtil.distance(initialTouchPoint, Offset(t.x, t.y)) < 24f / zoom -> HandleType.TOP_LEFT
                                MathUtil.distance(initialTouchPoint, Offset(t.x + t.width * t.scaleX, t.y)) < 24f / zoom -> HandleType.TOP_RIGHT
                                MathUtil.distance(initialTouchPoint, Offset(t.x, t.y + t.height * t.scaleY)) < 24f / zoom -> HandleType.BOTTOM_LEFT
                                MathUtil.distance(initialTouchPoint, Offset(t.x + t.width * t.scaleX, t.y + t.height * t.scaleY)) < 24f / zoom -> HandleType.BOTTOM_RIGHT
                                layerRect.contains(initialTouchPoint) -> HandleType.BODY
                                else -> HandleType.NONE
                            }
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val canvasDragX = dragAmount.x / zoom
                        val canvasDragY = dragAmount.y / zoom

                        if (activeTool == ActiveTool.DRAW) {
                            val cur = currentDrawingPoints.lastOrNull() ?: DrawingPoint(0f, 0f)
                            val nextPoint = DrawingPoint(cur.x + canvasDragX, cur.y + canvasDragY)
                            currentDrawingPoints.add(nextPoint)
                            return@detectDragGestures
                        }

                        if (selectedLayer != null && initialTransform != null && !selectedLayer.isLocked) {
                            val currT = selectedLayer.transform
                            when (activeHandle) {
                                HandleType.BODY -> {
                                    val unSnapped = currT.copy(
                                        x = currT.x + canvasDragX,
                                        y = currT.y + canvasDragY
                                    )
                                    val snapRes = MagneticSnapEngine.snapTransform(
                                        current = unSnapped,
                                        canvasWidth = project.width.toFloat(),
                                        canvasHeight = project.height.toFloat(),
                                        otherLayers = project.layers.filter { it.id != selectedLayer.id },
                                        snapEnabled = isSnapEnabled
                                    )
                                    activeGuideLines = snapRes.guideLines
                                    onUpdateLayerTransform(selectedLayer.id, snapRes.snappedTransform)
                                }
                                HandleType.BOTTOM_RIGHT -> {
                                    val newW = max(30f, currT.width + canvasDragX)
                                    val newH = max(30f, currT.height + canvasDragY)
                                    onUpdateLayerTransform(selectedLayer.id, currT.copy(width = newW, height = newH))
                                }
                                HandleType.ROTATE -> {
                                    val center = currT.center
                                    val currentTouch = Offset(
                                        (change.position.x - panOffset.x) / zoom,
                                        (change.position.y - panOffset.y) / zoom
                                    )
                                    val angle = MathUtil.calculateAngle(center, currentTouch)
                                    onUpdateLayerTransform(selectedLayer.id, currT.copy(rotation = angle - 90f))
                                }
                                else -> {
                                    // Other corner handles resize
                                    val newW = max(30f, currT.width + canvasDragX)
                                    val newH = max(30f, currT.height + canvasDragY)
                                    onUpdateLayerTransform(selectedLayer.id, currT.copy(width = newW, height = newH))
                                }
                            }
                        }
                    },
                    onDragEnd = {
                        activeGuideLines = emptyList()
                        if (activeTool == ActiveTool.DRAW && currentDrawingPoints.isNotEmpty()) {
                            val stroke = DrawingStroke(
                                points = currentDrawingPoints.toList(),
                                color = brushColor,
                                strokeWidth = brushSize,
                                isEraser = isEraser
                            )
                            onAddDrawingStroke(stroke)
                            currentDrawingPoints.clear()
                        }
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = project.width.toFloat()
            val canvasH = project.height.toFloat()

            // Center canvas in viewport initially
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

                    // 6. Transform Bounding Box for selected layer
                    if (selectedLayer != null && selectedLayer.isVisible && activeTool != ActiveTool.DRAW) {
                        drawTransformBoundingBox(selectedLayer, zoom)
                    }
                }
            }
        }
    }
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

private fun DrawScope.drawTransformBoundingBox(layer: Layer, zoom: Float) {
    val t = layer.transform
    val handleRadius = 6f / zoom
    val boxColor = ChampagneGold
    val w = t.width * t.scaleX
    val h = t.height * t.scaleY

    translate(left = t.x, top = t.y) {
        // Bounding outline
        drawRect(
            color = boxColor,
            topLeft = Offset.Zero,
            size = Size(w, h),
            style = Stroke(width = 1.5f / zoom)
        )

        // 4 Corner Handles
        listOf(
            Offset(0f, 0f),
            Offset(w, 0f),
            Offset(0f, h),
            Offset(w, h)
        ).forEach { pos ->
            drawCircle(
                color = Color(0xFF0A0A0C),
                radius = handleRadius,
                center = pos
            )
            drawCircle(
                color = boxColor,
                radius = handleRadius,
                center = pos,
                style = Stroke(width = 1.5f / zoom)
            )
        }

        // Top Rotation Handle
        val rotCenter = Offset(w / 2f, -30f / zoom)
        drawLine(
            color = boxColor,
            start = Offset(w / 2f, 0f),
            end = rotCenter,
            strokeWidth = 1.2f / zoom
        )
        drawCircle(
            color = boxColor,
            radius = handleRadius * 1.2f,
            center = rotCenter
        )
    }
}
