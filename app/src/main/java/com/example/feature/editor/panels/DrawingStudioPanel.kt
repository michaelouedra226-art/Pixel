package com.example.feature.editor.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.engine.model.ColorFill
import com.example.core.engine.model.DrawingLayer
import com.example.core.ui.LuxuryButton
import com.example.core.ui.LuxuryColorPicker
import com.example.core.ui.LuxurySliderRow
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextSecondary

@Composable
fun DrawingStudioPanel(
    layer: DrawingLayer,
    onUpdate: (DrawingLayer) -> Unit,
    brushColor: Long,
    onBrushColorChange: (Long) -> Unit,
    brushSize: Float,
    onBrushSizeChange: (Float) -> Unit,
    isEraser: Boolean,
    onEraserToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceDark)
            .padding(14.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Atelier Dessin à Main Levée", color = ChampagneGold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text("Dessinez et esquissez directement sur le canevas avec lissage dynamique", color = TextSecondary, fontSize = 11.sp)

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LuxuryButton(
                text = "Pinceau",
                icon = Icons.Default.Brush,
                isPrimary = !isEraser,
                onClick = { onEraserToggle(false) },
                modifier = Modifier.weight(1f)
            )

            LuxuryButton(
                text = "Gomme",
                icon = Icons.Default.Clear,
                isPrimary = isEraser,
                onClick = { onEraserToggle(true) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LuxurySliderRow(
            label = if (isEraser) "Taille de la gomme" else "Taille du pinceau",
            value = brushSize,
            onValueChange = onBrushSizeChange,
            valueRange = 2f..80f,
            unit = "px"
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (!isEraser) {
            Text("Couleur de l'encre", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            LuxuryColorPicker(
                fill = ColorFill(solidColor = brushColor),
                onFillChange = { onBrushColorChange(it.solidColor) },
                allowGradients = false
            )
        }

        if (layer.strokes.isNotEmpty()) {
            Spacer(modifier = Modifier.height(14.dp))
            LuxuryButton(
                text = "Effacer tous les traits de dessin",
                icon = Icons.Default.Delete,
                isPrimary = false,
                onClick = { onUpdate(layer.copy(strokes = emptyList())) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
