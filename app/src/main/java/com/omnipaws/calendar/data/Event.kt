package com.omnipaws.calendar.data

import androidx.compose.ui.graphics.Color
import com.omnipaws.calendar.ui.theme.CategoryHealth
import com.omnipaws.calendar.ui.theme.CategoryPersonal
import com.omnipaws.calendar.ui.theme.CategorySocial
import com.omnipaws.calendar.ui.theme.CategoryWork
import org.json.JSONObject
import java.time.LocalDate
import java.util.UUID

data class CalendarEvent(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val date: LocalDate,
    val startTime: String = "",
    val endTime: String = "",
    val category: EventCategory = EventCategory.PERSONAL,
    val note: String = ""
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("date", date.toString())
        put("startTime", startTime)
        put("endTime", endTime)
        put("category", category.name)
        put("note", note)
    }

    companion object {
        fun fromJson(json: JSONObject): CalendarEvent = CalendarEvent(
            id = json.getString("id"),
            title = json.getString("title"),
            date = LocalDate.parse(json.getString("date")),
            startTime = json.optString("startTime", ""),
            endTime = json.optString("endTime", ""),
            category = try {
                EventCategory.valueOf(json.getString("category"))
            } catch (_: Exception) {
                EventCategory.PERSONAL
            },
            note = json.optString("note", "")
        )
    }
}

enum class EventCategory(val label: String, val color: Color) {
    PERSONAL("Personal", CategoryPersonal),
    WORK("Work", CategoryWork),
    HEALTH("Health", CategoryHealth),
    SOCIAL("Social", CategorySocial)
}

fun formatEventTime(time: String): String {
    return try {
        val parts = time.split(":")
        val hour = parts[0].toInt()
        val min = parts[1].padStart(2, '0')
        val period = if (hour < 12) "AM" else "PM"
        val displayHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        "$displayHour:$min $period"
    } catch (_: Exception) {
        time
    }
}
