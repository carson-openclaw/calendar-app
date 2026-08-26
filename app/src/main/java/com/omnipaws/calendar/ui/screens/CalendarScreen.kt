package com.omnipaws.calendar.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import com.omnipaws.calendar.data.EventRepository
import com.omnipaws.calendar.ui.components.EventCard
import com.omnipaws.calendar.ui.components.MonthCalendar
import com.omnipaws.calendar.ui.components.MonthYearPickerSheet
import com.omnipaws.calendar.ui.theme.Accent
import com.omnipaws.calendar.ui.theme.Muted
import com.omnipaws.calendar.ui.theme.SoftDivider
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import com.omnipaws.calendar.data.CalendarEvent


private const val INITIAL_PAGE = 1200

private data class TimelineDay(
    val date: LocalDate,
    val events: List<CalendarEvent>
)

private fun isEventPast(event: CalendarEvent, today: LocalDate, now: LocalTime): Boolean {
    if (event.date.isBefore(today)) return true
    if (event.date.isAfter(today)) return false
    if (event.endTime.isBlank()) return false
    return try {
        val parts = event.endTime.split(":")
        val endTime = LocalTime.of(parts[0].toInt(), parts[1].toInt())
        endTime < now
    } catch (_: Exception) {
        false
    }
}

private fun isEventNow(event: CalendarEvent, today: LocalDate, now: LocalTime): Boolean {
    if (event.date != today) return false
    if (event.startTime.isBlank()) return false
    return try {
        val startParts = event.startTime.split(":")
        val startTime = LocalTime.of(startParts[0].toInt(), startParts[1].toInt())
        if (event.endTime.isBlank()) {
            startTime <= now
        } else {
            val endParts = event.endTime.split(":")
            val endTime = LocalTime.of(endParts[0].toInt(), endParts[1].toInt())
            startTime <= now && endTime >= now
        }
    } catch (_: Exception) {
        false
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CalendarScreen(
    onDayClick: (LocalDate) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val initialMonth = remember { YearMonth.from(today) }

    val pagerState = rememberPagerState(
        initialPage = INITIAL_PAGE,
        pageCount = { INITIAL_PAGE * 2 }
    )

    val scope = rememberCoroutineScope()

    val currentMonth by remember {
        derivedStateOf {
            initialMonth.plusMonths((pagerState.currentPage - INITIAL_PAGE).toLong())
        }
    }

    val events = EventRepository.events
    val eventDays = remember(currentMonth, events.size) {
        EventRepository.eventDaysInMonth(currentMonth)
    }

    var showPicker by remember { mutableStateOf(false) }

    fun computeDayTagColors(yearMonth: YearMonth): Map<Int, Set<Color>> {
        val monthEvents = EventRepository.eventsForMonth(yearMonth)
        return monthEvents.groupBy { it.date.dayOfMonth }
            .mapValues { (_, dayEvents) ->
                dayEvents.map { Color(EventRepository.tagById(it.tagId).color.toInt()) }.toSet()
            }
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
            onPrevious = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
            onNext = { scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
            onTitleClick = { showPicker = true }
        )

        Spacer(modifier = Modifier.height(20.dp))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            userScrollEnabled = true
        ) { page ->
            val ym = initialMonth.plusMonths((page - INITIAL_PAGE).toLong())
            val ymEventDays = remember(ym, events.size) {
                EventRepository.eventDaysInMonth(ym)
            }
            val ymTagColors = remember(ym, events.size) {
                computeDayTagColors(ym)
            }
            MonthCalendar(
                yearMonth = ym,
                today = today,
                selectedDay = null,
                eventDays = ymEventDays,
                onDayClick = { date -> onDayClick(date) },
                dayTagColors = ymTagColors
            )
        }

        if (showPicker) {
            ModalBottomSheet(
                onDismissRequest = { showPicker = false },
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                MonthYearPickerSheet(
                    initialMonth = currentMonth,
                    onSelect = { selected ->
                        showPicker = false
                        val targetPage =
                            INITIAL_PAGE + ChronoUnit.MONTHS.between(
                                initialMonth,
                                selected
                            ).toInt()
                        scope.launch { pagerState.animateScrollToPage(targetPage) }
                    },
                    onDismiss = { showPicker = false }
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        val now = remember { LocalTime.now() }

        val timelineDays = remember(events.size) {
            val allDates = events.map { it.date }
            val minDate = allDates.minOrNull() ?: today
            val maxDate = allDates.maxOrNull() ?: today
            val startDate = minOf(minDate, today.minusDays(365))
            val endDate = maxOf(maxDate, today.plusDays(365))

            (0..ChronoUnit.DAYS.between(startDate, endDate).toInt()).mapNotNull { offset ->
                val date = startDate.plusDays(offset.toLong())
                val dayEvents = EventRepository.eventsForDay(date)
                if (dayEvents.isNotEmpty()) TimelineDay(date, dayEvents) else null
            }
        }

        val todayLazyIndex = remember(timelineDays) {
            var index = 0
            for (day in timelineDays) {
                if (day.date == today) return@remember index
                index += 1 + day.events.size
            }
            -1
        }

        val lazyListState = rememberLazyListState()

        LaunchedEffect(Unit) {
            if (todayLazyIndex >= 0) {
                lazyListState.scrollToItem(todayLazyIndex)
            }
        }

        val isTodayVisible by remember {
            derivedStateOf {
                todayLazyIndex < 0 || lazyListState.layoutInfo.visibleItemsInfo.any { item ->
                    item.key == "day-${today}"
                }
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (timelineDays.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No events yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Muted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap a day on the calendar to add events",
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted.copy(alpha = 0.7f)
                    )
                }
            } else {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    timelineDays.forEach { day ->
                        stickyHeader(key = "day-${day.date}") {
                            val dayFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM")
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.background)
                            ) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = day.date.format(dayFormatter),
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.Normal
                                        ),
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = "${day.events.size} event${if (day.events.size != 1) "s" else ""}",
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
                            }
                        }

                        items(
                            items = day.events,
                            key = { it.id }
                        ) { event ->
                            EventCard(
                                event = event,
                                onClick = { onDayClick(event.date) },
                                isPast = isEventPast(event, today, now),
                                isNow = isEventNow(event, today, now)
                            )
                        }
                    }
                }
            }

        JumpToTodayButton(
            visible = !isTodayVisible && todayLazyIndex >= 0,
            onClick = {
                scope.launch {
                    lazyListState.animateScrollToItem(todayLazyIndex)
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
        }
    }
}

@Composable
private fun JumpToTodayButton(
    visible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 4 }),
        modifier = modifier
    ) {
        FloatingActionButton(
            onClick = onClick,
            containerColor = Accent,
            contentColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp),
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 4.dp,
                pressedElevation = 6.dp
            )
        ) {
            Text(
                text = "Today",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun MonthHeader(
    yearMonth: YearMonth,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onTitleClick: () -> Unit
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

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable(onClick = onTitleClick)
        ) {
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
                color = Accent,
                textAlign = TextAlign.Center
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
