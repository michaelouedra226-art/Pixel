package com.example.feature.editor.panels

import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.data.repository.PresetLibrary
import com.example.core.data.repository.ProjectPreset
import com.example.core.engine.model.CanvasProject
import com.example.core.engine.model.ColorFill
import com.example.core.ui.LuxuryColorPicker
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun BackgroundStudioPanel(
    project: CanvasProject,
    onUpdateProject: (CanvasProject) -> Unit,
    onApplyPresetSize: (ProjectPreset) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceDark)
            .padding(14.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Arrière-plan & Dimensions du canevas", color = ChampagneGold, fontSize = 14.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Fond Transparent (Alpha PNG)", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Switch(
                checked = project.isTransparentBg,
                onCheckedChange = { onUpdateProject(project.copy(isTransparentBg = it)) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = ChampagneGold,
                    checkedTrackColor = ChampagneGold.copy(alpha = 0.5f)
                )
            )
        }

        if (!project.isTransparentBg) {
            Spacer(modifier = Modifier.height(12.dp))
            Text("Couleur ou dégradé de fond", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))

            val currentFill = if (project.backgroundGradient != null) {
                ColorFill(gradient = project.backgroundGradient, isGradient = true)
            } else {
                ColorFill(solidColor = project.backgroundColor, isGradient = false)
            }

            LuxuryColorPicker(
                fill = currentFill,
                onFillChange = { fill ->
                    if (fill.isGradient && fill.gradient != null) {
                        onUpdateProject(project.copy(backgroundGradient = fill.gradient))
                    } else {
                        onUpdateProject(project.copy(backgroundColor = fill.solidColor, backgroundGradient = null))
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Formats & Ratios Prédéfinis", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PresetLibrary.presets.forEach { preset ->
                val isCurrent = project.width == preset.width && project.height == preset.height
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isCurrent) ChampagneGold else SurfaceElevated)
                        .clickable { onApplyPresetSize(preset) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "${preset.name} (${preset.width}x${preset.height})",
                        color = if (isCurrent) ObsidianBg else TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
