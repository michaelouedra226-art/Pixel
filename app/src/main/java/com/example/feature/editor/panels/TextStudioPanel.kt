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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.automirrored.filled.FormatAlignRight
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.core.engine.model.Layer3DEffect
import com.example.core.engine.model.ShadowDef
import com.example.core.engine.model.StrokeDef
import com.example.core.engine.model.TextCurvature
import com.example.core.engine.model.TextLayer
import com.example.core.engine.model.TextReflection
import com.example.core.ui.LuxuryColorPicker
import com.example.core.ui.LuxuryIconButton
import com.example.core.ui.LuxurySliderRow
import com.example.ui.theme.BorderGlass
import com.example.ui.theme.BorderGold
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TextStudioPanel(
    layer: TextLayer,
    onUpdate: (TextLayer) -> Unit,
    modifier: Modifier = Modifier
) {
    var subTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Text & Font", "Color & Fill", "Stroke & Shadow", "3D & Reflection")

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
                // Text input
                OutlinedTextField(
                    value = layer.text,
                    onValueChange = { onUpdate(layer.copy(text = it)) },
                    label = { Text("Content", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = ChampagneGold,
                        unfocusedBorderColor = BorderGlass,
                        focusedContainerColor = SurfaceCard,
                        unfocusedContainerColor = SurfaceCard
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Font Styling buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LuxuryIconButton(
                        icon = Icons.Default.FormatBold,
                        isSelected = layer.isBold,
                        onClick = { onUpdate(layer.copy(isBold = !layer.isBold)) },
                        contentDescription = "Bold"
                    )
                    LuxuryIconButton(
                        icon = Icons.Default.FormatItalic,
                        isSelected = layer.isItalic,
                        onClick = { onUpdate(layer.copy(isItalic = !layer.isItalic)) },
                        contentDescription = "Italic"
                    )
                    LuxuryIconButton(
                        icon = Icons.Default.FormatUnderlined,
                        isSelected = layer.isUnderline,
                        onClick = { onUpdate(layer.copy(isUnderline = !layer.isUnderline)) },
                        contentDescription = "Underline"
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    LuxuryIconButton(
                        icon = Icons.AutoMirrored.Filled.FormatAlignLeft,
                        isSelected = layer.textAlign == "LEFT",
                        onClick = { onUpdate(layer.copy(textAlign = "LEFT")) },
                        contentDescription = "Align Left"
                    )
                    LuxuryIconButton(
                        icon = Icons.Default.FormatAlignCenter,
                        isSelected = layer.textAlign == "CENTER",
                        onClick = { onUpdate(layer.copy(textAlign = "CENTER")) },
                        contentDescription = "Align Center"
                    )
                    LuxuryIconButton(
                        icon = Icons.AutoMirrored.Filled.FormatAlignRight,
                        isSelected = layer.textAlign == "RIGHT",
                        onClick = { onUpdate(layer.copy(textAlign = "RIGHT")) },
                        contentDescription = "Align Right"
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                LuxurySliderRow(
                    title = "Font Size",
                    value = layer.fontSize,
                    onValueChange = { onUpdate(layer.copy(fontSize = it)) },
                    valueRange = 16f..160f,
                    valueDisplay = "${layer.fontSize.toInt()} px"
                )

                LuxurySliderRow(
                    title = "Letter Spacing (Tracking)",
                    value = layer.letterSpacing,
                    onValueChange = { onUpdate(layer.copy(letterSpacing = it)) },
                    valueRange = -5f..25f,
                    valueDisplay = "${layer.letterSpacing.toInt()}"
                )
            }
            1 -> {
                // Color & Fill
                Text("Text Fill Color", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                LuxuryColorPicker(
                    currentFill = layer.fill,
                    onFillChanged = { onUpdate(layer.copy(fill = it)) }
                )
            }
            2 -> {
                // Stroke / Outline
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Outer Stroke", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
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
                            valueRange = 1f..30f,
                            valueDisplay = "${layer.stroke.width.toInt()} px"
                        )
                        LuxuryColorPicker(
                            currentFill = ColorFill(solidColor = layer.stroke.color),
                            onFillChanged = { onUpdate(layer.copy(stroke = layer.stroke.copy(color = it.solidColor))) },
                            allowGradients = false
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Drop Shadow
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Drop Shadow", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Switch(
                        checked = layer.shadow.isEnabled,
                        onCheckedChange = { onUpdate(layer.copy(shadow = layer.shadow.copy(isEnabled = it))) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ChampagneGold,
                            checkedTrackColor = ChampagneGold.copy(alpha = 0.5f)
                        )
                    )
                }

                AnimatedVisibility(visible = layer.shadow.isEnabled) {
                    Column {
                        LuxurySliderRow(
                            title = "Shadow Blur",
                            value = layer.shadow.radius,
                            onValueChange = { onUpdate(layer.copy(shadow = layer.shadow.copy(radius = it))) },
                            valueRange = 0f..40f
                        )
                        LuxurySliderRow(
                            title = "Shadow Offset Y",
                            value = layer.shadow.dy,
                            onValueChange = { onUpdate(layer.copy(shadow = layer.shadow.copy(dy = it))) },
                            valueRange = -30f..30f
                        )
                    }
                }
            }
            3 -> {
                // 3D Extrusion
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("3D Text Extrusion", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Switch(
                        checked = layer.effect3D.isEnabled,
                        onCheckedChange = { onUpdate(layer.copy(effect3D = layer.effect3D.copy(isEnabled = it))) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ChampagneGold,
                            checkedTrackColor = ChampagneGold.copy(alpha = 0.5f)
                        )
                    )
                }

                AnimatedVisibility(visible = layer.effect3D.isEnabled) {
                    Column {
                        LuxurySliderRow(
                            title = "3D Depth",
                            value = layer.effect3D.depth,
                            onValueChange = { onUpdate(layer.copy(effect3D = layer.effect3D.copy(depth = it))) },
                            valueRange = 1f..35f,
                            valueDisplay = "${layer.effect3D.depth.toInt()} px"
                        )
                        LuxurySliderRow(
                            title = "Light Angle",
                            value = layer.effect3D.lightAngle,
                            onValueChange = { onUpdate(layer.copy(effect3D = layer.effect3D.copy(lightAngle = it))) },
                            valueRange = 0f..360f,
                            valueDisplay = "${layer.effect3D.lightAngle.toInt()}°"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Reflection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Mirror Reflection", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Switch(
                        checked = layer.reflection.isEnabled,
                        onCheckedChange = { onUpdate(layer.copy(reflection = layer.reflection.copy(isEnabled = it))) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ChampagneGold,
                            checkedTrackColor = ChampagneGold.copy(alpha = 0.5f)
                        )
                    )
                }

                AnimatedVisibility(visible = layer.reflection.isEnabled) {
                    LuxurySliderRow(
                        title = "Reflection Opacity",
                        value = layer.reflection.opacity,
                        onValueChange = { onUpdate(layer.copy(reflection = layer.reflection.copy(opacity = it))) },
                        valueRange = 0.05f..1f,
                        valueDisplay = "${(layer.reflection.opacity * 100).toInt()}%"
                    )
                }
            }
        }
    }
}
