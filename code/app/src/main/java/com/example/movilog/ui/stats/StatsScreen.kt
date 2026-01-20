package com.example.movilog.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.movilog.data.model.Movie
import com.example.movilog.ui.MovieViewModel
import java.time.LocalDate
import java.time.YearMonth
import androidx.compose.foundation.clickable // Add this import
import androidx.compose.ui.window.Dialog


@Composable
fun StatsScreen(
    viewModel: MovieViewModel,
    onMovieClick: (Int) -> Unit
) {
    val state by viewModel.statsState.collectAsState()
    val now = LocalDate.now()
    val bg = Color(0xFF0B2A36)
    val cardBg = Color(0xFF6F7D86).copy(alpha = 0.55f)
    val accent = Color(0xFFF2B400)

    Scaffold(containerColor = bg) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Statistics",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        title = "Movies Watched",
                        value = state.watchedCount.toString(),
                        cardBg = cardBg,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Time spent overall",
                        value = formatTotalTime(state.totalMinutes),
                        cardBg = cardBg,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Card(


                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text(
                            "Movies watched this month",
                            color = Color.White.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            state.monthLabel,
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(10.dp))

                        MonthlyHeatmap(
                            year = now.year,
                            month = now.monthValue,
                            heatmap = state.heatmap,
                            accent = accent
                        )
                    }
                }
            }

            item {
                Text(
                    "Favorite Movies",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge
                )
            }

            item {
                PosterRow(movies = state.favorites, onMovieClick = onMovieClick)
            }

            item {
                Text(
                    "Recently Watched Movies",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge
                )
            }

            item {
                PosterRow(movies = state.recent, onMovieClick = onMovieClick)
            }
        }
    }

}

@Composable
private fun StatCard(title: String, value: String, cardBg: Color, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(18.dp),
        modifier = modifier
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                title,
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(Modifier.height(6.dp))
            Text(value, color = Color.White, style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
fun MonthlyHeatmap(
    year: Int,
    month: Int,
    heatmap: List<Int>,
    accent: Color
) {
    val emptyColor = Color.White.copy(alpha = 0.18f)
    val midColor = accent.copy(alpha = 0.55f)
    val fullColor = accent

    // Dialog state
    var selectedDayIndex by remember { mutableStateOf<Int?>(null) }

    val yearMonth = YearMonth.of(year, month)
    val daysInActualMonth = yearMonth.lengthOfMonth()
    val firstDayOfMonth = LocalDate.of(year, month, 1)
    val startOffset = firstDayOfMonth.dayOfWeek.value - 1

    val totalSlotsNeeded = startOffset + daysInActualMonth
    val totalWeeks = if (totalSlotsNeeded % 7 == 0) totalSlotsNeeded / 7 else (totalSlotsNeeded / 7) + 1

    val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    Column {
        Row(verticalAlignment = Alignment.Top) {
            // Day Labels Column
            Column(Modifier.width(40.dp)) {
                dayLabels.forEach { d ->
                    Box(Modifier.height(18.dp), contentAlignment = Alignment.CenterStart) {
                        Text(d, color = Color.White.copy(0.75f), style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }

            // Grid
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                for (w in 0 until totalWeeks) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        for (dow in 0 until 7) {
                            val dayIndexInGrid = (w * 7) + dow
                            val dayOfMonthIndex = dayIndexInGrid - startOffset

                            if (dayOfMonthIndex in 0 until daysInActualMonth) {
                                val v = heatmap.getOrNull(dayOfMonthIndex) ?: 0
                                val c = when {
                                    v <= 0 -> emptyColor
                                    v == 1 -> midColor
                                    else -> fullColor
                                }
                                Box(
                                    Modifier
                                        .size(18.dp)
                                        .background(c, RoundedCornerShape(6.dp))
                                        .clickable { selectedDayIndex = dayOfMonthIndex }
                                )
                            } else {
                                Box(Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }

        // 1. Legend moved below the grid row
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Less", color = Color.White.copy(0.6f), style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.width(6.dp))
            Box(Modifier.size(10.dp).background(emptyColor, RoundedCornerShape(2.dp)))
            Spacer(Modifier.width(4.dp))
            Box(Modifier.size(10.dp).background(midColor, RoundedCornerShape(2.dp)))
            Spacer(Modifier.width(4.dp))
            Box(Modifier.size(10.dp).background(fullColor, RoundedCornerShape(2.dp)))
            Spacer(Modifier.width(6.dp))
            Text("More", color = Color.White.copy(0.6f), style = MaterialTheme.typography.labelSmall)
        }
    }

    // 2. Click Feature: Showing the Date and Count
    selectedDayIndex?.let { index ->
        val date = LocalDate.of(year, month, index + 1)
        val count = heatmap.getOrNull(index) ?: 0

        AlertDialog(
            onDismissRequest = { selectedDayIndex = null },
            confirmButton = {
                TextButton(onClick = { selectedDayIndex = null }) {
                    Text("Close", color = accent)
                }
            },
            title = {
                Text(
                    text = "${date.dayOfMonth} ${date.month.name.lowercase().replaceFirstChar { it.uppercase() }} $year",
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Text("Movies watched: $count")
            },
            containerColor = Color(0xFF1B3A46), // Dark slate to match your theme
            titleContentColor = Color.White,
            textContentColor = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun PosterRow(movies: List<Movie>, onMovieClick: (Int) -> Unit) {
    if (movies.isEmpty()) {
        Text("—", color = Color.White.copy(alpha = 0.7f))
        return
    }

    // Limit the list to 10 items before passing it to items()
    val limitedMovies = movies.take(10)

    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(limitedMovies) { movie ->
            val posterUrl = movie.posterPath?.let { "https://image.tmdb.org/t/p/w342$it" }
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.width(120.dp),
                onClick = { onMovieClick(movie.id) }
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
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

private fun formatTotalTime(totalMinutes: Int): String {
    val minutes = totalMinutes.coerceAtLeast(0)
    val days = minutes / (60 * 24)
    val rem = minutes % (60 * 24)
    val hours = rem / 60
    return "${hours}h ${days}d"
}
