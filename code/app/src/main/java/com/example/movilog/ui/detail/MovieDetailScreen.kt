package com.example.movilog.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.example.movilog.data.model.CustomList
import com.example.movilog.data.remote.MovieDetailsDto
import com.example.movilog.ui.MovieViewModel
import com.example.movilog.ui.RatingsCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    movieId: Int,
    viewModel: MovieViewModel,
    onBack: () -> Unit = {}
) {
    LaunchedEffect(movieId) { viewModel.loadMovieDetails(movieId) }

    val state by viewModel.detailState.collectAsState()
    val customLists by viewModel.customLists.collectAsState()

    var showWatchedDialog by remember { mutableStateOf(false) }
    var showAddToListDialog by remember { mutableStateOf(false) }

    val bg = Color(0xFF0B2A36)
    val cardBg = Color(0xFF6F7D86).copy(alpha = 0.55f)
    val accent = Color(0xFFF2B400)

    Scaffold(
        containerColor = bg,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Movie Screen", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = bg)
            )
        }
    ) { padding ->
        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            state.details == null -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("Failed to load movie details", color = Color.White)
                }
            }
            else -> {
                val d = state.details!!
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    item { HeroCard(d, cardBg, state.isWatched) }

                    item {
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                when {
                                    state.isWatched -> {
                                        Button(
                                            onClick = { showAddToListDialog = true },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = accent)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
                                            Spacer(Modifier.width(8.dp))
                                            Text("Add to Collection", color = Color.Black)
                                        }
                                    }
                                    state.inWatchlist -> {
                                        OutlinedButton(onClick = {}, enabled = false, modifier = Modifier.weight(1f)) {
                                            Text("In Watchlist", color = Color.White)
                                        }
                                        Button(
                                            onClick = { showWatchedDialog = true },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = accent)
                                        ) {
                                            Text("Watched", color = Color.Black)
                                        }
                                    }
                                    else -> {
                                        OutlinedButton(
                                            onClick = { viewModel.addCurrentDetailToWatchlist() },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Add to Watchlist", color = Color.White)
                                        }
                                        Button(
                                            onClick = { showWatchedDialog = true },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = accent)
                                        ) {
                                            Text("Watched", color = Color.Black)
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                    }

                    item {
                        Text(
                            text = d.overview.ifBlank { "No description available." },
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(Modifier.height(18.dp))
                    }

                    item {
                        RatingsCard(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            cardBg = cardBg,
                            userRating = d.voteAverage ?: 0f
                        )
                    }
                }
            }
        }

        // Dialogs
        if (showWatchedDialog) {
            MarkWatchedDialog(
                onDismiss = { showWatchedDialog = false },
                onConfirm = { rating, watchedAt ->
                    showWatchedDialog = false
                    viewModel.markCurrentDetailAsWatched(rating, watchedAt)
                }
            )
        }

        if (showAddToListDialog) {
            AddToListDialog(
                availableLists = customLists,
                onDismiss = { showAddToListDialog = false },
                onListSelected = { listId ->
                    viewModel.addMovieToList(movieId, listId)
                    showAddToListDialog = false
                },
                onCreateNewList = { name ->
                    viewModel.createNewList(name)
                }
            )
        }
    }
}

// Helper components (HeroCard, InfoChip, etc) stay the same as your original code...


@Composable
private fun HeroCard(
    details: MovieDetailsDto,
    cardBg: Color,
    isWatched: Boolean
) {
    val backdropUrl = details.backdropPath?.let { "https://image.tmdb.org/t/p/w780$it" }
    val posterUrl = details.posterPath?.let { "https://image.tmdb.org/t/p/w342$it" }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            ) {
                AsyncImage(
                    model = backdropUrl,
                    contentDescription = "Backdrop",
                    modifier = Modifier.fillMaxSize()
                )

                if (posterUrl != null) {
                    Card(
                        modifier = Modifier
                            .padding(14.dp)
                            .width(120.dp)
                            .height(170.dp)
                            .align(Alignment.BottomStart),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        AsyncImage(
                            model = posterUrl,
                            contentDescription = "Poster",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Column(Modifier.padding(16.dp)) {
                Text(
                    text = details.title,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "Directed by …",
                    color = Color.White.copy(alpha = 0.75f),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Studios …",
                    color = Color.White.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    InfoChip(label = formatRuntime(details.runtime))
                    InfoChip(label = details.releaseDate ?: "Unknown date")
                    InfoChip(label = if (isWatched) "Watched" else "Not Watched")
                }
            }
        }
    }
}

@Composable
private fun InfoChip(label: String) {
    Surface(
        color = Color.Black.copy(alpha = 0.25f),
        shape = RoundedCornerShape(999.dp)
    ) {
        Text(
            text = label,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

// New File: com.example.movilog.ui.detail.AddToListDialog.kt

@Composable
fun AddToListDialog(
    availableLists: List<CustomList>,
    onDismiss: () -> Unit,
    onListSelected: (Long) -> Unit,
    onCreateNewList: (String) -> Unit
) {
    var newListName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to Custom List") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // List existing lists
                if (availableLists.isEmpty()) {
                    Text("No lists created yet.", style = MaterialTheme.typography.bodySmall)
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                        items(availableLists) { list ->
                            TextButton(
                                onClick = { onListSelected(list.listId) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(list.listName, color = Color.Black)
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                // Create New List field
                OutlinedTextField(
                    value = newListName,
                    onValueChange = { newListName = it },
                    label = { Text("New List Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        if (newListName.isNotBlank()) {
                            onCreateNewList(newListName)
                            newListName = ""
                        }
                    },
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .align(Alignment.End)
                ) {
                    Text("Create & Add")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

private fun formatRuntime(runtimeMinutes: Int?): String {
    if (runtimeMinutes == null || runtimeMinutes <= 0) return "—"
    val h = runtimeMinutes / 60
    val m = runtimeMinutes % 60
    return "${h} hr ${m} min"
}
