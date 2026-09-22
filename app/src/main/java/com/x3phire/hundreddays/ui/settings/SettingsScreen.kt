package com.x3phire.hundreddays.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.x3phire.hundreddays.domain.ChallengeState
import com.x3phire.hundreddays.domain.CommandResult
import com.x3phire.hundreddays.domain.DayKey
import com.x3phire.hundreddays.domain.DaysLeftCorner
import com.x3phire.hundreddays.domain.JournalService
import com.x3phire.hundreddays.domain.SetRange
import com.x3phire.hundreddays.ui.onboarding.DatePickDialog
import java.time.LocalDate
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    service: JournalService,
    challenge: ChallengeState,
    onBack: () -> Unit,
) {
    val active = challenge as? ChallengeState.Active
    val initial = active?.overview?.settings
    val scope = rememberCoroutineScope()

    var start by remember(initial) {
        mutableStateOf(initial?.window?.start?.value ?: LocalDate.now())
    }
    var end by remember(initial) {
        mutableStateOf(initial?.window?.endInclusive?.value ?: LocalDate.now().plusDays(99))
    }
    var columns by remember(initial) {
        mutableIntStateOf(initial?.layout?.columns ?: 10)
    }
    var corner by remember(initial) {
        mutableStateOf(initial?.daysLeftCorner ?: DaysLeftCorner.TOP_END)
    }
    var error by remember { mutableStateOf<String?>(null) }
    var pickingStart by remember { mutableStateOf(false) }
    var pickingEnd by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
            TextButton(onClick = { pickingStart = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Start: $start")
            }
            TextButton(onClick = { pickingEnd = true }, modifier = Modifier.fillMaxWidth()) {
                Text("End: $end")
            }
            Text("Grid columns: $columns", style = MaterialTheme.typography.bodyLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(7, 10, 14).forEach { option ->
                    FilterChip(
                        selected = columns == option,
                        onClick = { columns = option },
                        label = { Text("$option") },
                    )
                }
            }
            Text("Days-left badge", style = MaterialTheme.typography.bodyLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DaysLeftCorner.entries.forEach { option ->
                    FilterChip(
                        selected = corner == option,
                        onClick = { corner = option },
                        label = {
                            Text(
                                when (option) {
                                    DaysLeftCorner.TOP_START -> "Top start"
                                    DaysLeftCorner.TOP_END -> "Top end"
                                },
                            )
                        },
                    )
                }
            }
            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    scope.launch {
                        val result = service.execute(
                            SetRange(
                                start = DayKey.from(start),
                                endInclusive = DayKey.from(end),
                                columns = columns,
                                daysLeftCorner = corner,
                            ),
                        )
                        when (result) {
                            is CommandResult.Rejected -> error = result.reason.userMessage
                            else -> {
                                error = null
                                onBack()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Save")
            }
        }
    }

    if (pickingStart) {
        DatePickDialog(
            initial = start,
            onDismiss = { pickingStart = false },
            onConfirm = {
                start = it
                pickingStart = false
            },
        )
    }
    if (pickingEnd) {
        DatePickDialog(
            initial = end,
            onDismiss = { pickingEnd = false },
            onConfirm = {
                end = it
                pickingEnd = false
            },
        )
    }
}
