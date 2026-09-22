package com.x3phire.hundreddays.domain

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
    val daysLeftCorner: DaysLeftCorner,
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

    data object FutureDay : Rejection {
        override val userMessage: String = "Future days are for looking ahead only. Add notes on or before today."
    }
}

sealed interface CommandResult {
    data class Applied(val event: JournalEvent) : CommandResult
    data class Unchanged(val reason: NoChange) : CommandResult
    data class Rejected(val reason: Rejection) : CommandResult
}
