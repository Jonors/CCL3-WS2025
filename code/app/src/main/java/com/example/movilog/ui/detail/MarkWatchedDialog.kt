package com.example.movilog.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import kotlin.math.floor
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import kotlinx.coroutines.flow.filter
import kotlin.math.abs
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Spacer


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkWatchedDialog(
    onDismiss: () -> Unit,
    onConfirm: (rating: Float, watchedAtMillis: Long) -> Unit
) {
    // Theme constants (unchanged)
    val bg = Color(0xFF0B2A36)
    val cardBg = Color(0xFF6F7D86).copy(alpha = 0.55f)
    val accent = Color(0xFFF2B400)

    // ✅ Default: today
    val today = remember { LocalDate.now() }

    // ⭐ rating 1..10 (store as Int internally; convert to Float on confirm)
    var ratingInt by remember { mutableIntStateOf(0) }

    // 📅 wheel picker state
    var year by remember { mutableIntStateOf(today.year) }
    var month by remember { mutableIntStateOf(today.monthValue) } // 1..12
    var day by remember { mutableIntStateOf(today.dayOfMonth) }

    // keep day valid when month/year changes
    LaunchedEffect(year, month) {
        val maxDay = YearMonth.of(year, month).lengthOfMonth()
        if (day > maxDay) day = maxDay
        if (day < 1) day = 1
    }

    val canConfirm = ratingInt in 1..10

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = bg,
        shape = RoundedCornerShape(24.dp),
        title = { Text("Mark as Watched", color = Color.White) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    "Your rating (required)",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium
                )

                StarRating10(
                    value = ratingInt,
                    onValueChange = { ratingInt = it },
                    accent = accent,
                    inactive = Color.White.copy(alpha = 0.25f),
                )


                Text(
                    text = if (ratingInt == 0) "Select rating: 1–10" else "Selected rating: $ratingInt / 10",
                    color = if (ratingInt == 0) Color.White.copy(alpha = 0.6f) else accent,
                    style = MaterialTheme.typography.bodyLarge
                )

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                Text(
                    "When did you watch it? (required)",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium
                )

                MiniDateWheelPicker(
                    year = year,
                    month = month,
                    day = day,
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
                    // convert selected y/m/d -> millis at start of day
                    val selectedDate = LocalDate.of(year, month, day)
                    val millis = selectedDate
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()

                    onConfirm(ratingInt.toFloat(), millis)
                },
                enabled = canConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent,
                    contentColor = Color.Black,
                    disabledContainerColor = cardBg,
                    disabledContentColor = Color.White.copy(alpha = 0.3f)
                )
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White.copy(alpha = 0.7f))
            }
        }
    )
}

/**
 * ⭐ 10-star rating with drag support.
 * - Tap sets rating
 * - Drag across sets rating continuously
 */
@Composable
private fun StarRating10(
    value: Int,
    onValueChange: (Int) -> Unit,
    accent: Color,
    inactive: Color
) {
    val gap = 6.dp

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // verfügbare Breite im Dialog
        val availableWidth = maxWidth

        // dynamische Stern-Größe, damit garantiert 10 Sterne reinpassen
        val starSize = ((availableWidth - gap * 9) / 10f).coerceIn(18.dp, 26.dp)

        val density = LocalDensity.current
        val starPx = with(density) { starSize.toPx() }
        val gapPx = with(density) { gap.toPx() }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Drag AREA über die ganze Sternreihe
            Row(
                modifier = Modifier
                    .pointerInput(starSize, gap) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val r = ratingFromOffset(offset.x, starPx, gapPx)
                                onValueChange(r)
                            },
                            onDrag = { change, _ ->
                                val r = ratingFromOffset(change.position.x, starPx, gapPx)
                                onValueChange(r)
                            }
                        )
                    }
            ) {
                for (i in 1..10) {
                    val c = if (i <= value) accent else inactive

                    Text(
                        text = "★",
                        color = c,
                        fontSize = with(LocalDensity.current) { starSize.toSp() },
                        lineHeight = with(LocalDensity.current) { starSize.toSp() },
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            // ✅ Tap/Click pro Stern
                            .pointerInput(i) {
                                detectTapGestures {
                                    onValueChange(i)
                                }
                            }
                    )
                    if (i != 10) Spacer(Modifier.width(gap))
                }
            }
        }
    }
}


private fun ratingFromOffset(x: Float, starPx: Float, gapPx: Float): Int {
    // each "cell" = star + gap (except last gap, but close enough)
    val cell = starPx + gapPx
    val idx = floor(x / cell).toInt() + 1
    return idx.coerceIn(1, 10)
}

/**
 * 📅 Small wheel-like picker (Year / Month / Day) like the screenshot.
 * Uses 3 LazyColumns, snapping not required; simple + works well.
 */
@Composable
private fun MiniDateWheelPicker(
    year: Int,
    month: Int,
    day: Int,
    onYearChange: (Int) -> Unit,
    onMonthChange: (Int) -> Unit,
    onDayChange: (Int) -> Unit,
    accent: Color,
    cardBg: Color
) {
    val years = remember {
        val now = LocalDate.now().year
        (now - 80..now + 1).toList()
    }
    val months = (1..12).toList()

    val maxDay = YearMonth.of(year, month).lengthOfMonth()
    val days = (1..maxDay).toList()

    Card(
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            LabeledWheel(
                label = "Year",
                values = years,
                selected = year,
                onSelected = onYearChange,
                accent = accent,
                modifier = Modifier.weight(1.2f)
            )

            LabeledWheel(
                label = "Month",
                values = months,
                selected = month,
                onSelected = onMonthChange,
                accent = accent,
                modifier = Modifier.weight(0.9f)
            )

            LabeledWheel(
                label = "Day",
                values = days,
                selected = day,
                onSelected = onDayChange,
                accent = accent,
                modifier = Modifier.weight(0.9f)
            )
        }


    }
}
@Composable
private fun WheelPickerColumn(
    values: List<Int>,
    selected: Int,
    onSelected: (Int) -> Unit,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val itemHeight = 36.dp
    val visibleItems = 5
    val centerOffset = visibleItems / 2 // 2
    val listHeight = itemHeight * visibleItems

    // Padding-Items (damit erste/letzte Werte auch zentrierbar sind)
    val padded: List<Int?> = remember(values) {
        List(centerOffset) { null } + values.map { it } + List(centerOffset) { null }
    }

    val selectedIndex = values.indexOf(selected).coerceAtLeast(0)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = selectedIndex)
    val fling = rememberSnapFlingBehavior(lazyListState = listState)

    // ✅ Center-Value, die wirklich gerade im Zentrum sitzt (visuell)
    val centerValue by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            if (layoutInfo.visibleItemsInfo.isEmpty()) return@derivedStateOf null

            val viewportCenter =
                (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2

            val closest = layoutInfo.visibleItemsInfo.minByOrNull { item ->
                val itemCenter = item.offset + item.size / 2
                abs(itemCenter - viewportCenter)
            } ?: return@derivedStateOf null

            padded.getOrNull(closest.index)
        }
    }

    // ✅ Beim Öffnen / wenn selected extern geändert wird -> korrekt zentrieren
    LaunchedEffect(values, selected) {
        val idx = values.indexOf(selected).coerceAtLeast(0)
        if (listState.firstVisibleItemIndex != idx) {
            listState.scrollToItem(idx)
        }
    }

    // ✅ Nach Snap/Stop: CenterValue in State übernehmen
    LaunchedEffect(listState, centerValue) {
        snapshotFlow { listState.isScrollInProgress }
            .distinctUntilChanged()
            .collectLatest { scrolling ->
                if (!scrolling) {
                    val v = centerValue ?: return@collectLatest
                    if (v != selected) onSelected(v)
                }
            }
    }

    Box(
        modifier = modifier.height(listHeight),
        contentAlignment = Alignment.Center
    ) {
        // Oranges Zentrum-Feld
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight)
                .background(accent.copy(alpha = 0.22f), RoundedCornerShape(10.dp))
        )

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            flingBehavior = fling
        ) {
            items(padded.size) { idx ->
                val v = padded[idx]
                val isCentered = (v != null && v == centerValue)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight),
                    contentAlignment = Alignment.Center
                ) {
                    if (v == null) {
                        Spacer(Modifier.height(itemHeight))
                    } else {
                        Text(
                            text = v.toString(),
                            color = if (isCentered) Color.White else Color.White.copy(alpha = 0.45f),
                            style = if (isCentered) MaterialTheme.typography.titleMedium
                            else MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LabeledWheel(
    label: String,
    values: List<Int>,
    selected: Int,
    onSelected: (Int) -> Unit,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.7f),
            style = MaterialTheme.typography.labelSmall
        )

        Spacer(Modifier.height(6.dp))

        WheelPickerColumn(
            values = values,
            selected = selected,
            onSelected = onSelected,
            accent = accent,
            modifier = Modifier.fillMaxWidth()
        )
    }
}


