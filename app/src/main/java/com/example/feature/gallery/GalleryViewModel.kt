package com.example.feature.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.data.repository.PresetLibrary
import com.example.core.data.repository.ProjectPreset
import com.example.core.data.repository.ProjectRepository
import com.example.core.engine.model.CanvasProject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GalleryUiState(
    val selectedPreset: ProjectPreset = PresetLibrary.presets[0],
    val isCreateDialogOpen: Boolean = false,
    val customWidth: Int = 1080,
    val customHeight: Int = 1080
)

class GalleryViewModel(
    private val repository: ProjectRepository
) : ViewModel() {

    val projects: StateFlow<List<CanvasProject>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(GalleryUiState())
    val uiState: StateFlow<GalleryUiState> = _uiState.asStateFlow()

    fun createProjectFromPreset(preset: ProjectPreset, onCreated: (String) -> Unit) {
        viewModelScope.launch {
            val project = repository.createInitialStarterProject(preset)
            repository.saveProject(project)
            onCreated(project.id)
        }
    }

    fun duplicateProject(project: CanvasProject) {
        viewModelScope.launch {
            repository.duplicateProject(project)
        }
    }

    fun deleteProject(id: String) {
        viewModelScope.launch {
            repository.deleteProject(id)
        }
    }
}

class GalleryViewModelFactory(private val repository: ProjectRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GalleryViewModel(repository) as T
    }
}
