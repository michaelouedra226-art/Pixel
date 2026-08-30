package com.example.pixelforge

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.core.data.repository.ProjectRepository
import com.example.feature.editor.EditorScreen
import com.example.feature.editor.EditorViewModel
import com.example.feature.editor.EditorViewModelFactory
import com.example.feature.gallery.GalleryScreen
import com.example.feature.gallery.GalleryViewModel
import com.example.feature.gallery.GalleryViewModelFactory

@Composable
fun PixelForgeNavHost(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val repository = remember(context) { ProjectRepository.getInstance(context) }

    NavHost(
        navController = navController,
        startDestination = "gallery",
        modifier = modifier
    ) {
        composable("gallery") {
            val galleryViewModel: GalleryViewModel = viewModel(
                factory = GalleryViewModelFactory(repository)
            )
            GalleryScreen(
                viewModel = galleryViewModel,
                onOpenProject = { projectId ->
                    navController.navigate("editor/$projectId")
                }
            )
        }

        composable(
            route = "editor/{projectId}",
            arguments = listOf(
                navArgument("projectId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
            val editorViewModel: EditorViewModel = viewModel(
                factory = EditorViewModelFactory(repository)
            )
            EditorScreen(
                viewModel = editorViewModel,
                projectId = projectId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
