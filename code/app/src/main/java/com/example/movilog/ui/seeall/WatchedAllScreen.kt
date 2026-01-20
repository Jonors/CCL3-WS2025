package com.example.movilog.ui.seeall

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
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
import com.example.movilog.data.model.Movie
import com.example.movilog.ui.MovieViewModel
import com.example.movilog.ui.components.DeleteConfirmationDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchedAllScreen(
    viewModel: MovieViewModel,
    onBack: () -> Unit,
    onMovieClick: (Int) -> Unit
) {
    val watched by viewModel.watchedList.collectAsState(initial = emptyList())

    var isEditMode by remember { mutableStateOf(false) }
    var movieToRemoveId by remember { mutableStateOf<Int?>(null) }

    val bg = Color(0xFF0B2A36)
    val accent = Color(0xFFF2B400)

    Scaffold(
        containerColor = bg,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Watched list", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { isEditMode = !isEditMode },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (isEditMode) accent else Color.Transparent
                        )
                    ) {
                        Icon(
                            imageVector = if (isEditMode) Icons.Default.Done else Icons.Default.Edit,
                            contentDescription = "Toggle Edit Mode",
                            tint = if (isEditMode) Color.Black else Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = bg)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (watched.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No watched movies yet.", color = Color.White.copy(alpha = 0.6f))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),

                ) {
                    items(
                        items = watched,
                        key = { it.id }
                    ) { movie ->
                        WatchedMovieRow(
                            movie = movie,
                            isEditMode = isEditMode,
                            onOpen = {
                                if (!isEditMode) onMovieClick(movie.id)
                            },
                            onDeleteClick = { movieToRemoveId = movie.id }
                        )

                    }

                }

            }


            if (movieToRemoveId != null) {
                val id = movieToRemoveId!!
                DeleteConfirmationDialog(
                    title = "Remove Movie?",
                    text = "Remove this movie from your watched list?",
                    onDismiss = { movieToRemoveId = null },
                    onConfirm = {
                        // ✅ simplest: delete row (removes from watched list + stats)
                        viewModel.deleteMovie(id)

                        // Optional alternative (wenn du später "unwatch" willst statt delete):
                        // viewModel.unwatchMovie(id)

                        movieToRemoveId = null
                    }
                )
            }

        }

    }

}

@Composable
private fun WatchedMovieRow(
    movie: Movie,
    isEditMode: Boolean,
    onOpen: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val cardBg = Color(0xFF6F7D86).copy(alpha = 0.55f)
    val deleteRed = Color(0xFFE85B5B)
    val accent = Color(0xFFF2B400)

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
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = movie.title,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium
                )

                val r = movie.userRating
                if (r != null && r > 0f) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "${String.format("%.1f", r)} ★",
                        color = accent,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (isEditMode) {
                Button(
                    onClick = onDeleteClick,
                    colors = ButtonDefaults.buttonColors(containerColor = deleteRed),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Delete", style = MaterialTheme.typography.labelMedium, color = Color.White)
                }
            }
        }

    }

}
