package com.omnipaws.calendar.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.omnipaws.calendar.ui.theme.CategoryHealth
import com.omnipaws.calendar.ui.theme.CategoryPersonal
import com.omnipaws.calendar.ui.theme.CategorySocial
import com.omnipaws.calendar.ui.theme.CategoryWork
import org.json.JSONObject
import java.time.LocalDate
import java.util.UUID

data class EventTag(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val color: Long
)

val DefaultTags = listOf(
    EventTag(id = "tag-personal", name = "Personal", color = CategoryPersonal.toArgb().toLong()),
    EventTag(id = "tag-work", name = "Work", color = CategoryWork.toArgb().toLong()),
    EventTag(id = "tag-health", name = "Health", color = CategoryHealth.toArgb().toLong()),
    EventTag(id = "tag-social", name = "Social", color = CategorySocial.toArgb().toLong()),
)

data class CalendarEvent(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val date: LocalDate,
    val endDate: LocalDate? = null,
    val startTime: String = "",
    val endTime: String = "",
    val tagId: String = "tag-personal",
    val note: String = "",
    val location: String = "",
    val people: String = "",
    val externalId: String? = null,
    val source: String = "local"
) {
    val isMultiDay: Boolean get() = endDate != null && endDate != date
    val syncedFromGoogle: Boolean get() = source == "google"

    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("date", date.toString())
        if (endDate != null && endDate != date) {
            put("endDate", endDate.toString())
        }
        put("startTime", startTime)
        put("endTime", endTime)
        put("tagId", tagId)
        put("note", note)
        put("location", location)
        put("people", people)
        if (externalId != null) put("externalId", externalId)
        put("source", source)
    }

    companion object {
        fun fromJson(json: JSONObject): CalendarEvent {
            val tagId = if (json.has("tagId")) {
                json.getString("tagId")
            } else {
                val categoryName = json.optString("category", "PERSONAL")
                when (categoryName) {
                    "WORK" -> "tag-work"
                    "HEALTH" -> "tag-health"
                    "SOCIAL" -> "tag-social"
                    else -> "tag-personal"
                }
            }
            val endDate = if (json.has("endDate")) {
                try { LocalDate.parse(json.getString("endDate")) } catch (_: Exception) { null }
            } else {
                val millis = json.optLong("endDateMillis", 0L)
                if (millis > 0) {
                    try { LocalDate.ofEpochDay(millis / 86400000L) } catch (_: Exception) { null }
                } else null
            }
            return CalendarEvent(
                id = json.getString("id"),
                title = json.getString("title"),
                date = LocalDate.parse(json.getString("date")),
                endDate = endDate,
                startTime = json.optString("startTime", ""),
                endTime = json.optString("endTime", ""),
                tagId = tagId,
                note = json.optString("note", ""),
                location = json.optString("location", ""),
                people = json.optString("people", ""),
                externalId = if (json.has("externalId")) json.optString("externalId", null) else null,
                source = json.optString("source", "local")
            )
        }
    }
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
