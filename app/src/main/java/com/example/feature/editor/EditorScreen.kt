package com.example.feature.editor

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AlignHorizontalCenter
import androidx.compose.material.icons.filled.AlignHorizontalLeft
import androidx.compose.material.icons.filled.AlignHorizontalRight
import androidx.compose.material.icons.filled.AlignVerticalBottom
import androidx.compose.material.icons.filled.AlignVerticalCenter
import androidx.compose.material.icons.filled.AlignVerticalTop
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.engine.model.BezierLayer
import com.example.core.engine.model.DrawingLayer
import com.example.core.engine.model.ImageLayer
import com.example.core.engine.model.ShapeLayer
import com.example.core.engine.model.ShapeType
import com.example.core.engine.model.TextLayer
import com.example.core.ui.LuxuryButton
import com.example.feature.editor.components.ActiveTool
import com.example.feature.editor.components.EditorCanvas
import com.example.feature.editor.panels.BackgroundStudioPanel
import com.example.feature.editor.panels.BezierStudioPanel
import com.example.feature.editor.panels.DrawingStudioPanel
import com.example.feature.editor.panels.ExportDialog
import com.example.feature.editor.panels.ImageStickerStudioPanel
import com.example.feature.editor.panels.LayerManagerPanel
import com.example.feature.editor.panels.ShapeStudioPanel
import com.example.feature.editor.panels.TextStudioPanel
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
fun EditorScreen(
    viewModel: EditorViewModel,
    projectId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var showExportDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.processIntent(EditorIntent.AddImageLayer(it.toString())) }
    }

    LaunchedEffect(projectId) {
        viewModel.processIntent(EditorIntent.LoadProject(projectId))
    }

    LaunchedEffect(Unit) {
        viewModel.sideEffects.collect { effect ->
            when (effect) {
                is EditorSideEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is EditorSideEffect.ExportFinished -> {
                    Toast.makeText(context, "Exportation réussie !", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val selectedLayer = uiState.project.layers.find { it.id == uiState.selectedLayerId }

    Scaffold(
        containerColor = ObsidianBg,
        topBar = {
            // Luxury Obsidian Top Bar
            Surface(
                color = SurfaceDark,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .border(1.dp, BorderGlass)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onNavigateBack, modifier = Modifier.size(36.dp)) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Retour",
                                tint = ChampagneGold
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        Column {
                            Text(
                                text = uiState.project.title,
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = "${uiState.project.width} x ${uiState.project.height} px",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Undo / Redo
                        IconButton(
                            onClick = { viewModel.processIntent(EditorIntent.Undo) },
                            enabled = uiState.canUndo,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Undo,
                                contentDescription = "Annuler",
                                tint = if (uiState.canUndo) TextPrimary else TextMuted.copy(alpha = 0.4f)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.processIntent(EditorIntent.Redo) },
                            enabled = uiState.canRedo,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Redo,
                                contentDescription = "Rétablir",
                                tint = if (uiState.canRedo) TextPrimary else TextMuted.copy(alpha = 0.4f)
                            )
                        }

                        // Grid toggle
                        IconButton(
                            onClick = { viewModel.processIntent(EditorIntent.ToggleGrid(!uiState.isGridEnabled)) },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.GridOn,
                                contentDescription = "Grille",
                                tint = if (uiState.isGridEnabled) ChampagneGold else TextSecondary
                            )
                        }

                        // Save icon
                        IconButton(
                            onClick = { viewModel.processIntent(EditorIntent.SaveProject) },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "Enregistrer",
                                tint = TextPrimary
                            )
                        }

                        // Export Button
                        LuxuryButton(
                            text = "Exporter",
                            icon = Icons.Default.Download,
                            onClick = { showExportDialog = true },
                            modifier = Modifier.height(36.dp)
                        )
                    }
                }
            }
        },
        bottomBar = {
            // Bottom Studio Bar & Tool Inspector
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark)
                    .navigationBarsPadding()
            ) {
                // Collapsible Property Inspector for selected tool
                AnimatedVisibility(
                    visible = uiState.activeTool != ActiveTool.SELECT,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it })
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(290.dp)
                            .border(1.dp, BorderGlass)
                    ) {
                        when (uiState.activeTool) {
                            ActiveTool.TEXT -> {
                                if (selectedLayer is TextLayer) {
                                    TextStudioPanel(
                                        layer = selectedLayer,
                                        onUpdate = { viewModel.processIntent(EditorIntent.UpdateLayer(it)) }
                                    )
                                } else {
                                    EmptyToolSelectionState(
                                        title = "Studio Texte & Typographie",
                                        subtitle = "Ajoutez un calque de texte ou touchez un texte existant",
                                        actionText = "Ajouter un Texte",
                                        onAction = { viewModel.processIntent(EditorIntent.AddTextLayer) }
                                    )
                                }
                            }
                            ActiveTool.SHAPES -> {
                                if (selectedLayer is ShapeLayer) {
                                    ShapeStudioPanel(
                                        layer = selectedLayer,
                                        onUpdate = { viewModel.processIntent(EditorIntent.UpdateLayer(it)) },
                                        onConvertToPath = { viewModel.processIntent(EditorIntent.ConvertShapeToBezier(selectedLayer.id)) }
                                    )
                                } else {
                                    EmptyToolSelectionState(
                                        title = "Formes Géométriques",
                                        subtitle = "Insérez des formes vectorielles précises et personnalisables",
                                        actionText = "Insérer Rectangle Arrondi",
                                        onAction = { viewModel.processIntent(EditorIntent.AddShapeLayer(ShapeType.ROUNDED_RECT)) }
                                    )
                                }
                            }
                            ActiveTool.BEZIER -> {
                                if (selectedLayer is BezierLayer) {
                                    BezierStudioPanel(
                                        layer = selectedLayer,
                                        onUpdate = { viewModel.processIntent(EditorIntent.UpdateLayer(it)) },
                                        selectedAnchorIndex = uiState.selectedAnchorIndex,
                                        onSelectAnchor = { viewModel.processIntent(EditorIntent.SelectAnchor(it)) }
                                    )
                                } else {
                                    EmptyToolSelectionState(
                                        title = "Studio Courbe Bézier Vectorielle",
                                        subtitle = "Sélectionnez une forme ou convertissez-la en nœuds Bézier",
                                        actionText = "Créer Étoile Vectorielle",
                                        onAction = { viewModel.processIntent(EditorIntent.AddShapeLayer(ShapeType.STAR)) }
                                    )
                                }
                            }
                            ActiveTool.DRAW -> {
                                val drawLayer = uiState.project.layers.filterIsInstance<DrawingLayer>().firstOrNull()
                                    ?: DrawingLayer()
                                DrawingStudioPanel(
                                    layer = drawLayer,
                                    onUpdate = { viewModel.processIntent(EditorIntent.UpdateLayer(it)) },
                                    brushColor = uiState.brushColor,
                                    onBrushColorChange = { viewModel.processIntent(EditorIntent.SetBrushColor(it)) },
                                    brushSize = uiState.brushSize,
                                    onBrushSizeChange = { viewModel.processIntent(EditorIntent.SetBrushSize(it)) },
                                    isEraser = uiState.isEraser,
                                    onEraserToggle = { viewModel.processIntent(EditorIntent.ToggleEraser(it)) }
                                )
                            }
                            ActiveTool.STICKERS -> {
                                ImageStickerStudioPanel(
                                    layer = selectedLayer as? ImageLayer,
                                    onUpdate = { viewModel.processIntent(EditorIntent.UpdateLayer(it)) },
                                    onPickImageFromGallery = { photoPickerLauncher.launch("image/*") },
                                    onAddSticker = { viewModel.processIntent(EditorIntent.AddStickerLayer(it)) }
                                )
                            }
                            ActiveTool.LAYERS -> {
                                LayerManagerPanel(
                                    layers = uiState.project.layers,
                                    selectedLayerId = uiState.selectedLayerId,
                                    onSelectLayer = { viewModel.processIntent(EditorIntent.SelectLayer(it)) },
                                    onToggleVisibility = { viewModel.processIntent(EditorIntent.ToggleLayerVisibility(it)) },
                                    onToggleLock = { viewModel.processIntent(EditorIntent.ToggleLayerLock(it)) },
                                    onMoveLayerUp = { viewModel.processIntent(EditorIntent.MoveLayerUp(it)) },
                                    onMoveLayerDown = { viewModel.processIntent(EditorIntent.MoveLayerDown(it)) },
                                    onDuplicateLayer = {
                                        viewModel.processIntent(EditorIntent.SelectLayer(it))
                                        viewModel.processIntent(EditorIntent.DuplicateSelectedLayer)
                                    },
                                    onDeleteLayer = {
                                        viewModel.processIntent(EditorIntent.SelectLayer(it))
                                        viewModel.processIntent(EditorIntent.DeleteSelectedLayer)
                                    },
                                    onOpacityChange = { id, opacity ->
                                        val l = uiState.project.layers.find { it.id == id }
                                        if (l != null) viewModel.processIntent(EditorIntent.UpdateLayer(l.copyWithOpacity(opacity)))
                                    },
                                    onBlendModeChange = { id, mode ->
                                        val l = uiState.project.layers.find { it.id == id }
                                        if (l != null) viewModel.processIntent(EditorIntent.UpdateLayer(l.copyWithBlendMode(mode)))
                                    },
                                    onMergeLayers = { ids ->
                                        viewModel.processIntent(EditorIntent.MergeLayers(ids))
                                    }
                                )
                            }
                            ActiveTool.BACKGROUND -> {
                                BackgroundStudioPanel(
                                    project = uiState.project,
                                    onUpdateProject = { viewModel.processIntent(EditorIntent.UpdateProjectBg(it)) },
                                    onApplyPresetSize = { viewModel.processIntent(EditorIntent.ApplyPresetSize(it)) }
                                )
                            }
                            else -> Unit
                        }
                    }
                }

                // Primary Studio Toolbar Dock
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StudioToolItem(
                        icon = Icons.Default.NearMe,
                        label = "Sélection",
                        isSelected = uiState.activeTool == ActiveTool.SELECT,
                        onClick = { viewModel.processIntent(EditorIntent.SelectTool(ActiveTool.SELECT)) }
                    )
                    StudioToolItem(
                        icon = Icons.Default.TextFields,
                        label = "Texte",
                        isSelected = uiState.activeTool == ActiveTool.TEXT,
                        onClick = {
                            if (selectedLayer !is TextLayer) viewModel.processIntent(EditorIntent.AddTextLayer)
                            viewModel.processIntent(EditorIntent.SelectTool(ActiveTool.TEXT))
                        }
                    )
                    StudioToolItem(
                        icon = Icons.Default.Category,
                        label = "Formes",
                        isSelected = uiState.activeTool == ActiveTool.SHAPES,
                        onClick = { viewModel.processIntent(EditorIntent.SelectTool(ActiveTool.SHAPES)) }
                    )
                    StudioToolItem(
                        icon = Icons.Default.Edit,
                        label = "Bézier",
                        isSelected = uiState.activeTool == ActiveTool.BEZIER,
                        onClick = { viewModel.processIntent(EditorIntent.SelectTool(ActiveTool.BEZIER)) }
                    )
                    StudioToolItem(
                        icon = Icons.Default.Brush,
                        label = "Dessin",
                        isSelected = uiState.activeTool == ActiveTool.DRAW,
                        onClick = { viewModel.processIntent(EditorIntent.SelectTool(ActiveTool.DRAW)) }
                    )
                    StudioToolItem(
                        icon = Icons.Default.Image,
                        label = "Stickers",
                        isSelected = uiState.activeTool == ActiveTool.STICKERS,
                        onClick = { viewModel.processIntent(EditorIntent.SelectTool(ActiveTool.STICKERS)) }
                    )
                    StudioToolItem(
                        icon = Icons.Default.Layers,
                        label = "Calques",
                        isSelected = uiState.activeTool == ActiveTool.LAYERS,
                        onClick = { viewModel.processIntent(EditorIntent.SelectTool(ActiveTool.LAYERS)) }
                    )
                    StudioToolItem(
                        icon = Icons.Default.Wallpaper,
                        label = "Fond",
                        isSelected = uiState.activeTool == ActiveTool.BACKGROUND,
                        onClick = { viewModel.processIntent(EditorIntent.SelectTool(ActiveTool.BACKGROUND)) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            EditorCanvas(
                project = uiState.project,
                selectedLayerId = uiState.selectedLayerId,
                activeTool = uiState.activeTool,
                isGridEnabled = uiState.isGridEnabled,
                isSnapEnabled = uiState.isSnapEnabled,
                brushColor = uiState.brushColor,
                brushSize = uiState.brushSize,
                isEraser = uiState.isEraser,
                selectedAnchorIndex = uiState.selectedAnchorIndex,
                onSelectLayer = { viewModel.processIntent(EditorIntent.SelectLayer(it)) },
                onSelectAnchor = { viewModel.processIntent(EditorIntent.SelectAnchor(it)) },
                onUpdateLayerTransform = { id, t -> viewModel.processIntent(EditorIntent.UpdateTransform(id, t)) },
                onUpdateBezierAnchors = { id, anchors ->
                    val layer = uiState.project.layers.find { it.id == id } as? BezierLayer
                    if (layer != null) viewModel.processIntent(EditorIntent.UpdateLayer(layer.copy(anchors = anchors)))
                },
                onAddDrawingStroke = { viewModel.processIntent(EditorIntent.AddDrawingStroke(it)) },
                onDeleteSelectedLayer = { viewModel.processIntent(EditorIntent.DeleteSelectedLayer) },
                onDuplicateSelectedLayer = { viewModel.processIntent(EditorIntent.DuplicateSelectedLayer) },
                bitmapCache = uiState.bitmapCache
            )

            // PixelLab Floating Quick Alignment Bar (Appears when a layer is selected)
            if (selectedLayer != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceDark.copy(alpha = 0.92f))
                        .border(1.dp, BorderGold, RoundedCornerShape(16.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.processIntent(EditorIntent.AlignSelected(LayerAlignment.CENTER_HORIZONTAL)) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.AlignHorizontalCenter, contentDescription = "Centrer Horizontalement", tint = ChampagneGold, modifier = Modifier.size(18.dp))
                        }
                        IconButton(
                            onClick = { viewModel.processIntent(EditorIntent.AlignSelected(LayerAlignment.CENTER_VERTICAL)) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.AlignVerticalCenter, contentDescription = "Centrer Verticalement", tint = ChampagneGold, modifier = Modifier.size(18.dp))
                        }
                        IconButton(
                            onClick = { viewModel.processIntent(EditorIntent.AlignSelected(LayerAlignment.ALIGN_LEFT)) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.AlignHorizontalLeft, contentDescription = "Aligner Gauche", tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }
                        IconButton(
                            onClick = { viewModel.processIntent(EditorIntent.AlignSelected(LayerAlignment.ALIGN_RIGHT)) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.AlignHorizontalRight, contentDescription = "Aligner Droite", tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }
                        IconButton(
                            onClick = { viewModel.processIntent(EditorIntent.AlignSelected(LayerAlignment.ALIGN_TOP)) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.AlignVerticalTop, contentDescription = "Aligner Haut", tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }
                        IconButton(
                            onClick = { viewModel.processIntent(EditorIntent.AlignSelected(LayerAlignment.ALIGN_BOTTOM)) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.AlignVerticalBottom, contentDescription = "Aligner Bas", tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }
                        IconButton(
                            onClick = { viewModel.processIntent(EditorIntent.AlignSelected(LayerAlignment.FIT_CANVAS)) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.AspectRatio, contentDescription = "Ajuster au Canevas", tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }
                        IconButton(
                            onClick = { viewModel.processIntent(EditorIntent.AlignSelected(LayerAlignment.RESET_TRANSFORMS)) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = "Réinitialiser Rotations", tint = ChampagneGold, modifier = Modifier.size(18.dp))
                        }
                        IconButton(
                            onClick = { viewModel.processIntent(EditorIntent.DuplicateSelectedLayer) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Dupliquer", tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }
                        IconButton(
                            onClick = { viewModel.processIntent(EditorIntent.DeleteSelectedLayer) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = Color(0xFFFF453A), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }

    if (showExportDialog) {
        ExportDialog(
            project = uiState.project,
            isExporting = uiState.isExporting,
            exportSuccessMessage = uiState.exportSuccessMessage,
            onDismiss = { showExportDialog = false },
            onExport = { config ->
                viewModel.processIntent(EditorIntent.ExportArtwork(context, config))
            }
        )
    }
}

@Composable
private fun StudioToolItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) ChampagneGold else SurfaceElevated)
            .border(1.dp, if (isSelected) BorderGold else BorderGlass, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) ObsidianBg else TextPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = if (isSelected) ObsidianBg else TextSecondary,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun EmptyToolSelectionState(
    title: String,
    subtitle: String,
    actionText: String,
    onAction: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceDark)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = title, color = ChampagneGold, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = subtitle, color = TextSecondary, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(14.dp))
            LuxuryButton(
                text = actionText,
                icon = Icons.Default.Add,
                onClick = onAction
            )
        }
    }
}
