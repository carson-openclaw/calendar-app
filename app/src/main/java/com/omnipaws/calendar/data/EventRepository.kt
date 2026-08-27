package com.omnipaws.calendar.data

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID

object EventRepository {
    private val _events = mutableStateListOf<CalendarEvent>()
    val events: List<CalendarEvent> get() = _events

    private val _tags = mutableStateListOf<EventTag>()
    val tags: List<EventTag> get() = _tags

    private var eventFile: File? = null
    private var tagFile: File? = null
    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        eventFile = File(context.filesDir, "events.json")
        tagFile = File(context.filesDir, "tags.json")
        loadTags()
        if (_tags.isEmpty()) {
            _tags.addAll(DefaultTags)
            saveTags()
        }
        loadEvents()
        if (_events.isEmpty()) seedDemoEvents()
        initialized = true
    }

    // ── Tag CRUD ──

    fun addTag(name: String, color: Long): EventTag {
        val tag = EventTag(name = name, color = color)
        _tags.add(tag)
        saveTags()
        return tag
    }

    fun updateTag(id: String, name: String, color: Long) {
        val index = _tags.indexOfFirst { it.id == id }
        if (index >= 0) {
            _tags[index] = _tags[index].copy(name = name, color = color)
            saveTags()
        }
    }

    fun renameTag(id: String, newName: String) {
        val index = _tags.indexOfFirst { it.id == id }
        if (index >= 0) {
            _tags[index] = _tags[index].copy(name = newName)
            saveTags()
        }
    }

    fun deleteTag(id: String) {
        val fallbackTagId = DefaultTags.firstOrNull { it.id != id }?.id ?: "tag-personal"
        _events.forEachIndexed { index, event ->
            if (event.tagId == id) {
                _events[index] = event.copy(tagId = fallbackTagId)
            }
        }
        _tags.removeAll { it.id == id }
        saveTags()
        saveEvents()
    }

    fun tagById(id: String): EventTag =
        _tags.firstOrNull { it.id == id } ?: DefaultTags.first()

    // ── Event CRUD ──

    fun add(event: CalendarEvent) {
        _events.add(event)
        saveEvents()
    }

    fun update(event: CalendarEvent) {
        val index = _events.indexOfFirst { it.id == event.id }
        if (index >= 0) {
            _events[index] = event
            saveEvents()
        }
    }

    fun delete(eventId: String) {
        _events.removeAll { it.id == eventId }
        saveEvents()
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

    // ── Persistence ──

    private fun saveEvents() {
        val f = eventFile ?: return
        val jsonArray = JSONArray()
        _events.forEach { jsonArray.put(it.toJson()) }
        f.writeText(jsonArray.toString())
    }

    private fun loadEvents() {
        val f = eventFile ?: return
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

    private fun saveTags() {
        val f = tagFile ?: return
        val jsonArray = JSONArray()
        _tags.forEach { tag ->
            jsonArray.put(JSONObject().apply {
                put("id", tag.id)
                put("name", tag.name)
                put("color", tag.color)
            })
        }
        f.writeText(jsonArray.toString())
    }

    private fun loadTags() {
        val f = tagFile ?: return
        if (!f.exists()) return
        try {
            val text = f.readText()
            if (text.isBlank()) return
            val jsonArray = JSONArray(text)
            _tags.clear()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                _tags.add(
                    EventTag(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        color = obj.getLong("color")
                    )
                )
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
                tagId = "tag-health"
            ),
            CalendarEvent(
                id = UUID.randomUUID().toString(),
                title = "Design review",
                date = today,
                startTime = "10:00",
                endTime = "11:00",
                tagId = "tag-work",
                note = "Review new dashboard mockups",
                location = "Conference Room B",
                people = "Sarah, James"
            ),
            CalendarEvent(
                id = UUID.randomUUID().toString(),
                title = "Lunch with Sarah",
                date = today,
                startTime = "12:30",
                endTime = "13:30",
                tagId = "tag-social",
                location = "Cafe Lumiere",
                people = "Sarah"
            ),
            CalendarEvent(
                id = UUID.randomUUID().toString(),
                title = "Read 30 pages",
                date = today,
                startTime = "20:00",
                endTime = "20:45",
                tagId = "tag-personal"
            ),
            CalendarEvent(
                id = UUID.randomUUID().toString(),
                title = "Team standup",
                date = today.plusDays(1),
                startTime = "09:00",
                endTime = "09:30",
                tagId = "tag-work",
                location = "Zoom",
                people = "Design team"
            ),
            CalendarEvent(
                id = UUID.randomUUID().toString(),
                title = "Evening walk",
                date = today.plusDays(1),
                startTime = "18:00",
                endTime = "19:00",
                tagId = "tag-health"
            ),
            CalendarEvent(
                id = UUID.randomUUID().toString(),
                title = "Grocery shopping",
                date = today.plusDays(2),
                startTime = "17:30",
                endTime = "18:30",
                tagId = "tag-personal"
            ),
            CalendarEvent(
                id = UUID.randomUUID().toString(),
                title = "Dinner reservation",
                date = today.plusDays(2),
                startTime = "19:00",
                endTime = "21:00",
                tagId = "tag-social",
                location = "The Olive Garden",
                people = "Alice, Bob, Charlie"
            ),
        )
        seed.forEach { _events.add(it) }
        saveEvents()
    }
}
