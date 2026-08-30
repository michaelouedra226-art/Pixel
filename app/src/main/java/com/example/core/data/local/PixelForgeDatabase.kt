package com.example.core.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val title: String,
    val width: Int,
    val height: Int,
    val backgroundColor: Long,
    val backgroundGradientJson: String?,
    val isTransparentBg: Boolean,
    val layersJson: String,
    val thumbnailBase64: String?,
    val createdAt: Long,
    val updatedAt: Long
)

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY updatedAt DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getProjectById(id: String): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity)

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteProjectById(id: String)

    @Delete
    suspend fun deleteProject(project: ProjectEntity)
}

@Database(entities = [ProjectEntity::class], version = 1, exportSchema = false)
abstract class PixelForgeDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
}
