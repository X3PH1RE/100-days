package com.x3phire.hundreddays.ui.day

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.x3phire.hundreddays.domain.AddEntry
import com.x3phire.hundreddays.domain.CommandResult
import com.x3phire.hundreddays.domain.DayJournal
import com.x3phire.hundreddays.domain.DayKey
import com.x3phire.hundreddays.domain.DeleteEntry
import com.x3phire.hundreddays.domain.EditEntry
import com.x3phire.hundreddays.domain.EntryDraft
import com.x3phire.hundreddays.domain.EntryId
import com.x3phire.hundreddays.domain.EntryRef
import com.x3phire.hundreddays.domain.JournalEntry
import com.x3phire.hundreddays.domain.JournalService
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(day.value.toString()) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(journal.entries, key = { it.id.value }) { entry ->
                    EntryRow(
                        entry = entry,
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

            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(if (editingId == null) "New entry" else "Edit entry")
                },
                minLines = 2,
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
                    Text(if (editingId == null) "Add" else "Save")
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
    }
}

@Composable
private fun EntryRow(
    entry: JournalEntry,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = entry.text.value,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            style = MaterialTheme.typography.bodyLarge,
        )
        TextButton(onClick = onEdit) { Text("Edit") }
        IconButton(onClick = onDelete) {
            Icon(Icons.Outlined.Delete, contentDescription = "Delete")
        }
    }
}
