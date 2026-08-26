package com.omnipaws.calendar.data

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import org.json.JSONArray
import java.io.File
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

object EventRepository {
    private val _events = mutableStateListOf<CalendarEvent>()
    val events: List<CalendarEvent> get() = _events
    private var file: File? = null
    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        file = File(context.filesDir, "events.json")
        load()
        if (_events.isEmpty()) seedDemoEvents()
        initialized = true
    }

    fun add(event: CalendarEvent) {
        _events.add(event)
        save()
    }

    fun update(event: CalendarEvent) {
        val index = _events.indexOfFirst { it.id == event.id }
        if (index >= 0) {
            _events[index] = event
            save()
        }
    }

    fun delete(eventId: String) {
        _events.removeAll { it.id == eventId }
        save()
    }

    fun eventsForDay(date: LocalDate): List<CalendarEvent> =
        _events.filter { it.date == date }.sortedBy { it.startTime }

    fun eventsForMonth(yearMonth: YearMonth): List<CalendarEvent> =
        _events.filter { YearMonth.from(it.date) == yearMonth }.sortedBy { it.date }

    fun eventDaysInMonth(yearMonth: YearMonth): Set<Int> =
        eventsForMonth(yearMonth).map { it.date.dayOfMonth }.toSet()

    fun upcomingEvents(limit: Int = 20): List<CalendarEvent> {
        val today = LocalDate.now()
        return _events
            .filter { !it.date.isBefore(today) }
            .sortedWith(compareBy<CalendarEvent> { it.date }.thenBy { it.startTime })
            .take(limit)
    }

    private fun save() {
        val f = file ?: return
        val jsonArray = JSONArray()
        _events.forEach { jsonArray.put(it.toJson()) }
        f.writeText(jsonArray.toString())
    }

    private fun load() {
        val f = file ?: return
        if (!f.exists()) return
        try {
            val text = f.readText()
            if (text.isBlank()) return
            val jsonArray = JSONArray(text)
            _events.clear()
            for (i in 0 until jsonArray.length()) {
                _events.add(CalendarEvent.fromJson(jsonArray.getJSONObject(i)))
            }
        } catch (_: Exception) {
        }
    }

    private fun seedDemoEvents() {
        val today = LocalDate.now()
        val seed = listOf(
            CalendarEvent(
                id = UUID.randomUUID().toString(),
                title = "Morning yoga flow",
                date = today,
                startTime = "07:30",
                endTime = "08:30",
                category = EventCategory.HEALTH
            ),
            CalendarEvent(
                id = UUID.randomUUID().toString(),
                title = "Design review",
                date = today,
                startTime = "10:00",
                endTime = "11:00",
                category = EventCategory.WORK,
                note = "Review new dashboard mockups"
            ),
            CalendarEvent(
                id = UUID.randomUUID().toString(),
                title = "Lunch with Sarah",
                date = today,
                startTime = "12:30",
                endTime = "13:30",
                category = EventCategory.SOCIAL
            ),
            CalendarEvent(
                id = UUID.randomUUID().toString(),
                title = "Read 30 pages",
                date = today,
                startTime = "20:00",
                endTime = "20:45",
                category = EventCategory.PERSONAL
            ),
            CalendarEvent(
                id = UUID.randomUUID().toString(),
                title = "Team standup",
                date = today.plusDays(1),
                startTime = "09:00",
                endTime = "09:30",
                category = EventCategory.WORK
            ),
            CalendarEvent(
                id = UUID.randomUUID().toString(),
                title = "Evening walk",
                date = today.plusDays(1),
                startTime = "18:00",
                endTime = "19:00",
                category = EventCategory.HEALTH
            ),
            CalendarEvent(
                id = UUID.randomUUID().toString(),
                title = "Grocery shopping",
                date = today.plusDays(2),
                startTime = "17:30",
                endTime = "18:30",
                category = EventCategory.PERSONAL
            ),
            CalendarEvent(
                id = UUID.randomUUID().toString(),
                title = "Dinner reservation",
                date = today.plusDays(2),
                startTime = "19:00",
                endTime = "21:00",
                category = EventCategory.SOCIAL
            ),
        )
        seed.forEach { _events.add(it) }
        save()
    }
}
