# Candidate B usage

The caller sees one service, four commands, and two read models. Android framework, Room, and DataStore types do not cross this boundary.

## Compose day card

```kotlin
@Composable
fun DayCard(
    day: DayKey,
    service: JournalService,
) {
    val journal by service.observeDay(day)
        .collectAsState(initial = DayJournal.empty(day))

    Entries(
        entries = journal.entries,
        onAdd = { text ->
            launch {
                when (
                    val result = service.execute(
                        AddEntry(
                            id = EntryId.new(),
                            day = day,
                            draft = EntryDraft(text),
                        ),
                    )
                ) {
                    is CommandResult.Applied -> closeEditor()
                    is CommandResult.Unchanged -> closeEditor()
                    is CommandResult.Rejected -> show(result.reason.userMessage)
                }
            }
        },
        onEdit = { entry, text ->
            launch {
                service.execute(
                    EditEntry(
                        target = EntryRef(day, entry.id),
                        draft = EntryDraft(text),
                    ),
                )
            }
        },
        onDelete = { entry ->
            launch {
                service.execute(DeleteEntry(EntryRef(day, entry.id)))
            }
        },
    )
}
```

`DayJournal` is the complete model for one local date. An entry does not carry a second, potentially conflicting date.

## Compose contribution grid and settings

```kotlin
@Composable
fun ChallengeScreen(service: JournalService) {
    val challenge by service.observeChallenge()
        .collectAsState(initial = ChallengeState.NotConfigured)

    when (val state = challenge) {
        ChallengeState.NotConfigured -> Onboarding()
        is ChallengeState.Active -> {
            val view = state.overview
            ContributionGrid(
                cells = view.cells,
                columns = view.settings.layout.columns,
                showDaysLeftCorner = view.settings.showDaysLeftCorner,
            )
        }
    }
}

suspend fun saveChallenge(
    service: JournalService,
    start: LocalDate,
    end: LocalDate,
    columns: Int,
    showDaysLeftCorner: Boolean,
): CommandResult =
    service.execute(
        SetRange(
            start = DayKey.from(start),
            endInclusive = DayKey.from(end),
            columns = columns,
            showDaysLeftCorner = showDaysLeftCorner,
        ),
    )
```

The command boundary rejects an inverted range or invalid column count. Callers never construct partially valid `ChallengeSettings`.

## Glance widget

```kotlin
suspend fun provideGlance(service: JournalService) {
    val state = service.observeChallenge().first()

    provideContent {
        when (state) {
            ChallengeState.NotConfigured -> SetupPrompt()
            is ChallengeState.Active -> WidgetGrid(
                cells = state.overview.cells,
                onCellClick = { cell ->
                    actionStartActivity(
                        dayCardIntent(day = cell.day),
                    )
                },
            )
        }
    }
}
```

The widget uses the same `ChallengeOverview` as Compose. It remains read-only. Cell counts are queried from journal rows and converted to grid cells without a stored counts table.
