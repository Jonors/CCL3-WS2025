package com.example.movilog.ui.detail

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import kotlin.math.abs
import kotlin.math.floor
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkWatchedDialog(
    movieTitle: String,
    onDismiss: () -> Unit,
    onConfirm: (rating: Float, watchedAtMillis: Long) -> Unit
) {
    val bg = Color(0xFF0B2A36)
    val cardBg = Color(0xFF6F7D86).copy(alpha = 0.55f)
    val accent = Color(0xFFF2B400)
    val errorRed = Color(0xFFF2B400)

    val today = remember { LocalDate.now() }
    var ratingInt by remember { mutableIntStateOf(0) }
    var hasInteracted by remember { mutableStateOf(false) }

    var year by remember { mutableIntStateOf(today.year) }
    var month by remember { mutableIntStateOf(today.monthValue) }
    var day by remember { mutableIntStateOf(today.dayOfMonth) }

    LaunchedEffect(year, month, day) {
        val now = LocalDate.now()

        if (year > now.year) {
            year = now.year
        }

        if (year == now.year && month > now.monthValue) {
            month = now.monthValue
        }

        val maxDayInMonth = YearMonth.of(year, month).lengthOfMonth()
        if (day > maxDayInMonth) {
            day = maxDayInMonth
        }

        if (year == now.year && month == now.monthValue && day > now.dayOfMonth) {
            day = now.dayOfMonth
        }
    }

    val isError = hasInteracted && ratingInt == 0
    val ratingLabelColor by animateColorAsState(if (isError) errorRed else Color.White.copy(alpha = 0.8f))

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = bg,
        shape = RoundedCornerShape(28.dp),
        title = {
            Text(
                "Mark ${movieTitle} as Watched",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header for Rating
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Your Rating",
                        color = ratingLabelColor,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(" *", color = errorRed) // Visual "Mandatory" indicator
                }

                // Star Rating Component
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (isError) Modifier.background(
                                errorRed.copy(alpha = 0.1f),
                                RoundedCornerShape(12.dp)
                            ) else Modifier
                        )
                        .padding(8.dp)
                ) {
                    StarRating10(
                        value = ratingInt,
                        onValueChange = {
                            ratingInt = it
                            hasInteracted = true
                        },
                        accent = accent,
                        inactive = if (isError) errorRed.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.25f),
                    )
                }

                // Explicit Instruction Text
                Text(
                    text = when {
                        ratingInt == 0 && isError -> "Please select a rating to continue"
                        ratingInt == 0 -> "Tap or slide to rate (1–10)"
                        else -> "Selected: $ratingInt / 10"
                    },
                    color = when {
                        isError -> errorRed
                        ratingInt > 0 -> accent
                        else -> Color.White.copy(alpha = 0.6f)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (ratingInt > 0) FontWeight.Bold else FontWeight.Normal
                )

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 1.dp)

                // Date Picker Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Watch Date",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(" *", color = errorRed)
                }

                MiniDateWheelPicker(
                    year = year, month = month, day = day,
                    onYearChange = { year = it },
                    onMonthChange = { month = it },
                    onDayChange = { day = it },
                    accent = accent,
                    cardBg = cardBg
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (ratingInt == 0) {
                        hasInteracted = true
                    } else {
                        val selectedDate = LocalDate.of(year, month, day)
                        val millis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        onConfirm(ratingInt.toFloat(), millis)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (ratingInt > 0) accent else cardBg,
                    contentColor = if (ratingInt > 0) Color.Black else Color.White.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text("Save Entry", fontWeight = FontWeight.ExtraBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White.copy(alpha = 0.6f))
            }
        }
    )
}

/**
 * Enhanced StarRating with accessibility support
 */
@Composable
private fun StarRating10(
    value: Int,
    onValueChange: (Int) -> Unit,
    accent: Color,
    inactive: Color
) {
    val gap = 4.dp
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val starSize = ((maxWidth - (gap * 9)) / 10.5f).coerceIn(20.dp, 32.dp)
        val density = LocalDensity.current
        val starPx = with(density) { starSize.toPx() }
        val gapPx = with(density) { gap.toPx() }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        onValueChange(ratingFromOffset(change.position.x, starPx, gapPx))
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        onValueChange(ratingFromOffset(offset.x, starPx, gapPx))
                    }
                },
            horizontalArrangement = Arrangement.Center
        ) {
            for (i in 1..10) {
                val isSelected = i <= value
                Text(
                    text = "★",
                    color = if (isSelected) accent else inactive,
                    fontSize = with(density) { starSize.toSp() },
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }
        }
    }
}

private fun ratingFromOffset(x: Float, starPx: Float, gapPx: Float): Int {
    val cell = starPx + gapPx
    return (floor(x / cell).toInt() + 1).coerceIn(1, 10)
}

// --- Rest of the components (MiniDateWheelPicker, LabeledWheel, WheelPickerColumn) remain functionally the same ---
// (Included below for completeness in a single file copy)

@Composable
private fun MiniDateWheelPicker(
    year: Int, month: Int, day: Int,
    onYearChange: (Int) -> Unit, onMonthChange: (Int) -> Unit, onDayChange: (Int) -> Unit,
    accent: Color, cardBg: Color
) {
    val now = remember { LocalDate.now() }

    // Years: from 50 years ago up to current year
    val years = remember { (now.year - 50..now.year).toList() }

    // Months: 1..12, but if current year is selected, only up to now.monthValue
    val months = remember(year) {
        val maxMonth = if (year >= now.year) now.monthValue else 12
        (1..maxMonth).toList()
    }

    // Days: 1..maxInMonth, but if current year AND month are selected, only up to now.dayOfMonth
    val days = remember(year, month) {
        val maxInMonth = YearMonth.of(year, month).lengthOfMonth()
        val actualMax = if (year >= now.year && month >= now.monthValue) {
            now.dayOfMonth
        } else {
            maxInMonth
        }
        (1..actualMax).toList()
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LabeledWheel("Year", years, year, onYearChange, accent, Modifier.weight(1.2f))
            LabeledWheel("Month", months, month, onMonthChange, accent, Modifier.weight(1f))
            LabeledWheel("Day", days, day, onDayChange, accent, Modifier.weight(1f))
        }
    }
}

@Composable
private fun LabeledWheel(
    label: String, values: List<Int>, selected: Int,
    onSelected: (Int) -> Unit, accent: Color, modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.labelSmall)
        Spacer(Modifier.height(4.dp))
        WheelPickerColumn(values, selected, onSelected, accent)
    }
}

@Composable
private fun WheelPickerColumn(
    values: List<Int>, selected: Int, onSelected: (Int) -> Unit, accent: Color
) {
    val itemHeight = 32.dp
    val visibleItems = 3
    val centerOffset = visibleItems / 2
    val padded = remember(values) { List(centerOffset) { null } + values + List(centerOffset) { null } }

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = values.indexOf(selected).coerceAtLeast(0))
    val fling = rememberSnapFlingBehavior(lazyListState = listState)

    LaunchedEffect(selected) {
        val target = values.indexOf(selected).coerceAtLeast(0)
        if (listState.firstVisibleItemIndex != target) listState.animateScrollToItem(target)
    }

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val centerIndex = listState.firstVisibleItemIndex
            values.getOrNull(centerIndex)?.let { onSelected(it) }
        }
    }

    Box(modifier = Modifier.height(itemHeight * visibleItems), contentAlignment = Alignment.Center) {
        Box(Modifier.fillMaxWidth().height(itemHeight).background(accent.copy(alpha = 0.15f), RoundedCornerShape(8.dp)))
        LazyColumn(state = listState, flingBehavior = fling, modifier = Modifier.fillMaxSize()) {
            items(padded.size) { idx ->
                val v = padded[idx]
                Box(Modifier.fillMaxWidth().height(itemHeight), contentAlignment = Alignment.Center) {
                    Text(
                        text = v?.toString() ?: "",
                        color = if (v == selected) Color.White else Color.White.copy(alpha = 0.3f),
                        style = if (v == selected) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}