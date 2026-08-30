package com.example.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.engine.model.ColorFill
import com.example.core.engine.model.GradientDef
import com.example.core.engine.model.GradientType
import com.example.ui.theme.BorderGlass
import com.example.ui.theme.BorderGold
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

object LuxuryPalettes {
    val swatches = listOf(
        0xFFD4AF37, // Champagne Gold
        0xFFE5E4E2, // Platinum White
        0xFF0A0A0C, // Obsidian Deep Black
        0xFF1A1A1D, // Charcoal Surface
        0xFFFF3366, // Crimson Rose
        0xFFFF7A00, // Sunset Orange
        0xFFFFD600, // Radiant Yellow
        0xFF00E676, // Emerald Mint
        0xFF00E5FF, // Electric Cyan
        0xFF2979FF, // Royal Sapphire
        0xFF7C4DFF, // Imperial Purple
        0xFFFF4081, // Neon Pink
        0xFFFFFFFF, // Pure Snow
        0xFF8E8E93, // Titanium Gray
        0xFF3A3A3C, // Space Gray
        0xFF5B450C  // Antique Bronze
    )

    val gradients = listOf(
        GradientDef(GradientType.LINEAR, listOf(0xFFF3E5AB, 0xFFD4AF37, 0xFFAA820A), 45f), // Gold Deluxe
        GradientDef(GradientType.LINEAR, listOf(0xFFE5E4E2, 0xFFFFFFFF, 0xFF9E9E9E), 90f), // Pure Chrome
        GradientDef(GradientType.LINEAR, listOf(0xFF00E5FF, 0xFF9D4EDD), 135f),            // Cyberpunk
        GradientDef(GradientType.LINEAR, listOf(0xFFFF3366, 0xFFFF7A00), 90f),             // Sunset
        GradientDef(GradientType.LINEAR, listOf(0xFF00E676, 0xFF00E5FF), 45f),             // Aurora
        GradientDef(GradientType.LINEAR, listOf(0xFF2B1055, 0xFF7597DE), 180f),            // Velvet Sky
        GradientDef(GradientType.LINEAR, listOf(0xFF1A1A1D, 0xFF0A0A0C), 90f),             // Dark Obsidian
        GradientDef(GradientType.RADIAL, listOf(0xFFD4AF37, 0xFF141416), 0f)              // Radial Spotlight
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LuxuryColorPicker(
    fill: ColorFill = ColorFill.White,
    onFillChange: (ColorFill) -> Unit = {},
    modifier: Modifier = Modifier,
    allowGradients: Boolean = true
) {
    var selectedTab by remember { mutableIntStateOf(if (fill.isGradient && allowGradients) 1 else 0) }

    val currentColor = Color(fill.solidColor)
    var red by remember(fill.solidColor) { mutableStateOf(currentColor.red) }
    var green by remember(fill.solidColor) { mutableStateOf(currentColor.green) }
    var blue by remember(fill.solidColor) { mutableStateOf(currentColor.blue) }
    var alpha by remember(fill.solidColor) { mutableStateOf(currentColor.alpha) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceDark)
            .border(1.dp, BorderGlass, RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        if (allowGradients) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = SurfaceElevated,
                contentColor = ChampagneGold,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = ChampagneGold
                    )
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .height(38.dp)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        onFillChange(ColorFill(solidColor = fill.solidColor, isGradient = false))
                    },
                    text = {
                        Text(
                            "Couleur Unie",
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 0) ChampagneGold else TextSecondary
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        val grad = fill.gradient ?: LuxuryPalettes.gradients[0]
                        onFillChange(ColorFill(isGradient = true, gradient = grad))
                    },
                    text = {
                        Text(
                            "Dégradé",
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 1) ChampagneGold else TextSecondary
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        if (selectedTab == 0 || !allowGradients) {
            // Swatch Grid
            Text(
                "Nuancier Pro",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                LuxuryPalettes.swatches.forEach { colVal ->
                    val isSelected = !fill.isGradient && fill.solidColor == colVal
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(colVal))
                            .border(
                                width = if (isSelected) 2.5.dp else 1.dp,
                                color = if (isSelected) ChampagneGold else BorderGlass,
                                shape = CircleShape
                            )
                            .clickable {
                                onFillChange(ColorFill(solidColor = colVal, isGradient = false))
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = if (Color(colVal).red + Color(colVal).green + Color(colVal).blue > 1.5f) Color.Black else Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Custom RGBA Sliders
            Text(
                "Personnaliser RVB",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            LuxurySliderRow(
                label = "Rouge",
                value = red * 255f,
                valueRange = 0f..255f,
                unit = "",
                onValueChange = {
                    red = it / 255f
                    val newCol = Color(red, green, blue, alpha).toArgb().toLong()
                    onFillChange(ColorFill(solidColor = newCol, isGradient = false))
                }
            )

            LuxurySliderRow(
                label = "Vert",
                value = green * 255f,
                valueRange = 0f..255f,
                unit = "",
                onValueChange = {
                    green = it / 255f
                    val newCol = Color(red, green, blue, alpha).toArgb().toLong()
                    onFillChange(ColorFill(solidColor = newCol, isGradient = false))
                }
            )

            LuxurySliderRow(
                label = "Bleu",
                value = blue * 255f,
                valueRange = 0f..255f,
                unit = "",
                onValueChange = {
                    blue = it / 255f
                    val newCol = Color(red, green, blue, alpha).toArgb().toLong()
                    onFillChange(ColorFill(solidColor = newCol, isGradient = false))
                }
            )
        } else {
            // Gradient presets
            Text(
                "Styles de Dégradés",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                LuxuryPalettes.gradients.forEach { gradDef ->
                    val isSelected = fill.isGradient && fill.gradient == gradDef
                    Box(
                        modifier = Modifier
                            .size(width = 65.dp, height = 36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Brush.linearGradient(gradDef.colors.map { Color(it) }))
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) ChampagneGold else BorderGlass,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                onFillChange(ColorFill(isGradient = true, gradient = gradDef))
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            if (fill.isGradient && fill.gradient != null) {
                Spacer(modifier = Modifier.height(14.dp))
                LuxurySliderRow(
                    label = "Angle du dégradé",
                    value = fill.gradient.angle,
                    valueRange = 0f..360f,
                    unit = "°",
                    onValueChange = {
                        onFillChange(
                            fill.copy(
                                gradient = fill.gradient.copy(angle = it)
                            )
                        )
                    }
                )
            }
        }
    }
}
