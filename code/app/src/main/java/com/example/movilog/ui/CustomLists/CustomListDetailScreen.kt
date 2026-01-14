package com.example.movilog.ui.CustomLists

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.movilog.data.model.Movie
import com.example.movilog.ui.MovieViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.movilog.ui.components.DeleteConfirmationDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomListDetailScreen(
    listId: Long,
    viewModel: MovieViewModel,
    onBack: () -> Unit,
    onMovieClick: (Int) -> Unit
) {
    val listData by viewModel.getMoviesForList(listId).collectAsState(initial = null)

    // This state controls the dialog
    var movieToRemoveData by remember { mutableStateOf<Pair<Long, Int>?>(null) }

    val bg = Color(0xFF0B2A36)

    Scaffold(
        containerColor = bg,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(listData?.customList?.listName ?: "Loading...", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = bg)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                if (listData == null || listData!!.movies.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No movies in this collection yet.", color = Color.White.copy(alpha = 0.6f))
                    }
                } else {
                    val moviesInList = listData!!.movies

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
                    ) {
                        items(
                            items = moviesInList,
                            key = { movie -> movie.id }
                        ) { movie ->
                            CollectionMovieRow(
                                movie = movie,
                                onOpen = { onMovieClick(movie.id) },
                                // ✅ FIX: Set state instead of calling ViewModel
                                onRemoveClick = { movieToRemoveData = Pair(listId, movie.id) }
                            )
                        }
                    }
                }
            }

            // --- Confirmation Dialog ---
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
private fun CollectionMovieRow(
    movie: Movie,
    onOpen: () -> Unit,
    onRemoveClick: () -> Unit // Renamed for clarity
) {
    val cardBg = Color(0xFF6F7D86).copy(alpha = 0.55f)
    val deleteRed = Color(0xFFE85B5B)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        onClick = onOpen
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val posterUrl = movie.posterPath?.let { "https://image.tmdb.org/t/p/w185$it" }
            AsyncImage(
                model = posterUrl,
                contentDescription = movie.title,
                modifier = Modifier
                    .size(width = 54.dp, height = 80.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = movie.title,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = onRemoveClick, // Triggers state change in parent
                    colors = ButtonDefaults.buttonColors(containerColor = deleteRed),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("Remove", style = MaterialTheme.typography.labelMedium)
                }
            }

            movie.userRating?.let { rating ->
                Text(
                    text = "${String.format("%.1f", rating)} ★",
                    color = Color(0xFFF2B400),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}