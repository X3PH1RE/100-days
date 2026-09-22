# Synthesized architecture

## Base

Candidate B (`docs/design/arena-candidate-b/`).

Why. Explicit `ChallengeState.NotConfigured` matches first-launch onboarding. One `JournalService.execute` boundary returns Applied / Unchanged / Rejected. Counts stay derived. `DayKey` brands local dates.

## Grafts from candidate A

- `Intensity` buckets via `Intensity.forCount` live in domain. UI only maps intensity to color.
- Pure `ContributionGrid.project(...)` builds the shared in-app and widget read model, including days-left progress.
- After every successful journal or settings write, the store invalidates the Glance widget once. Callers never refresh.
- Entry save is upsert by `EntryId` so retries converge.
- `DayPhase` (past / today / future) rides on each grid cell for presentation.

## Rejected

- A's silent default 100-day challenge before onboarding. Product requires an explicit date pick.
- A's start-plus-length window construction for the public settings API. Date pickers supply two dates. Validation stays `ChallengeWindow.validated(start, end)`.
- Split Gradle modules `:core:domain` / `:core:data` for v1. Packages inside `:app` keep the same ownership with less scaffold. Split later if needed.
- Stored counts table or dual-write projection.
- kotlinx-datetime. `java.time` on minSdk 26 is enough.

## Module map (v1)

```
:app
  domain/          pure types + JournalService + projector
  data/            Room + DataStore adapters (internal)
  ui/              Compose onboarding, grid, day card, settings
  widget/          Glance (later phase)
  refresh/         midnight WorkManager (later phase)
```

## Public surface

```kotlin
interface JournalService {
    fun observeDay(day: DayKey): Flow<DayJournal>
    fun observeChallenge(): Flow<ChallengeState>
    suspend fun execute(command: JournalCommand): CommandResult
}
```

## Next implementation step

Domain types and pure projector with JVM unit tests, then Room CRUD, then DataStore settings, then Compose onboarding plus grid plus day card.
