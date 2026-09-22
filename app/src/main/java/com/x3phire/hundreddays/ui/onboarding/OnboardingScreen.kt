package com.x3phire.hundreddays.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.x3phire.hundreddays.domain.CommandResult
import com.x3phire.hundreddays.domain.DayKey
import com.x3phire.hundreddays.domain.DaysLeftCorner
import com.x3phire.hundreddays.domain.JournalService
import com.x3phire.hundreddays.domain.SetRange
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(service: JournalService) {
    val scope = rememberCoroutineScope()
    val today = remember { LocalDate.now() }
    var start by remember { mutableStateOf(today) }
    var end by remember { mutableStateOf(today.plusDays(99)) }
    var error by remember { mutableStateOf<String?>(null) }
    var pickingStart by remember { mutableStateOf(false) }
    var pickingEnd by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("100 Days", style = MaterialTheme.typography.headlineLarge)
        Text(
            "Pick the inclusive start and end of your challenge.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = { pickingStart = true }, modifier = Modifier.fillMaxWidth()) {
            Text("Start: $start")
        }
        TextButton(onClick = { pickingEnd = true }, modifier = Modifier.fillMaxWidth()) {
            Text("End: $end")
        }
        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }
        Spacer(Modifier.weight(1f))
        Button(
            onClick = {
                scope.launch {
                    val result = service.execute(
                        SetRange(
                            start = DayKey.from(start),
                            endInclusive = DayKey.from(end),
                            columns = 10,
                            daysLeftCorner = DaysLeftCorner.TOP_END,
                        ),
                    )
                    error = when (result) {
                        is CommandResult.Rejected -> result.reason.userMessage
                        else -> null
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Start challenge")
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DatePickDialog(
    initial: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit,
) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initial.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = state.selectedDateMillis ?: return@TextButton
                    val picked = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                    onConfirm(picked)
                },
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    ) {
        DatePicker(state = state)
    }
}
