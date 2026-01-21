package com.example.movilog.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.movilog.R
import com.example.movilog.data.model.Movie
import com.example.movilog.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseScreen(
    viewModel: MovieViewModel,
    onMovieClick: (Movie) -> Unit = {},
    onSeeAllPopular: () -> Unit = {},
    onSeeAllUpcoming: () -> Unit = {},
    onSeeAllNowPlaying: () -> Unit = {},
    onSeeAllTopRated: () -> Unit = {}
) {
    val query by viewModel.query.collectAsState()
    val popular by viewModel.popularMovies.collectAsState()
    val upcoming by viewModel.upcomingMovies.collectAsState()
    val topRated by viewModel.topRatedMovies.collectAsState()
    val nowPlaying by viewModel.nowPlayingMovies.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    val errorMessage by viewModel.errorMessage.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isOffline by viewModel.isOffline.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color(0xFF0B2A36)
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refreshAll() },
            modifier = Modifier.padding(padding)
        ) {
            // CRITICAL: The inner column needs the fillMaxSize to receive scroll events properly
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // --- OFFLINE BANNER ---
                if (isOffline) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Red.copy(alpha = 0.8f))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No Internet Connection", color = Color.White, fontSize = 14.sp)
                    }
                }

                // --- HEADER ---
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Image(painter = painterResource(R.drawable.logo), contentDescription = null, modifier = Modifier.size(40.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Movilog", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                }

                // --- SEARCH BAR ---
                TextField(
                    value = query,
                    onValueChange = { viewModel.onQueryChange(it) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).clip(RoundedCornerShape(24.dp)),
                    placeholder = { Text("Search movies...", color = TextSecondary) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = SurfaceNavy,
                        unfocusedContainerColor = SurfaceNavy,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(Modifier.height(24.dp))

                // --- CONTENT SECTIONS ---
                if (query.trim().isNotEmpty()) {
                    SearchResultsGrid("Search Results", searchResults, onMovieClick)
                } else {
                    MovieSection("Popular Movies", popular, onMovieClick, onSeeAllPopular)
                    MovieSection("Upcoming Movies", upcoming, onMovieClick, onSeeAllUpcoming)
                    MovieSection("New Movies", nowPlaying, onMovieClick, onSeeAllNowPlaying)
                    MovieSection("Top Rated", topRated, onMovieClick, onSeeAllTopRated)
                }

                // Add enough bottom padding so the last item isn't covered by bottom nav
                Spacer(Modifier.height(90.dp))
            }
        }
    }
}

// --- HELPER COMPOSABLES (OUTSIDE Main Function) ---

@Composable
fun MovieSection(title: String, movies: List<Movie>, onMovieClick: (Movie) -> Unit, onSeeAll: (() -> Unit)? = null) {
    if (movies.isEmpty()) return
    Column(modifier = Modifier.padding(bottom = 24.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            onSeeAll?.let { Text("See all", color = Color.White, modifier = Modifier.clickable { it() }.padding(start = 8.dp), style = MaterialTheme.typography.labelLarge.copy(textDecoration = TextDecoration.Underline)) }
        }
        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(movies) { MovieCard(it, onMovieClick) }
        }
    }
}

@Composable
fun MovieCard(movie: Movie, onClick: (Movie) -> Unit) {
    Column(modifier = Modifier.width(140.dp).clickable { onClick(movie) }) {
        AsyncImage(model = movie.posterPath?.let { "https://image.tmdb.org/t/p/w500$it" }, contentDescription = null, modifier = Modifier.height(200.dp).fillMaxWidth().clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
        Spacer(Modifier.height(8.dp))
        Text(movie.title, color = Color.White, style = MaterialTheme.typography.bodyMedium, maxLines = 1, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun SearchResultsGrid(title: String, movies: List<Movie>, onMovieClick: (Movie) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        if (movies.isEmpty()) {
            Text("No results.", color = Color.White.copy(0.7f), modifier = Modifier.padding(horizontal = 16.dp))
        } else {
            LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.fillMaxWidth().heightIn(max = 2000.dp), userScrollEnabled = false, contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(movies, key = { it.id }) { GridMovieCard(it, onMovieClick) }
            }
        }
    }
}

@Composable
fun GridMovieCard(movie: Movie, onClick: (Movie) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().clickable { onClick(movie) }) {
        AsyncImage(model = movie.posterPath?.let { "https://image.tmdb.org/t/p/w500$it" }, contentDescription = null, modifier = Modifier.aspectRatio(2f/3f).fillMaxWidth().clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
        Spacer(Modifier.height(6.dp))
        Text(movie.title, color = Color.White, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}