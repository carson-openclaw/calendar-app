package com.omnipaws.calendar.data

import android.content.Context
import android.content.Intent
import com.google.api.client.auth.oauth2.BearerToken
import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.auth.oauth2.TokenRequest
import com.google.api.client.auth.oauth2.TokenResponse
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.Event as GoogleEvent
import com.google.api.services.calendar.model.Events
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object GoogleConfig {
    var oauthClientId: String = ""
    val isConfigured: Boolean get() = oauthClientId.isNotBlank()
}

private val JSON_FACTORY = JacksonFactory.getDefaultInstance()

private fun httpTransport(): NetHttpTransport = GoogleNetHttpTransport.newTrustedTransport()

private fun buildGso(): GoogleSignInOptions =
    GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(GoogleConfig.oauthClientId)
        .requestServerAuthCode(GoogleConfig.oauthClientId)
        .requestScopes(Scope(CalendarScopes.CALENDAR))
        .requestEmail()
        .build()

fun buildSignInIntent(context: Context): Intent? {
    if (!GoogleConfig.isConfigured) return null
    return GoogleSignIn.getClient(context, buildGso()).signInIntent
}

fun googleSignInClient(context: Context): GoogleSignInClient =
    GoogleSignIn.getClient(context, buildGso())

private const val TOKEN_URL = "https://oauth2.googleapis.com/token"

fun buildCalendarServiceFromAuthCode(authCode: String): Calendar {
    val tokenRequest = TokenRequest(httpTransport(), JSON_FACTORY, com.google.api.client.http.GenericUrl(TOKEN_URL), "authorization_code")
    tokenRequest.set("code", authCode)
    tokenRequest.set("client_id", GoogleConfig.oauthClientId)
    val tokenResponse: TokenResponse = tokenRequest.execute()

    val credential = Credential.Builder(BearerToken.authorizationHeaderAccessMethod())
        .setTokenServerUrl(com.google.api.client.http.GenericUrl(TOKEN_URL))
        .setTransport(httpTransport())
        .setJsonFactory(JSON_FACTORY)
        .build()
        .setFromTokenResponse(tokenResponse)

    return Calendar.Builder(httpTransport(), JSON_FACTORY, credential)
        .setApplicationName("Aura Calendar")
        .build()
}

data class SyncResult(
    val countSynced: Int,
    val countUpdated: Int,
    val countSkipped: Int
)

suspend fun syncAllEvents(
    repo: EventRepository,
    calendarService: Calendar
): SyncResult {
    val now = Instant.now()
    val timeMin = DateTime(now.toEpochMilli(), 0)
    val futureInstant = now.plus(java.time.Duration.ofDays(730))
    val timeMax = DateTime(futureInstant.toEpochMilli(), 0)

    var pageToken: String? = null
    var synced = 0
    var updated = 0
    var skipped = 0

    do {
        val events: Events = calendarService.events()
            .list("primary")
            .setTimeMin(timeMin)
            .setTimeMax(timeMax)
            .setOrderBy("startTime")
            .setSingleEvents(true)
            .setMaxResults(250)
            .setPageToken(pageToken)
            .execute()

        for (gEvent in events.items.orEmpty()) {
            val summary = gEvent.summary ?: continue
            if (summary.isBlank()) { skipped++; continue }

            val calEvent = mapGoogleEvent(gEvent)
            if (calEvent == null) { skipped++; continue }

            val existing = repo.events.firstOrNull { it.externalId == calEvent.externalId }
            if (existing != null) {
                repo.update(existing.copy(
                    title = calEvent.title,
                    date = calEvent.date,
                    endDate = calEvent.endDate,
                    startTime = calEvent.startTime,
                    endTime = calEvent.endTime,
                    tagId = calEvent.tagId,
                    note = calEvent.note,
                    location = calEvent.location,
                    people = calEvent.people
                ))
                updated++
            } else {
                repo.add(calEvent)
                synced++
            }
        }
        pageToken = events.nextPageToken
    } while (pageToken != null)

    return SyncResult(synced, updated, skipped)
}

private val timeOnlyFormatter = DateTimeFormatter.ofPattern("HH:mm")

private fun mapGoogleEvent(gEvent: GoogleEvent): CalendarEvent? {
    val start = gEvent.start ?: return null
    val end = gEvent.end ?: return null

    val isAllDay = start.date != null

    return if (isAllDay) {
        val startDate = parseGoogleDate(start.date) ?: return null
        val endDate = parseGoogleDate(end.date)
        CalendarEvent(
            title = gEvent.summary.orEmpty(),
            date = startDate,
            endDate = if (endDate != null && endDate != startDate) endDate else null,
            startTime = "",
            endTime = "",
            tagId = mapColorToTag(gEvent.colorId),
            note = gEvent.description.orEmpty(),
            location = gEvent.location.orEmpty(),
            people = gEvent.attendees?.joinToString(", ") {
                it.displayName ?: it.email.orEmpty()
            }.orEmpty(),
            externalId = gEvent.id,
            source = "google"
        )
    } else {
        val startOdt = parseGoogleDateTime(start.dateTime) ?: return null
        val endOdt = parseGoogleDateTime(end.dateTime)
        val startDate = startOdt.toLocalDate()
        val startTime = startOdt.format(timeOnlyFormatter)
        val endDate = endOdt?.toLocalDate()
        val endTime = endOdt?.format(timeOnlyFormatter).orEmpty()

        CalendarEvent(
            title = gEvent.summary.orEmpty(),
            date = startDate,
            endDate = if (endDate != null && endDate != startDate) endDate else null,
            startTime = startTime,
            endTime = endTime,
            tagId = mapColorToTag(gEvent.colorId),
            note = gEvent.description.orEmpty(),
            location = gEvent.location.orEmpty(),
            people = gEvent.attendees?.joinToString(", ") {
                it.displayName ?: it.email.orEmpty()
            }.orEmpty(),
            externalId = gEvent.id,
            source = "google"
        )
    }
}

private fun parseGoogleDate(date: DateTime): LocalDate? {
    return try {
        val rfc = date.toStringRfc3339()
        LocalDate.parse(rfc.substring(0, 10))
    } catch (_: Exception) {
        null
    }
}

private fun parseGoogleDateTime(dateTime: DateTime?): OffsetDateTime? {
    dateTime ?: return null
    return try {
        OffsetDateTime.parse(dateTime.toStringRfc3339())
    } catch (_: Exception) {
        null
    }
}

private fun mapColorToTag(colorId: String?): String {
    if (colorId == null) return "tag-personal"
    return when (colorId) {
        "1", "9" -> "tag-personal"
        "2", "10" -> "tag-work"
        "3", "11" -> "tag-health"
        "4", "12" -> "tag-social"
        "5", "13" -> "tag-personal"
        "6", "14" -> "tag-work"
        "7", "15" -> "tag-health"
        else -> "tag-personal"
    }
}
