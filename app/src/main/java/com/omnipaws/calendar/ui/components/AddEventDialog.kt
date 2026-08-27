package com.omnipaws.calendar.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.omnipaws.calendar.data.CalendarEvent
import com.omnipaws.calendar.data.EventRepository
import com.omnipaws.calendar.ui.theme.Accent
import com.omnipaws.calendar.ui.theme.Ink
import com.omnipaws.calendar.ui.theme.Muted
import com.omnipaws.calendar.ui.theme.Outline
import com.omnipaws.calendar.ui.theme.PaperSurface
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventDialog(
    onDismiss: () -> Unit,
    onConfirm: (CalendarEvent) -> Unit,
    initialDate: LocalDate = LocalDate.now()
) {
    var title by remember { mutableStateOf("") }
    var selectedTagId by remember { mutableStateOf("tag-personal") }
    var startDate by remember { mutableStateOf(initialDate) }
    var endDate by remember { mutableStateOf<LocalDate?>(null) }
    var startTime by remember { mutableStateOf("09:00") }
    var endTime by remember { mutableStateOf("10:00") }
    var note by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var people by remember { mutableStateOf("") }
    var isRenaming by remember { mutableStateOf(false) }
    var renameText by remember { mutableStateOf("") }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val canSave = title.isNotBlank()
    val tags = EventRepository.tags

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

                // ── Tag: color circles + pencil icon ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    tags.forEach { tag ->
                        val isSelected = selectedTagId == tag.id
                        val tagColor = Color(tag.color.toInt())
                        val borderColor = if (isSelected) tagColor else Color.Transparent
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .background(tagColor, CircleShape)
                                .border(2.dp, borderColor, CircleShape)
                                .clickable {
                                    selectedTagId = tag.id
                                    isRenaming = false
                                }
                        )
                    }

                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "Rename tag",
                        tint = Muted,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable {
                                val tag = tags.firstOrNull { it.id == selectedTagId }
                                if (tag != null) {
                                    renameText = tag.name
                                    isRenaming = !isRenaming
                                }
                            }
                            .padding(1.dp)
                    )
                }

                val selectedTag = tags.firstOrNull { it.id == selectedTagId }
                if (selectedTag != null) {
                    Text(
                        text = selectedTag.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }

                AnimatedVisibility(visible = isRenaming) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = renameText,
                            onValueChange = { renameText = it },
                            label = { Text("Tag name") },
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
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (renameText.isNotBlank()) {
                                        EventRepository.renameTag(selectedTagId, renameText.trim())
                                        isRenaming = false
                                    }
                                }
                            )
                        )
                        TextButton(
                            onClick = {
                                if (renameText.isNotBlank()) {
                                    EventRepository.renameTag(selectedTagId, renameText.trim())
                                    isRenaming = false
                                }
                            }
                        ) {
                            Text("OK", color = Accent)
                        }
                    }
                }

                val displayFormatter = remember { DateTimeFormatter.ofPattern("EEE, d MMM") }

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
                    DateField(
                        label = "Start date",
                        date = startDate.format(displayFormatter),
                        onClick = { showStartDatePicker = true },
                        modifier = Modifier.weight(1f)
                    )
                    DateField(
                        label = "End date",
                        date = (endDate ?: startDate).format(displayFormatter),
                        onClick = { showEndDatePicker = true },
                        modifier = Modifier.weight(1f)
                    )
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
                        val effectiveEnd = if (endDate == startDate) null else endDate
                        onConfirm(
                            CalendarEvent(
                                title = title.trim(),
                                date = startDate,
                                endDate = effectiveEnd,
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

    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = startDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        startDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        if (endDate != null && endDate!!.isBefore(startDate)) {
                            endDate = startDate
                        }
                    }
                    showStartDatePicker = false
                }) {
                    Text("OK", color = Accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("Cancel", color = Muted)
                }
            },
            shape = RoundedCornerShape(20.dp)
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = Accent,
                    selectedDayContentColor = PaperSurface,
                    todayContentColor = Accent,
                    todayDateBorderColor = Accent
                )
            )
        }
    }

    if (showEndDatePicker) {
        val targetDate = endDate ?: startDate
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = targetDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        endDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    showEndDatePicker = false
                }) {
                    Text("OK", color = Accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("Cancel", color = Muted)
                }
            },
            shape = RoundedCornerShape(20.dp)
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = Accent,
                    selectedDayContentColor = PaperSurface,
                    todayContentColor = Accent,
                    todayDateBorderColor = Accent
                )
            )
        }
    }
}

@Composable
private fun DateField(
    label: String,
    date: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Muted,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Outline, RoundedCornerShape(4.dp))
                .background(PaperSurface, RoundedCornerShape(4.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Text(
                text = date,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.Serif
                ),
                color = Ink
            )
        }
    }
}
