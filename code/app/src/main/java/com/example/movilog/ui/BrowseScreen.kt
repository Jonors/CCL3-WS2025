package com.example.movilog.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.movilog.data.model.Movie

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseScreen(
    viewModel: MovieViewModel,
    onMovieClick: (Movie) -> Unit = {}
) {
    val query by viewModel.query.collectAsState()
    val popular by viewModel.popularMovies.collectAsState()
    val upcoming by viewModel.upcomingMovies.collectAsState()
    val search by viewModel.searchResults.collectAsState()

    val isSearching = query.trim().isNotEmpty()
    val popularToShow = if (isSearching) search else popular
    val upcomingToShow = if (isSearching) search else upcoming

    Column(modifier = Modifier.fillMaxSize()) {

        Text(
            text = "MoviLog",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        TextField(
            value = query,
            onValueChange = viewModel::onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = { Text("Search movies...") },
            singleLine = true
        )

        Spacer(Modifier.height(8.dp))

//        // ✅ Debug text: zeigt dir sofort ob Daten ankommen
//        Text(
//            text = "Movies loaded: ${moviesToShow.size}",
//            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
//        )

        Text(
            text = if (isSearching) "Search Results" else "",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()) // Parent scroll
        ) {
            // --- POPULAR SECTION ---
            Text(
                text = "Popular",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(popularToShow) { movie ->
                    // Constraint the width so the card doesn't take the whole screen
                    Box(modifier = Modifier.width(160.dp)) {
                        MovieCard(movie = movie, onClick = { onMovieClick(movie) })
                    }
                }
            }

            Text(
                text = "Upcoming movies count: ${upcomingToShow.size}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(upcomingToShow) { movie ->
                    Box(modifier = Modifier.width(160.dp)) {
                        MovieCard(movie = movie, onClick = { onMovieClick(movie) })
                    }
                }
            }
        }
    }
}

@Composable
private fun MovieCard(movie: Movie, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        val posterUrl = movie.posterPath?.let { "https://image.tmdb.org/t/p/w500$it" }

        AsyncImage(
            model = posterUrl,
            contentDescription = movie.title,
            modifier = Modifier
                .height(240.dp)
                .fillMaxWidth(),
            contentScale = ContentScale.Crop
        )

        Text(
            text = movie.title,
            modifier = Modifier.padding(10.dp),
            style = MaterialTheme.typography.titleSmall,
            maxLines = 2
        )
    }
}
