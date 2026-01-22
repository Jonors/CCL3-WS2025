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
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Today
import androidx.compose.ui.text.style.TextAlign
import java.time.Instant
import java.time.ZoneId


@Composable
fun StatsScreen(
    viewModel: MovieViewModel,
    onMovieClick: (Int) -> Unit
) {
    val state by viewModel.statsState.collectAsState()
    val selectedMonthState by viewModel.selectedMonth.collectAsState()
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

            // Top Section: Overall Stats
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(
                        title = "Total Watched",
                        value = "${state.watchedCount}",
                        cardBg = cardBg,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Total Watch Time",
                        value = formatTotalTime(state.totalMinutes), // Shows "1d 6h 15m"
                        cardBg = cardBg,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                val isNotCurrentMonth = selectedMonthState != YearMonth.now()
                var showYearPicker by remember { mutableStateOf(false) }

                Card(
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text(
                            "Movies watched history",
                            color = Color.White.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.titleSmall
                        )

                        Spacer(Modifier.height(8.dp))

                        // Navigation Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { viewModel.prevMonth() }) {
                                Icon(Icons.Default.ChevronLeft, "Prev", tint = Color.White)
                            }

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.weight(1f)
                            ) {
                                TextButton(onClick = { showYearPicker = true }) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            state.monthLabel,
                                            color = Color.White,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Icon(Icons.Default.ArrowDropDown, null, tint = Color.White)
                                    }
                                }
                                // Dropdown logic remains same
                                DropdownMenu(
                                    expanded = showYearPicker,
                                    onDismissRequest = { showYearPicker = false },
                                    modifier = Modifier
                                        .background(Color(0xFF1B3A46))
                                        .heightIn(max = 300.dp)
                                ) {
                                    viewModel.getYearRange().forEach { year ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    year.toString(),
                                                    color = if (year == selectedMonthState.year) accent else Color.White
                                                )
                                            },
                                            onClick = {
                                                viewModel.selectYear(year); showYearPicker = false
                                            }
                                        )
                                    }
                                }
                            }

                            IconButton(onClick = { viewModel.nextMonth() }) {
                                Icon(Icons.Default.ChevronRight, "Next", tint = Color.White)
                            }

                            Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                                if (isNotCurrentMonth) {
                                    IconButton(onClick = { viewModel.jumpToToday() }) {
                                        Icon(
                                            Icons.Default.Today,
                                            null,
                                            tint = accent,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        // Heatmap and Monthly Stats Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            // Left Side: Heatmap
                            Box(modifier = Modifier.weight(1f)) {
                                MonthlyHeatmap(
                                    year = selectedMonthState.year,
                                    month = selectedMonthState.monthValue,
                                    heatmap = state.heatmap,
                                    accent = accent,
                                    watchedMovies = state.watchedInMonth
                                )
                            }

                            Spacer(Modifier.width(16.dp))

                            // Right Side: Monthly Summary Info
                            Column(
                                modifier = Modifier.width(115.dp), // Increased width for "dd hh mm" string
                                horizontalAlignment = Alignment.End
                            ) {
                                Text("This Month", color = Color.White.copy(0.6f), style = MaterialTheme.typography.labelSmall)
                                Text("${state.watchedInMonth.size} movies", color = Color.White, style = MaterialTheme.typography.bodyLarge)

                                Spacer(Modifier.height(16.dp))

                                Text("Watch Time", color = Color.White.copy(0.6f), style = MaterialTheme.typography.labelSmall)
                                Text(
                                    formatTotalTime(state.watchedInMonth.sumOf { it.runtimeMinutes ?: 0 }),
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.End
                                )
                            }
                        }
                    }
                }
            }

            // ... Favorite and Recently Watched items
            item {
                Text(
                    "Highest rated movies",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            item { PosterRow(movies = state.favorites, onMovieClick = onMovieClick) }
            item {
                Text(
                    "Recently Watched Movies",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            item { PosterRow(movies = state.recent, onMovieClick = onMovieClick) }
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
    accent: Color,
    watchedMovies: List<Movie> // Add this parameter
) {
    val emptyColor = Color.White.copy(alpha = 0.18f)
    val midColor = accent.copy(alpha = 0.55f)
    val fullColor = accent

    var selectedDayIndex by remember { mutableStateOf<Int?>(null) }

    val yearMonth = YearMonth.of(year, month)
    val daysInActualMonth = yearMonth.lengthOfMonth()
    val firstDayOfMonth = LocalDate.of(year, month, 1)
    val startOffset = firstDayOfMonth.dayOfWeek.value - 1

    val totalSlotsNeeded = startOffset + daysInActualMonth
    val totalWeeks =
        if (totalSlotsNeeded % 7 == 0) totalSlotsNeeded / 7 else (totalSlotsNeeded / 7) + 1

    val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    Column {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.width(40.dp)) {
                dayLabels.forEach { d ->
                    Box(Modifier.height(18.dp), contentAlignment = Alignment.CenterStart) {
                        Text(
                            d,
                            color = Color.White.copy(0.75f),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }

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

        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Less",
                color = Color.White.copy(0.6f),
                style = MaterialTheme.typography.labelSmall
            )
            Spacer(Modifier.width(6.dp))
            Box(
                Modifier
                    .size(10.dp)
                    .background(emptyColor, RoundedCornerShape(2.dp))
            )
            Spacer(Modifier.width(4.dp))
            Box(
                Modifier
                    .size(10.dp)
                    .background(midColor, RoundedCornerShape(2.dp))
            )
            Spacer(Modifier.width(4.dp))
            Box(
                Modifier
                    .size(10.dp)
                    .background(fullColor, RoundedCornerShape(2.dp))
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "More",
                color = Color.White.copy(0.6f),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }

    // Dialog showing Titles
    selectedDayIndex?.let { index ->
        val selectedDate = LocalDate.of(year, month, index + 1)

        // Filter movies that match this specific date
        val moviesOnThisDay = watchedMovies.filter { movie ->
            movie.watchedAt?.let { ms ->
                val date = Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).toLocalDate()
                date == selectedDate
            } ?: false
        }

        AlertDialog(
            onDismissRequest = { selectedDayIndex = null },
            confirmButton = {
                TextButton(onClick = { selectedDayIndex = null }) {
                    Text("Close", color = accent)
                }
            },
            title = {
                Text(
                    text = "${selectedDate.dayOfMonth} ${
                        selectedDate.month.name.lowercase().replaceFirstChar { it.uppercase() }
                    } $year",
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (moviesOnThisDay.isEmpty()) {
                        Text(
                            "No movies recorded for this day.",
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    } else {
                        moviesOnThisDay.forEach { movie ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    Modifier
                                        .size(6.dp)
                                        .background(accent, RoundedCornerShape(50))
                                        .padding(end = 8.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    movie.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            },
            containerColor = Color(0xFF1B3A46),
            titleContentColor = Color.White,
            textContentColor = Color.White
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
    val remainingAfterDays = minutes % (60 * 24)
    val hours = remainingAfterDays / 60
    val finalMinutes = remainingAfterDays % 60

    return when {
        days > 0 -> "${days}d ${hours}h ${finalMinutes}m"
        hours > 0 -> "${hours}h ${finalMinutes}m"
        else -> "${finalMinutes}m"
    }
}
