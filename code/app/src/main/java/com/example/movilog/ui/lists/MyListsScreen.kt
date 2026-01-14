package com.example.movilog.ui.lists

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.movilog.data.model.Movie
import com.example.movilog.ui.MovieViewModel
import com.example.movilog.ui.components.DeleteConfirmationDialog

@Composable
fun MyListsScreen(
    viewModel: MovieViewModel,
    onMovieClick: (Int) -> Unit
) {
    val watched by viewModel.watchedList.collectAsState(initial = emptyList())
    val customCollections by viewModel.customCollections.collectAsState()

    // Dialog States
    var listIdToDelete by remember { mutableStateOf<Long?>(null) }
    var movieToRemoveData by remember { mutableStateOf<Pair<Long, Int>?>(null) }

    val bg = Color(0xFF0B2A36)

    Scaffold(containerColor = bg) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    Spacer(Modifier.height(16.dp))
                    Text("My Lists", color = Color.White, style = MaterialTheme.typography.headlineMedium)
                }

                // 1. Watched List
                item {
                    Section(
                        title = "Watched list",
                        listId = null,
                        movies = watched,
                        onMovieClick = onMovieClick,
                        onDeleteList = {},
                        onRemoveMovie = { _, _ -> }
                    )
                }

                // 2. Custom Collections
                items(customCollections) { collection ->
                    Section(
                        title = collection.customList.listName,
                        listId = collection.customList.listId,
                        movies = collection.movies,
                        onMovieClick = onMovieClick,
                        onDeleteList = { id -> listIdToDelete = id },
                        onRemoveMovie = { lId, mId -> movieToRemoveData = Pair(lId, mId) }
                    )
                }

                item { Spacer(Modifier.height(24.dp)) }
            }

            // --- Dialogs (Using standard 'if' to maintain Composable Context) ---

            if (listIdToDelete != null) {
                val currentId = listIdToDelete!!
                DeleteConfirmationDialog(
                    title = "Delete List?",
                    text = "This will delete the collection. The movies will remain in your database. Continue?",
                    onDismiss = { listIdToDelete = null },
                    onConfirm = {
                        viewModel.deleteCustomList(currentId)
                        listIdToDelete = null
                    }
                )
            }

            if (movieToRemoveData != null) {
                val (lId, mId) = movieToRemoveData!!
                DeleteConfirmationDialog(
                    title = "Remove Movie?",
                    text = "Remove this movie from this collection?",
                    onDismiss = { movieToRemoveData = null },
                    onConfirm = {
                        viewModel.removeMovieFromList(lId, mId)
                        movieToRemoveData = null
                    }
                )
            }
        }
    }
}

@Composable
private fun Section(
    title: String,
    listId: Long?,
    movies: List<Movie>,
    onMovieClick: (Int) -> Unit,
    onDeleteList: (Long) -> Unit,
    onRemoveMovie: (Long, Int) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, color = Color.White, style = MaterialTheme.typography.titleMedium)
            if (listId != null) {
                IconButton(onClick = { onDeleteList(listId) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete List", tint = Color.Red.copy(alpha = 0.6f))
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        if (movies.isEmpty()) {
            Text("No movies in this list yet.", color = Color.White.copy(alpha = 0.5f))
        } else {
            PosterRow(listId = listId, movies = movies, onMovieClick = onMovieClick, onRemoveMovie = onRemoveMovie)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PosterRow(
    listId: Long?,
    movies: List<Movie>,
    onMovieClick: (Int) -> Unit,
    onRemoveMovie: (Long, Int) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(movies) { movie ->
            val posterUrl = movie.posterPath?.let { "https://image.tmdb.org/t/p/w342$it" }
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .width(120.dp)
                    .combinedClickable(
                        onClick = { onMovieClick(movie.id) },
                        onLongClick = { listId?.let { onRemoveMovie(it, movie.id) } }
                    )
            ) {
                Column {
                    AsyncImage(
                        model = posterUrl,
                        contentDescription = movie.title,
                        modifier = Modifier.fillMaxWidth().height(170.dp)
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