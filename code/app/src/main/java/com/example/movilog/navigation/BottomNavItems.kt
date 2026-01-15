package com.example.movilog.navigation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Routes.BROWSE, "Browse", Icons.Filled.Search),
    BottomNavItem(Routes.WATCHLIST, "Watchlist", Icons.Filled.Star),
    BottomNavItem(Routes.MY_LISTS, "My Lists", Icons.Filled.List),
    BottomNavItem(Routes.STATS, "Stats", Icons.Filled.Person),
)
