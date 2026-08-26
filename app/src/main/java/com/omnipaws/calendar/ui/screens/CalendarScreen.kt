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
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omnipaws.calendar.data.EventRepository
import com.omnipaws.calendar.ui.components.EventCard
import com.omnipaws.calendar.ui.components.MonthCalendar
import com.omnipaws.calendar.ui.theme.Accent
import com.omnipaws.calendar.ui.theme.Muted
import com.omnipaws.calendar.ui.theme.SoftDivider
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter


@Composable
fun CalendarScreen(
    onDayClick: (LocalDate) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    var currentMonth by remember { mutableStateOf(YearMonth.from(today)) }

    val events = EventRepository.events
    val eventDays = remember(currentMonth, events.size) {
        EventRepository.eventDaysInMonth(currentMonth)
    }
    val todayEvents = remember(events.size) {
        EventRepository.eventsForDay(today)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        MonthHeader(
            yearMonth = currentMonth,
            onPrevious = { currentMonth = currentMonth.minusMonths(1) },
            onNext = { currentMonth = currentMonth.plusMonths(1) }
        )

        Spacer(modifier = Modifier.height(20.dp))

        MonthCalendar(
            yearMonth = currentMonth,
            today = today,
            selectedDay = null,
            eventDays = eventDays,
            onDayClick = { date -> onDayClick(date) }
        )

        Spacer(modifier = Modifier.height(28.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val dayLabel = today.format(DateTimeFormatter.ofPattern("d MMMM"))
            Text(
                text = "Today, $dayLabel",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Normal
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "${todayEvents.size} event${if (todayEvents.size != 1) "s" else ""}",
                style = MaterialTheme.typography.labelMedium,
                color = Muted
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(SoftDivider)
        ) {}

        Spacer(modifier = Modifier.height(12.dp))

        if (todayEvents.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No events today",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Muted
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tap a day on the calendar to see its events",
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
                    items = todayEvents,
                    key = { it.id }
                ) { event ->
                    EventCard(
                        event = event,
                        onClick = { onDayClick(event.date) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthHeader(
    yearMonth: YearMonth,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    val monthFormatter = DateTimeFormatter.ofPattern("MMMM")
    val yearFormatter = DateTimeFormatter.ofPattern("yyyy")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(
                imageVector = Icons.Rounded.ChevronLeft,
                contentDescription = "Previous month",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = yearMonth.format(monthFormatter),
                style = MaterialTheme.typography.headlineLarge.copy(
                    letterSpacing = (-0.5).sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = yearMonth.format(yearFormatter),
                style = MaterialTheme.typography.bodyMedium,
                color = Accent
            )
        }

        IconButton(onClick = onNext) {
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = "Next month",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
