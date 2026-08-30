package com.example.core.engine.snap

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.example.core.engine.model.Layer
import com.example.core.engine.model.Transform
import kotlin.math.abs

data class SnapGuideLine(
    val start: Offset,
    val end: Offset,
    val isHorizontal: Boolean
)

data class SnapResult(
    val snappedTransform: Transform,
    val guideLines: List<SnapGuideLine>
)

object MagneticSnapEngine {
    private const val SNAP_THRESHOLD = 12f

    fun snapTransform(
        current: Transform,
        canvasWidth: Float,
        canvasHeight: Float,
        otherLayers: List<Layer>,
        snapEnabled: Boolean = true
    ): SnapResult {
        if (!snapEnabled) {
            return SnapResult(current, emptyList())
        }

        var newX = current.x
        var newY = current.y
        val w = current.width * current.scaleX
        val h = current.height * current.scaleY

        val guides = mutableListOf<SnapGuideLine>()

        val left = newX
        val right = newX + w
        val centerX = newX + w / 2f

        val top = newY
        val bottom = newY + h
        val centerY = newY + h / 2f

        // Canvas Snap Points
        val canvasCenterX = canvasWidth / 2f
        val canvasCenterY = canvasHeight / 2f

        // 1. Horizontal Snap (X axis)
        // Center Snap
        if (abs(centerX - canvasCenterX) < SNAP_THRESHOLD) {
            newX = canvasCenterX - w / 2f
            guides.add(SnapGuideLine(Offset(canvasCenterX, 0f), Offset(canvasCenterX, canvasHeight), false))
        } else if (abs(left - 0f) < SNAP_THRESHOLD) {
            newX = 0f
            guides.add(SnapGuideLine(Offset(0f, 0f), Offset(0f, canvasHeight), false))
        } else if (abs(right - canvasWidth) < SNAP_THRESHOLD) {
            newX = canvasWidth - w
            guides.add(SnapGuideLine(Offset(canvasWidth, 0f), Offset(canvasWidth, canvasHeight), false))
        }

        // 2. Vertical Snap (Y axis)
        // Center Snap
        if (abs(centerY - canvasCenterY) < SNAP_THRESHOLD) {
            newY = canvasCenterY - h / 2f
            guides.add(SnapGuideLine(Offset(0f, canvasCenterY), Offset(canvasWidth, canvasCenterY), true))
        } else if (abs(top - 0f) < SNAP_THRESHOLD) {
            newY = 0f
            guides.add(SnapGuideLine(Offset(0f, 0f), Offset(canvasWidth, 0f), true))
        } else if (abs(bottom - canvasHeight) < SNAP_THRESHOLD) {
            newY = canvasHeight - h
            guides.add(SnapGuideLine(Offset(0f, canvasHeight), Offset(canvasWidth, canvasHeight), true))
        }

        // 3. Other layers snap
        for (other in otherLayers) {
            if (!other.isVisible) continue
            val otherCenterX = other.transform.x + (other.transform.width * other.transform.scaleX) / 2f
            val otherCenterY = other.transform.y + (other.transform.height * other.transform.scaleY) / 2f

            if (abs(centerX - otherCenterX) < SNAP_THRESHOLD && guides.none { !it.isHorizontal }) {
                newX = otherCenterX - w / 2f
                guides.add(SnapGuideLine(Offset(otherCenterX, 0f), Offset(otherCenterX, canvasHeight), false))
            }

            if (abs(centerY - otherCenterY) < SNAP_THRESHOLD && guides.none { it.isHorizontal }) {
                newY = otherCenterY - h / 2f
                guides.add(SnapGuideLine(Offset(0f, otherCenterY), Offset(canvasWidth, otherCenterY), true))
            }
        }

        return SnapResult(
            snappedTransform = current.copy(x = newX, y = newY),
            guideLines = guides
        )
    }
}
