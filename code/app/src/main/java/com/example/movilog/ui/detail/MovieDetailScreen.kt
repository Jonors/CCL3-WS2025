package com.example.movilog.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.movilog.data.remote.MovieDetailsDto
import com.example.movilog.ui.viewmodel.MovieViewModel
import com.example.movilog.ui.RatingsCard
import com.example.movilog.ui.detail.MarkWatchedDialog


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    movieId: Int,
    viewModel: MovieViewModel,
    onBack: () -> Unit = {}
) {
    // Load once
    LaunchedEffect(movieId) { viewModel.loadMovieDetails(movieId) }

    val state by viewModel.detailState.collectAsState()
    var showWatchedDialog by remember { mutableStateOf(false) }


    val bg = Color(0xFF0B2A36) // dark blue-ish like mock
    val cardBg = Color(0xFF6F7D86).copy(alpha = 0.55f)

    Scaffold(
        containerColor = bg,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Movie Screen", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = bg,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }

    ) { padding ->
        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.details == null -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("Failed to load movie details", color = Color.White)
                }
            }
            else -> {
                val d = state.details!!

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // --- Hero card (backdrop + poster + title block + chips) ---
                    HeroCard(
                        details = d,
                        cardBg = cardBg,
                        isWatched = state.isWatched
                    )

                    Spacer(Modifier.height(10.dp))

                    // --- Buttons row ---
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.addCurrentDetailToWatchlist()},
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) { Text("Add to Watchlist") }


                        Button(
                            onClick = { showWatchedDialog = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF2B400),
                                contentColor = Color.Black
                            )
                        ) { Text("Watched") }

                    }

                    Spacer(Modifier.height(14.dp))

                    // --- Overview ---
                    Text(
                        text = d.overview.ifBlank { "No description available." },
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(Modifier.height(18.dp))

                    // --- Ratings card (graph + user rating) ---
                    RatingsCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        cardBg = cardBg,
                        userRating = state.userRating ?: d.voteAverage ?: 0f
                    )

                    if (showWatchedDialog) {
                        MarkWatchedDialog(
                            onDismiss = { showWatchedDialog = false },
                            onConfirm = { rating, watchedAt ->
                                showWatchedDialog = false
                                viewModel.markCurrentDetailAsWatched(rating, watchedAt)
                            }
                        )
                    }

                }
            }
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
            // Backdrop
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            ) {
                AsyncImage(
                    model = backdropUrl,
                    contentDescription = "Backdrop",
                    modifier = Modifier.fillMaxSize()
                )

                // Poster overlay
                if (posterUrl != null) {
                    Card(
                        modifier = Modifier
                            .padding(14.dp)
                            .width(120.dp)
                            .height(170.dp)
                            .align(Alignment.BottomStart),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        AsyncImage(
                            model = posterUrl,
                            contentDescription = "Poster",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // Title + “credits placeholders”
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = details.title,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(6.dp))

                // Placeholders like in mock (Director / Studios etc.)
                // (Wenn du später Credits willst: /movie/{id}/credits endpoint)
                Text(
                    text = "Directed by …",
                    color = Color.White.copy(alpha = 0.75f),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Studios …",
                    color = Color.White.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(Modifier.height(12.dp))

                // Chips row (runtime, release date, watched)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    InfoChip(label = formatRuntime(details.runtime))
                    InfoChip(label = details.releaseDate ?: "Unknown date")
                    InfoChip(label = if (isWatched) "Watched" else "Not Watched")
                }
            }
        }
    }
}

@Composable
private fun InfoChip(label: String) {
    Surface(
        color = Color.Black.copy(alpha = 0.25f),
        shape = RoundedCornerShape(999.dp)
    ) {
        Text(
            text = label,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

private fun formatRuntime(runtimeMinutes: Int?): String {
    if (runtimeMinutes == null || runtimeMinutes <= 0) return "—"
    val h = runtimeMinutes / 60
    val m = runtimeMinutes % 60
    return "${h} hr ${m} min"
}
