# Candidate A rationale

Challenge-centric aggregate behind one store.

## Problem

100 Days is a greenfield offline Android app where one date range, one grid of per-day intensities, and one journal have to stay consistent across three consumers that never talk to each other: a Compose screen, a Glance widget, and a midnight WorkManager job. The shape is non-obvious because the obvious model is wrong in two specific ways. First, the grid looks like stored data, so intensity wants to become a column that the widget reads and something has to keep up to date. Grounding invariant 3 says intensity is the count of entries for a date, which makes it derivable and therefore not storable. Second, the challenge looks like the parent of its entries, so entries want a challenge id. Grounding invariant 4 says entries may exist outside the window and the window only controls visibility, which makes the challenge a lens over a date-keyed journal rather than its owner. The remaining Phase A constraints the design has to honour: the widget and the worker never write, DataStore has a single writer, the dominant reads are per-day counts for a whole window and all entries for one date, and v1 has no accounts and no sync.

## Usage (caller's view)

Written first, in [USAGE.md](USAGE.md), and the types were derived from it. The short version.

A caller imports one name.

```kotlin
val store: HundredDaysStore = context.hundredDays()

store.grid.collect { model -> model.rows.forEach { row -> render(row.cells) } }
store.dayCard(date).collect { card -> render(card) }
store.saveEntry(draftId, date, body)
store.setChallenge(current.copy(layout = GridLayout.Fixed(ColumnCount.TEN)))
```

The grid screen reads `cell.intensity` and `cell.phase` and renders. The widget calls `gridNow()` for its first frame and collects the same `grid` flow after. The day card mints an `EntryId` when the composer opens and calls `saveEntry`, which upserts. Settings edits a whole `Challenge` and hands it back.

No caller maps a count to a bucket, asks whether a date is in the window, builds an out-of-order date pair, refreshes the widget after a write, or branches on whether a challenge exists yet.

## Shape

**Data structures first.** `Challenge` is the aggregate and holds the window, the grid layout, and the days-left preference. `ChallengeWindow` is a start plus a `DayCount` rather than two dates, so a backwards range is not a value this type can hold, per type-system-discipline's construction-over-restriction pattern. The one place a raw pair of picked dates exists is `ChallengeWindow.of`, which returns null. `JournalEntry` is keyed by `LocalDate` and carries no challenge reference, which is what makes re-targeting the window a zero-entry-touch operation. `Intensity` is an enum with one `forCount` function and no storage anywhere. `DayCounts` is a total lookup so the projector has no null branch, and it has structural equality so the store can drop duplicate emissions.

`ChallengeProgress` is a sealed type of `NotStarted`, `Active`, and `Complete` instead of a bag of counters, and `DayPlacement` replaces every `if (date in window)` at a call site. Both are per model-the-domain: the branches that would otherwise spread across the grid, the day card, and the badge collapse into one `when`.

**Data flow.** `ContributionGrid.project(challenge, counts, today)` is the only projector, and it is pure. `today` is a parameter because the clock is a boundary, per boundary-discipline, which is also why the projector can be tested with three literal dates. The store composes `challenge` with `flatMapLatest { journal.counts(it.window) }` and the clock's day ticks. That `flatMapLatest` is load-bearing: the count query is resubscribed from the same window that is passed to the projector, so a model cannot pair a window with counts fetched for a different one. There is no later index or cache to add, because the dominant read is already the persistence query.

**Interface depth.** The public surface is `HundredDaysStore` with seven members plus the domain value types a caller names. Behind it sit four ports, the flow composition, the counts-follow-the-window rule, first-run default substitution, timestamp policy, and widget invalidation after every write. Learning the seven members does save a caller from learning the implementation, which is the test the red-flag list applies. What stays exposed is the `Challenge` aggregate itself, because settings genuinely edits it, and the read models, because rendering genuinely reads them. Nothing smaller would let onboarding change a window.

**Invariants in types.** Backwards ranges, zero-length windows, a one-column grid, blank entry bodies, and "hidden badge at the top end" are all unrepresentable. `DaysLeftPref` is a sealed `Hidden` or `At(corner)` rather than a corner plus a visibility boolean. Validation lives at three boundaries and nowhere else: `ChallengeWindow.of` for picked dates, `EntryBody.of` for typed or transcribed text, and the DataStore mapper for a stored aggregate that no longer deserializes. Inside the system the types are trusted.

**Idempotence.** `saveEntry` takes a caller-minted `EntryId` and upserts, so a double tap on Save edits one row instead of inserting two, per make-operations-idempotent. `deleteEntry` on an absent id is a no-op. `setChallenge` is a whole-aggregate replace, so it has no partial-application state. The midnight worker is enqueued as unique periodic work and only calls `updateAll`, so a duplicate run repaints the same pixels.

**Shared state.** Every write goes through one object. The widget and the worker read the projection and invalidate it, and neither writes domain state, so the sharing is eliminated rather than serialized, per separate-before-serializing-shared-state. There is no counts column to dual-write and nothing here needs a lock.

**What it deliberately does not do.** No sync, no accounts, no multi-challenge, no stored streaks, no per-field challenge setters, no voice provider. Entries outside the window are persisted and simply not projected.

**Red-flag screen.** The three entry-adjacent store methods each add policy on top of their port call (timestamp, invalidation, id minting rule), so none is a pass-through. Modules are grouped by knowledge rather than by load, validate, transform, and save stages. Storage rows, DataStore keys, `GlanceId`, and ISO date strings do not appear on the public surface, and the deep-link format has one home rather than one per consumer. The one leak I could not remove with types is that the ports must be public for the DI graph to name them, so a Konsist test fails the build if any `:app` file outside `di/` references a port, per encode-lessons-in-structure.

## Synthesis decision

Filled in by arena.

## Tradeoffs accepted

- We accept that `ContributionGrid.project` receives a `DayCounts` whose coverage is a documented invariant rather than a typed one, in exchange for a projector that a test can call with a three-entry literal map. A range-tagged counts type would only move the mismatch into a runtime equality check, and the store is the sole caller.
- We accept a caller-minted `EntryId` (a UUID string primary key, slightly larger than a rowid) in exchange for a save that is idempotent under double taps and retries.
- We accept that `challenge` and `grid` always emit a value, using a default 100 day window computed from today, in exchange for deleting the "not onboarded yet" branch from every screen and from the widget. Whether onboarding has been seen becomes a UI preference outside this package.
- We accept that the read model carries both `count` and `intensity` per cell, which looks redundant, in exchange for an accessibility label that states the real number without any consumer re-deriving the bucket.
- We accept `GridInvalidator` as a port with exactly one implementation, which looks like speculative indirection, in exchange for `:core:domain` having no Android dependency and the store's write path being unit-testable.
- We accept that entries carry no challenge reference, so a future multi-challenge feature cannot partition them by challenge without a new decision, in exchange for honouring grounding invariant 4 exactly.

## Alternatives considered

**Two repositories used directly by the UI (`ChallengeRepository` plus `EntryRepository`).** Rejected on interface depth. The surface is larger and hides less: each screen and the widget would combine two flows, call the projector themselves, and pass their own clock reading, which is the "callers coordinate several methods to complete one operation" sign from the red-flag list. It also has no home for post-write widget invalidation, so that rule ends up copied into every ViewModel and the next writer forgets it. The complexity it exposes is exactly the composition the store hides.

**Store intensity or a per-day count on a `ChallengeDay` row, written alongside each entry.** Rejected because it creates a second source of truth for a derived value and a dual-write the widget depends on. Every edit and delete needs a recount, a crash between the two writes leaves the grid lying, and a threshold change needs a migration. It buys a marginally cheaper widget read that a `GROUP BY` on an indexed date column already makes cheap.

**A single `UiState` per screen exposed from ViewModels, with the domain reduced to DAOs.** Rejected because the widget is not a screen and has no ViewModel, so the grid read model would exist twice, once per consumer, drifting on exactly the rules that matter. It fails the "one read model shared by app and widget" requirement structurally rather than by discipline.

**`Challenge` as `start: LocalDate` plus `end: LocalDate`.** Rejected because it admits `end < start` and pushes an ordering check into every consumer. The length-based construction costs one value class and removes the check entirely.

## Open questions and risks

- Should a challenge have a user-visible title? I left it out because nothing in the brief displays one, and adding it later is one field on the aggregate. Does onboarding ask for a name?
- Where should "has the user completed onboarding" live, given the domain no longer has an unset state to signal it? My assumption is a separate UI preference read only by the nav graph.
- Are the intensity thresholds 1, 2, 3, and 4-plus right for a journalling app, where most days will realistically have one entry? A five-bucket scale over a one-to-two entry distribution may render as a two-colour grid. Should the buckets be relative to the window's own maximum instead?
- For `GridLayout.WeekAligned`, should the week start follow the system locale rather than being a stored preference?
- Does the widget need to render a challenge that has not started yet, or should it show a countdown? `ChallengeProgress.NotStarted` carries the number, but the visual is a product call.
- Risk: `LocalClock.days()` firing at midnight is the one piece that cannot be verified by a unit test alone. It needs an instrumented check against timezone change and a device date roll before it is trusted.

## Next implementation step

Write `ChallengeWindow` and `ContributionGrid.project` with a JVM unit test that renders a known 100 day window in both layouts and asserts exact cell rows, since everything else in the design reads that projection.
