package com.example.movilog.ui.lists

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.movilog.data.model.ListWithMovies
import com.example.movilog.data.model.Movie
import com.example.movilog.ui.MovieViewModel
import com.example.movilog.ui.components.DeleteConfirmationDialog

@Composable
fun MyListsScreen(
    viewModel: MovieViewModel,
    onMovieClick: (Int) -> Unit,
    onListClick: (Long) -> Unit // Added this parameter
) {
    val watched by viewModel.watchedList.collectAsState(initial = emptyList())
    val customCollections by viewModel.customCollections.collectAsState()

    var listIdToDelete by remember { mutableStateOf<Long?>(null) }


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
                    Text(
                        "My Lists",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium
                    )
                }

                // 1. Static Watched List (Row style)
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

                // 2. Dynamic Custom Collections (Folder style)
                items(customCollections) { collection ->
                    CustomListFolderCard(
                        collection = collection,
                        onClick = { onListClick(collection.customList.listId) },
                        onDeleteClick = { listIdToDelete = collection.customList.listId }
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
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete List",
                        tint = Color.Red.copy(alpha = 0.6f)
                    )
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        if (movies.isEmpty()) {
            Text("No movies in this list yet.", color = Color.White.copy(alpha = 0.5f))
        } else {
            PosterRow(
                listId = listId,
                movies = movies,
                onMovieClick = onMovieClick,
                onRemoveMovie = onRemoveMovie
            )
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

@Composable
private fun CustomListFolderCard(
    collection: ListWithMovies,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val cardBg = Color(0xFF6F7D86).copy(alpha = 0.55f)
    val firstMovie = collection.movies.firstOrNull()
    val posterUrl = firstMovie?.posterPath?.let { "https://image.tmdb.org/t/p/w342$it" }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Folder Icon (First Movie Poster)
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                if (posterUrl != null) {
                    AsyncImage(
                        model = posterUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Text("Empty", color = Color.White, style = MaterialTheme.typography.labelSmall)
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    collection.customList.listName,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "${collection.movies.size} Movies",
                    color = Color.White.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            IconButton(onClick = onDeleteClick) {
                Icon(
                    androidx.compose.material.icons.Icons.Default.Delete,
                    contentDescription = null,
                    tint = Color.Red.copy(alpha = 0.6f)
                )
            }
        }
    }
}