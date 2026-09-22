# Arena frame — 100 Days Android domain architecture

## Artifact

One candidate design package under your assigned output directory:

- `USAGE.md` — caller usage first (Compose screen / widget call sites)
- `TYPES.kt` — core types and signatures with `not implemented` bodies
- `MODULES.md` — module map
- `RATIONALE.md` — shaped per architect `references/rationale-template.md`

## Rubric (picker only; candidates do not see scores)

1. Domain types make illegal ranges and orphan intensity math unrepresentable or derived once.
2. Public surface for UI/widget is small; storage/framework types stay behind repositories.
3. Dominant paths (grid counts, day entries, settings) are first-class, not afterthought indexes.
4. Widget and app share one read model without dual-write of counts.
5. Short call chains (≤3 hops from UI to persistence policy).
6. v1 non-goals respected (no sync, no accounts).

## Constraining brief

Read `docs/design/GROUNDING.md` in the repo root working directory.

Greenfield Kotlin Android app: Jetpack Compose, Glance widget, Room, DataStore, WorkManager, SpeechRecognizer, optional on-device LLM later.

Must support: date-range challenge, contribution-grid intensity from entry counts, day entry CRUD, settings for range and days-left corner, deep link from widget cell to day card.

## Structurally distinct mandate

Your assigned shape steer is below. Do not converge on a middle. Commit to the steer even if another shape feels safer.
