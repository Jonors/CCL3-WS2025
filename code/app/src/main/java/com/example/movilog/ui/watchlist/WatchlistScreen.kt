package com.example.movilog.ui.watchlist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.movilog.ui.MovieViewModel
import com.example.movilog.ui.detail.MarkWatchedDialog // Ensure this import is correct

@Composable
fun WatchlistScreen(
    viewModel: MovieViewModel,
    onMovieClick: (Int) -> Unit
) {
    val watchlist by viewModel.watchlist.collectAsState(initial = emptyList())
    val bg = Color(0xFF0B2A36)

    // State to track which movie is currently being rated
    var movieToMarkWatched by remember { mutableStateOf<Movie?>(null) }

    Scaffold(containerColor = bg) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 105.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        "Watchlist",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(Modifier.height(12.dp))
                }

                if (watchlist.isEmpty()) {
                    item {
                        Text(
                            "No movies in your watchlist yet.",
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                } else {
                    items(
                        items = watchlist,
                        key = { it.id }
                    ) { movie ->
                        WatchlistRow(
                            movie = movie,
                            onOpen = { onMovieClick(movie.id) },
                            // Changed: Instead of opening detail, we set state for the dialog
                            onWatchedClick = { movieToMarkWatched = movie },
                            onDelete = { viewModel.deleteMovie(movie.id) }
                        )
                    }
                }
            }

            // Show the rating dialog if a movie is selected
            movieToMarkWatched?.let { movie ->
                MarkWatchedDialog(
                    movieTitle = movie.title, // Pass title here
                    onDismiss = { movieToMarkWatched = null },
                    onConfirm = { rating, watchedAt ->
                        viewModel.markMovieAsWatchedFromWatchlist(movie, rating, watchedAt)
                        movieToMarkWatched = null
                    }
                )
            }
        }
    }
}

@Composable
private fun WatchlistRow(
    movie: Movie,
    onOpen: () -> Unit,
    onWatchedClick: () -> Unit, // Renamed for clarity
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
            modifier = Modifier.padding(8.dp),
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
                    Button(
                        onClick = onWatchedClick, // Opens dialog now
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accent,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 1.dp)
                    ) {
                        Text("Mark as watched", style = MaterialTheme.typography.labelLarge)
                    }

                    Button(
                        onClick = onDelete,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = deleteRed,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 1.dp)
                    ) {
                        Text("Delete", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            val avg = (movie.voteAverage ?: movie.userRating)
            if (avg != null && avg > 0f) {
                Row(
                    modifier = Modifier.align(Alignment.CenterVertically),
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