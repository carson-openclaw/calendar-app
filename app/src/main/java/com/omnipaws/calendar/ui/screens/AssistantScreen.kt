package com.omnipaws.calendar.ui.screens

import android.Manifest
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.omnipaws.calendar.data.AssistantAction
import com.omnipaws.calendar.data.EventRepository
import com.omnipaws.calendar.data.interpretCommand
import com.omnipaws.calendar.ui.theme.Accent
import com.omnipaws.calendar.ui.theme.AccentSoft
import com.omnipaws.calendar.ui.theme.Ink
import com.omnipaws.calendar.ui.theme.Muted
import com.omnipaws.calendar.ui.theme.Outline
import com.omnipaws.calendar.ui.theme.Paper
import com.omnipaws.calendar.ui.theme.PaperSurface
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private data class ChatMessage(
    val text: String,
    val isUser: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistantScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val messages = remember { mutableStateListOf<ChatMessage>() }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var triggerListening by remember { mutableStateOf(false) }
    val lazyListState = rememberLazyListState()

    fun send(text: String) {
        if (text.isBlank() || isLoading) return
        val trimmed = text.trim()
        messages.add(ChatMessage(trimmed, isUser = true))
        inputText = ""
        isLoading = true
        scope.launch {
            val action = interpretCommand(trimmed)
            val reply = executeAction(action)
            messages.add(ChatMessage(reply, isUser = false))
            isLoading = false
        }
    }

    fun startListening() {
        val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
        if (recognizer == null) {
            Toast.makeText(context, "Speech recognition not available", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        recognizer.setRecognitionListener(object : android.speech.RecognitionListener {
            override fun onResults(results: android.os.Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull()
                if (!text.isNullOrBlank()) {
                    inputText = text
                    send(text)
                }
                recognizer.destroy()
            }

            override fun onPartialResults(partialResults: android.os.Bundle?) {}
            override fun onReadyForSpeech(params: android.os.Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {
                recognizer.destroy()
            }

            override fun onEvent(eventType: Int, params: android.os.Bundle?) {}
        })
        recognizer.startListening(intent)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            triggerListening = true
        } else {
            Toast.makeText(context, "Microphone permission required for voice input", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(triggerListening) {
        if (triggerListening) {
            triggerListening = false
            startListening()
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            lazyListState.animateScrollToItem(messages.lastIndex)
        }
    }

    LaunchedEffect(Unit) {
        if (messages.isEmpty()) {
            messages.add(
                ChatMessage(
                    text = "Hello! I'm your calendar assistant. You can create, edit, delete, and look up events using natural language.\n\nTry:\n- \"Add team lunch tomorrow at 12pm\"\n- \"Create dentist on August 30\"\n- \"Delete design review\"\n- \"Show events today\"",
                    isUser = false
                )
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // ── Header ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Rounded.ChevronLeft,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "Assistant",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }

        // ── Chat Transcript ──
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages, key = { "${it.text}_${it.isUser}_${messages.indexOf(it)}" }) { msg ->
                ChatBubble(msg)
            }
        }

        if (isLoading) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = Accent
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Thinking...",
                    style = MaterialTheme.typography.bodySmall,
                    color = Muted
                )
            }
        }

        // ── Input Row ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text("Ask your assistant...", color = Muted.copy(alpha = 0.6f))
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = PaperSurface,
                    unfocusedContainerColor = PaperSurface,
                    focusedIndicatorColor = Accent,
                    unfocusedIndicatorColor = Outline,
                    cursorColor = Accent,
                    focusedTextColor = Ink,
                    unfocusedTextColor = Ink
                ),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            // Mic button
            IconButton(
                onClick = {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Mic,
                    contentDescription = "Voice input",
                    tint = Accent,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Send button
            IconButton(
                onClick = { send(inputText) }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Send,
                    contentDescription = "Send",
                    tint = if (inputText.isNotBlank()) Accent else Muted.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun ChatBubble(msg: ChatMessage) {
    val bubbleColor = if (msg.isUser) Accent else PaperSurface
    val textColor = if (msg.isUser) Paper else Ink
    val alignment = if (msg.isUser) Alignment.CenterEnd else Alignment.CenterStart
    val shape = if (msg.isUser) {
        RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Surface(
            shape = shape,
            color = bubbleColor,
            shadowElevation = if (msg.isUser) 0.dp else 2.dp,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Text(
                text = msg.text,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.Serif,
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                ),
                color = textColor,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
    }
}

private suspend fun executeAction(action: AssistantAction): String {
    val formatter = DateTimeFormatter.ofPattern("EEE d MMM")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    return when (action) {
        is AssistantAction.CreateEvent -> {
            val event = com.omnipaws.calendar.data.CalendarEvent(
                id = java.util.UUID.randomUUID().toString(),
                title = action.title,
                date = action.date,
                startTime = action.startTime,
                endTime = action.endTime,
                tagId = action.tagId,
                location = action.location,
                people = action.people,
                note = action.note
            )
            EventRepository.add(event)
            "Added \"${action.title}\" on ${action.date.format(formatter)} at ${action.startTime}"
        }

        is AssistantAction.DeleteEvent -> {
            val matcher = action.titleMatcher.lowercase()
            val found = EventRepository.events.firstOrNull {
                it.title.lowercase().contains(matcher)
            }
            if (found != null) {
                EventRepository.delete(found.id)
                "Deleted \"${found.title}\" on ${found.date.format(formatter)}"
            } else {
                "No event matching \"${action.titleMatcher}\" was found."
            }
        }

        is AssistantAction.MoveEvent -> {
            val matcher = action.titleMatcher.lowercase()
            val found = EventRepository.events.firstOrNull {
                it.title.lowercase().contains(matcher)
            }
            if (found != null) {
                val updated = found.copy(date = action.newDate)
                EventRepository.update(updated)
                "Moved \"${found.title}\" to ${action.newDate.format(formatter)}"
            } else {
                "No event matching \"${action.titleMatcher}\" was found."
            }
        }

        is AssistantAction.ListEvents -> {
            val events = EventRepository.eventsForDay(action.date)
            if (events.isEmpty()) {
                "No events on ${action.date.format(formatter)}."
            } else {
                val list = events.joinToString("\n") { ev ->
                    val time = if (ev.startTime.isNotBlank()) " at ${ev.startTime}" else ""
                    "  - ${ev.title}$time"
                }
                "Events on ${action.date.format(formatter)}:\n$list"
            }
        }

        is AssistantAction.Message -> action.text
    }
}
