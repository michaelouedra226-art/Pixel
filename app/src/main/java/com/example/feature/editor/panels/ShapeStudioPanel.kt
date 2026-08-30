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
import com.example.core.data.repository.TextureLibrary
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
    val tabs = listOf("Formes", "Couleur & Texture", "Contour & Ombres", "3D & Biseau")

    val shapeTypes = listOf(
        ShapeType.ROUNDED_RECT to "Coins arrondis",
        ShapeType.RECTANGLE to "Rectangle",
        ShapeType.CIRCLE to "Cercle",
        ShapeType.STAR to "Étoile",
        ShapeType.POLYGON to "Polygone",
        ShapeType.HEART to "Cœur",
        ShapeType.ARROW to "Flèche",
        ShapeType.LINE to "Ligne"
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
                Text("Sélectionnez la forme géométrique", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                        label = "Rayon des coins",
                        value = layer.cornerRadius,
                        valueRange = 0f..100f,
                        unit = "px",
                        onValueChange = { onUpdate(layer.copy(cornerRadius = it)) }
                    )
                }

                if (layer.shapeType == ShapeType.POLYGON) {
                    LuxurySliderRow(
                        label = "Nombre de côtés",
                        value = layer.polygonSides.toFloat(),
                        valueRange = 3f..12f,
                        unit = "",
                        onValueChange = { onUpdate(layer.copy(polygonSides = it.toInt())) }
                    )
                }

                if (layer.shapeType == ShapeType.STAR) {
                    LuxurySliderRow(
                        label = "Branches de l'étoile",
                        value = layer.starPoints.toFloat(),
                        valueRange = 3f..12f,
                        unit = "",
                        onValueChange = { onUpdate(layer.copy(starPoints = it.toInt())) }
                    )
                    LuxurySliderRow(
                        label = "Rayon intérieur",
                        value = layer.starInnerRadiusRatio * 100f,
                        valueRange = 10f..90f,
                        unit = "%",
                        onValueChange = { onUpdate(layer.copy(starInnerRadiusRatio = it / 100f)) }
                    )
                }

                LuxurySliderRow(
                    label = "Opacité",
                    value = layer.opacity * 100f,
                    valueRange = 0f..100f,
                    unit = "%",
                    onValueChange = { onUpdate(layer.copy(opacity = it / 100f)) }
                )

                Spacer(modifier = Modifier.height(14.dp))

                LuxuryButton(
                    text = "Convertir en Courbe Bézier modifiable",
                    onClick = onConvertToPath,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            1 -> {
                // Fill & Texture
                Text("Texture & Matériaux de luxe", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextureLibrary.textures.forEach { tex ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(tex.primaryColor))
                                .border(1.dp, ChampagneGold, RoundedCornerShape(10.dp))
                                .clickable {
                                    onUpdate(
                                        layer.copy(
                                            fill = ColorFill(
                                                solidColor = tex.primaryColor,
                                                gradient = tex.gradient,
                                                isGradient = true,
                                                textureName = tex.name
                                            )
                                        )
                                    )
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = tex.name,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                LuxuryColorPicker(
                    fill = layer.fill,
                    onFillChange = { onUpdate(layer.copy(fill = it)) }
                )
            }

            2 -> {
                // Stroke & Shadows
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Contour (Stroke)", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Switch(
                        checked = layer.stroke.isEnabled,
                        onCheckedChange = { onUpdate(layer.copy(stroke = layer.stroke.copy(isEnabled = it))) },
                        colors = SwitchDefaults.colors(checkedThumbColor = ObsidianBg, checkedTrackColor = ChampagneGold)
                    )
                }

                AnimatedVisibility(visible = layer.stroke.isEnabled) {
                    Column {
                        LuxurySliderRow(
                            label = "Épaisseur du contour",
                            value = layer.stroke.width,
                            valueRange = 1f..30f,
                            unit = "px",
                            onValueChange = { onUpdate(layer.copy(stroke = layer.stroke.copy(width = it))) }
                        )
                        LuxuryColorPicker(
                            fill = ColorFill(solidColor = layer.stroke.color),
                            onFillChange = { onUpdate(layer.copy(stroke = layer.stroke.copy(color = it.solidColor))) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Shadow
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Ombre portée (Shadow)", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Switch(
                        checked = layer.shadow.isEnabled,
                        onCheckedChange = { onUpdate(layer.copy(shadow = layer.shadow.copy(isEnabled = it))) },
                        colors = SwitchDefaults.colors(checkedThumbColor = ObsidianBg, checkedTrackColor = ChampagneGold)
                    )
                }

                AnimatedVisibility(visible = layer.shadow.isEnabled) {
                    Column {
                        LuxurySliderRow(
                            label = "Rayon de flou",
                            value = layer.shadow.radius,
                            valueRange = 0f..40f,
                            unit = "px",
                            onValueChange = { onUpdate(layer.copy(shadow = layer.shadow.copy(radius = it))) }
                        )
                        LuxurySliderRow(
                            label = "Décalage Y",
                            value = layer.shadow.dy,
                            valueRange = -30f..30f,
                            unit = "px",
                            onValueChange = { onUpdate(layer.copy(shadow = layer.shadow.copy(dy = it))) }
                        )
                    }
                }
            }

            3 -> {
                // 3D, Relief & Tilt
                Text("Rotation 3D (Inclinaison spatiale)", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                LuxurySliderRow(
                    label = "Inclinaison Axe X (Haut / Bas)",
                    value = layer.transform.rotationX,
                    valueRange = -70f..70f,
                    unit = "°",
                    onValueChange = { onUpdate(layer.copy(transform = layer.transform.copy(rotationX = it))) }
                )
                LuxurySliderRow(
                    label = "Inclinaison Axe Y (Gauche / Droite)",
                    value = layer.transform.rotationY,
                    valueRange = -70f..70f,
                    unit = "°",
                    onValueChange = { onUpdate(layer.copy(transform = layer.transform.copy(rotationY = it))) }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 3D Extrusion
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Extrusion 3D", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Switch(
                        checked = layer.effect3D.isEnabled,
                        onCheckedChange = { onUpdate(layer.copy(effect3D = layer.effect3D.copy(isEnabled = it))) },
                        colors = SwitchDefaults.colors(checkedThumbColor = ObsidianBg, checkedTrackColor = ChampagneGold)
                    )
                }

                AnimatedVisibility(visible = layer.effect3D.isEnabled) {
                    Column {
                        LuxurySliderRow(
                            label = "Profondeur 3D",
                            value = layer.effect3D.depth,
                            valueRange = 1f..40f,
                            unit = "px",
                            onValueChange = { onUpdate(layer.copy(effect3D = layer.effect3D.copy(depth = it))) }
                        )
                        LuxurySliderRow(
                            label = "Angle de lumière",
                            value = layer.effect3D.lightAngle,
                            valueRange = 0f..360f,
                            unit = "°",
                            onValueChange = { onUpdate(layer.copy(effect3D = layer.effect3D.copy(lightAngle = it))) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Biseau (Emboss)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Biseau & Relief (Emboss)", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Switch(
                        checked = layer.emboss.isEnabled,
                        onCheckedChange = { onUpdate(layer.copy(emboss = layer.emboss.copy(isEnabled = it))) },
                        colors = SwitchDefaults.colors(checkedThumbColor = ObsidianBg, checkedTrackColor = ChampagneGold)
                    )
                }

                AnimatedVisibility(visible = layer.emboss.isEnabled) {
                    LuxurySliderRow(
                        label = "Intensité du reflet",
                        value = layer.emboss.intensity,
                        valueRange = 0f..100f,
                        unit = "%",
                        onValueChange = { onUpdate(layer.copy(emboss = layer.emboss.copy(intensity = it))) }
                    )
                }
            }
        }
    }
}
