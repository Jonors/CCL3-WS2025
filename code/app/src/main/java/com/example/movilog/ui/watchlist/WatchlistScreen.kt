package com.example.movilog.ui.watchlist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.movilog.data.model.Movie
import com.example.movilog.data.remote.MovieDetailsDto
import com.example.movilog.ui.MovieViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun WatchlistScreen(
    viewModel: MovieViewModel,
    onMovieClick: (Int) -> Unit
) {
    val watchlist by viewModel.watchlist.collectAsState(initial = emptyList())

    val bg = Color(0xFF0B2A36)

    Scaffold(containerColor = bg) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Watchlist", color = Color.White, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(12.dp))

            if (watchlist.isEmpty()) {
                Text("No movies in your watchlist yet.", color = Color.White.copy(alpha = 0.8f))
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    watchlist.forEach { movie ->
                        WatchlistRow(
                            movie = movie,
                            onOpen = { onMovieClick(movie.id) },
                            onDelete = { viewModel.deleteMovie(movie.id) }
                        )
                    }
                }
            }
        }
    }
}
@Composable
private fun WatchlistRow(
    movie: Movie,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    val cardBg = Color(0xFF6F7D86).copy(alpha = 0.55f)
    val accent = Color(0xFFF2B400)
    val deleteRed = Color(0xFFE85B5B)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        onClick = onOpen
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val posterUrl = movie.posterPath?.let { "https://image.tmdb.org/t/p/w185$it" }
            AsyncImage(
                model = posterUrl,
                contentDescription = movie.title,
                modifier = Modifier
                    .size(width = 54.dp, height = 80.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = movie.title,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Watched (gelb)
                    Button(
                        onClick = onOpen, // du öffnest Detail -> dort "Watched" Dialog
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accent,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("Watched", style = MaterialTheme.typography.labelLarge)
                    }

                    // Delete (rot)
                    Button(
                        onClick = onDelete,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = deleteRed,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("Delete", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            // Right side rating (TMDB average)
            // Right side rating (TMDB average) – vertically centered
            val avg = (movie.voteAverage ?: movie.userRating)
            if (avg != null && avg > 0f) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterVertically),
                    contentAlignment = Alignment.Center
                ) {
                Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = String.format("%.1f", avg),
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "★",
                            color = accent,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }

        }
    }
}
