package com.example.feature.editor.panels

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.engine.model.ColorFill
import com.example.core.engine.model.ShapeLayer
import com.example.core.engine.model.ShapeType
import com.example.core.ui.LuxuryButton
import com.example.core.ui.LuxuryColorPicker
import com.example.core.ui.LuxurySliderRow
import com.example.ui.theme.BorderGlass
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ShapeStudioPanel(
    layer: ShapeLayer,
    onUpdate: (ShapeLayer) -> Unit,
    onConvertToPath: () -> Unit,
    modifier: Modifier = Modifier
) {
    var subTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Shape Type", "Fill Color", "Stroke & Shadow")

    val shapeTypes = listOf(
        ShapeType.ROUNDED_RECT to "Rounded",
        ShapeType.RECTANGLE to "Rectangle",
        ShapeType.CIRCLE to "Circle",
        ShapeType.STAR to "Star",
        ShapeType.POLYGON to "Polygon",
        ShapeType.HEART to "Heart",
        ShapeType.ARROW to "Arrow",
        ShapeType.LINE to "Line"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceDark)
            .padding(14.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Sub-tabs row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                val isSelected = subTab == index
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) ChampagneGold else SurfaceElevated)
                        .clickable { subTab = index }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = title,
                        color = if (isSelected) ObsidianBg else TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        when (subTab) {
            0 -> {
                Text("Select Geometric Form", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    shapeTypes.forEach { (type, name) ->
                        val isSelected = layer.shapeType == type
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) ChampagneGold.copy(alpha = 0.15f) else SurfaceElevated)
                                .border(1.5.dp, if (isSelected) ChampagneGold else BorderGlass, RoundedCornerShape(12.dp))
                                .clickable { onUpdate(layer.copy(shapeType = type)) }
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = name,
                                color = if (isSelected) ChampagneGold else TextPrimary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (layer.shapeType == ShapeType.ROUNDED_RECT) {
                    LuxurySliderRow(
                        title = "Corner Radius",
                        value = layer.cornerRadius,
                        onValueChange = { onUpdate(layer.copy(cornerRadius = it)) },
                        valueRange = 0f..100f,
                        valueDisplay = "${layer.cornerRadius.toInt()} dp"
                    )
                } else if (layer.shapeType == ShapeType.POLYGON) {
                    LuxurySliderRow(
                        title = "Polygon Sides",
                        value = layer.polygonSides.toFloat(),
                        onValueChange = { onUpdate(layer.copy(polygonSides = it.toInt())) },
                        valueRange = 3f..12f,
                        valueDisplay = "${layer.polygonSides}",
                        steps = 8
                    )
                } else if (layer.shapeType == ShapeType.STAR) {
                    LuxurySliderRow(
                        title = "Star Points",
                        value = layer.starPoints.toFloat(),
                        onValueChange = { onUpdate(layer.copy(starPoints = it.toInt())) },
                        valueRange = 3f..12f,
                        valueDisplay = "${layer.starPoints}",
                        steps = 8
                    )
                    LuxurySliderRow(
                        title = "Inner Radius Ratio",
                        value = layer.starInnerRadiusRatio,
                        onValueChange = { onUpdate(layer.copy(starInnerRadiusRatio = it)) },
                        valueRange = 0.2f..0.8f,
                        valueDisplay = "${(layer.starInnerRadiusRatio * 100).toInt()}%"
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                LuxuryButton(
                    text = "Convert to Editable Bézier",
                    onClick = onConvertToPath,
                    isPrimary = false,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            1 -> {
                Text("Shape Fill", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                LuxuryColorPicker(
                    currentFill = layer.fill,
                    onFillChanged = { onUpdate(layer.copy(fill = it)) }
                )
            }
            2 -> {
                // Stroke
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Border Stroke", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Switch(
                        checked = layer.stroke.isEnabled,
                        onCheckedChange = { onUpdate(layer.copy(stroke = layer.stroke.copy(isEnabled = it))) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ChampagneGold,
                            checkedTrackColor = ChampagneGold.copy(alpha = 0.5f)
                        )
                    )
                }

                AnimatedVisibility(visible = layer.stroke.isEnabled) {
                    Column {
                        LuxurySliderRow(
                            title = "Stroke Width",
                            value = layer.stroke.width,
                            onValueChange = { onUpdate(layer.copy(stroke = layer.stroke.copy(width = it))) },
                            valueRange = 1f..40f,
                            valueDisplay = "${layer.stroke.width.toInt()} px"
                        )
                        LuxuryColorPicker(
                            currentFill = ColorFill(solidColor = layer.stroke.color),
                            onFillChanged = { onUpdate(layer.copy(stroke = layer.stroke.copy(color = it.solidColor))) },
                            allowGradients = false
                        )
                    }
                }
            }
        }
    }
}
