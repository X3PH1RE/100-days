package com.x3phire.hundreddays.data

import com.x3phire.hundreddays.domain.DayJournal
import com.x3phire.hundreddays.domain.DayKey
import com.x3phire.hundreddays.domain.EntryId
import com.x3phire.hundreddays.domain.EntryRef
import com.x3phire.hundreddays.domain.EntryText
import com.x3phire.hundreddays.domain.EntryTextParse
import com.x3phire.hundreddays.domain.JournalEntry
import com.x3phire.hundreddays.domain.JournalEvent
import com.x3phire.hundreddays.domain.JournalMutation
import com.x3phire.hundreddays.domain.JournalStore
import com.x3phire.hundreddays.domain.NoChange
import com.x3phire.hundreddays.domain.Rejection
import com.x3phire.hundreddays.domain.StoreMutationResult
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class RoomJournalStore(
    private val dao: EntryDao,
) : JournalStore {
    override fun observeDay(day: DayKey): Flow<DayJournal> =
        dao.observeByDate(day.value.toString()).map { rows ->
            DayJournal(
                day = day,
                entries = rows.map { it.toDomain() },
            )
        }

    override fun observeCounts(range: ClosedRange<DayKey>): Flow<Map<DayKey, Int>> =
        dao.observeCountsBetween(
            start = range.start.value.toString(),
            end = range.endInclusive.value.toString(),
        ).map { rows ->
            rows.associate { row ->
                DayKey.parse(row.date)!! to row.count
            }
        }

    override suspend fun apply(mutation: JournalMutation): StoreMutationResult =
        when (mutation) {
            is JournalMutation.Upsert -> upsert(mutation)
            is JournalMutation.Edit -> edit(mutation)
            is JournalMutation.Delete -> delete(mutation)
        }

    private suspend fun upsert(mutation: JournalMutation.Upsert): StoreMutationResult {
        val existing = dao.findById(mutation.id.value)
        val target = EntryRef(mutation.day, mutation.id)
        if (existing == null) {
            dao.insert(
                EntryEntity(
                    id = mutation.id.value,
                    date = mutation.day.value.toString(),
                    text = mutation.text.value,
                    createdAt = mutation.at.toEpochMilli(),
                    updatedAt = mutation.at.toEpochMilli(),
                ),
            )
            return StoreMutationResult.Changed(JournalEvent.EntryAdded(target))
        }
        if (existing.date == mutation.day.value.toString() &&
            existing.text == mutation.text.value
        ) {
            return StoreMutationResult.Unchanged(NoChange.EntryAlreadyMatches(target))
        }
        dao.update(
            existing.copy(
                date = mutation.day.value.toString(),
                text = mutation.text.value,
                updatedAt = mutation.at.toEpochMilli(),
            ),
        )
        return StoreMutationResult.Changed(JournalEvent.EntryEdited(target))
    }

    private suspend fun edit(mutation: JournalMutation.Edit): StoreMutationResult {
        val existing = dao.findById(mutation.target.id.value)
            ?: return StoreMutationResult.Rejected(Rejection.EntryNotFound(mutation.target))
        if (existing.date != mutation.target.day.value.toString()) {
            return StoreMutationResult.Rejected(Rejection.EntryNotFound(mutation.target))
        }
        if (existing.text == mutation.text.value) {
            return StoreMutationResult.Unchanged(NoChange.EntryAlreadyMatches(mutation.target))
        }
        dao.update(
            existing.copy(
                text = mutation.text.value,
                updatedAt = mutation.at.toEpochMilli(),
            ),
        )
        return StoreMutationResult.Changed(JournalEvent.EntryEdited(mutation.target))
    }

    private suspend fun delete(mutation: JournalMutation.Delete): StoreMutationResult {
        val existing = dao.findById(mutation.target.id.value)
            ?: return StoreMutationResult.Unchanged(NoChange.EntryAlreadyAbsent(mutation.target))
        if (existing.date != mutation.target.day.value.toString()) {
            return StoreMutationResult.Unchanged(NoChange.EntryAlreadyAbsent(mutation.target))
        }
        dao.deleteById(mutation.target.id.value)
        return StoreMutationResult.Changed(JournalEvent.EntryDeleted(mutation.target))
    }

    private fun EntryEntity.toDomain(): JournalEntry {
        val body = when (val parsed = EntryText.parse(text)) {
            is EntryTextParse.Ok -> parsed.text
            else -> error("Stored entry text failed validation: $id")
        }
        return JournalEntry(
            id = EntryId(id),
            text = body,
            createdAt = Instant.ofEpochMilli(createdAt),
            updatedAt = Instant.ofEpochMilli(updatedAt),
        )
    }
}
