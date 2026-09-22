# Module map

Candidate A. Three Gradle modules. `:app` never sees a storage or framework type from the data layer, and `:core:domain` never sees Android.

```
:app  ──────────────▶  :core:domain  ◀──────────────  :core:data
(Compose, Glance,       (pure Kotlin)                 (Room, DataStore,
 Nav, WorkManager,                                     Glance updateAll,
 SpeechRecognizer,                                     system clock)
 DI graph)

:app ──▶ :core:data  only inside app/di/, to construct the port implementations.
```

`:core:data` depends on `:core:domain` and implements its ports. `:core:domain` depends on nothing but kotlinx-coroutines and kotlinx-datetime, so the projector runs on the JVM in a plain unit test.

## :core:domain

Owns the domain and every decision derived from it.

The aggregate is `Challenge`, holding the window, the layout, and the days-left preference. `ChallengeWindow` owns all date arithmetic, including `progressOn`, which is the single source of truth for where the run stands. `JournalEntry` is the child fact, keyed by local date and carrying no challenge reference.

`ContributionGrid.project` is the one pure projector. It turns a `Challenge`, a `DayCounts`, and today's date into the `ContributionGridModel` that both the in-app grid and the widget render. `Intensity.forCount` lives here and is called only from the projector and the day-card projection, so no bucket threshold exists anywhere else.

`HundredDaysStore` is the public surface: four reads, three writes. `DefaultHundredDaysStore` is internal and reached through a same-named factory function. It owns flow composition, the rule that counts always follow the current window, substitution of the first-run default challenge, and widget invalidation after every write.

Ports are declared here and implemented in `:core:data`: `ChallengeSettings`, `JournalStore`, `LocalClock`, `GridInvalidator`.

`DayDeepLink` lives here because the widget builds the link and the nav graph parses it. Two consumers of one format means the format gets one home.

## :core:data

Owns persistence mechanics and nothing else.

Room holds `EntryRow` with a `TEXT` primary key, an ISO local-date column, and an index on that column. The count query is `SELECT date, COUNT(*) FROM entries WHERE date BETWEEN :start AND :end GROUP BY date`, which is the grid's dominant read expressed as a query rather than a fold over loaded rows. Writes go through `INSERT ... ON CONFLICT(id) DO UPDATE SET body, updatedAt`, so an edit preserves `createdAt` and a repeated save converges.

DataStore holds the serialized `Challenge`. Its mapper is the boundary parse. A stored value that no longer deserializes reads as absent, which the store turns into the first-run default rather than a crash.

`SystemClock` implements `LocalClock`. Its `days()` flow emits on `ACTION_TIME_CHANGED`, `ACTION_TIMEZONE_CHANGED`, and at the next local midnight.

`GlanceInvalidator` implements `GridInvalidator` with `GridWidget().updateAll(context)`.

Row types, DAOs, DataStore keys, and serializers are `internal`. The module exports only the four port implementations.

## :app

Thin shell. Compose screens, ViewModels that only hold `stateIn` scope and a draft id, the nav graph, the Glance widget, the midnight `CoroutineWorker`, speech capture, and the DI graph.

No file in `:app` computes an intensity, compares a date to the window, or refreshes the widget after a write. The widget and the app screen render the same `ContributionGridModel` from the same flow.

## Hop counts for the dominant paths

| Path | Hops | Trace |
| --- | --- | --- |
| Grid counts | 3 | `GridScreen` reads `store.grid`, `DefaultHundredDaysStore` combines, `JournalStore.counts` runs the group-by |
| Widget grid | 3 | `GridWidget.provideGlance`, `store.grid` / `gridNow()`, same count query |
| Day entries | 3 | `DayCardViewModel`, `store.dayCard`, `JournalStore.entriesOn` |
| Save an entry | 3 | `DayCardViewModel.save`, `store.saveEntry`, `JournalStore.upsert` |
| Settings replace | 3 | `ChallengeSettingsScreen`, `store.setChallenge`, `ChallengeSettings.replace` |
| Midnight refresh | 3 | `MidnightRefreshWorker` calls `updateAll`, Glance re-runs `provideGlance`, `store.grid` re-projects against the new date |

Three files answers every "where does this number come from" question. The projector is called from one place, so tracing a cell's colour stops at `ContributionGrid.project`.

## Shared mutable state

| State | Writers | Readers |
| --- | --- | --- |
| Room `entries` | `DefaultHundredDaysStore` only | grid projection, day card |
| DataStore challenge | `DefaultHundredDaysStore` only | grid projection, settings |
| Glance widget state | nothing writes domain state | widget renders the projection |

One writer per store, reached through one object. The widget and the worker read and invalidate, they never write, so there is no counts column to dual-write and no merge policy to get wrong. Nothing here needs a lock.

## Structural guards

A Konsist test in `:app` fails the build when a file outside `app/di/` references `ChallengeSettings`, `JournalStore`, `LocalClock`, or `GridInvalidator`. The ports are public only because Kotlin's `internal` is module-scoped, and a build failure is a stronger guard than a convention note.

A second Konsist test fails the build when `:core:domain` gains an `android.*` or `androidx.*` import, which is what keeps the projector unit-testable.

## Deferred, and where it attaches

Voice cleanup produces text that goes through `EntryBody.of` and `saveEntry`, so a provider can land in `:core:data` or its own module without touching the store surface. A future streak or stats feature reads `ChallengeWindow.progressOn` and the same counts query. Multiple challenges would add an id to the aggregate and a window argument to the projector, and would leave `JournalEntry` untouched because entries are keyed by date.
