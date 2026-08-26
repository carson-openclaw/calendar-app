package com.omnipaws.calendar.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.omnipaws.calendar.data.EventRepository
import com.omnipaws.calendar.ui.components.EventCard
import com.omnipaws.calendar.ui.theme.Muted
import com.omnipaws.calendar.ui.theme.SoftDivider
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun EventsListScreen(
    date: String,
    onBack: () -> Unit,
    onEventClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val parsedDate = try {
        LocalDate.parse(date)
    } catch (_: Exception) {
        LocalDate.now()
    }

    val events = remember(date, EventRepository.events.size) {
        EventRepository.eventsForDay(parsedDate)
    }

    val headerFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM")
    val yearFormatter = DateTimeFormatter.ofPattern("yyyy")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Rounded.ChevronLeft,
                contentDescription = "Back to calendar",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Column(modifier = Modifier.padding(start = 4.dp)) {
            Text(
                text = parsedDate.format(headerFormatter),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Normal
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = parsedDate.format(yearFormatter),
                style = MaterialTheme.typography.bodyMedium,
                color = Muted
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(SoftDivider)
        ) {}

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "${events.size} event${if (events.size != 1) "s" else ""}",
            style = MaterialTheme.typography.labelLarge,
            color = Muted,
            modifier = Modifier.padding(start = 4.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (events.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 64.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No events for this day",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Muted
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tap + to add a new event",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted.copy(alpha = 0.7f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(
                    items = events,
                    key = { it.id }
                ) { event ->
                    EventCard(
                        event = event,
                        onClick = { onEventClick(event.id) }
                    )
                }
            }
        }
    }
}
