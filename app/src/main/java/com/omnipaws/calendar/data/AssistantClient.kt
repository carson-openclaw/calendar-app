package com.omnipaws.calendar.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.DayOfWeek
import java.time.temporal.TemporalAdjusters

// ── Configuration ──

object AssistantConfig {
    var apiKey: String = ""
    private const val ENDPOINT = "https://api.openai.com/v1/chat/completions"
    private const val MODEL = "gpt-4o-mini"

    suspend fun callLlm(systemPrompt: String, userMessage: String): String? {
        if (apiKey.isBlank()) return null
        return withContext(Dispatchers.IO) {
            try {
                val messages = JSONArray().apply {
                    put(JSONObject().put("role", "system").put("content", systemPrompt))
                    put(JSONObject().put("role", "user").put("content", userMessage))
                }
                val body = JSONObject().apply {
                    put("model", MODEL)
                    put("messages", messages)
                    put("temperature", 0.3)
                    put("max_tokens", 800)
                }
                val conn = URL(ENDPOINT).openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Authorization", "Bearer $apiKey")
                conn.setRequestProperty("Content-Type", "application/json")
                conn.doOutput = true
                conn.connectTimeout = 30_000
                conn.readTimeout = 30_000
                conn.outputStream.write(body.toString().toByteArray())
                val reader = BufferedReader(InputStreamReader(conn.inputStream))
                val responseBody = reader.readText()
                reader.close()
                conn.disconnect()
                JSONObject(responseBody)
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content")
            } catch (_: Exception) {
                null
            }
        }
    }
}

// ── Sealed action types ──

sealed interface AssistantAction {
    data class CreateEvent(
        val title: String,
        val date: LocalDate,
        val startTime: String = "09:00",
        val endTime: String = "10:00",
        val tagId: String = "tag-personal",
        val location: String = "",
        val people: String = "",
        val note: String = ""
    ) : AssistantAction

    data class DeleteEvent(val titleMatcher: String) : AssistantAction

    data class MoveEvent(
        val titleMatcher: String,
        val newDate: LocalDate
    ) : AssistantAction

    data class ListEvents(val date: LocalDate) : AssistantAction

    data class Message(val text: String) : AssistantAction
}

// ── Local rule-based parser ──

object AssistantParser {

    private val monthNameToNumber = mapOf(
        "january" to 1, "february" to 2, "march" to 3, "april" to 4,
        "may" to 5, "june" to 6, "july" to 7, "august" to 8,
        "september" to 9, "october" to 10, "november" to 11, "december" to 12,
        "jan" to 1, "feb" to 2, "mar" to 3, "apr" to 4,
        "jun" to 6, "jul" to 7, "aug" to 8, "sep" to 9, "oct" to 10, "nov" to 11, "dec" to 12
    )

    fun parse(rawText: String): AssistantAction {
        val text = rawText.trim()
        val lower = text.lowercase()

        // ── Delete / Remove ──
        if (lower.startsWith("delete ") || lower.startsWith("remove ")) {
            val matcher = text.removePrefix("delete ").removePrefix("remove ")
                .removePrefix("the event ").removePrefix("event ")
                .trim().trim('"', '\'', '.', '!')
            return AssistantAction.DeleteEvent(matcher)
        }

        // ── Move ──
        if (lower.startsWith("move ")) {
            val afterMove = text.removePrefix("move ").removePrefix("the event ").trim()
            val toIdx = afterMove.lowercase().indexOf(" to ")
            if (toIdx > 0) {
                val titleMatcher = afterMove.substring(0, toIdx).trim().trim('"', '\'')
                val dateStr = afterMove.substring(toIdx + 4).trim().trim('.')
                val date = parseDate(dateStr) ?: LocalDate.now()
                return AssistantAction.MoveEvent(titleMatcher, date)
            }
        }

        // ── List / Show ──
        if (lower.startsWith("list ") || lower.startsWith("show ") ||
            lower.startsWith("what ") || lower.startsWith("display ")
        ) {
            val date = parseDateFromText(lower) ?: LocalDate.now()
            return AssistantAction.ListEvents(date)
        }

        // ── Create ──
        if (lower.startsWith("add ") || lower.startsWith("create ") ||
            lower.startsWith("schedule ") || lower.startsWith("set up ") ||
            lower.startsWith("book ")
        ) {
            val content = text.removePrefix("add ").removePrefix("create ")
                .removePrefix("schedule ").removePrefix("set up ")
                .removePrefix("book ").trim()

            val title = extractTitle(content)
            val date = parseDateFromText(content.lowercase()) ?: LocalDate.now()
            val start = extractTime(content, "at") ?: extractTime(content, "from") ?: "09:00"
            val end = extractTime(content, "to") ?: incrementHour(start)
            val location = extractAfter(content.lowercase(), listOf("in ", "location ")) ?: ""
            val people = extractAfter(content.lowercase(), listOf("with ")) ?: ""
            val note = extractAfter(content.lowercase(), listOf("note ", "notes ")) ?: ""

            return AssistantAction.CreateEvent(
                title = title,
                date = date,
                startTime = start,
                endTime = end,
                location = location,
                people = people,
                note = note
            )
        }

        // ── Fallback: not matched ──
        return AssistantAction.Message(text)
    }

    private fun extractTitle(content: String): String {
        var result = content

        // Strip date phrases
        for (pattern in listOf(
            "tomorrow", "today", "next week", "next month",
            "on monday", "on tuesday", "on wednesday", "on thursday",
            "on friday", "on saturday", "on sunday",
            "on \\w+ \\d{1,2}", "on \\w+ \\d{1,2}(st|nd|rd|th)",
            "\\d{1,2}/\\d{1,2}", "\\d{4}-\\d{2}-\\d{2}",
            "\\w+ \\d{1,2}(st|nd|rd|th)", "\\w+ \\d{1,2}"
        )) {
            result = result.replace(Regex(pattern, RegexOption.IGNORE_CASE), "")
        }

        // Strip time phrases
        result = result.replace(Regex("\\d{1,2}:\\d{2}\\s*(am|pm)?", RegexOption.IGNORE_CASE), "")
        result = result.replace(Regex("\\d{1,2}\\s*(am|pm)", RegexOption.IGNORE_CASE), "")
        result = result.replace(Regex("(at|from|to|with|in|note|notes|location)\\s+.*", RegexOption.IGNORE_CASE), "")

        result = result.trim().trim(',', '.', '!', '?', '-', ':')
        return result.ifBlank { "Untitled event" }
    }

    private fun parseDateFromText(text: String): LocalDate? {
        val lower = text.lowercase()
        val today = LocalDate.now()

        if (lower.contains("today")) return today
        if (lower.contains("tomorrow")) return today.plusDays(1)
        if (lower.contains("day after tomorrow")) return today.plusDays(2)
        if (lower.contains("next week")) return today.plusWeeks(1)
        if (lower.contains("next month")) return today.plusMonths(1)

        // "next monday", "next friday", etc.
        val dayNames = mapOf(
            "monday" to 1, "tuesday" to 2, "wednesday" to 3, "thursday" to 4,
            "friday" to 5, "saturday" to 6, "sunday" to 7
        )
        for ((name, dow) in dayNames) {
            if (lower.contains("next $name") || lower.contains("this $name")) {
                var target = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.of(dow)))
                if (lower.contains("next") && !target.isAfter(today)) {
                    target = target.plusWeeks(1)
                }
                return target
            }
        }

        // "january 15", "aug 3", "1 january", "15 aug"
        val match = Regex(
            "(\\w+)\\s+(\\d{1,2})(st|nd|rd|th)?|(\\d{1,2})(st|nd|rd|th)?\\s+(\\w+)"
        ).find(lower)
        if (match != null) {
            val groups = match.groupValues
            val monthStr = groups[1].ifBlank { groups[6] }
            val dayStr = groups[2].ifBlank { groups[5] }
            val month = monthNameToNumber[monthStr.removeSuffix("st").removeSuffix("nd")
                .removeSuffix("rd").removeSuffix("th")]
            val day = dayStr.removeSuffix("st").removeSuffix("nd")
                .removeSuffix("rd").removeSuffix("th").toIntOrNull()
            if (month != null && day != null && day in 1..31) {
                return try {
                    LocalDate.of(today.year, month, day)
                } catch (_: Exception) {
                    try {
                        LocalDate.of(today.year + 1, month, day)
                    } catch (_: Exception) {
                        null
                    }
                }
            }
        }

        // ISO date "2025-03-15" or slash "03/15"
        val isoMatch = Regex("(\\d{4})-(\\d{2})-(\\d{2})").find(lower)
        if (isoMatch != null) {
            return try {
                LocalDate.parse(isoMatch.value)
            } catch (_: Exception) { null }
        }

        val slashMatch = Regex("(\\d{1,2})/(\\d{1,2})").find(lower)
        if (slashMatch != null) {
            val m = slashMatch.groupValues[1].toIntOrNull()
            val d = slashMatch.groupValues[2].toIntOrNull()
            if (m != null && d != null) {
                return try { LocalDate.of(today.year, m, d) } catch (_: Exception) { null }
            }
        }

        return null
    }

    private fun parseDate(dateStr: String): LocalDate? {
        return parseDateFromText(dateStr)
    }

    private fun extractTime(text: String, keyword: String): String? {
        val lower = text.lowercase()
        val idx = lower.indexOf("$keyword ")
        if (idx < 0) return null
        val after = text.substring(idx + keyword.length + 1)

        // "3pm", "3:30pm", "15:30", "3:30 pm"
        val timeMatch = Regex("(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?", RegexOption.IGNORE_CASE)
            .find(after) ?: return null

        var hour = timeMatch.groupValues[1].toIntOrNull() ?: return null
        val minute = timeMatch.groupValues[2].toIntOrNull() ?: 0
        val period = timeMatch.groupValues[3].lowercase()

        if (period == "pm" && hour < 12) hour += 12
        if (period == "am" && hour == 12) hour = 0
        if (period.isEmpty() && hour < 8) hour += 12 // assume PM for ambiguous small hours

        return "%02d:%02d".format(hour.coerceIn(0, 23), minute.coerceIn(0, 59))
    }

    private fun incrementHour(time: String): String {
        return try {
            val parts = time.split(":")
            val h = (parts[0].toInt() + 1) % 24
            "%02d:%02d".format(h, parts[1].toInt())
        } catch (_: Exception) {
            "10:00"
        }
    }

    private fun extractAfter(text: String, keywords: List<String>): String? {
        for (kw in keywords) {
            val idx = text.indexOf(kw)
            if (idx >= 0) {
                val after = text.substring(idx + kw.length).trim()
                val end = Regex("(\\d{1,2}:\\d{2}|\\d{1,2}\\s*(am|pm)|at |to |note |notes |location )", RegexOption.IGNORE_CASE)
                    .find(after)
                val result = if (end != null) after.substring(0, end.range.first).trim() else after
                if (result.isNotBlank()) return result.trim(',', '.', '!')
            }
        }
        return null
    }
}

// ── Main entry point ──

suspend fun interpretCommand(rawText: String): AssistantAction {
    // Try local parser first
    val action = AssistantParser.parse(rawText)
    if (action !is AssistantAction.Message) return action

    // Fall back to LLM if API key is available
    if (AssistantConfig.apiKey.isBlank()) {
        return AssistantAction.Message(
            "I can help you manage your calendar. Try commands like:\n\n" +
                "- \"Add team lunch tomorrow at 12pm\"\n" +
                "- \"Create dentist appointment on August 30 at 10am\"\n" +
                "- \"Delete design review\"\n" +
                "- \"Move grocery shopping to Friday\"\n" +
                "- \"Show events today\"\n\n" +
                "Configure an API key in Settings for AI-powered natural language."
        )
    }

    val systemPrompt = """You are a calendar assistant. Parse the user's command into a JSON action.
Return ONLY valid JSON with no markdown. Supported actions:

{"action":"create","title":"...","date":"YYYY-MM-DD","startTime":"HH:MM","endTime":"HH:MM","location":"...","people":"...","note":"..."}
{"action":"delete","matcher":"..."}
{"action":"move","matcher":"...","newDate":"YYYY-MM-DD"}
{"action":"list","date":"YYYY-MM-DD"}
{"action":"message","text":"..."}

Use today's date for unspecified dates: ${LocalDate.now()}
For times, default start 09:00 end 10:00 if not specified.
For fields not provided, use empty string."""

    val response = AssistantConfig.callLlm(systemPrompt, rawText)
        ?: return AssistantAction.Message("The AI service is temporarily unavailable. Please try again.")

    return try {
        val json = JSONObject(response.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim())
        when (json.optString("action")) {
            "create" -> AssistantAction.CreateEvent(
                title = json.optString("title", "Untitled"),
                date = LocalDate.parse(json.optString("date", LocalDate.now().toString())),
                startTime = json.optString("startTime", "09:00"),
                endTime = json.optString("endTime", "10:00"),
                location = json.optString("location", ""),
                people = json.optString("people", ""),
                note = json.optString("note", "")
            )
            "delete" -> AssistantAction.DeleteEvent(json.optString("matcher", ""))
            "move" -> AssistantAction.MoveEvent(
                titleMatcher = json.optString("matcher", ""),
                newDate = LocalDate.parse(json.optString("newDate", LocalDate.now().toString()))
            )
            "list" -> AssistantAction.ListEvents(
                LocalDate.parse(json.optString("date", LocalDate.now().toString()))
            )
            else -> AssistantAction.Message(json.optString("text", "I didn't understand that."))
        }
    } catch (_: Exception) {
        AssistantAction.Message("I couldn't parse that. Please try rephrasing.")
    }
}
