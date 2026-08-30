package com.example.feature.editor.panels

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
import androidx.compose.material.icons.filled.Star
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
import com.example.core.engine.model.ImageLayer
import com.example.core.ui.LuxuryButton
import com.example.core.ui.LuxurySliderRow
import com.example.ui.theme.BorderGlass
import com.example.ui.theme.BorderGold
import com.example.ui.theme.ChampagneGold
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
        BuiltInSticker("VIP CROWN", "Luxury", listOf(Color(0xFFF3E5AB), Color(0xFFD4AF37))),
        BuiltInSticker("GOLD CREST", "Luxury", listOf(Color(0xFFD4AF37), Color(0xFFAA820A))),
        BuiltInSticker("VERIFIED", "Badges", listOf(Color(0xFF00E5FF), Color(0xFF2979FF))),
        BuiltInSticker("CYBER NEON", "Flares", listOf(Color(0xFFFF3366), Color(0xFF9D4EDD))),
        BuiltInSticker("EMERALD SHIELD", "Badges", listOf(Color(0xFF00E676), Color(0xFF00B0FF))),
        BuiltInSticker("PREMIUM BADGE", "Luxury", listOf(Color(0xFFFFFFFF), Color(0xFFE5E4E2))),
        BuiltInSticker("FLAME STAR", "Shapes", listOf(Color(0xFFFF7A00), Color(0xFFFFD600))),
        BuiltInSticker("DIAMOND", "Luxury", listOf(Color(0xFFE0F7FA), Color(0xFF80DEEA)))
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
        Text("Visual Assets & Stickers", color = ChampagneGold, fontSize = 14.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(10.dp))

        LuxuryButton(
            text = "Import Image from Gallery",
            icon = Icons.Default.AddPhotoAlternate,
            onClick = onPickImageFromGallery,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text("Exclusive Luxury Stickers", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                        .size(width = 110.dp, height = 75.dp)
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
            Spacer(modifier = Modifier.height(16.dp))
            Text("Adjust Selected Image", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(6.dp))

            LuxurySliderRow(
                title = "Brightness",
                value = layer.brightness,
                onValueChange = { onUpdate(layer.copy(brightness = it)) },
                valueRange = 0.2f..2f,
                valueDisplay = "${(layer.brightness * 100).toInt()}%"
            )

            LuxurySliderRow(
                title = "Contrast",
                value = layer.contrast,
                onValueChange = { onUpdate(layer.copy(contrast = it)) },
                valueRange = 0.2f..2f,
                valueDisplay = "${(layer.contrast * 100).toInt()}%"
            )

            LuxurySliderRow(
                title = "Saturation",
                value = layer.saturation,
                onValueChange = { onUpdate(layer.copy(saturation = it)) },
                valueRange = 0f..2.5f,
                valueDisplay = "${(layer.saturation * 100).toInt()}%"
            )
        }
    }
}
