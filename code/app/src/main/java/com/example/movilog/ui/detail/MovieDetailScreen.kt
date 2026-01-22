package com.example.movilog.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.movilog.data.remote.MovieDetailsDto
import com.example.movilog.navigation.Routes
import com.example.movilog.ui.MovieViewModel
import com.example.movilog.ui.RatingsCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    movieId: Int,
    viewModel: MovieViewModel,
    navController: NavController,
    onBack: () -> Unit = {}
) {
    LaunchedEffect(movieId) { viewModel.loadMovieDetails(movieId) }

    val state by viewModel.detailState.collectAsState()
    val availableLists by viewModel.customLists.collectAsState()
    val scope = rememberCoroutineScope()

    var showWatchedDialog by remember { mutableStateOf(false) }
    var showAddToListDialog by remember { mutableStateOf(false) }

    val bg = Color(0xFF0B2A36)
    val cardBg = Color(0xFF6F7D86).copy(alpha = 0.55f)
    val accent = Color(0xFFF2B400)

    Scaffold(
        containerColor = bg,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Movie Screen", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = bg)
            )
        }
    ) { padding ->
        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = accent)
                }
            }
            state.details == null -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("Failed to load movie details", color = Color.White)
                }
            }
            else -> {
                val d = state.details!!
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(bottom = 105.dp)
                ) {
                    item { HeroCard(d, cardBg, state.isWatched) }

                    // --- BUTTON LAYOUT SECTION ---
                    item {
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .fillMaxWidth()
                        ) {
                            // 1. Mark as Watched Button (Primary Full Width)
                            Button(
                                onClick = { if (!state.isWatched) showWatchedDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !state.isWatched,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = accent,
                                    disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                                )
                            ) {
                                Text(
                                    text = if (state.isWatched) "Watched" else "Mark as Watched",
                                    color = if (state.isWatched) Color.White.copy(alpha = 0.5f) else Color.Black
                                )
                            }

                            Spacer(Modifier.height(12.dp))

                            // 2. Secondary Buttons Row (Watchlist & Add to List)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Add to Watchlist
                                OutlinedButton(
                                    onClick = {
                                        viewModel.addCurrentDetailToWatchlist()
                                        navController.navigate(Routes.WATCHLIST) {
                                            launchSingleTop = true
                                        }
                                              },
                                    modifier = Modifier.weight(1f),
                                    enabled = !state.inWatchlist && !state.isWatched,
                                    shape = RoundedCornerShape(12.dp),
                                    border = ButtonDefaults.outlinedButtonBorder.copy(
                                        brush = SolidColor(if (state.inWatchlist || state.isWatched) Color.Gray else Color.White)
                                    )
                                ) {
                                    Text(
                                        text = if (state.inWatchlist) "In Watchlist" else "Add to Watchlist",
                                        color = if (state.inWatchlist || state.isWatched) Color.Gray else Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                // Add to List
                                Button(
                                    onClick = { showAddToListDialog = true },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White.withAlpha(0.1f)
                                    )
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Add to List", color = Color.White, maxLines = 1)
                                }
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                    }

                    item {
                        Text(
                            text = d.overview.ifBlank { "No description available." },
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.height(24.dp))
                    }

                    item {
                        RatingsCard(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            cardBg = cardBg,
                            userRating = d.voteAverage ?: 0f
                        )
                    }
                }
            }
        }

        // Dialog Logic
        if (showWatchedDialog) {
            MarkWatchedDialog(
                movieTitle = state.details?.title ?: "Movie", // Pass title here
                onDismiss = { showWatchedDialog = false },
                onConfirm = { rating, watchedAt ->
                    showWatchedDialog = false
                    viewModel.markCurrentDetailAsWatched(rating, watchedAt)
                }
            )
        }

        if (showAddToListDialog) {
            AddToListDialog(
                availableLists = availableLists,
                onDismiss = { showAddToListDialog = false },
                onListSelected = { listId ->
                    scope.launch {
                        viewModel.addMovieToExistingList(movieId, listId)
                        navController.navigate("${Routes.CUSTOM_LIST_DETAIL}/$listId")
                        showAddToListDialog = false
                    }
                },
                onCreateNewList = { name ->
                    scope.launch {
                        val newListId = viewModel.createListAndAddMovie(name, movieId)
                        navController.navigate("${Routes.CUSTOM_LIST_DETAIL}/$newListId")
                        showAddToListDialog = false
                    }
                }
            )
        }
    }
}

@Composable
private fun HeroCard(
    details: MovieDetailsDto,
    cardBg: Color,
    isWatched: Boolean
) {
    val backdropUrl = details.backdropPath?.let { "https://image.tmdb.org/t/p/w780$it" }
    val posterUrl = details.posterPath?.let { "https://image.tmdb.org/t/p/w342$it" }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            ) {
                AsyncImage(
                    model = backdropUrl,
                    contentDescription = "Backdrop",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )

                if (posterUrl != null) {
                    Card(
                        modifier = Modifier
                            .padding(14.dp)
                            .width(100.dp)
                            .height(150.dp)
                            .align(Alignment.BottomStart),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        AsyncImage(
                            model = posterUrl,
                            contentDescription = "Poster",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    }
                }
            }

            Column(Modifier.padding(16.dp)) {
                Text(
                    text = details.title,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoChip(label = formatRuntime(details.runtime))
                    InfoChip(label = details.releaseDate?.take(4) ?: "N/A")
                    if (isWatched) {
                        InfoChip(label = "Watched", containerColor = Color(0xFF4CAF50).copy(alpha = 0.2f))
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoChip(
    label: String,
    containerColor: Color = Color.Black.copy(alpha = 0.25f)
) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = label,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium
        )
    }
}

private fun formatRuntime(runtimeMinutes: Int?): String {
    if (runtimeMinutes == null || runtimeMinutes <= 0) return "—"
    val h = runtimeMinutes / 60
    val m = runtimeMinutes % 60
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}

// Extension to help with color alphas in older compose versions if needed
fun Color.withAlpha(alpha: Float): Color = this.copy(alpha = alpha)