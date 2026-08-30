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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CallMerge
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.example.core.engine.model.BezierLayer
import com.example.core.engine.model.BlendModeDef
import com.example.core.engine.model.DrawingLayer
import com.example.core.engine.model.ImageLayer
import com.example.core.engine.model.Layer
import com.example.core.engine.model.ShapeLayer
import com.example.core.engine.model.TextLayer
import com.example.core.ui.LuxuryButton
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
fun LayerManagerPanel(
    layers: List<Layer>,
    selectedLayerId: String?,
    onSelectLayer: (String) -> Unit,
    onToggleVisibility: (String) -> Unit,
    onToggleLock: (String) -> Unit,
    onMoveLayerUp: (String) -> Unit,
    onMoveLayerDown: (String) -> Unit,
    onDuplicateLayer: (String) -> Unit,
    onDeleteLayer: (String) -> Unit,
    onOpacityChange: (String, Float) -> Unit,
    onBlendModeChange: (String, BlendModeDef) -> Unit,
    onMergeLayers: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedLayer = layers.find { it.id == selectedLayerId }
    val reversedLayers = layers.reversed()

    var isMergeMode by remember { mutableStateOf(false) }
    val selectedForMerge = remember { mutableStateListOf<String>() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceDark)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = null,
                    tint = ChampagneGold,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Pile des Calques (${layers.size})",
                    color = ChampagneGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isMergeMode) ChampagneGold else SurfaceElevated)
                    .clickable {
                        isMergeMode = !isMergeMode
                        selectedForMerge.clear()
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (isMergeMode) "Annuler Fusion" else "Fusionner",
                    color = if (isMergeMode) ObsidianBg else ChampagneGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (isMergeMode) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${selectedForMerge.size} calques cochés",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                if (selectedForMerge.size >= 2) {
                    LuxuryButton(
                        text = "Valider la fusion",
                        icon = Icons.Default.CallMerge,
                        onClick = {
                            onMergeLayers(selectedForMerge.toList())
                            isMergeMode = false
                            selectedForMerge.clear()
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Layers list
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            reversedLayers.forEach { layer ->
                val isSelected = layer.id == selectedLayerId
                val icon = when (layer) {
                    is TextLayer -> Icons.Default.TextFields
                    is ShapeLayer -> Icons.Default.Category
                    is BezierLayer -> Icons.Default.Edit
                    is DrawingLayer -> Icons.Default.Edit
                    is ImageLayer -> Icons.Default.Image
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) SurfaceCard else SurfaceElevated)
                        .border(
                            1.dp,
                            if (isSelected) BorderGold else BorderGlass,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onSelectLayer(layer.id) }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (isMergeMode) {
                                Checkbox(
                                    checked = selectedForMerge.contains(layer.id),
                                    onCheckedChange = { checked ->
                                        if (checked) selectedForMerge.add(layer.id)
                                        else selectedForMerge.remove(layer.id)
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = ChampagneGold,
                                        uncheckedColor = BorderGlass,
                                        checkmarkColor = ObsidianBg
                                    )
                                )
                            }

                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) ChampagneGold else TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = layer.name,
                                    color = if (isSelected) TextPrimary else TextSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    text = "Opacité ${(layer.opacity * 100).toInt()}%",
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        // Actions Row
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onToggleVisibility(layer.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (layer.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Visibilité",
                                    tint = if (layer.isVisible) ChampagneGold else TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            IconButton(
                                onClick = { onToggleLock(layer.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (layer.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                    contentDescription = "Verrouiller",
                                    tint = if (layer.isLocked) ChampagneGold else TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            IconButton(
                                onClick = { onMoveLayerUp(layer.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = "Monter",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            IconButton(
                                onClick = { onMoveLayerDown(layer.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDownward,
                                    contentDescription = "Descendre",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            IconButton(
                                onClick = { onDuplicateLayer(layer.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Dupliquer",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            IconButton(
                                onClick = { onDeleteLayer(layer.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Supprimer",
                                    tint = Color(0xFFFF453A),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Detailed controls for selected layer
        if (selectedLayer != null) {
            Spacer(modifier = Modifier.height(14.dp))
            LuxurySliderRow(
                label = "Opacité du calque sélectionné",
                value = selectedLayer.opacity * 100f,
                valueRange = 0f..100f,
                unit = "%",
                onValueChange = { onOpacityChange(selectedLayer.id, it / 100f) }
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text("Mode de fusion (Blend Mode)", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BlendModeDef.entries.forEach { mode ->
                    val isModeSelected = selectedLayer.blendMode == mode
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isModeSelected) ChampagneGold else SurfaceElevated)
                            .clickable { onBlendModeChange(selectedLayer.id, mode) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = mode.name,
                            color = if (isModeSelected) ObsidianBg else TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = if (isModeSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}
