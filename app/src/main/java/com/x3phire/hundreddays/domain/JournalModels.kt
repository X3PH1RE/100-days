package com.x3phire.hundreddays.domain

import java.time.Instant

data class EntryRef(
    val day: DayKey,
    val id: EntryId,
)

data class JournalEntry(
    val id: EntryId,
    val text: EntryText,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class DayJournal(
    val day: DayKey,
    val entries: List<JournalEntry>,
) {
    companion object {
        fun empty(day: DayKey): DayJournal = DayJournal(day, emptyList())
    }
}

data class EntryDraft(
    val text: String,
)
