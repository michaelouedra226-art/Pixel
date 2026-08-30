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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.ui.theme.TextPrimary
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
        Text("Freehand Drawing Studio", color = ChampagneGold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text("Draw with vector smoothing directly on canvas", color = TextSecondary, fontSize = 11.sp)

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LuxuryButton(
                text = "Brush Mode",
                icon = Icons.Default.Brush,
                isPrimary = !isEraser,
                onClick = { onEraserToggle(false) },
                modifier = Modifier.weight(1f)
            )

            LuxuryButton(
                text = "Eraser Mode",
                icon = Icons.Default.Clear,
                isPrimary = isEraser,
                onClick = { onEraserToggle(true) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LuxurySliderRow(
            title = if (isEraser) "Eraser Size" else "Brush Size",
            value = brushSize,
            onValueChange = onBrushSizeChange,
            valueRange = 2f..80f,
            valueDisplay = "${brushSize.toInt()} px"
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (!isEraser) {
            Text("Brush Ink Color", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            LuxuryColorPicker(
                currentFill = ColorFill(solidColor = brushColor),
                onFillChanged = { onBrushColorChange(it.solidColor) },
                allowGradients = false
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        LuxuryButton(
            text = "Clear Drawing Layer",
            icon = Icons.Default.Delete,
            isPrimary = false,
            onClick = {
                onUpdate(layer.copy(strokes = emptyList()))
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
