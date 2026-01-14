package com.example.movilog.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkWatchedDialog(
    onDismiss: () -> Unit,
    onConfirm: (rating: Float, watchedAtMillis: Long) -> Unit
) {
    var rating by remember { mutableFloatStateOf(0f) }
    val datePickerState = rememberDatePickerState()
    val selectedDateMillis = datePickerState.selectedDateMillis
    val canConfirm = rating > 0f && selectedDateMillis != null

    // Theme constants
    val bg = Color(0xFF0B2A36)
    val cardBg = Color(0xFF6F7D86).copy(alpha = 0.55f)
    val accent = Color(0xFFF2B400)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = bg,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text("Mark as Watched", color = Color.White)
        },
        text = {
            // Added verticalScroll because DatePicker + Slider is quite tall
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    "Your rating (required)",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Slider(
                        value = rating,
                        onValueChange = { rating = it },
                        valueRange = 0f..5f,
                        steps = 9,
                        colors = SliderDefaults.colors(
                            thumbColor = accent,
                            activeTrackColor = accent,
                            inactiveTrackColor = cardBg,
                            activeTickColor = Color.Black.copy(alpha = 0.4f),
                            inactiveTickColor = Color.White.copy(alpha = 0.4f)
                        )
                    )
                    Text(
                        "Selected rating: ${String.format("%.1f", rating)} / 5",
                        color = accent,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                Text(
                    "When did you watch it? (required)",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium
                )

                // Themed DatePicker
                DatePicker(
                    state = datePickerState,
                    showModeToggle = false,
                    title = null,
                    headline = null,
                    colors = DatePickerDefaults.colors(
                        containerColor = bg,
                        titleContentColor = Color.White,
                        headlineContentColor = Color.White,
                        weekdayContentColor = Color.White.copy(alpha = 0.6f),
                        subheadContentColor = Color.White.copy(alpha = 0.6f),
                        navigationContentColor = Color.White,
                        yearContentColor = Color.White,
                        dayContentColor = Color.White,
                        selectedDayContainerColor = accent,
                        selectedDayContentColor = Color.Black,
                        todayContentColor = accent,
                        todayDateBorderColor = accent
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(rating, selectedDateMillis!!) },
                enabled = canConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent,
                    contentColor = Color.Black,
                    disabledContainerColor = cardBg,
                    disabledContentColor = Color.White.copy(alpha = 0.3f)
                )
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White.copy(alpha = 0.7f))
            }
        }
    )
}