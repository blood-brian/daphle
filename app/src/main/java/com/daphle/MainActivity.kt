package com.daphle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.daphle.data.PuzzleRepository
import com.daphle.ui.archive.ArchiveScreen
import com.daphle.ui.game.GameScreen
import com.daphle.ui.home.HomeScreen
import com.daphle.ui.theme.DaphleTheme
import com.daphle.viewmodel.ArchiveViewModel
import com.daphle.viewmodel.GameViewModel
import com.daphle.viewmodel.HomeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DaphleTheme {
                DaphleApp()
            }
        }
    }
}

@Composable
fun DaphleApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val repository = PuzzleRepository(context)

    NavHost(navController = navController, startDestination = "home") {

        composable("home") {
            val vm: HomeViewModel = viewModel(
                factory = HomeViewModel.Factory(repository)
            )
            HomeScreen(
                viewModel = vm,
                onPickLength = { length ->
                    navController.navigate("archive/$length")
                },
            )
        }

        composable(
            route = "archive/{length}",
            arguments = listOf(navArgument("length") { type = NavType.IntType }),
        ) { backStackEntry ->
            val length = backStackEntry.arguments!!.getInt("length")
            val vm: ArchiveViewModel = viewModel(
                factory = ArchiveViewModel.Factory(repository, length),
            )
            ArchiveScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onPuzzleTap = { puzzleIndex ->
                    navController.navigate("game/$length/$puzzleIndex")
                },
            )
        }

        composable(
            route = "game/{length}/{puzzleIndex}",
            arguments = listOf(
                navArgument("length") { type = NavType.IntType },
                navArgument("puzzleIndex") { type = NavType.IntType },
            ),
        ) { backStackEntry ->
            val length = backStackEntry.arguments!!.getInt("length")
            val puzzleIndex = backStackEntry.arguments!!.getInt("puzzleIndex")
            val vm: GameViewModel = viewModel(
                factory = GameViewModel.Factory(repository, length, puzzleIndex),
            )
            GameScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
