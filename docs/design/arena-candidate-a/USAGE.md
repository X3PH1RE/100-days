# 100 Days: caller's usage

Candidate A. Challenge-centric aggregate behind one store.

## What a caller imports

```kotlin
import com.hundreddays.domain.HundredDaysStore
```

That is the whole entry point. Everything a screen, a widget, or a worker needs hangs off one interface. Read models come back already projected. Room, DataStore, Glance, and WorkManager types never appear in a call site.

```kotlin
interface HundredDaysStore {
    val challenge: Flow<Challenge>
    val grid: Flow<ContributionGridModel>
    suspend fun gridNow(): ContributionGridModel
    fun dayCard(date: LocalDate): Flow<DayCard>
    suspend fun saveEntry(id: EntryId, date: LocalDate, body: EntryBody)
    suspend fun deleteEntry(id: EntryId)
    suspend fun setChallenge(challenge: Challenge)
}
```

Seven members. Four reads, three writes.

## Quickstart

```kotlin
val store: HundredDaysStore = context.hundredDays()   // one DI accessor, app-wide singleton

// In-app grid, already bucketed and laid out.
store.grid.collect { model ->
    model.rows.forEach { row -> render(row.cells) }
}

// One day's entries, with the day's position in the challenge.
store.dayCard(LocalDate(2026, 3, 14)).collect { card -> render(card) }

// Change the challenge. Whole-aggregate replace, so running it twice lands the same state.
store.setChallenge(current.copy(layout = GridLayout.Fixed(ColumnCount.TEN)))
```

`challenge` and `grid` always emit. There is no "not onboarded yet" hole for a caller to branch on. Before the user picks anything, the store serves a 100 day window starting today.

## Call site 1: the in-app grid screen

```kotlin
@Composable
fun GridScreen(store: HundredDaysStore, onOpenDay: (LocalDate) -> Unit) {
    val model by store.grid.collectAsStateWithLifecycle(initialValue = null)
    val grid = model ?: return

    Box {
        Column {
            grid.rows.forEach { row ->
                Row {
                    row.cells.forEach { cell ->
                        when (cell) {
                            is GridCell.Padding -> Spacer(Modifier.size(CellSize))
                            is GridCell.Day -> Box(
                                Modifier
                                    .size(CellSize)
                                    .background(cell.intensity.color())
                                    .alpha(if (cell.phase == DayPhase.FUTURE) 0.4f else 1f)
                                    .clickable { onOpenDay(cell.date) }
                                    .semantics {
                                        contentDescription =
                                            "Day ${cell.dayNumber}, ${cell.count} entries"
                                    }
                            )
                        }
                    }
                }
            }
        }
        grid.badgeCorner?.let { corner ->
            DaysLeftBadge(progress = grid.progress, corner = corner)
        }
    }
}
```

The screen never counts entries, never compares a date to the window, and never decides what "medium" intensity means. `cell.intensity` and `grid.progress` arrive decided. `intensity.color()` is the only thing the UI layer owns here, because a palette is a theme concern and not a domain one.

## Call site 2: the Glance widget

```kotlin
class GridWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val store = context.hundredDays()
        val initial = store.gridNow()

        provideContent {
            val grid by store.grid.collectAsState(initial)
            GridWidgetContent(
                grid = grid,
                onCellClick = { date ->
                    actionStartActivity(Intent(Intent.ACTION_VIEW, DayDeepLink.uri(date).toUri()))
                },
            )
        }
    }
}
```

Same `ContributionGridModel` the app screen renders, from the same flow. No second counting query, no widget-side cache, no stored intensity column. `gridNow()` exists because Glance needs a value before the first composition, not because the widget has a different read model.

The midnight worker shares the same path:

```kotlin
class MidnightRefreshWorker(...) : CoroutineWorker(...) {
    override suspend fun doWork(): Result {
        GridWidget().updateAll(applicationContext)
        return Result.success()
    }
}
```

The worker holds no domain logic. It re-runs `provideGlance`, which re-reads the projection, which recomputes days-left against the new local date.

## Call site 3: the day card, writing an entry

```kotlin
class DayCardViewModel(
    private val store: HundredDaysStore,
    private val date: LocalDate,
) : ViewModel() {

    val card: StateFlow<DayCard?> =
        store.dayCard(date).stateIn(viewModelScope, WhileSubscribed(5_000), null)

    // Minted once when the composer opens, so a double tap on Save upserts the same row.
    var draftId: EntryId by mutableStateOf(EntryId.new())
        private set

    fun save(text: String) {
        val body = EntryBody.of(text) ?: return   // blank input is not an error, it is a no-op
        viewModelScope.launch {
            store.saveEntry(draftId, date, body)
            draftId = EntryId.new()
        }
    }

    fun delete(id: EntryId) = viewModelScope.launch { store.deleteEntry(id) }
}
```

`saveEntry` is an upsert keyed by an id the caller already holds, so the same call edits an existing entry and retries a failed insert. The ViewModel does not refresh the widget after writing. The store does that, once, for every mutation.

Voice capture lands on the same method. Speech recognition produces text, `EntryBody.of` parses it, `saveEntry` persists it. Nothing about the domain surface knows a transcript was involved.

## Call site 4: onboarding and settings

```kotlin
@Composable
fun ChallengeSettingsScreen(store: HundredDaysStore) {
    val challenge by store.challenge.collectAsStateWithLifecycle(initialValue = null)
    val current = challenge ?: return
    val scope = rememberCoroutineScope()

    DateRangePicker(
        start = current.window.start,
        end = current.window.endInclusive,
        onPicked = { start, end ->
            // The only place a raw pair of dates exists. Invalid pairs never become a Challenge.
            val window = ChallengeWindow.of(start, end) ?: return@DateRangePicker showRangeError()
            scope.launch { store.setChallenge(current.copy(window = window)) }
        },
    )

    CornerPicker(
        selected = current.daysLeft,
        onPicked = { badge -> scope.launch { store.setChallenge(current.copy(daysLeft = badge)) } }
    )
}
```

Settings edits a `Challenge` and hands the whole aggregate back. There is no per-field setter to keep in sync with the other fields, and no ordering rule for the caller to respect.

## What no caller ever does

- Map a count to an intensity bucket. `Intensity.forCount` is called once per cell inside the projector.
- Ask whether a date is inside the window. `DayPlacement` and `DayPhase` arrive on the read model.
- Build a start/end pair that is out of order. `ChallengeWindow` cannot hold one.
- Refresh the widget after a write.
- Touch a Room entity, a DAO, a DataStore key, a Glance `GlanceId`, or an ISO date string.
- Branch on "is there a challenge yet".
