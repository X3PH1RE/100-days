package com.x3phire.hundreddays.ui.day

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.x3phire.hundreddays.domain.AddEntry
import com.x3phire.hundreddays.domain.CommandResult
import com.x3phire.hundreddays.domain.DayJournal
import com.x3phire.hundreddays.domain.DayKey
import com.x3phire.hundreddays.domain.DayPhase
import com.x3phire.hundreddays.domain.DeleteEntry
import com.x3phire.hundreddays.domain.EditEntry
import com.x3phire.hundreddays.domain.EntryDraft
import com.x3phire.hundreddays.domain.EntryId
import com.x3phire.hundreddays.domain.EntryRef
import com.x3phire.hundreddays.domain.JournalEntry
import com.x3phire.hundreddays.domain.JournalService
import com.x3phire.hundreddays.domain.RuleBasedTranscriptCleaner
import com.x3phire.hundreddays.domain.TranscriptCleaner
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayScreen(
    day: DayKey,
    service: JournalService,
    onBack: () -> Unit,
    transcriptCleaner: TranscriptCleaner = remember { RuleBasedTranscriptCleaner() },
) {
    val journal by service.observeDay(day).collectAsStateWithLifecycle(
        initialValue = DayJournal.empty(day),
    )
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var draft by remember { mutableStateOf("") }
    var editingId by remember { mutableStateOf<EntryId?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var speechNotice by remember { mutableStateOf<String?>(null) }
    var isListening by remember { mutableStateOf(false) }
    var isCleaning by remember { mutableStateOf(false) }

    val today = remember { DayKey.from(LocalDate.now()) }
    val phase = remember(day, today) { DayPhase.of(day, today) }
    val canWrite = phase != DayPhase.FUTURE
    val titleFormatter = remember { DateTimeFormatter.ofPattern("EEE, MMM d yyyy") }

    val speechRecognizer = remember {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            SpeechRecognizer.createSpeechRecognizer(context)
        } else {
            null
        }
    }

    DisposableEffect(speechRecognizer) {
        onDispose {
            speechRecognizer?.destroy()
        }
    }

    val recognitionIntent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }
    }

    fun startListening() {
        if (speechRecognizer == null) {
            message = "Speech recognition is not available on this device."
            return
        }
        message = null
        speechNotice = "Listening... Speak your journal note"
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isListening = true
            }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                isListening = false
                speechNotice = null
            }
            override fun onError(error: Int) {
                isListening = false
                speechNotice = null
                when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH,
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                        // Non-fatal timeouts when user finishes speaking quietly
                    }
                    SpeechRecognizer.ERROR_AUDIO -> {
                        message = "Audio recording error"
                    }
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
                        message = "Microphone permission required"
                    }
                    else -> {
                        message = "Speech recognition error ($error)"
                    }
                }
            }
            override fun onResults(results: Bundle?) {
                isListening = false
                speechNotice = null
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull()?.trim()
                if (!text.isNullOrEmpty()) {
                    draft = if (draft.isBlank()) text else "${draft.trim()}\n$text"
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val partial = matches?.firstOrNull()
                if (!partial.isNullOrEmpty()) {
                    speechNotice = "Listening: $partial"
                }
            }
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        speechRecognizer.startListening(recognitionIntent)
    }

    fun stopListening() {
        isListening = false
        speechNotice = null
        speechRecognizer?.stopListening()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        if (isGranted) {
            startListening()
        } else {
            message = "Audio recording permission is required for voice journaling."
        }
    }

    fun toggleListening() {
        if (isListening) {
            stopListening()
        } else {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO,
            ) == PackageManager.PERMISSION_GRANTED
            if (hasPermission) {
                startListening()
            } else {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(day.value.format(titleFormatter))
                        Text(
                            text = when (phase) {
                                DayPhase.TODAY -> "Today"
                                DayPhase.PAST -> "Past day"
                                DayPhase.FUTURE -> "Upcoming · read only"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (journal.entries.isEmpty()) {
                Text(
                    text = if (canWrite) "No notes yet. Speak or type one below." else "No notes on this day.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp),
                )
            }
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(journal.entries, key = { it.id.value }) { entry ->
                    EntryRow(
                        entry = entry,
                        canWrite = canWrite,
                        onEdit = {
                            editingId = entry.id
                            draft = entry.text.value
                        },
                        onDelete = {
                            scope.launch {
                                service.execute(DeleteEntry(EntryRef(day, entry.id)))
                            }
                        },
                    )
                }
            }

            if (!canWrite) {
                Text(
                    text = "Future days are for looking ahead. Come back on that date to write.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                        .padding(14.dp),
                )
            } else {
                AnimatedVisibility(
                    visible = speechNotice != null,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error),
                        )
                        Text(
                            text = speechNotice.orEmpty(),
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(onClick = { stopListening() }) {
                            Text("Done", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(if (editingId == null) "New note" else "Edit note")
                    },
                    trailingIcon = {
                        IconButton(onClick = { toggleListening() }) {
                            Icon(
                                imageVector = if (isListening) Icons.Filled.MicOff else Icons.Filled.Mic,
                                contentDescription = if (isListening) "Stop voice input" else "Voice input",
                                tint = if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            )
                        }
                    },
                    minLines = 3,
                    shape = RoundedCornerShape(14.dp),
                )

                message?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Button(
                            onClick = {
                                scope.launch {
                                    val result = if (editingId == null) {
                                        service.execute(
                                            AddEntry(
                                                id = EntryId.new(),
                                                day = day,
                                                draft = EntryDraft(draft),
                                            ),
                                        )
                                    } else {
                                        service.execute(
                                            EditEntry(
                                                target = EntryRef(day, editingId!!),
                                                draft = EntryDraft(draft),
                                            ),
                                        )
                                    }
                                    when (result) {
                                        is CommandResult.Applied, is CommandResult.Unchanged -> {
                                            draft = ""
                                            editingId = null
                                            message = null
                                        }
                                        is CommandResult.Rejected -> {
                                            message = result.reason.userMessage
                                        }
                                    }
                                }
                            },
                        ) {
                            Text(if (editingId == null) "Add note" else "Save")
                        }

                        if (editingId != null) {
                            TextButton(
                                onClick = {
                                    editingId = null
                                    draft = ""
                                    message = null
                                },
                            ) {
                                Text("Cancel")
                            }
                        }
                    }

                    // Format bullets with AI / rule-based cleaner
                    OutlinedButton(
                        onClick = {
                            if (draft.isNotBlank() && !isCleaning) {
                                isCleaning = true
                                scope.launch {
                                    draft = transcriptCleaner.cleanTranscript(draft)
                                    isCleaning = false
                                }
                            }
                        },
                        enabled = draft.isNotBlank() && !isCleaning,
                    ) {
                        if (isCleaning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Formatting...")
                        } else {
                            Icon(
                                Icons.Outlined.AutoAwesome,
                                contentDescription = "Format transcript into clean bullets",
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Format Bullets")
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun EntryRow(
    entry: JournalEntry,
    canWrite: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.7f))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "•  ${entry.text.value}",
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            style = MaterialTheme.typography.bodyLarge,
        )
        if (canWrite) {
            TextButton(onClick = onEdit) { Text("Edit") }
            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.Delete, contentDescription = "Delete")
            }
        }
    }
}
