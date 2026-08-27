package com.omnipaws.calendar.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.omnipaws.calendar.ui.theme.Accent
import com.omnipaws.calendar.ui.theme.AccentSoft
import com.omnipaws.calendar.ui.theme.DayText
import com.omnipaws.calendar.ui.theme.Ink
import com.omnipaws.calendar.ui.theme.Muted
import com.omnipaws.calendar.ui.theme.OutlineVariant
import com.omnipaws.calendar.ui.theme.Paper
import com.omnipaws.calendar.ui.theme.PaperSurface
import java.time.YearMonth

private val monthShortLabels = listOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
)

@Composable
fun MonthYearPickerSheet(
    initialMonth: YearMonth,
    onSelect: (YearMonth) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedYear by remember { mutableIntStateOf(initialMonth.year) }
    var selectedMonth by remember { mutableIntStateOf(initialMonth.monthValue) }

    val today = remember { YearMonth.now() }
    val yearListState = rememberLazyListState()

    LaunchedEffect(selectedYear) {
        val idx = selectedYear - 1900
        if (idx in 0..220) {
            yearListState.animateScrollToItem(idx)
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp),
        shape = RoundedCornerShape(20.dp),
        color = PaperSurface,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Select Month & Year",
                style = MaterialTheme.typography.titleMedium,
                color = Muted,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // ── Month Grid (4 × 3) ──
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (row in 0 until 3) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (col in 0 until 4) {
                            val monthVal = row * 4 + col + 1
                            val isSelected = monthVal == selectedMonth
                            val isCurrentMonth = monthVal == today.monthValue &&
                                    selectedYear == today.year

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { selectedMonth = monthVal },
                                shape = RoundedCornerShape(10.dp),
                                color = when {
                                    isSelected -> Accent
                                    isCurrentMonth -> AccentSoft
                                    else -> OutlineVariant.copy(alpha = 0.5f)
                                }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = monthShortLabels[monthVal - 1],
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                                        ),
                                        color = when {
                                            isSelected -> Paper
                                            isCurrentMonth -> Accent
                                            else -> DayText
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Year Selector ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { if (selectedYear > 1900) selectedYear-- }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ChevronLeft,
                        contentDescription = "Previous year",
                        tint = Muted
                    )
                }

                LazyRow(
                    state = yearListState,
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items((1900..2120).toList(), key = { it }) { year ->
                        val isSelected = year == selectedYear
                        val isCurrentYear = year == today.year

                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedYear = year },
                            shape = RoundedCornerShape(8.dp),
                            color = when {
                                isSelected -> Accent
                                isCurrentYear -> AccentSoft
                                else -> OutlineVariant.copy(alpha = 0.3f)
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(52.dp)
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$year",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                                    ),
                                    color = when {
                                        isSelected -> Paper
                                        isCurrentYear -> Accent
                                        else -> Ink
                                    },
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                IconButton(
                    onClick = { if (selectedYear < 2120) selectedYear++ }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = "Next year",
                        tint = Muted
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Confirm ──
            Button(
                onClick = { onSelect(YearMonth.of(selectedYear, selectedMonth)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Accent,
                    contentColor = Paper
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Confirm",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}
