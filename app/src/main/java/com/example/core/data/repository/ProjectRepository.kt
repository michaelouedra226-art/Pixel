package com.example.core.data.repository

import android.content.Context
import androidx.room.Room
import com.example.core.data.local.PixelForgeDatabase
import com.example.core.data.local.ProjectDao
import com.example.core.data.local.ProjectEntity
import com.example.core.engine.model.BezierLayer
import com.example.core.engine.model.CanvasProject
import com.example.core.engine.model.ColorEraserDef
import com.example.core.engine.model.ColorFill
import com.example.core.engine.model.DrawingLayer
import com.example.core.engine.model.DrawingStroke
import com.example.core.engine.model.GradientDef
import com.example.core.engine.model.GradientType
import com.example.core.engine.model.ImageLayer
import com.example.core.engine.model.Layer
import com.example.core.engine.model.Layer3DEffect
import com.example.core.engine.model.ShapeLayer
import com.example.core.engine.model.ShapeType
import com.example.core.engine.model.StrokeDef
import com.example.core.engine.model.TextCurvature
import com.example.core.engine.model.TextLayer
import com.example.core.engine.model.TextReflection
import com.example.core.engine.model.Transform
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

data class ProjectPreset(
    val name: String,
    val width: Int,
    val height: Int,
    val category: String,
    val iconName: String
)

object PresetLibrary {
    val presets = listOf(
        ProjectPreset("Carré Instagram", 1080, 1080, "Réseaux", "instagram"),
        ProjectPreset("Story / Reel Instagram", 1080, 1920, "Réseaux", "story"),
        ProjectPreset("Miniature YouTube", 1280, 720, "Vidéo", "youtube"),
        ProjectPreset("Publication Facebook", 1200, 630, "Réseaux", "facebook"),
        ProjectPreset("Bannière Twitter / X", 1500, 500, "En-tête", "twitter"),
        ProjectPreset("Affiche A4 (300 DPI)", 1240, 1754, "Impression", "poster"),
        ProjectPreset("Bannière Twitch", 1200, 480, "Gaming", "twitch"),
        ProjectPreset("Format Personnalisé", 1080, 1080, "Custom", "custom")
    )
}

class ProjectRepository(
    private val projectDao: ProjectDao
) {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val allProjects: Flow<List<CanvasProject>> = projectDao.getAllProjects().map { entities ->
        entities.map { entityToProject(it) }
    }

    suspend fun getProjectById(id: String): CanvasProject? = withContext(Dispatchers.IO) {
        projectDao.getProjectById(id)?.let { entityToProject(it) }
    }

    suspend fun saveProject(project: CanvasProject) = withContext(Dispatchers.IO) {
        val entity = projectToEntity(project)
        projectDao.insertProject(entity)
    }

    suspend fun deleteProject(id: String) = withContext(Dispatchers.IO) {
        projectDao.deleteProjectById(id)
    }

    suspend fun duplicateProject(project: CanvasProject): CanvasProject = withContext(Dispatchers.IO) {
        val newProject = project.copy(
            id = UUID.randomUUID().toString(),
            title = "${project.title} (Copie)",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        saveProject(newProject)
        newProject
    }

    fun createInitialStarterProject(preset: ProjectPreset = PresetLibrary.presets[0]): CanvasProject {
        val w = preset.width.toFloat()
        val h = preset.height.toFloat()

        val bgShape = ShapeLayer(
            id = UUID.randomUUID().toString(),
            name = "Fond Géométrique",
            transform = Transform(
                x = w * 0.1f,
                y = h * 0.15f,
                width = w * 0.8f,
                height = h * 0.7f
            ),
            shapeType = ShapeType.ROUNDED_RECT,
            cornerRadius = 32f,
            fill = ColorFill(
                solidColor = 0xFF141416,
                gradient = GradientDef(
                    type = GradientType.LINEAR,
                    colors = listOf(0xFF1E1E24, 0xFF121215),
                    angle = 45f
                ),
                isGradient = true
            ),
            stroke = StrokeDef(isEnabled = true, color = 0x66D4AF37, width = 3f),
            zIndex = 0
        )

        val titleText = TextLayer(
            id = UUID.randomUUID().toString(),
            name = "Titre Principal",
            transform = Transform(
                x = w * 0.15f,
                y = h * 0.38f,
                width = w * 0.7f,
                height = 140f
            ),
            text = "PIXELFORGE",
            fontSize = 52f,
            isBold = true,
            fill = ColorFill.Gold,
            effect3D = Layer3DEffect(isEnabled = true, depth = 8f, color = 0xFF5B450C),
            stroke = StrokeDef(isEnabled = true, color = 0xFF0A0A0C, width = 2f),
            zIndex = 1
        )

        val subtitleText = TextLayer(
            id = UUID.randomUUID().toString(),
            name = "Sous-titre",
            transform = Transform(
                x = w * 0.2f,
                y = h * 0.52f,
                width = w * 0.6f,
                height = 70f
            ),
            text = "STUDIO GRAPHIQUE PRO",
            fontSize = 18f,
            isBold = true,
            letterSpacing = 4f,
            fill = ColorFill(solidColor = 0xFFE5E4E2),
            stroke = StrokeDef(isEnabled = false),
            zIndex = 2
        )

        return CanvasProject(
            id = UUID.randomUUID().toString(),
            title = "Nouveau ${preset.name}",
            width = preset.width,
            height = preset.height,
            backgroundColor = 0xFF0A0A0C,
            backgroundGradient = GradientDef(
                type = GradientType.LINEAR,
                colors = listOf(0xFF141416, 0xFF0A0A0C),
                angle = 90f
            ),
            layers = listOf(bgShape, titleText, subtitleText)
        )
    }

    private fun projectToEntity(project: CanvasProject): ProjectEntity {
        val layersJson = try {
            val jsonAdapter = moshi.adapter(List::class.java)
            jsonAdapter.toJson(project.layers.map { layerToMap(it) })
        } catch (_: Exception) {
            "[]"
        }

        return ProjectEntity(
            id = project.id,
            title = project.title,
            width = project.width,
            height = project.height,
            backgroundColor = project.backgroundColor,
            backgroundGradientJson = project.backgroundGradient?.let {
                moshi.adapter(GradientDef::class.java).toJson(it)
            },
            isTransparentBg = project.isTransparentBg,
            layersJson = layersJson,
            thumbnailBase64 = null,
            createdAt = project.createdAt,
            updatedAt = project.updatedAt
        )
    }

    private fun entityToProject(entity: ProjectEntity): CanvasProject {
        val layers = mutableListOf<Layer>()
        try {
            val jsonAdapter = moshi.adapter(List::class.java)
            val list = jsonAdapter.fromJson(entity.layersJson) as? List<Map<String, Any>>
            list?.forEach { map ->
                mapToLayer(map)?.let { layers.add(it) }
            }
        } catch (_: Exception) {}

        val grad = try {
            entity.backgroundGradientJson?.let {
                moshi.adapter(GradientDef::class.java).fromJson(it)
            }
        } catch (_: Exception) { null }

        return CanvasProject(
            id = entity.id,
            title = entity.title,
            width = entity.width,
            height = entity.height,
            backgroundColor = entity.backgroundColor,
            backgroundGradient = grad,
            isTransparentBg = entity.isTransparentBg,
            layers = if (layers.isNotEmpty()) layers else createInitialStarterProject().layers,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    private fun layerToMap(layer: Layer): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "id" to layer.id,
            "name" to layer.name,
            "isVisible" to layer.isVisible,
            "isLocked" to layer.isLocked,
            "opacity" to layer.opacity,
            "zIndex" to layer.zIndex,
            "x" to layer.transform.x,
            "y" to layer.transform.y,
            "width" to layer.transform.width,
            "height" to layer.transform.height,
            "rotation" to layer.transform.rotation,
            "scaleX" to layer.transform.scaleX,
            "scaleY" to layer.transform.scaleY,
            "rotationX" to layer.transform.rotationX,
            "rotationY" to layer.transform.rotationY,
            "perspective" to layer.transform.perspective
        )
        when (layer) {
            is TextLayer -> {
                map["type"] = "TEXT"
                map["text"] = layer.text
                map["fontSize"] = layer.fontSize
                map["isBold"] = layer.isBold
                map["isItalic"] = layer.isItalic
                map["isUnderline"] = layer.isUnderline
                map["letterSpacing"] = layer.letterSpacing
                map["textAlign"] = layer.textAlign
                map["fillColor"] = layer.fill.solidColor
                map["strokeColor"] = layer.stroke.color
                map["strokeWidth"] = layer.stroke.width
                map["strokeEnabled"] = layer.stroke.isEnabled
                map["effect3DEnabled"] = layer.effect3D.isEnabled
                map["effect3DDepth"] = layer.effect3D.depth
                map["effect3DColor"] = layer.effect3D.color
                map["reflectionEnabled"] = layer.reflection.isEnabled
                map["curvatureBend"] = layer.curvature.bend
                map["curvatureEnabled"] = layer.curvature.isEnabled
            }
            is ShapeLayer -> {
                map["type"] = "SHAPE"
                map["shapeType"] = layer.shapeType.name
                map["cornerRadius"] = layer.cornerRadius
                map["fillColor"] = layer.fill.solidColor
                map["strokeColor"] = layer.stroke.color
                map["strokeWidth"] = layer.stroke.width
                map["strokeEnabled"] = layer.stroke.isEnabled
                map["polygonSides"] = layer.polygonSides
                map["starPoints"] = layer.starPoints
                map["effect3DEnabled"] = layer.effect3D.isEnabled
                map["effect3DDepth"] = layer.effect3D.depth
            }
            is ImageLayer -> {
                map["type"] = "IMAGE"
                map["isSticker"] = layer.isSticker
                map["stickerName"] = layer.stickerName
                if (layer.imageUri != null) {
                    map["imageUri"] = layer.imageUri
                }
                map["colorEraserEnabled"] = layer.colorEraser.isEnabled
                map["colorEraserColor"] = layer.colorEraser.targetColor
                map["colorEraserTolerance"] = layer.colorEraser.tolerance
            }
            else -> {
                map["type"] = "GENERIC"
            }
        }
        return map
    }

    private fun mapToLayer(map: Map<String, Any>): Layer? {
        val type = map["type"] as? String ?: return null
        val id = map["id"] as? String ?: UUID.randomUUID().toString()
        val name = map["name"] as? String ?: "Calque"
        val isVisible = map["isVisible"] as? Boolean ?: true
        val isLocked = map["isLocked"] as? Boolean ?: false
        val opacity = (map["opacity"] as? Number)?.toFloat() ?: 1f
        val zIndex = (map["zIndex"] as? Number)?.toInt() ?: 0

        val transform = Transform(
            x = (map["x"] as? Number)?.toFloat() ?: 100f,
            y = (map["y"] as? Number)?.toFloat() ?: 100f,
            width = (map["width"] as? Number)?.toFloat() ?: 200f,
            height = (map["height"] as? Number)?.toFloat() ?: 200f,
            rotation = (map["rotation"] as? Number)?.toFloat() ?: 0f,
            scaleX = (map["scaleX"] as? Number)?.toFloat() ?: 1f,
            scaleY = (map["scaleY"] as? Number)?.toFloat() ?: 1f,
            rotationX = (map["rotationX"] as? Number)?.toFloat() ?: 0f,
            rotationY = (map["rotationY"] as? Number)?.toFloat() ?: 0f,
            perspective = (map["perspective"] as? Number)?.toFloat() ?: 0f
        )

        return when (type) {
            "TEXT" -> TextLayer(
                id = id,
                name = name,
                isVisible = isVisible,
                isLocked = isLocked,
                opacity = opacity,
                zIndex = zIndex,
                transform = transform,
                text = map["text"] as? String ?: "Texte",
                fontSize = (map["fontSize"] as? Number)?.toFloat() ?: 36f,
                isBold = map["isBold"] as? Boolean ?: true,
                isItalic = map["isItalic"] as? Boolean ?: false,
                isUnderline = map["isUnderline"] as? Boolean ?: false,
                letterSpacing = (map["letterSpacing"] as? Number)?.toFloat() ?: 1f,
                textAlign = map["textAlign"] as? String ?: "CENTER",
                fill = ColorFill(solidColor = (map["fillColor"] as? Number)?.toLong() ?: 0xFFD4AF37),
                stroke = StrokeDef(
                    isEnabled = map["strokeEnabled"] as? Boolean ?: false,
                    color = (map["strokeColor"] as? Number)?.toLong() ?: 0xFF0A0A0C,
                    width = (map["strokeWidth"] as? Number)?.toFloat() ?: 2f
                ),
                effect3D = Layer3DEffect(
                    isEnabled = map["effect3DEnabled"] as? Boolean ?: false,
                    depth = (map["effect3DDepth"] as? Number)?.toFloat() ?: 8f,
                    color = (map["effect3DColor"] as? Number)?.toLong() ?: 0xFF5B450C
                ),
                reflection = TextReflection(
                    isEnabled = map["reflectionEnabled"] as? Boolean ?: false
                ),
                curvature = TextCurvature(
                    isEnabled = map["curvatureEnabled"] as? Boolean ?: false,
                    bend = (map["curvatureBend"] as? Number)?.toFloat() ?: 0f
                )
            )
            "SHAPE" -> ShapeLayer(
                id = id,
                name = name,
                isVisible = isVisible,
                isLocked = isLocked,
                opacity = opacity,
                zIndex = zIndex,
                transform = transform,
                shapeType = try {
                    ShapeType.valueOf(map["shapeType"] as? String ?: "ROUNDED_RECT")
                } catch (_: Exception) { ShapeType.ROUNDED_RECT },
                cornerRadius = (map["cornerRadius"] as? Number)?.toFloat() ?: 16f,
                polygonSides = (map["polygonSides"] as? Number)?.toInt() ?: 5,
                starPoints = (map["starPoints"] as? Number)?.toInt() ?: 5,
                fill = ColorFill(solidColor = (map["fillColor"] as? Number)?.toLong() ?: 0xFF1A1A1D),
                stroke = StrokeDef(
                    isEnabled = map["strokeEnabled"] as? Boolean ?: true,
                    color = (map["strokeColor"] as? Number)?.toLong() ?: 0xFFD4AF37,
                    width = (map["strokeWidth"] as? Number)?.toFloat() ?: 3f
                ),
                effect3D = Layer3DEffect(
                    isEnabled = map["effect3DEnabled"] as? Boolean ?: false,
                    depth = (map["effect3DDepth"] as? Number)?.toFloat() ?: 8f
                )
            )
            "IMAGE" -> ImageLayer(
                id = id,
                name = name,
                isVisible = isVisible,
                isLocked = isLocked,
                opacity = opacity,
                zIndex = zIndex,
                transform = transform,
                imageUri = map["imageUri"] as? String,
                isSticker = map["isSticker"] as? Boolean ?: false,
                stickerName = map["stickerName"] as? String ?: "",
                colorEraser = ColorEraserDef(
                    isEnabled = map["colorEraserEnabled"] as? Boolean ?: false,
                    targetColor = (map["colorEraserColor"] as? Number)?.toLong() ?: 0xFFFFFFFF,
                    tolerance = (map["colorEraserTolerance"] as? Number)?.toFloat() ?: 25f
                )
            )
            else -> null
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ProjectRepository? = null

        fun getInstance(context: Context): ProjectRepository {
            return INSTANCE ?: synchronized(this) {
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    PixelForgeDatabase::class.java,
                    "pixelforge.db"
                ).fallbackToDestructiveMigration().build()
                val repo = ProjectRepository(db.projectDao())
                INSTANCE = repo
                repo
            }
        }
    }
}
