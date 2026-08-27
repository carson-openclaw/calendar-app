package com.omnipaws.calendar.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.omnipaws.calendar.data.EventRepository
import com.omnipaws.calendar.data.formatEventTime
import com.omnipaws.calendar.ui.components.AddEventDialog
import com.omnipaws.calendar.ui.theme.Accent
import com.omnipaws.calendar.ui.theme.Muted
import com.omnipaws.calendar.ui.theme.Outline
import com.omnipaws.calendar.ui.theme.PaperSurface
import com.omnipaws.calendar.ui.theme.SoftDivider
import java.time.format.DateTimeFormatter

@Composable
fun EventDetailScreen(
    eventId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val event = remember(eventId, EventRepository.events.size) {
        EventRepository.events.firstOrNull { it.id == eventId }
    }

    val tag = remember(event) {
        event?.let { EventRepository.tagById(it.tagId) }
    }

    val tagColor = remember(tag) {
        tag?.let { Color(it.color.toInt()) } ?: Muted
    }

    var showEdit by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            if (event != null) {
                IconButton(onClick = { showEdit = true }) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "Edit",
                        tint = Accent
                    )
                }
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "Delete",
                        tint = Muted
                    )
                }
            }
        }

        if (event == null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Event not found",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Muted
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "This event may have been removed.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Muted.copy(alpha = 0.7f)
                )
            }
        } else {
            val headerFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")

            Column(modifier = Modifier.padding(start = 4.dp)) {
                Text(
                    text = tag?.name ?: "",
                    style = MaterialTheme.typography.labelLarge,
                    color = tagColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = event.title,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Normal
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(SoftDivider)
            ) {}

            Spacer(modifier = Modifier.height(20.dp))

            // Date
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.CalendarToday,
                    contentDescription = null,
                    tint = Muted,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                val dateText = if (event.isMultiDay) {
                    val shortFmt = DateTimeFormatter.ofPattern("EEE d MMM")
                    "${event.date.format(shortFmt)} \u2013 ${event.endDate!!.format(shortFmt)}"
                } else {
                    event.date.format(headerFormatter)
                }
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Time
            if (event.startTime.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = null,
                        tint = Muted,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    val timeRange = buildString {
                        append(formatEventTime(event.startTime))
                        if (event.endTime.isNotBlank() && event.endTime != event.startTime) {
                            append(" \u2013 ")
                            append(formatEventTime(event.endTime))
                        }
                    }
                    Text(
                        text = timeRange,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Location
            if (event.location.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Place,
                        contentDescription = null,
                        tint = Muted,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = event.location,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // People
            if (event.people.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.People,
                        contentDescription = null,
                        tint = Muted,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = event.people,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Note
            if (event.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(tagColor.copy(alpha = 0.06f), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = event.note,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }

    if (showEdit && event != null) {
        AddEventDialog(
            onDismiss = { showEdit = false },
            onConfirm = { updatedEvent ->
                EventRepository.update(updatedEvent)
                showEdit = false
            },
            initialEvent = event
        )
    }

    if (showDeleteConfirm && event != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Event") },
            text = { Text("Are you sure you want to delete \"${event.title}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    EventRepository.delete(event.id)
                    showDeleteConfirm = false
                    onBack()
                }) {
                    Text("Delete", color = Muted)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = Muted)
                }
            },
            containerColor = PaperSurface,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
        )
    }
}
