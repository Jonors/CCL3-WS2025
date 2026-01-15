package com.example.movilog.ui.seeall

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.movilog.data.model.Movie
import com.example.movilog.ui.MovieViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchedAllScreen(
    viewModel: MovieViewModel,
    onBack: () -> Unit,
    onMovieClick: (Int) -> Unit
) {
    val watched by viewModel.watchedList.collectAsState(initial = emptyList())
    val bg = Color(0xFF0B2A36)

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
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = bg,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 90.dp, top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(watched, key = { it.id }) { movie ->
                WatchedGridMovieCard(movie = movie, onClick = { onMovieClick(movie.id) })
            }
        }
    }
}

@Composable
private fun WatchedGridMovieCard(
    movie: Movie,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        val posterUrl = movie.posterPath?.let { "https://image.tmdb.org/t/p/w500$it" }

        AsyncImage(
            model = posterUrl,
            contentDescription = movie.title,
            modifier = Modifier
                .aspectRatio(2f / 3f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.height(6.dp))

        // optional: rating anzeigen (User rating)
        val r = movie.userRating
        Text(
            text = if (r != null && r > 0f) "${movie.title}  •  ${String.format("%.1f", r)}★" else movie.title,
            color = Color.White,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
