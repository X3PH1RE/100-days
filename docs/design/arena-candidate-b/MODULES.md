# Candidate B module map

`domain`, `storage-room`, and `storage-settings` are ownership packages inside one `core-journal` Kotlin module. This keeps their ports `internal` while exporting only the service and domain models to Android callers.

## `core-journal/domain`

Owns `DayKey`, `DayJournal`, challenge settings, commands, results, and the pure intensity function. It has no Android, Room, or DataStore dependency.

Its public API is intentionally narrow:

- `JournalService.observeDay` returns the first-class model for one date.
- `JournalService.observeChallenge` returns either `NotConfigured` or one shared app and widget read model.
- `JournalService.execute` is the only mutation boundary.
- `computeGridIntensity` derives ordered cells from a closed date range and grouped entry counts.

`DefaultJournalService` validates raw command fields once. It converts accepted commands into internal typed mutations. Add, edit, and delete produce journal events. Range replacement produces a settings event.

## `core-journal/storage-room`

Implements `JournalStore`.

Room entities, DAOs, SQL column names, and transactions remain private to this module. The day query maps rows directly into one `DayJournal`. The range query uses `GROUP BY localDate` over entry rows and returns `Map<DayKey, Int>`. There is no counts entity, cache table, or dual write.

`apply` owns entry transaction policy. A repeated add with the same ID and payload is unchanged. The same ID with different data is a conflict. A repeated edit to identical text and a repeated delete are unchanged.

## `core-journal/storage-settings`

Implements `ChallengeSettingsStore` over DataStore.

The adapter parses stored primitives into `ChallengeWindow` and `GridLayout` before exposing settings. Missing settings become `ChallengeState.NotConfigured` through the service. Storage schema and preference keys remain private.

## `app-feature`

Compose screens depend only on `JournalService` and domain models. The day card observes a `DayJournal` and emits commands. Onboarding and settings emit `SetRange`. The grid renders `ChallengeState.Active.overview`.

The main paths stay short:

- Day UI to `JournalService` to `JournalStore`.
- Grid UI to `JournalService` to both stores.
- Settings UI to `JournalService` to `ChallengeSettingsStore`.

## `widget`

Glance reads `JournalService.observeChallenge().first()`. It never writes journal data. Deep-link intent construction stays in this Android-facing module and carries the selected `DayKey` to the app.

## `refresh`

WorkManager compares the current local `DayKey` with the active challenge window and requests a widget refresh. It does not mutate journals or challenge settings.

## Dependency direction

`app-feature`, `widget`, and `refresh` depend on `core-journal`.

The storage packages depend inward on the domain package to implement its internal ports.

The composition root creates the storage adapters and `DefaultJournalService`. No UI module sees a DAO, entity, preference key, or persistence transaction.
