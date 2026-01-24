package com.example.movilog.navigation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.movilog.ui.BrowseScreen
import com.example.movilog.ui.MovieViewModel
import com.example.movilog.ui.detail.MovieDetailScreen
import com.example.movilog.ui.watchlist.WatchlistScreen
import com.example.movilog.ui.lists.MyListsScreen
import com.example.movilog.ui.stats.StatsScreen
import com.example.movilog.ui.seeall.SeeAllMoviesScreen
import com.example.movilog.ui.seeall.WatchedAllScreen
import com.example.movilog.ui.seeall.parseCategory
import com.example.movilog.ui.CustomLists.CustomListDetailScreen
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild

@Composable
fun AppNavHost(viewModel: MovieViewModel) {
    val navController = rememberNavController()
    val hazeState = remember { HazeState() }
    val bg = Color(0xFF0B2A36)

    Scaffold(
        containerColor = bg,
        bottomBar = {}
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // 1. Content Layer
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .haze(
                        state = hazeState,
                        style = HazeStyle(tint = bg.copy(alpha = 0.5f), blurRadius = 30.dp)
                    )
            ) {
                NavHost(
                    navController = navController,
                    startDestination = Routes.BROWSE,
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable(Routes.BROWSE) {
                        BrowseScreen(
                            viewModel = viewModel,
                            onMovieClick = { movie -> navController.navigate("${Routes.MOVIE_DETAIL}/${movie.id}") },
                            onSeeAllPopular = { navController.navigate("${Routes.SEE_ALL}/popular") },
                            onSeeAllUpcoming = { navController.navigate("${Routes.SEE_ALL}/upcoming") },
                            onSeeAllNowPlaying = { navController.navigate("${Routes.SEE_ALL}/now_playing") },
                            onSeeAllTopRated = { navController.navigate("${Routes.SEE_ALL}/top_rated") }
                        )
                    }
                    composable(Routes.WATCHED_ALL) {
                        WatchedAllScreen(viewModel = viewModel, onBack = { navController.popBackStack() },
                            onMovieClick = { id -> navController.navigate("${Routes.MOVIE_DETAIL}/$id") })
                    }
                    composable("${Routes.SEE_ALL}/{category}") { backStackEntry ->
                        val key = backStackEntry.arguments?.getString("category")
                        SeeAllMoviesScreen(viewModel = viewModel, category = parseCategory(key),
                            onBack = { navController.popBackStack() },
                            onMovieClick = { m -> navController.navigate("${Routes.MOVIE_DETAIL}/${m.id}") })
                    }
                    composable(Routes.WATCHLIST) {
                        WatchlistScreen(viewModel = viewModel, onMovieClick = { id -> navController.navigate("${Routes.MOVIE_DETAIL}/$id") })
                    }
                    composable(Routes.MY_LISTS) {
                        MyListsScreen(navController, viewModel = viewModel,
                            onMovieClick = { id -> navController.navigate("${Routes.MOVIE_DETAIL}/$id") },
                            onListClick = { id -> navController.navigate("${Routes.CUSTOM_LIST_DETAIL}/$id") },
                            onSeeAllWatched = { navController.navigate(Routes.WATCHED_ALL) })
                    }
                    composable(Routes.STATS) {
                        StatsScreen(viewModel = viewModel, onMovieClick = { id -> navController.navigate("${Routes.MOVIE_DETAIL}/$id") })
                    }
                    composable("${Routes.MOVIE_DETAIL}/{movieId}") { backStackEntry ->
                        val movieId = backStackEntry.arguments?.getString("movieId")?.toInt() ?: return@composable
                        MovieDetailScreen(movieId = movieId, viewModel = viewModel, navController = navController, onBack = { navController.popBackStack() })
                    }
                    composable("${Routes.CUSTOM_LIST_DETAIL}/{listId}") { backStackEntry ->
                        val listId = backStackEntry.arguments?.getString("listId")?.toLong() ?: 0L
                        CustomListDetailScreen(listId = listId, viewModel = viewModel, onBack = { navController.popBackStack() },
                            onMovieClick = { id -> navController.navigate("${Routes.MOVIE_DETAIL}/$id") })
                    }
                }
            }

            // 2. Optimized Floating Navbar Layer
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding() // Better handling for gesture navigation
            ) {
                GlassBottomBar(navController = navController, hazeState = hazeState)
            }
        }
    }
}

@Composable
fun GlassBottomBar(navController: NavHostController, hazeState: HazeState) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val bg = Color(0xFF0B2A36)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp)
            .height(68.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(100))
                .hazeChild(
                    state = hazeState,
                    shape = RoundedCornerShape(100),
                    style = HazeStyle(
                        tint = bg.copy(alpha = 0.25f), // Darker tint for visibility
                        blurRadius = 25.dp,
                        noiseFactor = 0.15f, // Added texture
                    )
                )
                // The "Liquid Sheen" layer
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.12f),
                            Color.Transparent
                        )
                    )
                )
                .border(
                    width = 1.5.dp, // Slightly thicker for "Glass Edge" look
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.35f),
                            Color.White.copy(alpha = 0.1f)
                        )
                    ),
                    shape = RoundedCornerShape(100)
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                bottomNavItems.forEach { item ->
                    val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                    GhostNavItem(
                        item = item,
                        isSelected = isSelected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun GhostNavItem(item: BottomNavItem, isSelected: Boolean, onClick: () -> Unit) {
    val glowAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "glow_alpha"
    )
    val selectedColor = Color(0xFFF2B400) // Your gold color

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(64.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
    ) {
        // Selection Glow
        Box(
            modifier = Modifier
                .size(45.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(selectedColor.copy(alpha = 0.3f * glowAlpha), Color.Transparent)
                    )
                )
        )

        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = if (isSelected) selectedColor else Color.White.copy(alpha = 0.6f),
            modifier = Modifier.size(26.dp)
        )
    }
}