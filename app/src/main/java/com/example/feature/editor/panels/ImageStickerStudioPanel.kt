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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.engine.model.ColorEraserDef
import com.example.core.engine.model.ColorFill
import com.example.core.engine.model.ImageLayer
import com.example.core.engine.model.ShadowDef
import com.example.core.engine.model.StrokeDef
import com.example.core.ui.LuxuryButton
import com.example.core.ui.LuxuryColorPicker
import com.example.core.ui.LuxurySliderRow
import com.example.ui.theme.BorderGlass
import com.example.ui.theme.BorderGold
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

data class BuiltInSticker(
    val name: String,
    val category: String,
    val colors: List<Color>
)

object LuxuryStickersCatalog {
    val stickers = listOf(
        BuiltInSticker("COURONNE VIP", "Luxe", listOf(Color(0xFFF3E5AB), Color(0xFFD4AF37))),
        BuiltInSticker("ÉCUSSON ROYAL", "Luxe", listOf(Color(0xFFD4AF37), Color(0xFFAA820A))),
        BuiltInSticker("CERTIFIÉ VÉRIFIÉ", "Badges", listOf(Color(0xFF00E5FF), Color(0xFF2979FF))),
        BuiltInSticker("CYBER NÉON", "Effets", listOf(Color(0xFFFF3366), Color(0xFF9D4EDD))),
        BuiltInSticker("BOUCLIER ÉMERAUDE", "Badges", listOf(Color(0xFF00E676), Color(0xFF00B0FF))),
        BuiltInSticker("BADGE PREMIUM", "Luxe", listOf(Color(0xFFFFFFFF), Color(0xFFE5E4E2))),
        BuiltInSticker("ÉTOILE FLAMME", "Formes", listOf(Color(0xFFFF7A00), Color(0xFFFFD600))),
        BuiltInSticker("DIAMANT PUR", "Luxe", listOf(Color(0xFFE0F7FA), Color(0xFF80DEEA)))
    )
}

@Composable
fun ImageStickerStudioPanel(
    layer: ImageLayer?,
    onUpdate: (ImageLayer) -> Unit,
    onPickImageFromGallery: () -> Unit,
    onAddSticker: (BuiltInSticker) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceDark)
            .padding(14.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Photos, Images & Stickers", color = ChampagneGold, fontSize = 14.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(10.dp))

        LuxuryButton(
            text = "Importer une photo depuis la galerie",
            icon = Icons.Default.AddPhotoAlternate,
            onClick = onPickImageFromGallery,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text("Stickers Exclusifs PixelLab", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LuxuryStickersCatalog.stickers.forEach { sticker ->
                Box(
                    modifier = Modifier
                        .size(width = 115.dp, height = 75.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.linearGradient(sticker.colors))
                        .border(1.dp, BorderGold, RoundedCornerShape(12.dp))
                        .clickable { onAddSticker(sticker) }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = sticker.name,
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp
                    )
                }
            }
        }

        if (layer != null) {
            Spacer(modifier = Modifier.height(18.dp))
            Text("Ajustements de l'élément sélectionné", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(6.dp))

            // Gomme de couleur (Chroma Key)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Gomme de couleur (Supprimer le fond)", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Switch(
                    checked = layer.colorEraser.isEnabled,
                    onCheckedChange = { onUpdate(layer.copy(colorEraser = layer.colorEraser.copy(isEnabled = it))) },
                    colors = SwitchDefaults.colors(checkedThumbColor = ObsidianBg, checkedTrackColor = ChampagneGold)
                )
            }

            AnimatedVisibility(visible = layer.colorEraser.isEnabled) {
                Column {
                    Text("Couleur de fond à effacer :", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            0xFFFFFFFF to "Blanc",
                            0xFF00FF00 to "Vert Fond Vert",
                            0xFF000000 to "Noir",
                            0xFF0000FF to "Bleu Fond Bleu"
                        ).forEach { (col, name) ->
                            val isSel = layer.colorEraser.targetColor == col
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(col))
                                    .border(2.dp, if (isSel) ChampagneGold else BorderGlass, RoundedCornerShape(8.dp))
                                    .clickable { onUpdate(layer.copy(colorEraser = layer.colorEraser.copy(targetColor = col))) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = name,
                                    color = if (col == 0xFFFFFFFF) Color.Black else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LuxurySliderRow(
                        label = "Tolérance de la couleur",
                        value = layer.colorEraser.tolerance,
                        valueRange = 1f..80f,
                        unit = "%",
                        onValueChange = { onUpdate(layer.copy(colorEraser = layer.colorEraser.copy(tolerance = it))) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3D Spatial Tilt
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

            Spacer(modifier = Modifier.height(12.dp))

            // Filters
            LuxurySliderRow(
                label = "Luminosité",
                value = layer.brightness * 100f,
                valueRange = 20f..200f,
                unit = "%",
                onValueChange = { onUpdate(layer.copy(brightness = it / 100f)) }
            )

            LuxurySliderRow(
                label = "Contraste",
                value = layer.contrast * 100f,
                valueRange = 20f..200f,
                unit = "%",
                onValueChange = { onUpdate(layer.copy(contrast = it / 100f)) }
            )

            LuxurySliderRow(
                label = "Saturation",
                value = layer.saturation * 100f,
                valueRange = 0f..200f,
                unit = "%",
                onValueChange = { onUpdate(layer.copy(saturation = it / 100f)) }
            )

            LuxurySliderRow(
                label = "Opacité globale",
                value = layer.opacity * 100f,
                valueRange = 0f..100f,
                unit = "%",
                onValueChange = { onUpdate(layer.copy(opacity = it / 100f)) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Stroke & Shadow
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Contour de l'image", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Switch(
                    checked = layer.stroke.isEnabled,
                    onCheckedChange = { onUpdate(layer.copy(stroke = layer.stroke.copy(isEnabled = it))) },
                    colors = SwitchDefaults.colors(checkedThumbColor = ObsidianBg, checkedTrackColor = ChampagneGold)
                )
            }

            AnimatedVisibility(visible = layer.stroke.isEnabled) {
                LuxurySliderRow(
                    label = "Épaisseur du contour",
                    value = layer.stroke.width,
                    valueRange = 1f..30f,
                    unit = "px",
                    onValueChange = { onUpdate(layer.copy(stroke = layer.stroke.copy(width = it))) }
                )
            }
        }
    }
}
