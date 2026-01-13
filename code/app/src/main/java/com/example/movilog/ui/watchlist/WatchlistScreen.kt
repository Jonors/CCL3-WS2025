package com.example.movilog.ui.watchlist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.movilog.data.model.Movie
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        onClick = onOpen
    ) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            val posterUrl = movie.posterPath?.let { "https://image.tmdb.org/t/p/w185$it" }
            AsyncImage(
                model = posterUrl,
                contentDescription = movie.title,
                modifier = Modifier.size(width = 54.dp, height = 80.dp)
            )

            Column(Modifier.weight(1f)) {
                Text(
                    movie.title,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onOpen,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF2B400), contentColor = Color.Black),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) { Text("Watched") }

                    OutlinedButton(
                        onClick = onDelete,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) { Text("Delete") }
                }
            }

            // right side: show userRating if exists (else placeholder)
            Text(
                text = movie.userRating?.let { String.format("%.1f★", it) } ?: "",
                color = Color(0xFFF2B400),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
