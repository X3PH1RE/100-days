# Graph Report - 100 days  (2026-09-24)

## Corpus Check
- 51 files · ~19,168 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 17 file(s) not represented in the graph (top: .xml 10, (none) 2, .properties 2)

## Summary
- 705 nodes · 1169 edges · 62 communities (45 shown, 17 thin omitted)
- Extraction: 98% EXTRACTED · 2% INFERRED · 0% AMBIGUOUS · INFERRED: 27 edges (avg confidence: 0.94)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `a2de09ed`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- Theme.kt
- MidnightRefreshWorker.kt
- RoomJournalStore.kt
- DayScreen.kt
- JournalService
- DefaultJournalService
- GridScreen.kt
- DataStoreChallengeSettingsStore.kt
- HundredDaysNav.kt
- Ports.kt
- RoomJournalStore
- EntryDao
- DayKey
- arena-candidate-b/TYPES.kt
- ContributionGrid.kt
- arena-candidate-a/TYPES.kt
- Flow
- AGENTS.md — 100 Days
- ChallengeWindow
- HundredDaysStore
- EntryId
- DayKey
- CommandResult
- ContributionGridWidget.kt
- DefaultHundredDaysStore
- DayPhase
- MainActivity.kt
- Intensity
- Rejection
- DayCounts
- DayPhase
- GridCorner
- JournalEvent
- Contribution Grid
- CommandResult
- LocalClock
- ChallengeSettings
- NoChange
- gradlew
- Entries Outside Challenge Window
- Candidate B Module Map
- 100 Days Android Domain Architecture
- Dominant Access Patterns
- Candidate A Module Map
- Candidate A Caller Usage
- Candidate B Caller Usage
- Rejection
- EntryTextParse
- Intensity
- .startingOn
- BootCompletedReceiver.kt
- JournalEvent
- NoChange
- ChallengeSettingsStore
- StoreMutationResult
- alpha
- animatefloatasstate
- localsize
- tween

## God Nodes (most connected - your core abstractions)
1. `DayKey` - 17 edges
2. `HundredDaysStore` - 15 edges
3. `DayKey` - 14 edges
4. `AGENTS.md — 100 Days` - 14 edges
5. `DefaultJournalService` - 13 edges
6. `RoomJournalStore` - 12 edges
7. `AppContainer` - 12 edges
8. `HundredDaysNav()` - 12 edges
9. `DayScreen()` - 12 edges
10. `JournalService` - 11 edges

## Surprising Connections (you probably didn't know these)
- `Load-bearing invariants (do not break)` --references--> `FutureDay`  [INFERRED]
  AGENTS.md → app/src/main/java/com/x3phire/hundreddays/domain/Commands.kt
- `Wrong APK already burned a user session` --references--> `ContributionGridWidgetReceiver`  [INFERRED]
  AGENTS.md → app/src/main/java/com/x3phire/hundreddays/widget/ContributionGridWidget.kt
- `What is already implemented` --references--> `GlanceGridInvalidator`  [INFERRED]
  AGENTS.md → app/src/main/java/com/x3phire/hundreddays/data/GlanceGridInvalidator.kt
- `Always-on graph rules for agents` --references--> `RoomJournalStore`  [INFERRED]
  AGENTS.md → app/src/main/java/com/x3phire/hundreddays/data/RoomJournalStore.kt
- `Always-on graph rules for agents` --references--> `AppContainer`  [INFERRED]
  AGENTS.md → app/src/main/java/com/x3phire/hundreddays/di/AppContainer.kt

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Candidate B Service and Storage Boundary** — docs_design_arena_candidate_b_modules_journalservice, docs_design_arena_candidate_b_modules_journalstore, docs_design_arena_candidate_b_modules_challengesettingsstore [EXTRACTED 1.00]
- **Shared Contribution Grid Read Model** — docs_design_synthesis_contributiongrid, docs_design_arena_candidate_a_usage_gridscreen, docs_design_arena_candidate_a_usage_gridwidget [EXTRACTED 1.00]
- **Synthesized Candidate Architecture** — docs_design_synthesis_journalservice, docs_design_synthesis_contributiongrid, docs_design_synthesis_intensity, docs_design_synthesis_dayphase [EXTRACTED 1.00]

## Communities (62 total, 17 thin omitted)

### Community 0 - "Theme.kt"
Cohesion: 0.15
Nodes (12): background, box, brush, composable, fontfamily, fontweight, lightcolorscheme, materialtheme (+4 more)

### Community 1 - "MidnightRefreshWorker.kt"
Cohesion: 0.07
Nodes (28): Package map inside single module `:app`, AppDatabase, GlanceGridInvalidator, DayKey, SystemLocalClock, AppContainer, JournalService, HundredDaysApp (+20 more)

### Community 2 - "RoomJournalStore.kt"
Cohesion: 0.31
Nodes (6): DayJournal, EntryDraft, EntryRef, JournalEntry, DayKey, instant

### Community 3 - "DayScreen.kt"
Cohesion: 0.09
Nodes (40): arrangement, arrowback, button, clip, column, consumewindowinsets, datepicker, datepickerdialog (+32 more)

### Community 4 - "JournalService"
Cohesion: 0.06
Nodes (39): ChallengeSettings, ContributionGrid.project, Room EntryRow, GlanceInvalidator, Grouped Entry Count Query, HundredDaysStore, JournalStore, Challenge-centric Aggregate Behind One Store (+31 more)

### Community 5 - "DefaultJournalService"
Cohesion: 0.13
Nodes (17): AddEntry, DefaultJournalService, JournalService, ChallengeState, DayJournal, DayKey, Flow, CommandResult (+9 more)

### Community 6 - "GridScreen.kt"
Cohesion: 0.08
Nodes (33): DaysLeftCorner, TOP_END, TOP_START, badgeCornerHint(), DayCell(), daysLeftCopy(), GridScreen(), IntensityLegend() (+25 more)

### Community 7 - "DataStoreChallengeSettingsStore.kt"
Cohesion: 0.11
Nodes (16): DataStoreChallengeSettingsStore, ChallengeSettings, Flow, ChallengeSettings, ChallengeWindow, GridLayout, ClosedRange, DayKey (+8 more)

### Community 8 - "HundredDaysNav.kt"
Cohesion: 0.09
Nodes (30): Always-on graph rules for agents, graphify, AddEntry, DeleteEntry, EditEntry, JournalCommand, SetRange, DayScreen() (+22 more)

### Community 9 - "Ports.kt"
Cohesion: 0.14
Nodes (11): Delete, Edit, GridInvalidator, JournalMutation, JournalStore, ClosedRange, DayJournal, DayKey (+3 more)

### Community 10 - "RoomJournalStore"
Cohesion: 0.15
Nodes (11): ClosedRange, DayJournal, DayKey, Flow, JournalEntry, RoomJournalStore, EntryId, JournalMutation (+3 more)

### Community 11 - "EntryDao"
Cohesion: 0.14
Nodes (11): EntryDao, Flow, DayCountRow, EntryEntity, dao, entity, insert, onconflictstrategy (+3 more)

### Community 12 - "DayKey"
Cohesion: 0.08
Nodes (14): DayKey, Comparable, ChallengeWindowTest, ContributionGridTest, IntensityTest, MidnightRefreshWorkerTest, assertequals, assertnull (+6 more)

### Community 13 - "arena-candidate-b/TYPES.kt"
Cohesion: 0.14
Nodes (13): AddEntry, ChallengeOverview, DeleteEntry, EditEntry, EntryCount, EntryDraft, EntryId, EntryRef (+5 more)

### Community 14 - "ContributionGrid.kt"
Cohesion: 0.15
Nodes (16): Load-bearing invariants (do not break), Active, ChallengeState, ContributionGrid, ContributionGridModel, Day, GridCell, ChallengeSettings (+8 more)

### Community 15 - "arena-candidate-a/TYPES.kt"
Cohesion: 0.14
Nodes (8): ColumnCount, Day, DayCount, DayDeepLink, GridCell, GridInvalidator, GridRow, Padding

### Community 16 - "Flow"
Cohesion: 0.20
Nodes (7): Active, ChallengeState, DayJournal, DefaultJournalService, JournalService, Flow, NotConfigured

### Community 17 - "AGENTS.md — 100 Days"
Cohesion: 0.10
Nodes (19): AGENTS.md — 100 Days, Architecture (source of truth), Critical build / APK gotchas (read carefully), Current git / delivery state (as of 2026-09-23), Intensity buckets, OneDrive vs `C:\dev\100-days`, Principles already applied (keep them), Public surface (only thing UI / widget should call) (+11 more)

### Community 18 - "ChallengeWindow"
Cohesion: 0.15
Nodes (8): Active, ChallengeProgress, ChallengeWindow, Complete, DayPlacement, InWindow, NotStarted, Outside

### Community 19 - "HundredDaysStore"
Cohesion: 0.26
Nodes (6): Challenge, ChallengeSettings, ContributionGrid, HundredDaysStore, ContributionGridModel, EntryId

### Community 20 - "EntryId"
Cohesion: 0.20
Nodes (4): EntryBody, EntryId, JournalEntry, JournalStore

### Community 21 - "DayKey"
Cohesion: 0.23
Nodes (7): ChallengeWindow, computeGridIntensity(), DayIntensity, DayKey, JournalStore, ClosedRange, Comparable

### Community 22 - "CommandResult"
Cohesion: 0.20
Nodes (10): Add, Applied, Changed, CommandResult, Delete, Edit, JournalMutation, Rejected (+2 more)

### Community 23 - "ContributionGridWidget.kt"
Cohesion: 0.08
Nodes (43): Action, ActionCallback, ActionParameters, actionparametersof, actionruncallback, actionstartactivity, Deep link, Widget implementation notes (+35 more)

### Community 24 - "DefaultHundredDaysStore"
Cohesion: 0.36
Nodes (4): ContributionGridModel, DayCard, DefaultHundredDaysStore, Flow

### Community 25 - "DayPhase"
Cohesion: 0.29
Nodes (5): DayPhase, FUTURE, PAST, TODAY, DayKey

### Community 26 - "MainActivity.kt"
Cohesion: 0.22
Nodes (8): consumer, disposableeffect, enableedgetoedge, fillmaxsize, intent, modifier, remembernavcontroller, setcontent

### Community 27 - "Intensity"
Cohesion: 0.29
Nodes (6): Intensity, HEAVY, LIGHT, MEDIUM, NONE, PEAK

### Community 28 - "Rejection"
Cohesion: 0.29
Nodes (7): BlankEntry, EntryIdConflict, EntryNotFound, EntryTooLong, InvalidColumnCount, InvertedRange, Rejection

### Community 30 - "DayPhase"
Cohesion: 0.40
Nodes (4): DayPhase, FUTURE, PAST, TODAY

### Community 31 - "GridCorner"
Cohesion: 0.40
Nodes (5): GridCorner, BOTTOM_END, BOTTOM_START, TOP_END, TOP_START

### Community 32 - "JournalEvent"
Cohesion: 0.40
Nodes (5): ChallengeSettingsChanged, EntryAdded, EntryDeleted, EntryEdited, JournalEvent

### Community 33 - "Contribution Grid"
Cohesion: 0.40
Nodes (5): 100 Days, Contribution Grid, Glance Widget, Local-first Android App, Room Journal Entries

### Community 34 - "CommandResult"
Cohesion: 0.50
Nodes (4): Applied, CommandResult, Rejected, Unchanged

### Community 37 - "NoChange"
Cohesion: 0.50
Nodes (4): EntryAlreadyAbsent, EntryAlreadyMatches, NoChange, SettingsAlreadyMatch

### Community 38 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 39 - "Entries Outside Challenge Window"
Cohesion: 0.67
Nodes (3): Challenge Window as Journal Lens, Inclusive Challenge Window, Entries Outside Challenge Window

### Community 49 - "Rejection"
Cohesion: 0.29
Nodes (7): BlankEntry, EntryNotFound, EntryTooLong, FutureDay, InvalidColumnCount, InvertedRange, Rejection

### Community 50 - "EntryTextParse"
Cohesion: 0.43
Nodes (5): Blank, EntryText, EntryTextParse, Ok, TooLong

### Community 51 - "Intensity"
Cohesion: 0.25
Nodes (6): Intensity, EMPTY, HIGH, LOW, MAX, MEDIUM

### Community 52 - ".startingOn"
Cohesion: 0.29
Nodes (6): At, DaysLeftPref, Fixed, GridLayout, Hidden, WeekAligned

### Community 53 - "BootCompletedReceiver.kt"
Cohesion: 0.53
Nodes (4): BootCompletedReceiver, Context, Intent, BroadcastReceiver

### Community 54 - "JournalEvent"
Cohesion: 0.40
Nodes (5): ChallengeSettingsChanged, EntryAdded, EntryDeleted, EntryEdited, JournalEvent

### Community 55 - "NoChange"
Cohesion: 0.50
Nodes (4): EntryAlreadyAbsent, EntryAlreadyMatches, NoChange, SettingsAlreadyMatch

### Community 57 - "StoreMutationResult"
Cohesion: 0.50
Nodes (4): Changed, Rejected, StoreMutationResult, Unchanged

## Knowledge Gaps
- **111 isolated node(s):** `TOP_START`, `TOP_END`, `EntryAdded`, `EntryEdited`, `EntryDeleted` (+106 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 289 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **17 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `DayKey` connect `DayKey` to `RoomJournalStore.kt`, `DayScreen.kt`, `GridScreen.kt`, `DataStoreChallengeSettingsStore.kt`, `HundredDaysNav.kt`, `ContributionGridWidget.kt`?**
  _High betweenness centrality (0.090) - this node is a cross-community bridge._
- **Why does `AppContainer` connect `MidnightRefreshWorker.kt` to `HundredDaysNav.kt`, `RoomJournalStore`, `DefaultJournalService`, `DataStoreChallengeSettingsStore.kt`?**
  _High betweenness centrality (0.061) - this node is a cross-community bridge._
- **Why does `Always-on graph rules for agents` connect `HundredDaysNav.kt` to `MidnightRefreshWorker.kt`, `RoomJournalStore`, `HundredDaysStore`?**
  _High betweenness centrality (0.057) - this node is a cross-community bridge._
- **What connects `TOP_START`, `TOP_END`, `EntryAdded` to the rest of the system?**
  _111 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `MidnightRefreshWorker.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.06504065040650407 - nodes in this community are weakly interconnected._
- **Should `DayScreen.kt` be split into smaller, more focused modules?**
  _Cohesion score 0.08748615725359911 - nodes in this community are weakly interconnected._
- **Should `JournalService` be split into smaller, more focused modules?**
  _Cohesion score 0.058029689608636977 - nodes in this community are weakly interconnected._