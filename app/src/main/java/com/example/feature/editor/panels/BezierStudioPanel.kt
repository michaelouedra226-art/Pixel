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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Polyline
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.engine.model.AnchorPoint
import com.example.core.engine.model.BezierLayer
import com.example.core.ui.LuxuryButton
import com.example.core.ui.LuxuryColorPicker
import com.example.core.ui.LuxurySliderRow
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.UUID

@Composable
fun BezierStudioPanel(
    layer: BezierLayer,
    onUpdate: (BezierLayer) -> Unit,
    selectedAnchorIndex: Int?,
    onSelectAnchor: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceDark)
            .padding(14.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Bézier Vector Node Studio",
            color = ChampagneGold,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Select anchor points on canvas to drag nodes and handles",
            color = TextSecondary,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LuxuryButton(
                text = "Add Node",
                icon = Icons.Default.Add,
                onClick = {
                    val last = layer.anchors.lastOrNull()?.position ?: Offset(100f, 100f)
                    val newAnchor = AnchorPoint(
                        id = UUID.randomUUID().toString(),
                        position = last + Offset(50f, 30f),
                        handleIn = Offset(-25f, 0f),
                        handleOut = Offset(25f, 0f),
                        isSmooth = true
                    )
                    onUpdate(layer.copy(anchors = layer.anchors + newAnchor))
                },
                modifier = Modifier.weight(1f)
            )

            LuxuryButton(
                text = "Delete Node",
                icon = Icons.Default.Delete,
                isPrimary = false,
                isEnabled = selectedAnchorIndex != null && layer.anchors.size > 2,
                onClick = {
                    selectedAnchorIndex?.let { idx ->
                        if (layer.anchors.size > 2 && idx in layer.anchors.indices) {
                            val mutable = layer.anchors.toMutableList()
                            mutable.removeAt(idx)
                            onUpdate(layer.copy(anchors = mutable))
                            onSelectAnchor(null)
                        }
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Close Path Loop", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Switch(
                checked = layer.isClosed,
                onCheckedChange = { onUpdate(layer.copy(isClosed = it)) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = ChampagneGold,
                    checkedTrackColor = ChampagneGold.copy(alpha = 0.5f)
                )
            )
        }

        if (selectedAnchorIndex != null && selectedAnchorIndex in layer.anchors.indices) {
            val anchor = layer.anchors[selectedAnchorIndex]
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Smooth / Sharp Node",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Switch(
                    checked = anchor.isSmooth,
                    onCheckedChange = { isSmooth ->
                        val updated = layer.anchors.toMutableList()
                        updated[selectedAnchorIndex] = anchor.copy(isSmooth = isSmooth)
                        onUpdate(layer.copy(anchors = updated))
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ChampagneGold,
                        checkedTrackColor = ChampagneGold.copy(alpha = 0.5f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LuxurySliderRow(
            title = "Curve Stroke Width",
            value = layer.stroke.width,
            onValueChange = { onUpdate(layer.copy(stroke = layer.stroke.copy(width = it))) },
            valueRange = 1f..30f,
            valueDisplay = "${layer.stroke.width.toInt()} px"
        )

        Spacer(modifier = Modifier.height(10.dp))
        Text("Stroke Color", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        LuxuryColorPicker(
            currentFill = com.example.core.engine.model.ColorFill(solidColor = layer.stroke.color),
            onFillChanged = { onUpdate(layer.copy(stroke = layer.stroke.copy(color = it.solidColor))) },
            allowGradients = false
        )
    }
}
