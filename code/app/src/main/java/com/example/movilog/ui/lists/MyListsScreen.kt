package com.example.movilog.ui.lists

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
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
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.movilog.data.model.ListWithMovies
import com.example.movilog.data.model.Movie
import com.example.movilog.navigation.Routes
import com.example.movilog.ui.MovieViewModel
import com.example.movilog.ui.components.DeleteConfirmationDialog
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyListsScreen(
    navController: NavController,
    viewModel: MovieViewModel,
    onMovieClick: (Int) -> Unit,
    onListClick: (Long) -> Unit,
    onSeeAllWatched: () -> Unit
) {
    val watched by viewModel.watchedList.collectAsState(initial = emptyList())
    val customCollections by viewModel.customCollections.collectAsState()
    val scope = rememberCoroutineScope()

    var showAddToListDialog by remember { mutableStateOf(false) }
    var listIdToDelete by remember { mutableStateOf<Long?>(null) }
    var isEditMode by remember { mutableStateOf(false) }

    val bg = Color(0xFF0B2A36)
    val accent = Color(0xFFF2B400)

    Scaffold(
        containerColor = bg,
        topBar = {
            TopAppBar( // ✅ Changed from CenterAlignedTopAppBar to TopAppBar
                title = {
                    Text(
                        "My Lists",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium
                    )
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
                colors = TopAppBarDefaults.topAppBarColors( // ✅ Updated color utility name
                    containerColor = bg
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {

            if (showAddToListDialog) {
                CreateNewList(
                    onDismiss = { showAddToListDialog = false },
                    onCreateNewList = { name ->
                        scope.launch {
                            val newListId = viewModel.createCustomList(name)
                            navController.navigate("${Routes.CUSTOM_LIST_DETAIL}/$newListId")
                            showAddToListDialog = false
                        }
                    }
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(15.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 70.dp),
            ) {
                // 1. Static Watched List
                item {
                    Section(
                        title = "Watched list",
                        listId = null,
                        movies = watched,
                        onMovieClick = onMovieClick,
                        onDeleteList = {},
                        onRemoveMovie = { _, _ -> },
                        onSeeAll = { onSeeAllWatched() } // <-- Callback
                    )

                }

                // 2. Add New List Placeholder (Edit Mode only)
                if (isEditMode) {
                    item {
                        AddListPlaceholderCard(onClick = { showAddToListDialog = true }, accent = accent)
                    }
                }

                // 3. Dynamic Custom Collections
                items(customCollections) { collection ->
                    CustomListFolderCard(
                        collection = collection,
                        isEditMode = isEditMode,
                        onClick = {
                            if (!isEditMode) onListClick(collection.customList.listId)
                        },
                        onDeleteClick = { listIdToDelete = collection.customList.listId }
                    )
                }

                item { Spacer(Modifier.height(24.dp)) }
            }

            if (listIdToDelete != null) {
                val currentId = listIdToDelete!!
                DeleteConfirmationDialog(
                    title = "Delete List?",
                    text = "This will delete the List. The movies will remain in your Watched list. Continue?",
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
private fun AddListPlaceholderCard(onClick: () -> Unit, accent: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = accent)
            Spacer(Modifier.width(12.dp))
            Text("Create New List", color = accent, style = MaterialTheme.typography.titleMedium)
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
    onRemoveMovie: (Long, Int) -> Unit,
    onSeeAll: (() -> Unit)? = null
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(title, color = Color.White, style = MaterialTheme.typography.titleMedium)

            // ✅ rechts: See all (optional) + Delete (optional)
            Row(verticalAlignment = Alignment.CenterVertically) {

                if (onSeeAll != null) {
                    Text(
                        text = "See all",
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge.copy(
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                        ),
                        modifier = Modifier
                            .clickable { onSeeAll() }
                            .padding(end = 10.dp)
                    )
                }

                if (listId != null) {
                    IconButton(onClick = { onDeleteList(listId) }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            tint = Color.Red.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        if (movies.isEmpty()) {
            Text("No movies yet.", color = Color.White.copy(alpha = 0.5f))
        } else {
            // ✅ bleibt EXACT gleich
            PosterRow(listId, movies, onMovieClick, onRemoveMovie)
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
                AsyncImage(
                    model = posterUrl,
                    contentDescription = movie.title,
                    modifier = Modifier.fillMaxWidth().height(170.dp),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
private fun CustomListFolderCard(
    collection: ListWithMovies,
    isEditMode: Boolean,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val cardBg = Color(0xFF6F7D86).copy(alpha = 0.55f)
    val posterUrl = collection.movies.firstOrNull()?.posterPath?.let { "https://image.tmdb.org/t/p/w342$it" }
    val deleteRed = Color(0xFFE85B5B)
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
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Text(collection.customList.listName, color = Color.White, style = MaterialTheme.typography.titleMedium)
                Text("${collection.movies.size} Movies", color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.bodySmall)
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
            } else {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.White.copy(alpha = 0.3f))
            }
        }
    }
}