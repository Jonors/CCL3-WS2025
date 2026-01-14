// File: com.example.movilog.ui.lists.MyListsScreen.kt

package com.example.movilog.ui.lists

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.movilog.data.model.ListWithMovies
import com.example.movilog.ui.MovieViewModel

@Composable
fun MyListsScreen(
    viewModel: MovieViewModel,
    onMovieClick: (Int) -> Unit
) {
    val watched by viewModel.watchedList.collectAsState(initial = emptyList())
    // Observe the new dynamic collections
    val customCollections by viewModel.customCollections.collectAsState()

    val bg = Color(0xFF0B2A36)

    Scaffold(containerColor = bg) { padding ->
        // Use LazyColumn to make the whole screen scrollable vertically
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Spacer(Modifier.height(16.dp))
                Text("My Lists", color = Color.White, style = MaterialTheme.typography.headlineMedium)
            }

            // --- 1. Static Watched List ---
            item {
                Section(title = "Watched list", movies = watched, onMovieClick = onMovieClick)
            }

            // --- 2. Dynamic Custom Collections ---
            // This replaces the hardcoded "Very good" / "Old but Gold" logic
            // with actual data from your "Add to Collection" dialog
            items(customCollections) { collection ->
                Section(
                    title = collection.customList.listName,
                    movies = collection.movies,
                    onMovieClick = onMovieClick
                )
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun Section(title: String, movies: List<Movie>, onMovieClick: (Int) -> Unit) {
    Column {
        Text(title, color = Color.White, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(10.dp))

        if (movies.isEmpty()) {
            Text("No movies in this list yet.", color = Color.White.copy(alpha = 0.5f))
        } else {
            PosterRow(movies = movies, onMovieClick = onMovieClick)
        }
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
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}