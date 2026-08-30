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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.data.repository.FontLibrary
import com.example.core.data.repository.FrenchQuotesLibrary
import com.example.core.data.repository.TextPresetsLibrary
import com.example.core.data.repository.TextureLibrary
import com.example.core.engine.model.ColorFill
import com.example.core.engine.model.EmbossDef
import com.example.core.engine.model.GlowDef
import com.example.core.engine.model.InnerShadowDef
import com.example.core.engine.model.Layer3DEffect
import com.example.core.engine.model.ShadowDef
import com.example.core.engine.model.StrokeDef
import com.example.core.engine.model.TextCurvature
import com.example.core.engine.model.TextLayer
import com.example.core.engine.model.TextReflection
import com.example.core.ui.GlassCard
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
    val tabs = listOf(
        "Texte & Police",
        "Couleur & Texture",
        "Contour & Ombres",
        "3D & Courbure",
        "Styles & Citations"
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
                // Text input
                OutlinedTextField(
                    value = layer.text,
                    onValueChange = { onUpdate(layer.copy(text = it)) },
                    label = { Text("Contenu du texte", color = TextSecondary) },
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

                // Font selector horizontal cards
                Text("Polices typographiques", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FontLibrary.fonts.forEach { font ->
                        val isSelected = layer.fontFamily == font.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) SurfaceElevated else SurfaceCard)
                                .border(1.dp, if (isSelected) ChampagneGold else BorderGlass, RoundedCornerShape(8.dp))
                                .clickable { onUpdate(layer.copy(fontFamily = font.id)) }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = font.name,
                                color = if (isSelected) ChampagneGold else TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Font Styling buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LuxuryIconButton(
                        icon = Icons.Default.FormatBold,
                        isSelected = layer.isBold,
                        onClick = { onUpdate(layer.copy(isBold = !layer.isBold)) },
                        contentDescription = "Gras"
                    )
                    LuxuryIconButton(
                        icon = Icons.Default.FormatItalic,
                        isSelected = layer.isItalic,
                        onClick = { onUpdate(layer.copy(isItalic = !layer.isItalic)) },
                        contentDescription = "Italique"
                    )
                    LuxuryIconButton(
                        icon = Icons.Default.FormatUnderlined,
                        isSelected = layer.isUnderline,
                        onClick = { onUpdate(layer.copy(isUnderline = !layer.isUnderline)) },
                        contentDescription = "Souligné"
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    LuxuryIconButton(
                        icon = Icons.AutoMirrored.Filled.FormatAlignLeft,
                        isSelected = layer.textAlign == "LEFT",
                        onClick = { onUpdate(layer.copy(textAlign = "LEFT")) },
                        contentDescription = "Aligner à gauche"
                    )
                    LuxuryIconButton(
                        icon = Icons.Default.FormatAlignCenter,
                        isSelected = layer.textAlign == "CENTER",
                        onClick = { onUpdate(layer.copy(textAlign = "CENTER")) },
                        contentDescription = "Centrer"
                    )
                    LuxuryIconButton(
                        icon = Icons.AutoMirrored.Filled.FormatAlignRight,
                        isSelected = layer.textAlign == "RIGHT",
                        onClick = { onUpdate(layer.copy(textAlign = "RIGHT")) },
                        contentDescription = "Aligner à droite"
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                LuxurySliderRow(
                    label = "Taille de police",
                    value = layer.fontSize,
                    valueRange = 12f..160f,
                    unit = "pt",
                    onValueChange = { onUpdate(layer.copy(fontSize = it)) }
                )

                LuxurySliderRow(
                    label = "Espacement des lettres",
                    value = layer.letterSpacing,
                    valueRange = 0f..20f,
                    unit = "px",
                    onValueChange = { onUpdate(layer.copy(letterSpacing = it)) }
                )

                LuxurySliderRow(
                    label = "Opacité",
                    value = layer.opacity * 100f,
                    valueRange = 0f..100f,
                    unit = "%",
                    onValueChange = { onUpdate(layer.copy(opacity = it / 100f)) }
                )
            }

            1 -> {
                // Color & Textures
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

                Spacer(modifier = Modifier.height(16.dp))

                LuxuryColorPicker(
                    fill = layer.fill,
                    onFillChange = { onUpdate(layer.copy(fill = it)) }
                )
            }

            2 -> {
                // Stroke, Shadows & Glow
                Text("Contour (Stroke)", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Activer le contour", color = TextSecondary, fontSize = 13.sp)
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

                // Drop Shadow
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
                            label = "Décalage X",
                            value = layer.shadow.dx,
                            valueRange = -30f..30f,
                            unit = "px",
                            onValueChange = { onUpdate(layer.copy(shadow = layer.shadow.copy(dx = it))) }
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

                Spacer(modifier = Modifier.height(14.dp))

                // Outer Glow
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Lueur externe (Glow)", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Switch(
                        checked = layer.glow.isEnabled,
                        onCheckedChange = { onUpdate(layer.copy(glow = layer.glow.copy(isEnabled = it))) },
                        colors = SwitchDefaults.colors(checkedThumbColor = ObsidianBg, checkedTrackColor = ChampagneGold)
                    )
                }

                AnimatedVisibility(visible = layer.glow.isEnabled) {
                    Column {
                        LuxurySliderRow(
                            label = "Rayon de lueur",
                            value = layer.glow.radius,
                            valueRange = 2f..60f,
                            unit = "px",
                            onValueChange = { onUpdate(layer.copy(glow = layer.glow.copy(radius = it))) }
                        )
                        LuxurySliderRow(
                            label = "Intensité",
                            value = layer.glow.opacity * 100f,
                            valueRange = 0f..100f,
                            unit = "%",
                            onValueChange = { onUpdate(layer.copy(glow = layer.glow.copy(opacity = it / 100f))) }
                        )
                    }
                }
            }

            3 -> {
                // 3D Effects, Tilt & Curvature
                Text("Effet 3D & Extrusion", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Activer Texte 3D", color = TextSecondary, fontSize = 13.sp)
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
                            label = "Angle d'éclairage 3D",
                            value = layer.effect3D.lightAngle,
                            valueRange = 0f..360f,
                            unit = "°",
                            onValueChange = { onUpdate(layer.copy(effect3D = layer.effect3D.copy(lightAngle = it))) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3D Tilt (Rotation X & Y)
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

                // Text Curvature
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Courbure du texte (Arc)", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Switch(
                        checked = layer.curvature.isEnabled,
                        onCheckedChange = { onUpdate(layer.copy(curvature = layer.curvature.copy(isEnabled = it))) },
                        colors = SwitchDefaults.colors(checkedThumbColor = ObsidianBg, checkedTrackColor = ChampagneGold)
                    )
                }

                AnimatedVisibility(visible = layer.curvature.isEnabled) {
                    LuxurySliderRow(
                        label = "Courbure de l'arc",
                        value = layer.curvature.bend,
                        valueRange = -100f..100f,
                        unit = "%",
                        onValueChange = { onUpdate(layer.copy(curvature = layer.curvature.copy(bend = it))) }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Emboss / Biseau
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
                    Column {
                        LuxurySliderRow(
                            label = "Intensité du reflet",
                            value = layer.emboss.intensity,
                            valueRange = 0f..100f,
                            unit = "%",
                            onValueChange = { onUpdate(layer.copy(emboss = layer.emboss.copy(intensity = it))) }
                        )
                        LuxurySliderRow(
                            label = "Angle de la lumière",
                            value = layer.emboss.lightAngle,
                            valueRange = 0f..360f,
                            unit = "°",
                            onValueChange = { onUpdate(layer.copy(emboss = layer.emboss.copy(lightAngle = it))) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Reflection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Reflet miroir", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Switch(
                        checked = layer.reflection.isEnabled,
                        onCheckedChange = { onUpdate(layer.copy(reflection = layer.reflection.copy(isEnabled = it))) },
                        colors = SwitchDefaults.colors(checkedThumbColor = ObsidianBg, checkedTrackColor = ChampagneGold)
                    )
                }

                AnimatedVisibility(visible = layer.reflection.isEnabled) {
                    LuxurySliderRow(
                        label = "Opacité du reflet",
                        value = layer.reflection.opacity * 100f,
                        valueRange = 0f..100f,
                        unit = "%",
                        onValueChange = { onUpdate(layer.copy(reflection = layer.reflection.copy(opacity = it / 100f))) }
                    )
                }
            }

            4 -> {
                // 1-Click Style Presets & French Quotes
                Text("Styles 3D Prédéfinis (1-Clic)", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextPresetsLibrary.presets.forEach { preset ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(SurfaceCard)
                                .border(1.dp, BorderGlass, RoundedCornerShape(10.dp))
                                .clickable { onUpdate(preset.applyToLayer(layer)) }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(preset.name, color = ChampagneGold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text(preset.description, color = TextSecondary, fontSize = 11.sp)
                                }
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = ChampagneGold,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // French Quotes
                Text("Bibliothèque de Citations en Français", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))

                var selectedQuoteCategory by remember { mutableStateOf("Motivation") }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FrenchQuotesLibrary.categories.forEach { cat ->
                        val isCatSelected = selectedQuoteCategory == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isCatSelected) ChampagneGold else SurfaceElevated)
                                .clickable { selectedQuoteCategory = cat }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = cat,
                                color = if (isCatSelected) ObsidianBg else TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = if (isCatSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                val filteredQuotes = FrenchQuotesLibrary.quotes.filter { it.category == selectedQuoteCategory }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    filteredQuotes.forEach { q ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(SurfaceCard)
                                .border(1.dp, BorderGlass, RoundedCornerShape(10.dp))
                                .clickable { onUpdate(layer.copy(text = "\"${q.quote}\"")) }
                                .padding(12.dp)
                        ) {
                            Column {
                                Text("\"${q.quote}\"", color = TextPrimary, fontSize = 12.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("— ${q.author}", color = ChampagneGold, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}
