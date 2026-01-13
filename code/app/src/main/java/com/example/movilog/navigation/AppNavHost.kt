package com.example.movilog.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.movilog.ui.BrowseScreen
import com.example.movilog.ui.MovieDetailScreen
import com.example.movilog.ui.MovieViewModel

@Composable
fun AppNavHost(viewModel: MovieViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.BROWSE
    ) {
        composable(Routes.BROWSE) {
            BrowseScreen(
                viewModel = viewModel,
                onMovieClick = { movie ->
                    navController.navigate("${Routes.MOVIE_DETAIL}/${movie.id}")
                }
            )
        }

        composable(
            route = "${Routes.MOVIE_DETAIL}/{movieId}",
            arguments = listOf(navArgument("movieId") { type = NavType.IntType })
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getInt("movieId") ?: return@composable
            MovieDetailScreen(
                movieId = movieId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
