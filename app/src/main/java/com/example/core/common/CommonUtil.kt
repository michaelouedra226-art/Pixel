package com.example.core.common

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object HapticUtil {
    fun performHaptic(context: Context, isStrong: Boolean = false) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val effect = if (isStrong) {
                        VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                    } else {
                        VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                    }
                    vibrator.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(if (isStrong) 25L else 10L)
                }
            }
        } catch (_: Exception) {}
    }
}

object MathUtil {
    fun rotatePoint(point: Offset, center: Offset, angleDegrees: Float): Offset {
        val angleRad = Math.toRadians(angleDegrees.toDouble())
        val cos = cos(angleRad)
        val sin = sin(angleRad)
        val dx = point.x - center.x
        val dy = point.y - center.y
        val rotatedX = center.x + (dx * cos - dy * sin).toFloat()
        val rotatedY = center.y + (dx * sin + dy * cos).toFloat()
        return Offset(rotatedX, rotatedY)
    }

    fun calculateAngle(center: Offset, point: Offset): Float {
        val dx = point.x - center.x
        val dy = point.y - center.y
        var degrees = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
        if (degrees < 0) degrees += 360f
        return degrees
    }

    fun distance(p1: Offset, p2: Offset): Float {
        val dx = p1.x - p2.x
        val dy = p1.y - p2.y
        return sqrt((dx * dx + dy * dy).toDouble()).toFloat()
    }

    fun lerp(start: Float, stop: Float, fraction: Float): Float {
        return start + (stop - start) * fraction
    }

    fun clamp(value: Float, min: Float, max: Float): Float {
        return value.coerceIn(min, max)
    }
}
