# Graph Report - 100 days  (2026-09-23)

## Corpus Check
- Corpus is ~16,548 words - fits in a single context window. You may not need a graph.

## Summary
- 663 nodes · 1103 edges · 49 communities (37 shown, 12 thin omitted)
- Extraction: 99% EXTRACTED · 1% INFERRED · 0% AMBIGUOUS · INFERRED: 7 edges (avg confidence: 0.89)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- Glance Widget Actions
- App DI and Data Wiring
- Room Journal Mutations
- Compose Material UI
- Candidate A Store Design
- JournalService Runtime
- In-App Grid Screen
- DataStore Challenge Settings
- Navigation and ChallengeState
- Domain Storage Ports
- RoomJournalStore Reads
- EntryDao SQL API
- Domain Unit Tests
- Candidate B Type Sketch
- ContributionGrid Projector
- Candidate A Type Sketch
- JournalService Interface
- MainActivity Shell
- Candidate A Challenge Model
- HundredDaysStore Surface
- Entry Body and Identity
- DayKey and Intensity Math
- Mutation Result Types
- DayKey Value Type
- Store Day Card Flows
- DayPhase Enum
- Grid Layout Prefs
- Intensity Buckets
- Command Rejection Reasons
- DayCounts Helper
- DayPhase Helpers
- Grid Corner Prefs
- Journal Events
- Product Overview Docs
- CommandResult Variants
- LocalClock Port
- Settings Store Port
- Idempotent NoChange
- Gradle Wrapper Scripts
- Challenge Window Lens
- Synthesis vs Candidate B
- Arena Grounding Docs
- Shared Read Model Docs
- Candidate A Modules Doc
- Candidate A Usage Doc
- Candidate B Usage Doc

## God Nodes (most connected - your core abstractions)
1. `DayKey` - 17 edges
2. `HundredDaysStore` - 14 edges
3. `DayKey` - 14 edges
4. `DefaultJournalService` - 13 edges
5. `RoomJournalStore` - 11 edges
6. `HundredDaysNav()` - 11 edges
7. `DayScreen()` - 11 edges
8. `JournalService` - 11 edges
9. `AppContainer` - 10 edges
10. `DaysLeftCorner` - 10 edges

## Surprising Connections (you probably didn't know these)
- `ContributionGridTest` --calls--> `ChallengeSettings`  [INFERRED]
  app/src/test/java/com/x3phire/hundreddays/domain/ContributionGridTest.kt → app/src/main/java/com/x3phire/hundreddays/domain/Challenge.kt
- `DataStore Settings` --semantically_similar_to--> `ChallengeSettingsStore`  [INFERRED] [semantically similar]
  docs/design/GROUNDING.md → docs/design/arena-candidate-b/MODULES.md
- `HundredDaysStore` --semantically_similar_to--> `JournalService`  [INFERRED] [semantically similar]
  docs/design/arena-candidate-a/MODULES.md → docs/design/arena-candidate-b/MODULES.md
- `AppContainer` --calls--> `DataStoreChallengeSettingsStore`  [EXTRACTED]
  app/src/main/java/com/x3phire/hundreddays/di/AppContainer.kt → app/src/main/java/com/x3phire/hundreddays/data/DataStoreChallengeSettingsStore.kt
- `AppContainer` --calls--> `RoomJournalStore`  [EXTRACTED]
  app/src/main/java/com/x3phire/hundreddays/di/AppContainer.kt → app/src/main/java/com/x3phire/hundreddays/data/RoomJournalStore.kt

## Import Cycles
- None detected.

## Hyperedges (group relationships)
- **Shared Contribution Grid Read Model** — docs_design_synthesis_contributiongrid, docs_design_arena_candidate_a_usage_gridscreen, docs_design_arena_candidate_a_usage_gridwidget [EXTRACTED 1.00]
- **Candidate B Service and Storage Boundary** — docs_design_arena_candidate_b_modules_journalservice, docs_design_arena_candidate_b_modules_journalstore, docs_design_arena_candidate_b_modules_challengesettingsstore [EXTRACTED 1.00]
- **Synthesized Candidate Architecture** — docs_design_synthesis_journalservice, docs_design_synthesis_contributiongrid, docs_design_synthesis_intensity, docs_design_synthesis_dayphase [EXTRACTED 1.00]

## Communities (49 total, 12 thin omitted)

### Community 0 - "Glance Widget Actions"
Cohesion: 0.05
Nodes (52): Action, ActionCallback, ActionParameters, actionparametersof, actionruncallback, actionstartactivity, Intensity, EMPTY (+44 more)

### Community 1 - "App DI and Data Wiring"
Cohesion: 0.06
Nodes (31): AppDatabase, GlanceGridInvalidator, DayKey, SystemLocalClock, AppContainer, JournalService, HundredDaysApp, BootCompletedReceiver (+23 more)

### Community 2 - "Room Journal Mutations"
Cohesion: 0.07
Nodes (36): AddEntry, BlankEntry, ChallengeSettingsChanged, DeleteEntry, EditEntry, EntryAdded, EntryAlreadyAbsent, EntryAlreadyMatches (+28 more)

### Community 3 - "Compose Material UI"
Cohesion: 0.10
Nodes (37): arrangement, arrowback, button, column, datepicker, datepickerdialog, datetimeformatter, delete (+29 more)

### Community 4 - "Candidate A Store Design"
Cohesion: 0.06
Nodes (39): ChallengeSettings, ContributionGrid.project, Room EntryRow, GlanceInvalidator, Grouped Entry Count Query, HundredDaysStore, JournalStore, Challenge-centric Aggregate Behind One Store (+31 more)

### Community 5 - "JournalService Runtime"
Cohesion: 0.12
Nodes (16): AddEntry, DefaultJournalService, ChallengeState, DayJournal, DayKey, Flow, CommandResult, DeleteEntry (+8 more)

### Community 6 - "In-App Grid Screen"
Cohesion: 0.08
Nodes (28): alignment, alpha, animatefloatasstate, DaysLeftCorner, TOP_END, TOP_START, badgeCornerHint(), DayCell() (+20 more)

### Community 7 - "DataStore Challenge Settings"
Cohesion: 0.11
Nodes (16): DataStoreChallengeSettingsStore, ChallengeSettings, Flow, ChallengeSettings, ChallengeWindow, GridLayout, ClosedRange, DayKey (+8 more)

### Community 8 - "Navigation and ChallengeState"
Cohesion: 0.10
Nodes (24): SetRange, Active, ChallengeState, NotConfigured, JournalService, HundredDaysNav(), DayKey, JournalService (+16 more)

### Community 9 - "Domain Storage Ports"
Cohesion: 0.10
Nodes (17): ChallengeSettingsStore, Changed, Delete, Edit, GridInvalidator, JournalMutation, JournalStore, ChallengeSettings (+9 more)

### Community 10 - "RoomJournalStore Reads"
Cohesion: 0.15
Nodes (11): ClosedRange, DayJournal, DayKey, Flow, JournalEntry, RoomJournalStore, EntryId, JournalMutation (+3 more)

### Community 11 - "EntryDao SQL API"
Cohesion: 0.14
Nodes (11): EntryDao, Flow, DayCountRow, EntryEntity, dao, entity, insert, onconflictstrategy (+3 more)

### Community 12 - "Domain Unit Tests"
Cohesion: 0.12
Nodes (9): ChallengeWindowTest, ContributionGridTest, IntensityTest, MidnightRefreshWorkerTest, assertequals, assertnull, assertthrows, asserttrue (+1 more)

### Community 13 - "Candidate B Type Sketch"
Cohesion: 0.14
Nodes (13): AddEntry, ChallengeOverview, DeleteEntry, EditEntry, EntryCount, EntryDraft, EntryId, EntryRef (+5 more)

### Community 14 - "ContributionGrid Projector"
Cohesion: 0.18
Nodes (12): ContributionGrid, ContributionGridModel, Day, GridCell, ChallengeSettings, DayKey, Padding, WeekColumn (+4 more)

### Community 15 - "Candidate A Type Sketch"
Cohesion: 0.14
Nodes (8): ColumnCount, Day, DayCount, DayDeepLink, GridCell, GridInvalidator, GridRow, Padding

### Community 16 - "JournalService Interface"
Cohesion: 0.20
Nodes (7): Active, ChallengeState, DayJournal, DefaultJournalService, JournalService, Flow, NotConfigured

### Community 17 - "MainActivity Shell"
Cohesion: 0.21
Nodes (11): MainActivity, AppBackground(), HundredDaysTheme(), Bundle, ComponentActivity, consumer, disposableeffect, enableedgetoedge (+3 more)

### Community 18 - "Candidate A Challenge Model"
Cohesion: 0.15
Nodes (8): Active, ChallengeProgress, ChallengeWindow, Complete, DayPlacement, InWindow, NotStarted, Outside

### Community 19 - "HundredDaysStore Surface"
Cohesion: 0.26
Nodes (6): Challenge, ChallengeSettings, ContributionGrid, HundredDaysStore, ContributionGridModel, EntryId

### Community 20 - "Entry Body and Identity"
Cohesion: 0.20
Nodes (4): EntryBody, EntryId, JournalEntry, JournalStore

### Community 21 - "DayKey and Intensity Math"
Cohesion: 0.23
Nodes (7): ChallengeWindow, computeGridIntensity(), DayIntensity, DayKey, JournalStore, ClosedRange, Comparable

### Community 22 - "Mutation Result Types"
Cohesion: 0.20
Nodes (10): Add, Applied, Changed, CommandResult, Delete, Edit, JournalMutation, Rejected (+2 more)

### Community 23 - "DayKey Value Type"
Cohesion: 0.20
Nodes (4): DayKey, Comparable, clock, zoneid

### Community 24 - "Store Day Card Flows"
Cohesion: 0.36
Nodes (4): ContributionGridModel, DayCard, DefaultHundredDaysStore, Flow

### Community 25 - "DayPhase Enum"
Cohesion: 0.29
Nodes (5): DayPhase, FUTURE, PAST, TODAY, DayKey

### Community 26 - "Grid Layout Prefs"
Cohesion: 0.29
Nodes (6): At, DaysLeftPref, Fixed, GridLayout, Hidden, WeekAligned

### Community 27 - "Intensity Buckets"
Cohesion: 0.29
Nodes (6): Intensity, HEAVY, LIGHT, MEDIUM, NONE, PEAK

### Community 28 - "Command Rejection Reasons"
Cohesion: 0.29
Nodes (7): BlankEntry, EntryIdConflict, EntryNotFound, EntryTooLong, InvalidColumnCount, InvertedRange, Rejection

### Community 30 - "DayPhase Helpers"
Cohesion: 0.40
Nodes (4): DayPhase, FUTURE, PAST, TODAY

### Community 31 - "Grid Corner Prefs"
Cohesion: 0.40
Nodes (5): GridCorner, BOTTOM_END, BOTTOM_START, TOP_END, TOP_START

### Community 32 - "Journal Events"
Cohesion: 0.40
Nodes (5): ChallengeSettingsChanged, EntryAdded, EntryDeleted, EntryEdited, JournalEvent

### Community 33 - "Product Overview Docs"
Cohesion: 0.40
Nodes (5): 100 Days, Contribution Grid, Glance Widget, Local-first Android App, Room Journal Entries

### Community 34 - "CommandResult Variants"
Cohesion: 0.50
Nodes (4): Applied, CommandResult, Rejected, Unchanged

### Community 37 - "Idempotent NoChange"
Cohesion: 0.50
Nodes (4): EntryAlreadyAbsent, EntryAlreadyMatches, NoChange, SettingsAlreadyMatch

### Community 38 - "Gradle Wrapper Scripts"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 39 - "Challenge Window Lens"
Cohesion: 0.67
Nodes (3): Challenge Window as Journal Lens, Inclusive Challenge Window, Entries Outside Challenge Window

## Knowledge Gaps
- **103 isolated node(s):** `TOP_START`, `TOP_END`, `EntryAdded`, `EntryEdited`, `EntryDeleted` (+98 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 266 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **12 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `DayKey` connect `DayKey Value Type` to `Glance Widget Actions`, `Room Journal Mutations`, `Compose Material UI`, `In-App Grid Screen`, `DataStore Challenge Settings`, `Navigation and ChallengeState`?**
  _High betweenness centrality (0.097) - this node is a cross-community bridge._
- **Why does `JournalService` connect `Navigation and ChallengeState` to `App DI and Data Wiring`, `Compose Material UI`, `JournalService Runtime`?**
  _High betweenness centrality (0.069) - this node is a cross-community bridge._
- **Why does `RoomJournalStore` connect `RoomJournalStore Reads` to `App DI and Data Wiring`, `Room Journal Mutations`?**
  _High betweenness centrality (0.041) - this node is a cross-community bridge._
- **What connects `TOP_START`, `TOP_END`, `EntryAdded` to the rest of the system?**
  _103 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Glance Widget Actions` be split into smaller, more focused modules?**
  _Cohesion score 0.0544464609800363 - nodes in this community are weakly interconnected._
- **Should `App DI and Data Wiring` be split into smaller, more focused modules?**
  _Cohesion score 0.058279370952821465 - nodes in this community are weakly interconnected._
- **Should `Room Journal Mutations` be split into smaller, more focused modules?**
  _Cohesion score 0.06620209059233449 - nodes in this community are weakly interconnected._