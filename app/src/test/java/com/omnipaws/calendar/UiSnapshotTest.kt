package com.omnipaws.calendar

import app.cash.paparazzi.Paparazzi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.omnipaws.calendar.data.CalendarEvent
import com.omnipaws.calendar.data.EventRepository
import com.omnipaws.calendar.ui.components.AddEventDialog
import com.omnipaws.calendar.ui.components.EventCard
import com.omnipaws.calendar.ui.screens.CalendarScreen
import com.omnipaws.calendar.ui.screens.EventDetailScreen
import com.omnipaws.calendar.ui.screens.EventsListScreen
import com.omnipaws.calendar.ui.theme.AuraTheme
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.util.UUID

/**
 * Paparazzi render tests — capture real screenshots of key UI on the JVM (no emulator).
 * Visual QA checkpoint: after each OpenCode chunk, render these and review the PNGs
 * for layout/visual regressions (spacing, gaps, alignment) that the compiler cannot catch.
 *
 * Run with:      ./gradlew :app:recordPaparazziDebug
 * Verify with:   ./gradlew :app:verifyPaparazziDebug
 *
 * NOTE: uses EventRepository.add() only (public API). init() is never called, so
 * tagById() falls back to DefaultTags.first() (sage) for all events — fine for
 * checking layout/spacing, which is the purpose of these snapshots.
 */

class UiSnapshotTest {

    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = app.cash.paparazzi.DeviceConfig.PIXEL_5,
        theme = "android:Theme.Material.Light.NoActionBar",
    )

    /** Populate the in-memory repository (no init -> no filesystem) with demo data. */
    private fun seedDemo() {
        val today = LocalDate.now()
        val workTag = "tag-work"
        val personalTag = "tag-personal"

        // Four same-day events on TODAY at successive hours -> exposes vertical
        // spacing/packing between adjacent schedule blocks in the lower timeline.
        repeat(4) { i ->
            EventRepository.add(
                CalendarEvent(
                    id = UUID.randomUUID().toString(),
                    title = "Blocked meeting ${i + 1}",
                    date = today,
                    startTime = "${9 + i}:00",
                    endTime = "${9 + i + 1}:00",
                    tagId = workTag,
                    location = if (i == 0) "Conference Room B" else "",
                    people = if (i == 0) "Sarah, James" else ""
                )
            )
        }
        // Past-day event (should render greyed).
        EventRepository.add(
            CalendarEvent(
                id = UUID.randomUUID().toString(),
                title = "Yesterday sync",
                date = today.minusDays(1),
                startTime = "10:00",
                endTime = "10:30",
                tagId = workTag
            )
        )
        // Future-day event.
        EventRepository.add(
            CalendarEvent(
                id = UUID.randomUUID().toString(),
                title = "Tomorrow review",
                date = today.plusDays(1),
                startTime = "14:00",
                endTime = "15:00",
                tagId = personalTag
            )
        )
    }

    @Test
    fun calendarScreen_timelineSpacing() {
        seedDemo()
        paparazzi.snapshot {
            AuraTheme {
                CalendarScreen()
            }
        }
    }

    @Test
    fun eventsListScreen_day() {
        seedDemo()
        paparazzi.snapshot {
            AuraTheme {
                EventsListScreen(
                    date = LocalDate.now().toString(),
                    onBack = {},
                    onEventClick = {}
                )
            }
        }
    }

    @Test
    fun addEventDialog_tagCirclesAndDates() {
        seedDemo()
        paparazzi.snapshot {
            AuraTheme {
                AddEventDialog(
                    onDismiss = {},
                    onConfirm = {},
                    initialDate = LocalDate.now()
                )
            }
        }
    }

    @Test
    fun eventDetailScreen() {
        seedDemo()
        val firstId = EventRepository.events.first().id
        paparazzi.snapshot {
            AuraTheme {
                EventDetailScreen(
                    eventId = firstId,
                    onBack = {}
                )
            }
        }
    }

    @Test
    fun pastEventCardIsSingleSurface() {
        // Regression: past cards used to alpha-fade the whole Surface, making the
        // shadow show through as a grey rounded 'border' with a sharp inner box.
        // The card must now be ONE clean opaque surface with no grey ring.
        paparazzi.snapshot {
            AuraTheme {
                androidx.compose.foundation.layout.Box(
                    modifier = androidx.compose.ui.Modifier
                        .fillMaxSize()
                        .background(com.omnipaws.calendar.ui.theme.PaperSurface)
                        .padding(20.dp)
                ) {
                    EventCard(
                        event = CalendarEvent(
                            id = "past-event-1",
                            title = "Morning yoga flow",
                            date = LocalDate.now().minusDays(1),
                            startTime = "07:30",
                            endTime = "08:30",
                            tagId = "tag-work"
                        ),
                        onClick = {},
                        isPast = true,
                        isNow = false
                    )
                }
            }
        }
    }
}
