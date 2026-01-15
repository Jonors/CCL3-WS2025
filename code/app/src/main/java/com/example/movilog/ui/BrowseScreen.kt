package com.example.movilog.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.movilog.R
import com.example.movilog.data.model.Movie
import com.example.movilog.ui.theme.SurfaceNavy
import com.example.movilog.ui.theme.TextSecondary
import com.example.movilog.ui.theme.TextWhite
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.ui.text.style.TextOverflow


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseScreen(
    viewModel: MovieViewModel,
    onMovieClick: (Movie) -> Unit = {},
    onSeeAllPopular: () -> Unit = {},
    onSeeAllUpcoming: () -> Unit = {}
) {
    val query by viewModel.query.collectAsState()
    val popular by viewModel.popularMovies.collectAsState()
    val upcoming by viewModel.upcomingMovies.collectAsState()
    val topRated by viewModel.topRatedMovies.collectAsState()
    val nowPlaying by viewModel.nowPlayingMovies.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    val isSearching = query.trim().isNotEmpty()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0B2A36) // Deep Navy Background from screenshot
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // --- HEADER ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Placeholder for your Logo
                Image(
                    painter = painterResource(id = R.drawable.logo), // Replace with R.drawable.logo
                    contentDescription = "Logo",
                    modifier = Modifier.size(40.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Movilog",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold, // Use your Akhand-like font here
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            // --- SEARCH BAR ---
            TextField(
                value = query,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(24.dp)),
                placeholder = {
                    Text("Search movies...", color = TextSecondary, style = MaterialTheme.typography.labelLarge)
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary)
                },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    // Background color
                    focusedContainerColor = SurfaceNavy,
                    unfocusedContainerColor = SurfaceNavy,
                    disabledContainerColor = SurfaceNavy,
                    // Indicators (set to Transparent to remove the bottom line)
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    // Text colors
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                )
            )

            Spacer(Modifier.height(24.dp))

            // --- CONTENT ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                if (isSearching) {
                    SearchResultsGrid(
                        title = "Search Results",
                        movies = searchResults,
                        onMovieClick = onMovieClick
                    )
                    Spacer(Modifier.height(30.dp))
                } else {
                    MovieSection("Popular Movies", popular, onMovieClick, onSeeAll = onSeeAllPopular)
                    MovieSection("Upcoming Movies", upcoming, onMovieClick, onSeeAll = onSeeAllUpcoming)
                    MovieSection("New Movies", nowPlaying, onMovieClick)
                    MovieSection("Top Rated", topRated, onMovieClick)
                }

                Spacer(Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun MovieSection(
    title: String,
    movies: List<Movie>,
    onMovieClick: (Movie) -> Unit,
    onSeeAll: (() -> Unit)? = null
) {
    if (movies.isEmpty()) return

    Column(modifier = Modifier.padding(bottom = 24.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            if (onSeeAll != null) {
                Text(
                    text = "See all",
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge.copy(
                        textDecoration = TextDecoration.Underline
                    ),
                    modifier = Modifier
                        .clickable { onSeeAll() }
                        .padding(start = 8.dp)
                )
            }
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(movies) { movie ->
                MovieCard(movie, onMovieClick)
            }
        }
    }
}

@Composable
private fun MovieCard(movie: Movie, onClick: (Movie) -> Unit) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .clickable { onClick(movie) }
    ) {
        val posterUrl = movie.posterPath?.let { "https://image.tmdb.org/t/p/w500$it" }

        AsyncImage(
            model = posterUrl,
            contentDescription = movie.title,
            modifier = Modifier
                .height(200.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = movie.title,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            fontWeight = FontWeight.Medium
        )
    }
}
@Composable
private fun SearchResultsGrid(
    title: String,
    movies: List<Movie>,
    onMovieClick: (Movie) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (movies.isEmpty()) {
            Text(
                text = "No results.",
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            return
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 2000.dp), // prevents "infinite height" issues
            userScrollEnabled = false,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            gridItems(movies, key = { it.id }) { movie ->
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
                .aspectRatio(2f / 3f) // nice poster ratio
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
