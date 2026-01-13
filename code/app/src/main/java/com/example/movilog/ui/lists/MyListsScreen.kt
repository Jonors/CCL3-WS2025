package com.example.movilog.ui.lists

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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

@Composable
fun MyListsScreen(
    viewModel: MovieViewModel,
    onMovieClick: (Int) -> Unit
) {
    val watched by viewModel.watchedList.collectAsState(initial = emptyList())

    val bg = Color(0xFF0B2A36)
    Scaffold(containerColor = bg) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("My Lists", color = Color.White, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(12.dp))

            Text("Watched list", color = Color.White, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(10.dp))

            if (watched.isEmpty()) {
                Text("No watched movies yet.", color = Color.White.copy(alpha = 0.8f))
            } else {
                PosterRow(movies = watched, onMovieClick = onMovieClick)
            }

            Spacer(Modifier.height(18.dp))

            // Sektionen wie im Mock: wir machen’s erstmal automatisch nach Rating
            val veryGood = watched.filter { (it.userRating ?: 0f) >= 4.0f }
            val oldButGold = watched.filter { (it.userRating ?: 0f) in 3.0f..3.9f }

            Section(title = "Very good", movies = veryGood, onMovieClick = onMovieClick)
            Spacer(Modifier.height(16.dp))
            Section(title = "Old but Gold", movies = oldButGold, onMovieClick = onMovieClick)
        }
    }
}

@Composable
private fun Section(title: String, movies: List<Movie>, onMovieClick: (Int) -> Unit) {
    Text(title, color = Color.White, style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(10.dp))

    if (movies.isEmpty()) {
        Text("—", color = Color.White.copy(alpha = 0.6f))
    } else {
        PosterRow(movies = movies, onMovieClick = onMovieClick)
    }
}

@Composable
private fun PosterRow(movies: List<Movie>, onMovieClick: (Int) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(movies) { movie ->
            val posterUrl = movie.posterPath?.let { "https://image.tmdb.org/t/p/w342$it" }
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .width(120.dp)
                    .wrapContentHeight()
                    .clickable { onMovieClick(movie.id) }
            ) {
                Column {
                    AsyncImage(
                        model = posterUrl,
                        contentDescription = movie.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                    )
                    Text(
                        text = movie.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}
