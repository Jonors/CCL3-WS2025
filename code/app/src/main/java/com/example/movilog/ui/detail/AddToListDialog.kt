package com.example.movilog.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.movilog.data.model.CustomList


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToListDialog(
    availableLists: List<CustomList>,
    onDismiss: () -> Unit,
    onListSelected: (Long) -> Unit,
    onCreateNewList: (String) -> Unit
) {
    var newListName by remember { mutableStateOf("") }

    val bg = Color(0xFF0B2A36)
    val accent = Color(0xFFF2B400)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = bg,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                "Add to Custom List",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // --- SECTION 1: SELECT EXISTING ---
                if (availableLists.isNotEmpty()) {
                    Text(
                        "Select an exisitng list:",
                        color = Color.White.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                        items(availableLists) { list ->
                            TextButton(
                                onClick = { onListSelected(list.listId) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.textButtonColors(contentColor = accent)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(12.dp))
                                    Text(list.listName, style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                        }
                    }
                } else {
                    Text(
                        "No lists created yet.",
                        color = Color.White.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    color = Color.White.copy(alpha = 0.1f)
                )

                // --- SECTION 2: CREATE NEW (Always Visible) ---
                Text(
                    "Create a new list:",
                    color = Color.White.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = newListName,
                    onValueChange = { newListName = it },
                    label = { Text("New List Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = accent,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                        focusedBorderColor = accent,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        cursorColor = accent
                    )
                )

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (newListName.isNotBlank()) {
                            onCreateNewList(newListName)
                            newListName = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = accent),
                    shape = RoundedCornerShape(12.dp),
                    enabled = newListName.isNotBlank()
                ) {
                    Text("Create & Add", color = accent)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White.copy(alpha = 0.7f))
            }
        }
    )
}