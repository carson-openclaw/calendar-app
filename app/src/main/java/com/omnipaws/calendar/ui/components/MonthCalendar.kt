package com.omnipaws.calendar.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.omnipaws.calendar.ui.theme.Accent
import com.omnipaws.calendar.ui.theme.AccentSoft
import com.omnipaws.calendar.ui.theme.DayText
import com.omnipaws.calendar.ui.theme.DayTextMuted
import com.omnipaws.calendar.ui.theme.Paper
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

private val weekdayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

@Composable
fun MonthCalendar(
    yearMonth: YearMonth,
    today: LocalDate,
    selectedDay: LocalDate?,
    eventDays: Set<Int>,
    onDayClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    dayTagColors: Map<Int, Set<Color>> = emptyMap()
) {
    Column(modifier = modifier.fillMaxWidth()) {
        WeekdayHeader()

        MonthGrid(
            yearMonth = yearMonth,
            today = today,
            selectedDay = selectedDay,
            eventDays = eventDays,
            onDayClick = onDayClick,
            dayTagColors = dayTagColors
        )
    }
}

@Composable
private fun WeekdayHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        weekdayLabels.forEach { label ->
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = DayTextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MonthGrid(
    yearMonth: YearMonth,
    today: LocalDate,
    selectedDay: LocalDate?,
    eventDays: Set<Int>,
    onDayClick: (LocalDate) -> Unit,
    dayTagColors: Map<Int, Set<Color>>
) {
    val firstDayOfMonth = yearMonth.atDay(1)
    val daysInMonth = yearMonth.lengthOfMonth()
    val startOffset = (firstDayOfMonth.dayOfWeek.value + 7 - DayOfWeek.MONDAY.value) % 7

    val totalCells = startOffset + daysInMonth
    val rows = (totalCells + 6) / 7

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (row in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col
                    val dayNumber = cellIndex - startOffset + 1
                    val isCurrentMonth = dayNumber in 1..daysInMonth

                    if (isCurrentMonth) {
                        val date = yearMonth.atDay(dayNumber)
                        val isToday = date == today
                        val isSelected = date == selectedDay
                        val hasEvent = dayNumber in eventDays
                        val colors = dayTagColors[dayNumber] ?: emptySet()

                        DayCell(
                            day = dayNumber,
                            isToday = isToday,
                            isSelected = isSelected,
                            hasEvent = hasEvent,
                            tagColors = colors,
                            onClick = { onDayClick(date) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    isToday: Boolean,
    isSelected: Boolean,
    hasEvent: Boolean,
    tagColors: Set<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSelected -> Accent
            isToday -> AccentSoft
            else -> Color.Transparent
        },
        label = "dayBg"
    )

    val textColor = when {
        isSelected -> Paper
        isToday -> Accent
        else -> DayText
    }

    Surface(
        modifier = modifier
            .aspectRatio(1f)
            .then(
                if (isToday && !isSelected) {
                    Modifier.border(
                        BorderStroke(1.5.dp, Accent),
                        CircleShape
                    )
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = backgroundColor,
        contentColor = textColor
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$day",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            if (hasEvent) {
                val displayColors = tagColors.take(3).toList()
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.padding(top = 1.dp)
                ) {
                    displayColors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) Paper else color
                                )
                        )
                    }
                    if (displayColors.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) Paper else Accent
                                )
                        )
                    }
                }
            } else {
                Box(modifier = Modifier.size(4.dp))
            }
        }
    }
}
