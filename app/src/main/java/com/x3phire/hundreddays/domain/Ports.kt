package com.x3phire.hundreddays.domain

import java.time.Instant
import kotlinx.coroutines.flow.Flow

interface LocalClock {
    fun today(): DayKey
    fun now(): Instant
}

interface GridInvalidator {
    suspend fun invalidate()
}

internal sealed interface JournalMutation {
    data class Upsert(
        val id: EntryId,
        val day: DayKey,
        val text: EntryText,
        val at: Instant,
    ) : JournalMutation

    data class Edit(
        val target: EntryRef,
        val text: EntryText,
        val at: Instant,
    ) : JournalMutation

    data class Delete(
        val target: EntryRef,
    ) : JournalMutation
}

internal sealed interface StoreMutationResult {
    data class Changed(val event: JournalEvent) : StoreMutationResult
    data class Unchanged(val reason: NoChange) : StoreMutationResult
    data class Rejected(val reason: Rejection) : StoreMutationResult
}

internal interface JournalStore {
    fun observeDay(day: DayKey): Flow<DayJournal>
    fun observeCounts(range: ClosedRange<DayKey>): Flow<Map<DayKey, Int>>
    suspend fun apply(mutation: JournalMutation): StoreMutationResult
}

internal interface ChallengeSettingsStore {
    fun observe(): Flow<ChallengeSettings?>
    suspend fun replace(settings: ChallengeSettings): Boolean
}
