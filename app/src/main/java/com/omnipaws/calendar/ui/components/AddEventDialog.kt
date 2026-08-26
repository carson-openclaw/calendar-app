package com.omnipaws.calendar.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Label
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.People
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.omnipaws.calendar.data.CalendarEvent
import com.omnipaws.calendar.data.EventRepository
import com.omnipaws.calendar.ui.theme.Accent
import com.omnipaws.calendar.ui.theme.Ink
import com.omnipaws.calendar.ui.theme.Muted
import com.omnipaws.calendar.ui.theme.Outline
import com.omnipaws.calendar.ui.theme.PaperSurface
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventDialog(
    onDismiss: () -> Unit,
    onConfirm: (CalendarEvent) -> Unit,
    initialDate: LocalDate = LocalDate.now()
) {
    var title by remember { mutableStateOf("") }
    var selectedTagId by remember { mutableStateOf("tag-personal") }
    var startTime by remember { mutableStateOf("09:00") }
    var endTime by remember { mutableStateOf("10:00") }
    var note by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var people by remember { mutableStateOf("") }
    var showManageTags by remember { mutableStateOf(false) }

    val canSave = title.isNotBlank()
    val tags = EventRepository.tags

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PaperSurface,
        titleContentColor = Ink,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "New Event",
                    style = MaterialTheme.typography.headlineSmall
                )
                Icon(
                    imageVector = Icons.Rounded.Label,
                    contentDescription = "Manage tags",
                    tint = Accent,
                    modifier = Modifier
                        .size(22.dp)
                        .clickable { showManageTags = true }
                        .padding(2.dp)
                )
            }
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tag",
                        style = MaterialTheme.typography.labelMedium,
                        color = Muted,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Edit tags",
                        style = MaterialTheme.typography.labelSmall,
                        color = Accent,
                        modifier = Modifier
                            .clickable { showManageTags = true }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tags.forEach { tag ->
                        val isSelected = selectedTagId == tag.id
                        val tagColor = Color(tag.color.toInt())
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedTagId = tag.id },
                            label = {
                                Text(
                                    text = tag.name,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            leadingIcon = if (isSelected) {
                                {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(tagColor, CircleShape)
                                    )
                                }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = tagColor.copy(alpha = 0.15f),
                                selectedLabelColor = tagColor,
                                containerColor = PaperSurface,
                                labelColor = Muted
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Place,
                            contentDescription = null,
                            tint = Muted,
                            modifier = Modifier.size(18.dp)
                        )
                    },
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

                OutlinedTextField(
                    value = people,
                    onValueChange = { people = it },
                    label = { Text("People") },
                    placeholder = { Text("Alice, Bob", color = Muted.copy(alpha = 0.5f)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.People,
                            contentDescription = null,
                            tint = Muted,
                            modifier = Modifier.size(18.dp)
                        )
                    },
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
                                tagId = selectedTagId,
                                note = note.trim(),
                                location = location.trim(),
                                people = people.trim()
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

    if (showManageTags) {
        ModalBottomSheet(
            onDismissRequest = { showManageTags = false },
            containerColor = PaperSurface,
            sheetState = rememberModalBottomSheetState()
        ) {
            ManageTagsSheet(onDismiss = { showManageTags = false })
        }
    }
}
