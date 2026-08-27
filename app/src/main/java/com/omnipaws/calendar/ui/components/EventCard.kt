package com.omnipaws.calendar.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.People
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.omnipaws.calendar.data.CalendarEvent
import com.omnipaws.calendar.data.EventRepository
import com.omnipaws.calendar.data.formatEventTime
import com.omnipaws.calendar.ui.theme.Accent
import com.omnipaws.calendar.ui.theme.CardShadow
import com.omnipaws.calendar.ui.theme.CardSurface
import com.omnipaws.calendar.ui.theme.Muted
import com.omnipaws.calendar.ui.theme.MutedLight
import java.time.format.DateTimeFormatter

@Composable
fun EventCard(
    event: CalendarEvent,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPast: Boolean = false,
    isNow: Boolean = false
) {
    val tagColor = Color(EventRepository.tagById(event.tagId).color.toInt())

    val pastTagColor = if (isPast) MutedLight else tagColor

    val timeText = buildString {
        if (event.startTime.isNotBlank()) append(formatEventTime(event.startTime))
        if (event.endTime.isNotBlank() && event.endTime != event.startTime) {
            append(" \u2013 ")
            append(formatEventTime(event.endTime))
        }
    }

    val cardColor by animateColorAsState(
        targetValue = CardSurface,
        animationSpec = tween(300),
        label = "cardColor"
    )

    val contentAlpha = if (isPast) 0.65f else 1f

    val nowBorder = Modifier.then(
        if (isNow) Modifier.border(
            width = 1.2.dp,
            color = Accent.copy(alpha = 0.55f),
            shape = RoundedCornerShape(14.dp)
        ) else Modifier
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(nowBorder)
            .shadow(
                elevation = if (isPast) 1.dp else 2.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = CardShadow
            )
            .alpha(contentAlpha)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = cardColor,
        tonalElevation = if (isPast) 0.dp else 0.5.dp
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
                    .background(pastTagColor, RoundedCornerShape(2.dp))
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                if (event.isMultiDay) {
                    val shortFormatter = DateTimeFormatter.ofPattern("d MMM")
                    val rangeText = "${event.date.format(shortFormatter)} \u2013 ${event.endDate!!.format(shortFormatter)}"
                    Text(
                        text = rangeText,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isPast) MutedLight else Muted
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }
                if (timeText.isNotBlank()) {
                    Text(
                        text = timeText,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isPast) MutedLight else Muted
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = if (isPast) MutedLight else MaterialTheme.colorScheme.onSurface
                )
                if (event.location.isNotBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Place,
                            contentDescription = null,
                            tint = Muted.copy(alpha = 0.7f),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = event.location,
                            style = MaterialTheme.typography.bodySmall,
                            color = Muted.copy(alpha = 0.8f),
                            maxLines = 1
                        )
                    }
                }
                if (event.people.isNotBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.People,
                            contentDescription = null,
                            tint = Muted.copy(alpha = 0.7f),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = event.people,
                            style = MaterialTheme.typography.bodySmall,
                            color = Muted.copy(alpha = 0.8f),
                            maxLines = 1
                        )
                    }
                }
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

            if (isNow) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(
                            Accent.copy(alpha = 0.12f),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Accent, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "NOW",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Accent
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(pastTagColor, CircleShape)
                )
            }
        }
    }
}
