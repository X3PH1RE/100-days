package com.x3phire.hundreddays.ui.day

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayScreen(
    day: DayKey,
    service: JournalService,
    onBack: () -> Unit,
) {
    val journal by service.observeDay(day).collectAsStateWithLifecycle(
        initialValue = DayJournal.empty(day),
    )
    val scope = rememberCoroutineScope()
    var draft by remember { mutableStateOf("") }
    var editingId by remember { mutableStateOf<EntryId?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    val today = remember { DayKey.from(LocalDate.now()) }
    val phase = remember(day, today) { DayPhase.of(day, today) }
    val canWrite = phase != DayPhase.FUTURE
    val titleFormatter = remember { DateTimeFormatter.ofPattern("EEE, MMM d yyyy") }

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
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (journal.entries.isEmpty()) {
                Text(
                    text = if (canWrite) "No notes yet. Capture one below." else "No notes on this day.",
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
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(if (editingId == null) "New note" else "Edit note")
                    },
                    minLines = 3,
                    shape = RoundedCornerShape(14.dp),
                )
                message?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
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
