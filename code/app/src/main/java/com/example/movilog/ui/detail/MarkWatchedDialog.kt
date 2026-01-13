package com.example.movilog.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mark as Watched") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {

                Text("Your rating (required)")
                Slider(
                    value = rating,
                    onValueChange = { rating = it },
                    valueRange = 0f..5f,
                    steps = 9 // 0.5 steps (0.0, 0.5, 1.0 ... 5.0)
                )
                Text("Selected rating: ${String.format("%.1f", rating)} / 5")

                Divider()

                Text("When did you watch it? (required)")
                DatePicker(state = datePickerState)
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(rating, selectedDateMillis!!) },
                enabled = canConfirm
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
