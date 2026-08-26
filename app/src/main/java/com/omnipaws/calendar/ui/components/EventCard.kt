package com.omnipaws.calendar.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.omnipaws.calendar.data.CalendarEvent
import com.omnipaws.calendar.data.formatEventTime
import com.omnipaws.calendar.ui.theme.CardShadow
import com.omnipaws.calendar.ui.theme.CardSurface
import com.omnipaws.calendar.ui.theme.Muted

@Composable
fun EventCard(
    event: CalendarEvent,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeText = buildString {
        if (event.startTime.isNotBlank()) append(formatEventTime(event.startTime))
        if (event.endTime.isNotBlank() && event.endTime != event.startTime) {
            append(" \u2013 ")
            append(formatEventTime(event.endTime))
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = CardShadow
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = CardSurface,
        tonalElevation = 0.5.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(40.dp)
                    .background(event.category.color, RoundedCornerShape(2.dp))
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                if (timeText.isNotBlank()) {
                    Text(
                        text = timeText,
                        style = MaterialTheme.typography.labelMedium,
                        color = Muted
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (event.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = event.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = Muted,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(event.category.color, CircleShape)
            )
        }
    }
}
