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

enum class SeeAllCategory(val key: String, val title: String) {
    POPULAR("popular", "Popular Movies"),
    UPCOMING("upcoming", "Upcoming Movies"),
    NOW_PLAYING("now_playing", "New Movies"),
    TOP_RATED("top_rated", "Top Rated"),

}

fun parseCategory(key: String?): SeeAllCategory =
    SeeAllCategory.entries.firstOrNull { it.key == key } ?: SeeAllCategory.POPULAR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeeAllMoviesScreen(
    viewModel: MovieViewModel,
    category: SeeAllCategory,
    onBack: () -> Unit,
    onMovieClick: (Movie) -> Unit
) {
    val bg = Color(0xFF0B2A36)

    // ✅ pick the right list
    val movies by when (category) {
        SeeAllCategory.POPULAR -> viewModel.popularMovies.collectAsState()
        SeeAllCategory.UPCOMING -> viewModel.upcomingMovies.collectAsState()
        SeeAllCategory.NOW_PLAYING -> viewModel.nowPlayingMovies.collectAsState()
        SeeAllCategory.TOP_RATED -> viewModel.topRatedMovies.collectAsState()
    }


    Scaffold(
        containerColor = bg,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(category.title, color = Color.White) },
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
                .padding(padding)               // ✅ safe under TopBar + BottomBar
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 90.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(movies, key = { it.id }) { movie ->
                GridMovieCard(movie = movie, onClick = onMovieClick)
            }
        }
    }
}

@Composable
private fun GridMovieCard(
    movie: Movie,
    onClick: (Movie) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(movie) }
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

        Text(
            text = movie.title,
            color = Color.White,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
