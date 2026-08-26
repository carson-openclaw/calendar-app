package com.omnipaws.calendar.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.unit.dp
import com.omnipaws.calendar.data.CalendarEvent
import com.omnipaws.calendar.data.EventCategory
import com.omnipaws.calendar.ui.theme.Accent
import com.omnipaws.calendar.ui.theme.Ink
import com.omnipaws.calendar.ui.theme.InkLight
import com.omnipaws.calendar.ui.theme.Muted
import com.omnipaws.calendar.ui.theme.Outline
import com.omnipaws.calendar.ui.theme.PaperSurface
import java.time.LocalDate

@Composable
fun AddEventDialog(
    onDismiss: () -> Unit,
    onConfirm: (CalendarEvent) -> Unit,
    initialDate: LocalDate = LocalDate.now()
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(EventCategory.PERSONAL) }
    var startTime by remember { mutableStateOf("09:00") }
    var endTime by remember { mutableStateOf("10:00") }
    var note by remember { mutableStateOf("") }

    val canSave = title.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PaperSurface,
        titleContentColor = Ink,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "New Event",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Accent,
                        unfocusedBorderColor = Outline,
                        focusedContainerColor = PaperSurface,
                        unfocusedContainerColor = PaperSurface,
                        cursorColor = Accent,
                        focusedLabelColor = Accent,
                        unfocusedLabelColor = Muted
                    )
                )

                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium,
                    color = Muted
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EventCategory.entries.forEach { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = {
                                Text(
                                    text = category.label,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = category.color.copy(alpha = 0.15f),
                                selectedLabelColor = category.color,
                                containerColor = PaperSurface,
                                labelColor = Muted
                            )
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Start") },
                        placeholder = { Text("09:00") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Accent,
                            unfocusedBorderColor = Outline,
                            focusedContainerColor = PaperSurface,
                            unfocusedContainerColor = PaperSurface,
                            cursorColor = Accent,
                            focusedLabelColor = Accent,
                            unfocusedLabelColor = Muted
                        )
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("End") },
                        placeholder = { Text("10:00") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Accent,
                            unfocusedBorderColor = Outline,
                            focusedContainerColor = PaperSurface,
                            unfocusedContainerColor = PaperSurface,
                            cursorColor = Accent,
                            focusedLabelColor = Accent,
                            unfocusedLabelColor = Muted
                        )
                    )
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Accent,
                        unfocusedBorderColor = Outline,
                        focusedContainerColor = PaperSurface,
                        unfocusedContainerColor = PaperSurface,
                        cursorColor = Accent,
                        focusedLabelColor = Accent,
                        unfocusedLabelColor = Muted
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (canSave) {
                        onConfirm(
                            CalendarEvent(
                                title = title.trim(),
                                date = initialDate,
                                startTime = startTime.trim(),
                                endTime = endTime.trim(),
                                category = selectedCategory,
                                note = note.trim()
                            )
                        )
                    }
                },
                enabled = canSave
            ) {
                Text("Save", color = if (canSave) Accent else Muted)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Muted)
            }
        }
    )
}
