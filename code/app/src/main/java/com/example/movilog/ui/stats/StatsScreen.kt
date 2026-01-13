package com.example.movilog.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import kotlin.math.roundToInt

@Composable
fun StatsScreen(
    viewModel: MovieViewModel,
    onMovieClick: (Int) -> Unit
) {
    val state by viewModel.statsState.collectAsState()

    val bg = Color(0xFF0B2A36)
    val cardBg = Color(0xFF6F7D86).copy(alpha = 0.55f)
    val accent = Color(0xFFF2B400)

    Scaffold(containerColor = bg) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Statistics", color = Color.White, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(14.dp))

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

            Spacer(Modifier.height(12.dp))

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
                        heatmap = state.heatmap,
                        accent = accent
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text("Favorite Movies", color = Color.White, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(10.dp))
            PosterRow(movies = state.favorites, onMovieClick = onMovieClick)

            Spacer(Modifier.height(16.dp))

            Text("Recently Watched Movies", color = Color.White, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(10.dp))
            PosterRow(movies = state.recent, onMovieClick = onMovieClick)
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
            Text(title, color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(6.dp))
            Text(value, color = Color.White, style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
private fun MonthlyHeatmap(heatmap: List<Int>, accent: Color) {
    // heatmap is weeks*7, Mon..Sun
    val emptyColor = Color.White.copy(alpha = 0.18f)
    val midColor = accent.copy(alpha = 0.55f)
    val fullColor = accent

    val weeks = if (heatmap.isEmpty()) 0 else heatmap.size / 7
    val dayLabels = listOf("Mon","Tue","Wed","Thu","Fri","Sat","Sun")

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.width(40.dp)) {
                dayLabels.forEach { d ->
                    Text(d, color = Color.White.copy(alpha = 0.75f), style = MaterialTheme.typography.labelSmall)
                    Spacer(Modifier.height(8.dp))
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                for (w in 0 until weeks) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        for (dow in 0 until 7) {
                            val v = heatmap[w * 7 + dow]
                            val c = when {
                                v <= 0 -> emptyColor
                                v == 1 -> midColor
                                else -> fullColor
                            }
                            Box(
                                Modifier
                                    .size(18.dp)
                                    .background(c, RoundedCornerShape(6.dp))
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(14.dp).background(emptyColor, RoundedCornerShape(4.dp)))
            Text("Less", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
            Box(Modifier.size(14.dp).background(midColor, RoundedCornerShape(4.dp)))
            Box(Modifier.size(14.dp).background(fullColor, RoundedCornerShape(4.dp)))
            Text("More", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun PosterRow(movies: List<Movie>, onMovieClick: (Int) -> Unit) {
    if (movies.isEmpty()) {
        Text("—", color = Color.White.copy(alpha = 0.7f))
        return
    }

    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(movies) { movie ->
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
