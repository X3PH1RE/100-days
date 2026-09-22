package com.x3phire.hundreddays.domain

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

interface JournalService {
    fun observeDay(day: DayKey): Flow<DayJournal>
    fun observeChallenge(): Flow<ChallengeState>
    suspend fun execute(command: JournalCommand): CommandResult
}

@OptIn(ExperimentalCoroutinesApi::class)
internal class DefaultJournalService(
    private val journals: JournalStore,
    private val settings: ChallengeSettingsStore,
    private val clock: LocalClock,
    private val invalidator: GridInvalidator,
) : JournalService {
    override fun observeDay(day: DayKey): Flow<DayJournal> = journals.observeDay(day)

    override fun observeChallenge(): Flow<ChallengeState> =
        settings.observe()
            .flatMapLatest { stored ->
                if (stored == null) {
                    flowOf(ChallengeState.NotConfigured)
                } else {
                    journals.observeCounts(stored.window).map { counts ->
                        ChallengeState.Active(
                            ContributionGrid.project(stored, counts, clock.today()),
                        )
                    }
                }
            }
            .distinctUntilChanged()

    override suspend fun execute(command: JournalCommand): CommandResult =
        when (command) {
            is AddEntry -> addEntry(command)
            is EditEntry -> editEntry(command)
            is DeleteEntry -> deleteEntry(command)
            is SetRange -> setRange(command)
        }

    private suspend fun addEntry(command: AddEntry): CommandResult {
        val text = when (val parsed = EntryText.parse(command.draft.text)) {
            EntryTextParse.Blank -> return CommandResult.Rejected(Rejection.BlankEntry)
            is EntryTextParse.TooLong ->
                return CommandResult.Rejected(Rejection.EntryTooLong(parsed.maximumCharacters))
            is EntryTextParse.Ok -> parsed.text
        }
        return journals.apply(
            JournalMutation.Upsert(
                id = command.id,
                day = command.day,
                text = text,
                at = clock.now(),
            ),
        ).toCommandResult()
    }

    private suspend fun editEntry(command: EditEntry): CommandResult {
        val text = when (val parsed = EntryText.parse(command.draft.text)) {
            EntryTextParse.Blank -> return CommandResult.Rejected(Rejection.BlankEntry)
            is EntryTextParse.TooLong ->
                return CommandResult.Rejected(Rejection.EntryTooLong(parsed.maximumCharacters))
            is EntryTextParse.Ok -> parsed.text
        }
        return journals.apply(
            JournalMutation.Edit(
                target = command.target,
                text = text,
                at = clock.now(),
            ),
        ).toCommandResult()
    }

    private suspend fun deleteEntry(command: DeleteEntry): CommandResult =
        journals.apply(JournalMutation.Delete(command.target)).toCommandResult()

    private suspend fun setRange(command: SetRange): CommandResult {
        val window = ChallengeWindow.of(command.start, command.endInclusive)
            ?: return CommandResult.Rejected(Rejection.InvertedRange)
        val layout = GridLayout.of(command.columns)
            ?: return CommandResult.Rejected(Rejection.InvalidColumnCount(command.columns))
        val next = ChallengeSettings(
            window = window,
            layout = layout,
            daysLeftCorner = command.daysLeftCorner,
        )
        val changed = settings.replace(next)
        return if (changed) {
            invalidator.invalidate()
            CommandResult.Applied(JournalEvent.ChallengeSettingsChanged(next))
        } else {
            CommandResult.Unchanged(NoChange.SettingsAlreadyMatch(next))
        }
    }

    private suspend fun StoreMutationResult.toCommandResult(): CommandResult =
        when (this) {
            is StoreMutationResult.Changed -> {
                invalidator.invalidate()
                CommandResult.Applied(event)
            }
            is StoreMutationResult.Unchanged -> CommandResult.Unchanged(reason)
            is StoreMutationResult.Rejected -> CommandResult.Rejected(reason)
        }
}
