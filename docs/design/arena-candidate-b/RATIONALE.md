# Candidate B rationale

## Problem

The app needs one offline source of journal truth for Compose and Glance while treating local calendar days, not entries or screens, as the primary unit. The shape must support entries outside the visible challenge window, derive grid counts without synchronized projections, keep widget actors read-only, and prevent Room or DataStore representations from leaking into callers.

## Usage (caller's view)

Callers receive one `JournalService`. A day card observes `DayJournal` and sends `AddEntry`, `EditEntry`, or `DeleteEntry`. Onboarding sends `SetRange`. Compose and Glance observe the same `ChallengeState`, then render the active `ChallengeOverview`. The complete call sites are in `USAGE.md`.

The usage deliberately gives callers raw entry text and range fields only when issuing a command. The service returns `Applied`, `Unchanged`, or `Rejected`, so UI code can respond without exceptions or knowledge of persistence behavior.

## Shape

The data model follows the three main reads: one day, the active challenge, and grouped counts. `DayKey` brands `LocalDate`. `DayJournal` owns the ordered entries for exactly one day, and each contained entry omits a duplicate date. This makes the day-card read explicit and removes contradictory date fields, per foundational-thinking, model-the-domain, and type-system-discipline.

`ChallengeSettings` owns the valid `ChallengeWindow`, `GridLayout`, and days-left preference. It is separate from all journals because changing visibility or layout must not rewrite entries. `ChallengeState.NotConfigured` represents first launch without nullable or partially initialized settings, per type-system-discipline.

All writes cross `JournalService.execute`. The service exhaustively dispatches four command variants, validates raw boundary fields once, and translates them into internal typed mutations. Store adapters receive trusted `EntryText`, `ChallengeWindow`, and `GridLayout` values, per boundary-discipline. Returned `JournalEvent` values describe committed changes. Repeated equivalent commands return `Unchanged`, while conflicting reuse of an entry ID is rejected, per make-operations-idempotent.

The challenge read path combines settings with a Room aggregate query. `computeGridIntensity(ClosedRange<DayKey>, Map<DayKey, Int>)` emits every visible day in order and fills absent counts with zero. Counts remain derived from journal rows. No counts table, cache invalidation policy, or second writer exists.

The interface is deep. Three public operations hide command validation, idempotence policy, Room transactions, DataStore parsing, flow combination, missing-day filling, and framework adaptation. Callers retain only product decisions such as what to render after rejection. Storage ports and mutations stay internal to `core-journal`, keeping the call chain at UI to service to store, per laziness-protocol and minimize-reader-load.

## Synthesis decision

This is an unsynthesized arena candidate. The arena picker must record whether candidate B became the base, which parts were grafted elsewhere, and which parts were rejected.

## Tradeoffs accepted

- We accept one service that knows both journal and challenge commands in exchange for a single validation and result boundary.
- We accept recomputing grouped counts when journal rows change in exchange for eliminating a synchronized counts table.
- We accept commands carrying unvalidated text and layout primitives in exchange for keeping validation inside the service boundary.
- We accept one `core-journal` Kotlin module containing domain and storage packages in exchange for truly internal storage ports and a smaller exported API.
- We accept entry IDs being allocated before `AddEntry` in exchange for safe command retries without a command ledger.

## Alternatives considered

- Aggregate repositories were rejected. Separate `EntryRepository`, `SettingsRepository`, and `CountRepository` interfaces would expose coordination to every caller, provide shallow pass-through methods, and make count consistency a UI concern.
- Event sourcing was rejected. An append-only command or event log would hide replay mechanics behind the service, but it adds migration, compaction, and projection complexity that offline single-writer v1 does not need.
- A stored daily-count projection was rejected. Reads would be simple, but every entry transaction would also need projection repair policy. That duplicates one invariant and creates a second representation the widget must trust.
- An entry-first model was rejected. Returning flat dated entries would make every day screen regroup data and allow a `DayJournal` key to disagree with dates carried by its children.

## Open questions and risks

- Which grid column counts are valid for phone, tablet, and widget layouts?
- Is entry order strictly creation order, or will manual reordering become a product requirement?
- What maximum entry length should `EntryText` enforce?
- Should changing the device time zone preserve the original local `DayKey`, or reinterpret entries from their timestamps?
- Does the widget need a distinct compact read model after Glance size behavior is designed, or can it continue projecting `ChallengeOverview` locally?

## Next implementation step

Build domain type tests and command-boundary tests for valid ranges, day ownership, retries, and derived zero-count cells before adding Room or DataStore adapters.
