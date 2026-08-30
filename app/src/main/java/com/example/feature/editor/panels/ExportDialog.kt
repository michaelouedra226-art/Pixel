package com.example.feature.editor.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.window.Dialog
import com.example.core.data.export.ExportConfig
import com.example.core.data.export.ExportFormat
import com.example.core.engine.model.CanvasProject
import com.example.core.ui.GlassCard
import com.example.core.ui.LuxuryButton
import com.example.core.ui.LuxurySliderRow
import com.example.ui.theme.BorderGlass
import com.example.ui.theme.BorderGold
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ExportDialog(
    project: CanvasProject,
    isExporting: Boolean,
    exportSuccessMessage: String?,
    onDismiss: () -> Unit,
    onExport: (ExportConfig) -> Unit
) {
    var selectedFormat by remember { mutableStateOf(ExportFormat.PNG) }
    var selectedScale by remember { mutableFloatStateOf(1f) }
    var quality by remember { mutableIntStateOf(95) }

    val outputWidth = (project.width * selectedScale).toInt()
    val outputHeight = (project.height * selectedScale).toInt()

    Dialog(onDismissRequest = onDismiss) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            backgroundColor = SurfaceDark
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Export Artwork",
                        color = ChampagneGold,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Format selector
                Text("Output Format", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        ExportFormat.PNG to "PNG",
                        ExportFormat.JPG to "JPG",
                        ExportFormat.WEBP to "WebP",
                        ExportFormat.SVG to "SVG (Vector)"
                    ).forEach { (format, label) ->
                        val isSelected = selectedFormat == format
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) ChampagneGold else SurfaceElevated)
                                .clickable { selectedFormat = format }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) ObsidianBg else TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scale / Resolution
                Text("Resolution Scale", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        1f to "1x (Standard)",
                        2f to "2x (4K Ultra)",
                        3f to "3x (8K Pro)"
                    ).forEach { (scale, label) ->
                        val isSelected = selectedScale == scale
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) ChampagneGold else SurfaceElevated)
                                .clickable { selectedScale = scale }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) ObsidianBg else TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Info card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceCard)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Dimensions:", color = TextMuted, fontSize = 12.sp)
                        Text("$outputWidth x $outputHeight px", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (selectedFormat != ExportFormat.PNG) {
                    Spacer(modifier = Modifier.height(10.dp))
                    LuxurySliderRow(
                        title = "Compression Quality",
                        value = quality.toFloat(),
                        onValueChange = { quality = it.toInt() },
                        valueRange = 50f..100f,
                        valueDisplay = "$quality%"
                    )
                }

                if (exportSuccessMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(EmeraldSuccess.copy(alpha = 0.15f))
                            .padding(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldSuccess,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = exportSuccessMessage,
                            color = EmeraldSuccess,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (isExporting) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ChampagneGold, modifier = Modifier.size(36.dp))
                    }
                } else {
                    LuxuryButton(
                        text = "Save to Gallery & Export",
                        icon = Icons.Default.Download,
                        onClick = {
                            onExport(
                                ExportConfig(
                                    format = selectedFormat,
                                    scaleFactor = selectedScale,
                                    quality = quality
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
