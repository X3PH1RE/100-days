package app.hundreddays.domain

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.flow.Flow

@JvmInline
value class DayKey(val value: LocalDate) : Comparable<DayKey> {
    override fun compareTo(other: DayKey): Int =
        TODO("Compare wrapped local dates")

    companion object {
        fun from(date: LocalDate): DayKey =
            TODO("Wrap the validated local date")
    }
}

@JvmInline
value class EntryId(val value: UUID) {
    companion object {
        fun new(): EntryId =
            TODO("Allocate a stable client-side ID")
    }
}

data class EntryRef(
    val day: DayKey,
    val id: EntryId,
)

@JvmInline
value class EntryText private constructor(val value: String) {
    internal companion object {
        fun parse(raw: String): EntryText = TODO("Trim and validate non-blank length")
    }
}

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
        fun empty(day: DayKey): DayJournal =
            TODO("Create an empty journal for this day")
    }
}

@ConsistentCopyVisibility
data class ChallengeWindow private constructor(
    override val start: DayKey,
    override val endInclusive: DayKey,
) : ClosedRange<DayKey> {
    internal companion object {
        fun validated(start: DayKey, endInclusive: DayKey): ChallengeWindow =
            TODO("Reject start after endInclusive")
    }
}

@JvmInline
value class GridLayout private constructor(
    val columns: Int,
) {
    internal companion object {
        fun validated(columns: Int): GridLayout =
            TODO("Reject non-positive or unsupported column counts")
    }
}

data class ChallengeSettings(
    val window: ChallengeWindow,
    val layout: GridLayout,
    val showDaysLeftCorner: Boolean,
)

@JvmInline
value class EntryCount private constructor(val value: Int) {
    internal companion object {
        fun fromStore(value: Int): EntryCount =
            TODO("Assert the trusted aggregate is non-negative")
    }
}

data class DayIntensity(
    val day: DayKey,
    val count: EntryCount,
)

data class ChallengeOverview(
    val settings: ChallengeSettings,
    val cells: List<DayIntensity>,
)

sealed interface ChallengeState {
    data object NotConfigured : ChallengeState
    data class Active(val overview: ChallengeOverview) : ChallengeState
}

data class EntryDraft(
    val text: String,
)

sealed interface JournalCommand

data class AddEntry(
    val id: EntryId,
    val day: DayKey,
    val draft: EntryDraft,
) : JournalCommand

data class EditEntry(
    val target: EntryRef,
    val draft: EntryDraft,
) : JournalCommand

data class DeleteEntry(
    val target: EntryRef,
) : JournalCommand

data class SetRange(
    val start: DayKey,
    val endInclusive: DayKey,
    val columns: Int,
    val showDaysLeftCorner: Boolean,
) : JournalCommand

sealed interface JournalEvent {
    data class EntryAdded(val target: EntryRef) : JournalEvent
    data class EntryEdited(val target: EntryRef) : JournalEvent
    data class EntryDeleted(val target: EntryRef) : JournalEvent
    data class ChallengeSettingsChanged(val settings: ChallengeSettings) : JournalEvent
}

sealed interface NoChange {
    data class EntryAlreadyMatches(val target: EntryRef) : NoChange
    data class EntryAlreadyAbsent(val target: EntryRef) : NoChange
    data class SettingsAlreadyMatch(val settings: ChallengeSettings) : NoChange
}

sealed interface Rejection {
    val userMessage: String

    data object BlankEntry : Rejection {
        override val userMessage: String = "Write something before saving."
    }

    data class EntryTooLong(val maximumCharacters: Int) : Rejection {
        override val userMessage: String = "This entry is too long."
    }

    data object InvertedRange : Rejection {
        override val userMessage: String = "The end date must not be before the start date."
    }

    data class InvalidColumnCount(val supplied: Int) : Rejection {
        override val userMessage: String = "Choose a supported grid width."
    }

    data class EntryNotFound(val target: EntryRef) : Rejection {
        override val userMessage: String = "That entry no longer exists."
    }

    data class EntryIdConflict(val id: EntryId) : Rejection {
        override val userMessage: String = "That entry could not be saved."
    }
}

sealed interface CommandResult {
    data class Applied(val event: JournalEvent) : CommandResult
    data class Unchanged(val reason: NoChange) : CommandResult
    data class Rejected(val reason: Rejection) : CommandResult
}

interface JournalService {
    fun observeDay(day: DayKey): Flow<DayJournal>
    fun observeChallenge(): Flow<ChallengeState>
    suspend fun execute(command: JournalCommand): CommandResult
}

fun computeGridIntensity(
    range: ClosedRange<DayKey>,
    counts: Map<DayKey, Int>,
): List<DayIntensity> =
    TODO("Emit every day in order, treating absent counts as zero")

internal sealed interface JournalMutation {
    data class Add(
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

internal class DefaultJournalService(
    private val journals: JournalStore,
    private val settings: ChallengeSettingsStore,
    private val clock: Clock,
) : JournalService {
    override fun observeDay(day: DayKey): Flow<DayJournal> =
        TODO("Delegate to the store's typed day read")

    override fun observeChallenge(): Flow<ChallengeState> =
        TODO("Combine settings and grouped row counts, then call computeGridIntensity")

    override suspend fun execute(command: JournalCommand): CommandResult =
        TODO("Validate once, translate to a typed mutation, and return the resulting event")
}
