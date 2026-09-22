# 100 Days — Phase A grounding (greenfield)

No existing app code. Constraints come from the build spec and the empty MIT repo at `https://github.com/X3PH1RE/100-days`.

## Product invariants

1. One local challenge window: inclusive start date through inclusive end date.
2. Each calendar day in that window is one grid cell.
3. Cell intensity is the count of journal entries for that local date.
4. Entries may exist for dates outside the window; the window only controls visibility.
5. Fully offline. No accounts. No sync in v1.
6. Android is primary. PWA is a separate codebase with the same conceptual model.

## Dominant access patterns

| Actor | Read | Write |
| --- | --- | --- |
| In-app grid / Glance widget | range settings, per-day counts for all days in range | none (display) |
| Day entry card | all entries for one date | insert / update / delete entry |
| Onboarding / settings | current range + layout + UI prefs | replace range/layout/prefs |
| Midnight refresh | today vs end date | widget UI only |
| Voice cleanup | raw transcript | new draft bullets (not persisted until user saves) |

## Shared mutable state

- Room DB file: app process + WorkManager / widget host may read. Writes only from the app process on the main/user path. Widget is read-only against derived counts.
- DataStore settings: single-writer from settings/onboarding screens.
- Isolation rule: widget and WorkManager never write entries. They only call `GlanceAppWidget.update`.

## Out of scope for the first architecture package

Voice LLM providers, Glance resize polish, PWA host deploy. Those hang off the same domain types later.
