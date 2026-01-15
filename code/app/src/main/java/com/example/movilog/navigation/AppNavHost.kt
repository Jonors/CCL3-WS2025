package com.example.movilog.navigation

import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.movilog.ui.BrowseScreen
import com.example.movilog.ui.detail.MovieDetailScreen
import com.example.movilog.ui.watchlist.WatchlistScreen
import com.example.movilog.ui.lists.MyListsScreen
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.navigation.NavController
import com.example.movilog.ui.CustomLists.CustomListDetailScreen
import com.example.movilog.ui.MovieViewModel
import com.example.movilog.ui.stats.StatsScreen
import com.example.movilog.ui.seeall.SeeAllMoviesScreen
import com.example.movilog.ui.seeall.parseCategory





@Composable
fun AppNavHost(viewModel: MovieViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    // current route (für selected tab)
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            val bg = androidx.compose.ui.graphics.Color(0xFF0B2A36)
            val selected = androidx.compose.ui.graphics.Color(0xFFF2B400)
            val unselected = androidx.compose.ui.graphics.Color.White

            NavigationBar(
                containerColor = bg,
                tonalElevation = 0.dp
            ) {
                bottomNavItems.forEach { item ->
                    val isSelected = currentRoute == item.route

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = false
                            }
                        },
                        icon = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    modifier = Modifier.size(30.dp)
                                )
                                Spacer(Modifier.height(4.dp))
                            }
                        },
                        label = {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelLarge
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = bg,               // ✅ removes the pill highlight
                            selectedIconColor = selected,
                            selectedTextColor = selected,
                            unselectedIconColor = unselected,
                            unselectedTextColor = unselected
                        ),
                        alwaysShowLabel = true               // ✅ label always visible like in screenshot
                    )
                }
            }
        }

    ) { padding ->

        NavHost(
            navController = navController,
            startDestination = Routes.BROWSE
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

            composable(
                route = "${Routes.SEE_ALL}/{category}",
                arguments = listOf(navArgument("category") { type = NavType.StringType })
            ) { backStackEntry ->
                val key = backStackEntry.arguments?.getString("category")
                val category = parseCategory(key)

                SeeAllMoviesScreen(
                    viewModel = viewModel,
                    category = category,
                    onBack = { navController.popBackStack() },
                    onMovieClick = { movie ->
                        navController.navigate("${Routes.MOVIE_DETAIL}/${movie.id}")
                    }
                )
            }



            composable(Routes.WATCHLIST) {
                WatchlistScreen(
                    viewModel = viewModel,
                    onMovieClick = { id ->
                        navController.navigate("${Routes.MOVIE_DETAIL}/$id")
                    }
                )
            }

            composable(Routes.MY_LISTS) {
                MyListsScreen(
                    navController,
                    viewModel = viewModel,
                    onMovieClick = { id ->
                        navController.navigate("${Routes.MOVIE_DETAIL}/$id")
                    },
                    onListClick = { listId ->
                        navController.navigate("${Routes.CUSTOM_LIST_DETAIL}/$listId")
                    }
                )
            }

            composable(Routes.STATS) {
                StatsScreen(
                    viewModel = viewModel,
                    onMovieClick = { id -> navController.navigate("${Routes.MOVIE_DETAIL}/$id") }
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
                    navController = navController, // ✅ Pass the navController here
                    onBack = { navController.popBackStack() }
                )
            }


// Update your detail route to use the constant
            composable(
                route = "${Routes.CUSTOM_LIST_DETAIL}/{listId}",
                arguments = listOf(navArgument("listId") { type = NavType.LongType })
            ) { backStackEntry ->
                val listId = backStackEntry.arguments?.getLong("listId") ?: 0L
                CustomListDetailScreen(
                    listId = listId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onMovieClick = { movieId ->
                        navController.navigate("${Routes.MOVIE_DETAIL}/$movieId")
                    }
                )
            }

        }

    }
}

@Composable
private fun StatsPlaceholderScreen() {
    Surface {
        Text("Stats coming soon…")
    }
}
