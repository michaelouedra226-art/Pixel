package com.example.feature.editor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.data.export.ExportConfig
import com.example.core.data.export.ExportEngine
import com.example.core.data.export.ExportFormat
import com.example.core.data.repository.ProjectPreset
import com.example.core.data.repository.ProjectRepository
import com.example.core.engine.commands.CommandManager
import com.example.core.engine.commands.ProjectSnapshotCommand
import com.example.core.engine.model.AnchorPoint
import com.example.core.engine.model.BezierLayer
import com.example.core.engine.model.CanvasProject
import com.example.core.engine.model.ColorFill
import com.example.core.engine.model.DrawingLayer
import com.example.core.engine.model.DrawingStroke
import com.example.core.engine.model.ImageLayer
import com.example.core.engine.model.Layer
import com.example.core.engine.model.ShapeLayer
import com.example.core.engine.model.ShapeType
import com.example.core.engine.model.TextLayer
import com.example.core.engine.model.Transform
import com.example.feature.editor.components.ActiveTool
import com.example.feature.editor.panels.BuiltInSticker
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

enum class LayerAlignment {
    CENTER_HORIZONTAL,
    CENTER_VERTICAL,
    ALIGN_LEFT,
    ALIGN_RIGHT,
    ALIGN_TOP,
    ALIGN_BOTTOM,
    FIT_CANVAS,
    RESET_TRANSFORMS
}

sealed interface EditorIntent {
    data class LoadProject(val projectId: String) : EditorIntent
    data class SelectTool(val tool: ActiveTool) : EditorIntent
    data class SelectLayer(val layerId: String?) : EditorIntent
    data class UpdateLayer(val layer: Layer) : EditorIntent
    data class UpdateTransform(val layerId: String, val transform: Transform) : EditorIntent
    data class AlignSelected(val alignment: LayerAlignment) : EditorIntent
    data object AddTextLayer : EditorIntent
    data class AddShapeLayer(val shapeType: ShapeType) : EditorIntent
    data class AddStickerLayer(val sticker: BuiltInSticker) : EditorIntent
    data class AddImageLayer(val uri: String) : EditorIntent
    data class AddDrawingStroke(val stroke: DrawingStroke) : EditorIntent
    data object DeleteSelectedLayer : EditorIntent
    data object DuplicateSelectedLayer : EditorIntent
    data class MoveLayerUp(val layerId: String) : EditorIntent
    data class MoveLayerDown(val layerId: String) : EditorIntent
    data class ToggleLayerVisibility(val layerId: String) : EditorIntent
    data class ToggleLayerLock(val layerId: String) : EditorIntent
    data class ConvertShapeToBezier(val layerId: String) : EditorIntent
    data class UpdateProjectBg(val project: CanvasProject) : EditorIntent
    data class ApplyPresetSize(val preset: ProjectPreset) : EditorIntent
    data class MergeLayers(val layerIds: List<String>) : EditorIntent
    data object Undo : EditorIntent
    data object Redo : EditorIntent
    data object SaveProject : EditorIntent
    data class ExportArtwork(val context: Context, val config: ExportConfig) : EditorIntent
    data class ToggleGrid(val enabled: Boolean) : EditorIntent
    data class ToggleSnap(val enabled: Boolean) : EditorIntent
    data class SetBrushColor(val color: Long) : EditorIntent
    data class SetBrushSize(val size: Float) : EditorIntent
    data class ToggleEraser(val isEraser: Boolean) : EditorIntent
    data class SelectAnchor(val index: Int?) : EditorIntent
}

data class EditorUiState(
    val project: CanvasProject = CanvasProject(),
    val selectedLayerId: String? = null,
    val activeTool: ActiveTool = ActiveTool.SELECT,
    val isGridEnabled: Boolean = false,
    val isSnapEnabled: Boolean = true,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val brushColor: Long = 0xFFD4AF37,
    val brushSize: Float = 10f,
    val isEraser: Boolean = false,
    val selectedAnchorIndex: Int? = null,
    val isExportDialogOpen: Boolean = false,
    val isExporting: Boolean = false,
    val exportSuccessMessage: String? = null,
    val bitmapCache: Map<String, Bitmap> = emptyMap()
)

sealed interface EditorSideEffect {
    data class ShowToast(val message: String) : EditorSideEffect
    data class ExportFinished(val uri: Uri) : EditorSideEffect
}

class EditorViewModel(
    private val repository: ProjectRepository
) : ViewModel() {

    private val commandManager = CommandManager()

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private val _sideEffects = MutableSharedFlow<EditorSideEffect>()
    val sideEffects: SharedFlow<EditorSideEffect> = _sideEffects.asSharedFlow()

    init {
        viewModelScope.launch {
            commandManager.canUndo.collect { undoable ->
                _uiState.update { it.copy(canUndo = undoable) }
            }
        }
        viewModelScope.launch {
            commandManager.canRedo.collect { redoable ->
                _uiState.update { it.copy(canRedo = redoable) }
            }
        }
    }

    fun processIntent(intent: EditorIntent) {
        when (intent) {
            is EditorIntent.LoadProject -> loadProject(intent.projectId)
            is EditorIntent.SelectTool -> _uiState.update { it.copy(activeTool = intent.tool) }
            is EditorIntent.SelectLayer -> _uiState.update { it.copy(selectedLayerId = intent.layerId) }
            is EditorIntent.UpdateLayer -> updateLayer(intent.layer)
            is EditorIntent.UpdateTransform -> updateLayerTransform(intent.layerId, intent.transform)
            is EditorIntent.AlignSelected -> alignSelectedLayer(intent.alignment)
            is EditorIntent.AddTextLayer -> addTextLayer()
            is EditorIntent.AddShapeLayer -> addShapeLayer(intent.shapeType)
            is EditorIntent.AddStickerLayer -> addStickerLayer(intent.sticker)
            is EditorIntent.AddImageLayer -> addImageLayer(intent.uri)
            is EditorIntent.AddDrawingStroke -> addDrawingStroke(intent.stroke)
            is EditorIntent.DeleteSelectedLayer -> deleteSelectedLayer()
            is EditorIntent.DuplicateSelectedLayer -> duplicateSelectedLayer()
            is EditorIntent.MoveLayerUp -> moveLayer(intent.layerId, up = true)
            is EditorIntent.MoveLayerDown -> moveLayer(intent.layerId, up = false)
            is EditorIntent.ToggleLayerVisibility -> toggleLayerVisibility(intent.layerId)
            is EditorIntent.ToggleLayerLock -> toggleLayerLock(intent.layerId)
            is EditorIntent.ConvertShapeToBezier -> convertShapeToBezier(intent.layerId)
            is EditorIntent.UpdateProjectBg -> updateProjectBg(intent.project)
            is EditorIntent.ApplyPresetSize -> applyPresetSize(intent.preset)
            is EditorIntent.MergeLayers -> mergeLayers(intent.layerIds)
            is EditorIntent.Undo -> undo()
            is EditorIntent.Redo -> redo()
            is EditorIntent.SaveProject -> saveCurrentProject()
            is EditorIntent.ExportArtwork -> exportProject(intent.context, intent.config)
            is EditorIntent.ToggleGrid -> _uiState.update { it.copy(isGridEnabled = intent.enabled) }
            is EditorIntent.ToggleSnap -> _uiState.update { it.copy(isSnapEnabled = intent.enabled) }
            is EditorIntent.SetBrushColor -> _uiState.update { it.copy(brushColor = intent.color) }
            is EditorIntent.SetBrushSize -> _uiState.update { it.copy(brushSize = intent.size) }
            is EditorIntent.ToggleEraser -> _uiState.update { it.copy(isEraser = intent.isEraser) }
            is EditorIntent.SelectAnchor -> _uiState.update { it.copy(selectedAnchorIndex = intent.index) }
        }
    }

    private fun loadProject(projectId: String) {
        viewModelScope.launch {
            val project = repository.getProjectById(projectId) ?: repository.createInitialStarterProject()
            commandManager.clear()
            _uiState.update {
                it.copy(
                    project = project,
                    selectedLayerId = project.layers.lastOrNull()?.id
                )
            }
        }
    }

    private fun updateLayer(layer: Layer) {
        val current = _uiState.value.project
        val oldLayers = current.layers
        val newLayers = current.layers.map { if (it.id == layer.id) layer else it }
        val updatedProject = commandManager.execute(
            ProjectSnapshotCommand("Modifier ${layer.name}", oldLayers, newLayers),
            current
        )
        _uiState.update { it.copy(project = updatedProject) }
    }

    private fun updateLayerTransform(layerId: String, transform: Transform) {
        val current = _uiState.value.project
        val newLayers = current.layers.map {
            if (it.id == layerId) it.copyWithTransform(transform) else it
        }
        _uiState.update { it.copy(project = current.copy(layers = newLayers)) }
    }

    private fun alignSelectedLayer(alignment: LayerAlignment) {
        val selectedId = _uiState.value.selectedLayerId ?: return
        val current = _uiState.value.project
        val layer = current.layers.find { it.id == selectedId } ?: return
        val t = layer.transform
        val canvasW = current.width.toFloat()
        val canvasH = current.height.toFloat()

        val updatedTransform = when (alignment) {
            LayerAlignment.CENTER_HORIZONTAL -> t.copy(x = (canvasW - t.width * t.scaleX) / 2f)
            LayerAlignment.CENTER_VERTICAL -> t.copy(y = (canvasH - t.height * t.scaleY) / 2f)
            LayerAlignment.ALIGN_LEFT -> t.copy(x = 0f)
            LayerAlignment.ALIGN_RIGHT -> t.copy(x = canvasW - t.width * t.scaleX)
            LayerAlignment.ALIGN_TOP -> t.copy(y = 0f)
            LayerAlignment.ALIGN_BOTTOM -> t.copy(y = canvasH - t.height * t.scaleY)
            LayerAlignment.FIT_CANVAS -> t.copy(
                x = canvasW * 0.05f,
                y = canvasH * 0.05f,
                width = canvasW * 0.9f,
                height = canvasH * 0.9f,
                scaleX = 1f,
                scaleY = 1f
            )
            LayerAlignment.RESET_TRANSFORMS -> t.copy(
                rotation = 0f,
                rotationX = 0f,
                rotationY = 0f,
                scaleX = 1f,
                scaleY = 1f
            )
        }

        updateLayerTransform(selectedId, updatedTransform)
    }

    private fun addTextLayer() {
        val current = _uiState.value.project
        val cx = current.width / 2f - 160f
        val cy = current.height / 2f - 55f
        val newLayer = TextLayer(
            name = "Texte ${current.layers.size + 1}",
            text = "NOUVEAU TEXTE",
            transform = Transform(x = cx, y = cy, width = 320f, height = 110f),
            zIndex = current.layers.size
        )
        val oldLayers = current.layers
        val newLayers = current.layers + newLayer
        val updatedProject = commandManager.execute(
            ProjectSnapshotCommand("Ajouter Texte", oldLayers, newLayers),
            current
        )
        _uiState.update {
            it.copy(
                project = updatedProject,
                selectedLayerId = newLayer.id,
                activeTool = ActiveTool.TEXT
            )
        }
    }

    private fun addShapeLayer(shapeType: ShapeType) {
        val current = _uiState.value.project
        val cx = current.width / 2f - 110f
        val cy = current.height / 2f - 110f
        val newLayer = ShapeLayer(
            name = "${shapeType.name.lowercase().replaceFirstChar { it.uppercase() }} ${current.layers.size + 1}",
            shapeType = shapeType,
            transform = Transform(x = cx, y = cy, width = 220f, height = 220f),
            zIndex = current.layers.size
        )
        val oldLayers = current.layers
        val newLayers = current.layers + newLayer
        val updatedProject = commandManager.execute(
            ProjectSnapshotCommand("Ajouter Forme", oldLayers, newLayers),
            current
        )
        _uiState.update {
            it.copy(
                project = updatedProject,
                selectedLayerId = newLayer.id,
                activeTool = ActiveTool.SHAPES
            )
        }
    }

    private fun addStickerLayer(sticker: BuiltInSticker) {
        val current = _uiState.value.project
        val cx = current.width / 2f - 100f
        val cy = current.height / 2f - 60f
        val newLayer = ImageLayer(
            name = sticker.name,
            isSticker = true,
            stickerName = sticker.name,
            transform = Transform(x = cx, y = cy, width = 200f, height = 120f),
            zIndex = current.layers.size
        )
        val oldLayers = current.layers
        val newLayers = current.layers + newLayer
        val updatedProject = commandManager.execute(
            ProjectSnapshotCommand("Ajouter Sticker", oldLayers, newLayers),
            current
        )
        _uiState.update {
            it.copy(
                project = updatedProject,
                selectedLayerId = newLayer.id,
                activeTool = ActiveTool.STICKERS
            )
        }
    }

    private fun addImageLayer(uri: String) {
        val current = _uiState.value.project
        val cx = current.width / 2f - 150f
        val cy = current.height / 2f - 150f
        val newLayer = ImageLayer(
            name = "Image ${current.layers.size + 1}",
            imageUri = uri,
            transform = Transform(x = cx, y = cy, width = 300f, height = 300f),
            zIndex = current.layers.size
        )
        val oldLayers = current.layers
        val newLayers = current.layers + newLayer
        val updatedProject = commandManager.execute(
            ProjectSnapshotCommand("Ajouter Image", oldLayers, newLayers),
            current
        )
        _uiState.update {
            it.copy(
                project = updatedProject,
                selectedLayerId = newLayer.id,
                activeTool = ActiveTool.STICKERS
            )
        }
    }

    private fun addDrawingStroke(stroke: DrawingStroke) {
        val current = _uiState.value.project
        val drawingLayer = current.layers.filterIsInstance<DrawingLayer>().firstOrNull()
            ?: DrawingLayer(
                name = "Zone de dessin",
                transform = Transform(0f, 0f, current.width.toFloat(), current.height.toFloat()),
                zIndex = current.layers.size
            )

        val updatedDrawingLayer = drawingLayer.copy(strokes = drawingLayer.strokes + stroke)
        val oldLayers = current.layers
        val newLayers = if (current.layers.any { it.id == drawingLayer.id }) {
            current.layers.map { if (it.id == drawingLayer.id) updatedDrawingLayer else it }
        } else {
            current.layers + updatedDrawingLayer
        }

        val updatedProject = commandManager.execute(
            ProjectSnapshotCommand("Tracé Dessin", oldLayers, newLayers),
            current
        )
        _uiState.update { it.copy(project = updatedProject) }
    }

    private fun deleteSelectedLayer() {
        val selectedId = _uiState.value.selectedLayerId ?: return
        val current = _uiState.value.project
        val oldLayers = current.layers
        val newLayers = current.layers.filter { it.id != selectedId }
        val updatedProject = commandManager.execute(
            ProjectSnapshotCommand("Supprimer Calque", oldLayers, newLayers),
            current
        )
        _uiState.update {
            it.copy(
                project = updatedProject,
                selectedLayerId = newLayers.lastOrNull()?.id
            )
        }
    }

    private fun duplicateSelectedLayer() {
        val selectedId = _uiState.value.selectedLayerId ?: return
        val current = _uiState.value.project
        val target = current.layers.find { it.id == selectedId } ?: return
        val duplicated = when (target) {
            is TextLayer -> target.copy(
                id = UUID.randomUUID().toString(),
                name = "${target.name} (Copie)",
                transform = target.transform.copy(x = target.transform.x + 30f, y = target.transform.y + 30f),
                zIndex = current.layers.size
            )
            is ShapeLayer -> target.copy(
                id = UUID.randomUUID().toString(),
                name = "${target.name} (Copie)",
                transform = target.transform.copy(x = target.transform.x + 30f, y = target.transform.y + 30f),
                zIndex = current.layers.size
            )
            is BezierLayer -> target.copy(
                id = UUID.randomUUID().toString(),
                name = "${target.name} (Copie)",
                transform = target.transform.copy(x = target.transform.x + 30f, y = target.transform.y + 30f),
                zIndex = current.layers.size
            )
            is ImageLayer -> target.copy(
                id = UUID.randomUUID().toString(),
                name = "${target.name} (Copie)",
                transform = target.transform.copy(x = target.transform.x + 30f, y = target.transform.y + 30f),
                zIndex = current.layers.size
            )
            is DrawingLayer -> target.copy(
                id = UUID.randomUUID().toString(),
                name = "${target.name} (Copie)",
                zIndex = current.layers.size
            )
        }
        val oldLayers = current.layers
        val newLayers = current.layers + duplicated
        val updatedProject = commandManager.execute(
            ProjectSnapshotCommand("Dupliquer Calque", oldLayers, newLayers),
            current
        )
        _uiState.update {
            it.copy(
                project = updatedProject,
                selectedLayerId = duplicated.id
            )
        }
    }

    private fun moveLayer(layerId: String, up: Boolean) {
        val current = _uiState.value.project
        val index = current.layers.indexOfFirst { it.id == layerId }
        if (index == -1) return
        val targetIndex = if (up) index + 1 else index - 1
        if (targetIndex !in current.layers.indices) return

        val mutable = current.layers.toMutableList()
        val item = mutable.removeAt(index)
        mutable.add(targetIndex, item)
        val reindexed = mutable.mapIndexed { idx, layer -> layer.copyWithZIndex(idx) }

        val oldLayers = current.layers
        val updatedProject = commandManager.execute(
            ProjectSnapshotCommand("Réorganiser Calque", oldLayers, reindexed),
            current
        )
        _uiState.update { it.copy(project = updatedProject) }
    }

    private fun toggleLayerVisibility(layerId: String) {
        val current = _uiState.value.project
        val newLayers = current.layers.map {
            if (it.id == layerId) it.copyWithVisibility(!it.isVisible) else it
        }
        _uiState.update { it.copy(project = current.copy(layers = newLayers)) }
    }

    private fun toggleLayerLock(layerId: String) {
        val current = _uiState.value.project
        val newLayers = current.layers.map {
            if (it.id == layerId) it.copyWithLock(!it.isLocked) else it
        }
        _uiState.update { it.copy(project = current.copy(layers = newLayers)) }
    }

    private fun convertShapeToBezier(layerId: String) {
        val current = _uiState.value.project
        val target = current.layers.find { it.id == layerId } as? ShapeLayer ?: return
        val t = target.transform
        val w = t.width
        val h = t.height

        val bezierLayer = BezierLayer(
            id = UUID.randomUUID().toString(),
            name = "${target.name} (Bézier)",
            transform = t,
            zIndex = target.zIndex,
            isClosed = true,
            fill = target.fill,
            stroke = target.stroke,
            anchors = listOf(
                AnchorPoint(position = androidx.compose.ui.geometry.Offset(0f, 0f), handleIn = androidx.compose.ui.geometry.Offset(-20f, 0f), handleOut = androidx.compose.ui.geometry.Offset(20f, 0f)),
                AnchorPoint(position = androidx.compose.ui.geometry.Offset(w, 0f), handleIn = androidx.compose.ui.geometry.Offset(-20f, 0f), handleOut = androidx.compose.ui.geometry.Offset(20f, 0f)),
                AnchorPoint(position = androidx.compose.ui.geometry.Offset(w, h), handleIn = androidx.compose.ui.geometry.Offset(20f, 0f), handleOut = androidx.compose.ui.geometry.Offset(-20f, 0f)),
                AnchorPoint(position = androidx.compose.ui.geometry.Offset(0f, h), handleIn = androidx.compose.ui.geometry.Offset(20f, 0f), handleOut = androidx.compose.ui.geometry.Offset(-20f, 0f))
            )
        )

        val oldLayers = current.layers
        val newLayers = current.layers.map { if (it.id == layerId) bezierLayer else it }
        val updatedProject = commandManager.execute(
            ProjectSnapshotCommand("Convertir en Bézier", oldLayers, newLayers),
            current
        )
        _uiState.update {
            it.copy(
                project = updatedProject,
                selectedLayerId = bezierLayer.id,
                activeTool = ActiveTool.BEZIER
            )
        }
    }

    private fun mergeLayers(layerIds: List<String>) {
        if (layerIds.size < 2) return
        val current = _uiState.value.project
        val toMerge = current.layers.filter { it.id in layerIds }.sortedBy { it.zIndex }
        if (toMerge.isEmpty()) return

        val minX = toMerge.minOf { it.transform.x }
        val minY = toMerge.minOf { it.transform.y }
        val maxX = toMerge.maxOf { it.transform.x + it.transform.width * it.transform.scaleX }
        val maxY = toMerge.maxOf { it.transform.y + it.transform.height * it.transform.scaleY }
        val mergedW = (maxX - minX).coerceAtLeast(50f)
        val mergedH = (maxY - minY).coerceAtLeast(50f)

        val mergedLayer = ImageLayer(
            id = UUID.randomUUID().toString(),
            name = "Calques Fusionnés",
            transform = Transform(x = minX, y = minY, width = mergedW, height = mergedH),
            zIndex = toMerge.first().zIndex,
            isSticker = true,
            stickerName = "FUSION"
        )

        val oldLayers = current.layers
        val remaining = current.layers.filterNot { it.id in layerIds }
        val newLayers = (remaining + mergedLayer).sortedBy { it.zIndex }

        val updatedProject = commandManager.execute(
            ProjectSnapshotCommand("Fusionner Calques", oldLayers, newLayers),
            current
        )
        _uiState.update {
            it.copy(
                project = updatedProject,
                selectedLayerId = mergedLayer.id
            )
        }
        viewModelScope.launch {
            _sideEffects.emit(EditorSideEffect.ShowToast("Calques fusionnés avec succès !"))
        }
    }

    private fun updateProjectBg(project: CanvasProject) {
        val current = _uiState.value.project
        val updated = commandManager.execute(
            ProjectSnapshotCommand(
                "Changer Fond",
                current.layers,
                current.layers,
                current.backgroundColor,
                project.backgroundColor
            ),
            project
        )
        _uiState.update { it.copy(project = updated) }
    }

    private fun applyPresetSize(preset: ProjectPreset) {
        val current = _uiState.value.project
        _uiState.update {
            it.copy(
                project = current.copy(
                    width = preset.width,
                    height = preset.height
                )
            )
        }
    }

    private fun undo() {
        val current = _uiState.value.project
        val previous = commandManager.undo(current)
        _uiState.update { it.copy(project = previous) }
    }

    private fun redo() {
        val current = _uiState.value.project
        val next = commandManager.redo(current)
        _uiState.update { it.copy(project = next) }
    }

    private fun saveCurrentProject() {
        viewModelScope.launch {
            val current = _uiState.value.project
            repository.saveProject(current)
            _sideEffects.emit(EditorSideEffect.ShowToast("Projet enregistré avec succès !"))
        }
    }

    private fun exportProject(context: Context, config: ExportConfig) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportSuccessMessage = null) }
            val project = _uiState.value.project
            val uri = if (config.format == ExportFormat.SVG) {
                val svg = ExportEngine.generateProjectSvg(project)
                ExportEngine.saveSvgToStorage(context, svg, project.title)
            } else {
                val bitmap = ExportEngine.renderProjectToBitmap(project, config, _uiState.value.bitmapCache)
                ExportEngine.saveBitmapToGallery(
                    context,
                    bitmap,
                    project.title,
                    config.format,
                    config.quality
                )
            }
            _uiState.update {
                it.copy(
                    isExporting = false,
                    exportSuccessMessage = if (uri != null) {
                        if (config.format == ExportFormat.SVG) "Fichier vectoriel SVG sauvegardé dans Téléchargements/PixelForge !"
                        else "Image HD sauvegardée dans Galerie/PixelForge !"
                    } else "Erreur lors de l'export"
                )
            }
            if (uri != null) {
                _sideEffects.emit(EditorSideEffect.ExportFinished(uri))
            }
        }
    }
}

class EditorViewModelFactory(private val repository: ProjectRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EditorViewModel(repository) as T
    }
}
