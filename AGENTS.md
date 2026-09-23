# AGENTS.md — 100 Days

Handoff document for any agent continuing this project. Read this before editing code. Prefer `graphify query` / `graphify path` / `graphify explain` when `graphify-out/graph.json` exists and the question is about architecture or relationships.

---

## What this product is

**100 Days** is a local-first Android app (Kotlin, Jetpack Compose, Room, DataStore, Glance) that tracks daily journal entries toward a user-chosen deadline.

- Home / in-app surface: a **GitHub-style contribution graph** (weeks as columns, 7 weekday rows, Mon–Sun).
- Cell intensity = number of journal entries that day (5 buckets: EMPTY → MAX).
- Top copy shows **days left** until the inclusive end date.
- Tapping a cell opens that day’s entry list (add / edit / delete for past and today only).
- **Glance home-screen widget** mirrors the same read model; cell taps deep-link `hundreddays://day/{yyyy-MM-dd}`.
- Fully offline. No accounts, no sync, no network (except optional future model download). PWA companion is **out of band** and not started yet.

Repo: https://github.com/X3PH1RE/100-days  
License: MIT (already on `main`).  
Package / applicationId: `com.x3phire.hundreddays`  
Namespace: `com.x3phire.hundreddays`

---

## Current git / delivery state (as of 2026-09-23)

Branch: `main` tracking `origin/main`.

Recent commits (newest first):

1. `e478953` — Blue UI polish, GitHub-style week grid, Glance widget load fixes, future-day write block.
2. `ea8b388` — Restored Glance widget sources after OneDrive dropped them.
3. `bc98359` — Partial widget commit (deps/test only; sources recovered in ea8b388).
4. `778019f` — Domain + Room + DataStore + Compose vertical slice.
5. `e973370` — Gradle scaffold + architecture docs.

**Uncommitted local change likely present:** `app/src/main/res/xml/contribution_grid_widget_info.xml` switched `previewLayout` → `previewImage` for picker reliability. Include it in the next commit if still dirty.

Working tree often lives under **OneDrive** (`C:\Users\ashwi\OneDrive\Desktop\100 days`). Builds are unreliable there (KSP cache / file locks). Prefer building from the mirror:

`C:\dev\100-days`

Sync with robocopy before build when you edit in the OneDrive workspace.

---

## Architecture (source of truth)

Design arena produced candidates A and B. **Ship shape is synthesized Candidate B + grafts from A.**

Read in this order:

1. `docs/design/GROUNDING.md` — product invariants and actors  
2. `docs/design/SYNTHESIS.md` — chosen shape and rejected alternatives  
3. `docs/design/arena-candidate-b/` — base design package  
4. `docs/design/arena-candidate-a/` — grafts only (intensity projector, upsert-by-id, store-owned invalidation)

### Public surface (only thing UI / widget should call)

```kotlin
interface JournalService {
    fun observeDay(day: DayKey): Flow<DayJournal>
    fun observeChallenge(): Flow<ChallengeState>  // NotConfigured | Active(ContributionGridModel)
    suspend fun execute(command: JournalCommand): CommandResult  // Applied | Unchanged | Rejected
}
```

Commands: `AddEntry`, `EditEntry`, `DeleteEntry`, `SetRange`.

### Package map inside single module `:app`

```
app/src/main/java/com/x3phire/hundreddays/
  domain/     Pure Kotlin. DayKey, ChallengeWindow, ContributionGrid, JournalService,
              Intensity, DayPhase, WeekColumn / WeekSlot, commands, ports.
              ZERO android.* imports.
  data/       Room (EntryEntity/Dao/AppDatabase), RoomJournalStore,
              DataStoreChallengeSettingsStore, SystemLocalClock, GlanceGridInvalidator.
  di/         AppContainer wires Room + DataStore + DefaultJournalService + Glance invalidator.
  ui/         Compose: onboarding, GitHub-style GridScreen, DayScreen, Settings, theme (blue).
  widget/     ContributionGridWidget + ContributionGridWidgetReceiver + OpenDayAction.
  refresh/    MidnightRefreshWorker (OneTimeWork → next local midnight) + BootCompletedReceiver.
```

Composition root: `HundredDaysApp` creates `AppContainer` and schedules midnight refresh.

### Load-bearing invariants (do not break)

1. **One challenge window** (inclusive start/end). Window controls **visibility**, not entry existence. Entries outside the window remain in Room.
2. **Counts are derived**, never stored. `GROUP BY date` in Room; projector fills missing days with zero / EMPTY.
3. **Widget and WorkManager never write journal or settings.** They only `updateAll` / schedule.
4. **Validation at `JournalService.execute` only.** Stores receive trusted domain types.
5. **Upsert by `EntryId`** for adds so retries / double-taps converge.
6. **Future days are read-only for notes.** `Rejection.FutureDay` on Add/Edit when `day > clock.today()`. UI hides composer; viewing/delete of existing rows allowed for cleanup.
7. **Today marking:** thin border / ring on `DayPhase.TODAY` in both Compose grid and Glance cells.
8. **GitHub week layout:** `ContributionGrid.toWeekColumns()` / `ContributionGridModel.asWeekColumns()` — Monday-aligned columns of 7 slots (`WeekSlot.Padding` | `WeekSlot.Day`).

### Intensity buckets

| Count | Intensity |
| --- | --- |
| 0 | EMPTY |
| 1 | LOW |
| 2 | MEDIUM |
| 3 | HIGH |
| 4+ | MAX |

UI maps via `Intensity.toCellColor()` (blue scale). Do not scatter bucket thresholds.

### Deep link

- Scheme: `hundreddays://day/{LocalDate}`  
- Manifest intent-filter on `MainActivity`.  
- Nav route: `day/{day}` with `navDeepLink`.  
- Widget uses `OpenDayAction` (`ActionCallback`) with `ActionParameters` key `day_key`.

---

## What is already implemented

- [x] Room `entries(id, date, text, createdAt, updatedAt)` + grouped counts query  
- [x] DataStore challenge settings (window, columns, days-left corner)  
- [x] Onboarding date range → `SetRange`  
- [x] In-app GitHub-style horizontal scroll grid + intensity legend  
- [x] Day screen CRUD (blocked for future)  
- [x] Settings (range, days-left corner; columns still stored but week layout ignores column count for display)  
- [x] Glance widget (week columns, today ring, days-left badge, click → day)  
- [x] `GlanceGridInvalidator` after successful writes  
- [x] Midnight WorkManager refresh + boot reschedule  
- [x] Unit tests: ChallengeWindow, Intensity, ContributionGrid (+ week columns), MidnightRefreshWorker delay  
- [x] Blue theme / serif display typography / gradient `AppBackground`  

## What is NOT done (suggested next slices)

Build order from original spec remaining:

1. **SpeechRecognizer** on day card (raw transcript first). Permission already in manifest (`RECORD_AUDIO`).  
2. **On-device LLM bullet cleanup** (Gemini Nano / MediaPipe) with rule-based fallback; settings toggle.  
3. **Widget polish:** resize / SizeMode, ensure picker visibility across OEMs, optional pin-from-app.  
4. **PWA companion** (separate codebase, IndexedDB, Web Speech) — not blocking Android.  
5. Optional: daily reminder notifications (explicitly non-goal for v1 unless asked).

Also update `docs/design/SYNTHESIS.md` “Next implementation step” — it is stale (domain/UI/widget already shipped).

---

## Critical build / APK gotchas (read carefully)

### OneDrive vs `C:\dev\100-days`

| Path | Role |
| --- | --- |
| `C:\Users\ashwi\OneDrive\Desktop\100 days` | Cursor workspace / git checkout. KSP and `clean` often fail here. |
| `C:\dev\100-days` | Preferred Gradle build tree. Robocopy sources here, then `gradlew`. |

JDK: Microsoft OpenJDK 17  
`JAVA_HOME=C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot`  
Android SDK: `C:\Users\ashwi\AppData\Local\Android\Sdk` (API 34/35, build-tools 35).  
`local.properties` is gitignored; recreate `sdk.dir` if missing.

```powershell
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
$env:ANDROID_HOME = "C:\Users\ashwi\AppData\Local\Android\Sdk"
robocopy "C:\Users\ashwi\OneDrive\Desktop\100 days" "C:\dev\100-days" /E /XD .gradle build app\build .idea
cd C:\dev\100-days
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug --no-daemon --max-workers=1
```

### Wrong APK already burned a user session

The APK under the OneDrive project’s `app\build\outputs\apk\debug\` was once **missing** `ContributionGridWidgetReceiver` (Glance trampolines present, our receiver absent). The good APK lived at:

`C:\dev\100-days\app\build\outputs\apk\debug\app-debug.apk`

A verified copy was placed at:

`C:\Users\ashwi\OneDrive\Desktop\100-Days-widget.apk`

**Before telling the user to install:** run `aapt dump xmltree <apk> AndroidManifest.xml` and confirm `ContributionGridWidgetReceiver` appears.

After install: user must **open the app once** before Android shows the widget in the picker (Android 12+). Then long-press home → Widgets → search **100 Days** / **100 Days grid**.

---

## Widget implementation notes

- Receiver: `ContributionGridWidgetReceiver` → `GlanceAppWidgetReceiver`.  
- Provider XML: `res/xml/contribution_grid_widget_info.xml` (wide/short for horizontal graph). Prefer `previewImage` over fragile `previewLayout`.  
- `SizeMode.Exact`. Week columns capped with `takeLast(MaxWidgetWeeks)` (16) to reduce RemoteViews / binder pressure.  
- Prefer `actionRunCallback<OpenDayAction>` over one PendingIntent Intent per cell when possible.  
- `provideGlance` must tolerate missing Application cast and fall back to `NotConfigured` UI rather than crashing blank.  
- Late-finishing subagents once **deleted** widget sources from the OneDrive tree while claiming “no widget yet.” Always `git status` after delegated work. Restore from `main` if widget files vanish.

---

## UI / theme conventions

- Accent: **blue** scale (`Theme.kt`: Ink, BlueDeep, BlueMid, BlueSoft, BlueMist, BlueEmpty). Not green. Avoid purple defaults.  
- Display type: `FontFamily.Serif`. Body: SansSerif with tightened Material3 scale.  
- Atmosphere: `AppBackground` linear gradient (Paper → PaperDeep → cool blue). Transparent scaffolds on day/settings so gradient shows.  
- Grid cells: 12.dp squares, 2.dp radius, 1.dp today ring (`TodayRing`). Future cells ~0.4 alpha.  
- Do not put day-of-month numerals inside contribution cells (GitHub style is color-only).

---

## Testing

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

Key tests under `app/src/test/.../domain/` and `.../refresh/`. Prefer behavior assertions on projectors and command boundaries over implementation details. When adding Speech/LLM, keep domain pure and test cleanup adapters separately.

---

## graphify

A knowledge graph was built for this repo.

Outputs (under `graphify-out/`):

- `graph.html` — interactive viz  
- `graph.json` — GraphRAG / query input  
- `GRAPH_REPORT.md` — communities, god nodes, surprises  

### Always-on graph rules for agents

1. **Before answering architecture / “how does X relate to Y” questions:** if `graphify-out/graph.json` exists, run `graphify query "<question>"` (or `graphify path` / `graphify explain`) before broad Grep/Glob sweeps.  
2. **After substantive code edits:** run `graphify update .` (AST-only, no API cost) so the graph stays current.  
3. **Broad architecture review:** read `graphify-out/GRAPH_REPORT.md` (or wiki if present).  
4. If no graph exists yet and the user asks about relationships, offer `/graphify .` or `graphify extract . --code-only`.

God nodes observed in the last build (high connectivity): `DayKey`, `DefaultJournalService` / `JournalService`, `RoomJournalStore`, `AppContainer`, `HundredDaysNav`, `DayScreen`. Design-doc ghosts of `HundredDaysStore` (Candidate A) still appear in the graph — that type was **rejected** for shipping; runtime code uses `JournalService`.

Graph health note from last build: some dangling-endpoint edges between AST and sketch docs; graph remains usable. Prefer code communities over design-candidate communities when answering “what does the app do today.”

---

## Tooling / identity

- GitHub CLI authenticated as `X3PH1RE`.  
- Git user already configured (do not rewrite `git config` unless the user asks).  
- Prefer small commits after each verifiable slice; push to `origin/main` when the user wants remote updated.  
- Secrets: run pre-commit-secrets-check before commit/push. Never commit `local.properties`, `.env`, or APKs unless explicitly requested.

---

## Principles already applied (keep them)

- Data structures and domain types before feature sprawl (`ContributionGrid`, `ChallengeState`, branded `DayKey`).  
- Boundary discipline: parse/validate in `JournalService`; trust ports inward.  
- Laziness: single `:app` module for v1; no premature multi-module split.  
- Widget invalidation owned by the service path, not ViewModels.  
- Sequence verifiable units: domain tests → assemble → commit.

---

## Quick start for a new agent session

1. Read this file and `docs/design/SYNTHESIS.md`.  
2. `git status` + `git log -5 --oneline`.  
3. If exploring relationships: `graphify query "..."`.  
4. Edit in the workspace; build in `C:\dev\100-days`.  
5. Verify widget APK with `aapt` before handing install paths to the user.  
6. After code changes: tests + `graphify update .` + commit if asked.

When in doubt, keep the public surface as `JournalService` and keep counts derived.
