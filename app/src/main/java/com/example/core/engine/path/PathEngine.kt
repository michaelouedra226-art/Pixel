package com.example.core.engine.path

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import com.example.core.engine.model.AnchorPoint
import com.example.core.engine.model.DrawingPoint
import com.example.core.engine.model.ShapeType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

object PathEngine {

    fun createShapePath(
        shapeType: ShapeType,
        width: Float,
        height: Float,
        cornerRadius: Float = 16f,
        polygonSides: Int = 5,
        starPoints: Int = 5,
        starInnerRatio: Float = 0.5f
    ): Path {
        val path = Path()
        val w = width.coerceAtLeast(1f)
        val h = height.coerceAtLeast(1f)

        when (shapeType) {
            ShapeType.RECTANGLE -> {
                path.addRect(Rect(0f, 0f, w, h))
            }
            ShapeType.ROUNDED_RECT -> {
                val r = cornerRadius.coerceIn(0f, min(w, h) / 2f)
                path.addRoundRect(
                    RoundRect(
                        left = 0f,
                        top = 0f,
                        right = w,
                        bottom = h,
                        radiusX = r,
                        radiusY = r
                    )
                )
            }
            ShapeType.CIRCLE, ShapeType.OVAL -> {
                path.addOval(Rect(0f, 0f, w, h))
            }
            ShapeType.LINE -> {
                path.moveTo(0f, h / 2f)
                path.lineTo(w, h / 2f)
            }
            ShapeType.POLYGON -> {
                val sides = polygonSides.coerceAtLeast(3)
                val cx = w / 2f
                val cy = h / 2f
                val radius = min(cx, cy)
                val angleStep = (2 * PI / sides).toFloat()
                for (i in 0 until sides) {
                    val angle = -PI.toFloat() / 2f + i * angleStep
                    val px = cx + radius * cos(angle)
                    val py = cy + radius * sin(angle)
                    if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                }
                path.close()
            }
            ShapeType.STAR -> {
                val points = (starPoints.coerceAtLeast(3)) * 2
                val cx = w / 2f
                val cy = h / 2f
                val outerR = min(cx, cy)
                val innerR = outerR * starInnerRatio.coerceIn(0.1f, 0.9f)
                val angleStep = (2 * PI / points).toFloat()
                for (i in 0 until points) {
                    val r = if (i % 2 == 0) outerR else innerR
                    val angle = -PI.toFloat() / 2f + i * angleStep
                    val px = cx + r * cos(angle)
                    val py = cy + r * sin(angle)
                    if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                }
                path.close()
            }
            ShapeType.ARROW -> {
                val headW = w * 0.4f
                val shaftH = h * 0.35f
                val shaftTop = (h - shaftH) / 2f
                val shaftBottom = shaftTop + shaftH
                path.moveTo(0f, shaftTop)
                path.lineTo(w - headW, shaftTop)
                path.lineTo(w - headW, 0f)
                path.lineTo(w, h / 2f)
                path.lineTo(w - headW, h)
                path.lineTo(w - headW, shaftBottom)
                path.lineTo(0f, shaftBottom)
                path.close()
            }
            ShapeType.HEART -> {
                // Precise parametric heart path
                path.moveTo(w / 2f, h * 0.85f)
                path.cubicTo(
                    w * 0.1f, h * 0.6f,
                    0f, h * 0.25f,
                    w * 0.25f, h * 0.05f
                )
                path.cubicTo(
                    w * 0.45f, -h * 0.05f,
                    w / 2f, h * 0.2f,
                    w / 2f, h * 0.25f
                )
                path.cubicTo(
                    w / 2f, h * 0.2f,
                    w * 0.55f, -h * 0.05f,
                    w * 0.75f, h * 0.05f
                )
                path.cubicTo(
                    w, h * 0.25f,
                    w * 0.9f, h * 0.6f,
                    w / 2f, h * 0.85f
                )
                path.close()
            }
            ShapeType.BEZIER_CUSTOM -> {
                path.addRect(Rect(0f, 0f, w, h))
            }
        }
        return path
    }

    fun createBezierPath(anchors: List<AnchorPoint>, isClosed: Boolean): Path {
        val path = Path()
        if (anchors.isEmpty()) return path

        path.moveTo(anchors[0].position.x, anchors[0].position.y)

        for (i in 0 until anchors.size - 1) {
            val curr = anchors[i]
            val next = anchors[i + 1]
            val cp1 = curr.position + curr.handleOut
            val cp2 = next.position + next.handleIn
            path.cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, next.position.x, next.position.y)
        }

        if (isClosed && anchors.size > 2) {
            val last = anchors.last()
            val first = anchors.first()
            val cp1 = last.position + last.handleOut
            val cp2 = first.position + first.handleIn
            path.cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, first.position.x, first.position.y)
            path.close()
        }

        return path
    }

    fun createSmoothStrokePath(points: List<DrawingPoint>): Path {
        val path = Path()
        if (points.isEmpty()) return path
        if (points.size == 1) {
            path.addOval(Rect(points[0].x - 1f, points[0].y - 1f, points[0].x + 1f, points[0].y + 1f))
            return path
        }

        path.moveTo(points[0].x, points[0].y)

        if (points.size == 2) {
            path.lineTo(points[1].x, points[1].y)
            return path
        }

        // Catmull-Rom or Quadratic Midpoint Bezier smoothing for silky smooth lines
        for (i in 1 until points.size - 1) {
            val midX = (points[i].x + points[i + 1].x) / 2f
            val midY = (points[i].y + points[i + 1].y) / 2f
            path.quadraticBezierTo(points[i].x, points[i].y, midX, midY)
        }

        path.lineTo(points.last().x, points.last().y)
        return path
    }
}
