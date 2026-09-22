// Candidate A type sketch. Derived from USAGE.md, which is the spec.
// Bodies are `TODO("not implemented")`. Pseudocode marks the logic worth arguing about.
//
// Everything below compiles as pure Kotlin. `:core:domain` has no Android dependency, so the
// projector and the window arithmetic are unit-testable with literal dates.
//
// Section headers name the module each block lands in. MODULES.md has the dependency map.

@file:Suppress("unused")

package com.hundreddays.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

// ---------------------------------------------------------------------------
// :core:domain / public surface
// ---------------------------------------------------------------------------

/**
 * The only type a screen, widget, or worker imports.
 *
 * Reads are projected read models, not rows. Writes are whole intents that converge, so a retry
 * or a double tap lands the same state. Every write invalidates the widget exactly once, which is
 * why no caller does it.
 *
 * Both read flows are total. There is no unset state, because an unset challenge is served as a
 * 100 day window starting today.
 */
interface HundredDaysStore {

    /** Current challenge, for settings and onboarding to edit. Never empty. */
    val challenge: Flow<Challenge>

    /**
     * The one grid read model shared by the in-app grid and the Glance widget.
     *
     * Re-emits when the challenge changes, when a day's entry count changes, or when the local
     * date rolls over. Counts are always fetched for the current window, so a model can never
     * pair a window with counts from a different one.
     */
    val grid: Flow<ContributionGridModel>

    /** One-shot projection for callers that need a value before their first composition. */
    suspend fun gridNow(): ContributionGridModel

    /**
     * Entries for one local date, plus that date's position in the challenge.
     *
     * The date may sit outside the window. Entries exist independently of the window, which only
     * controls what the grid displays.
     */
    fun dayCard(date: LocalDate): Flow<DayCard>

    /**
     * Upsert one entry. Idempotent in [id]: the same id edits in place rather than inserting again.
     * `createdAt` is set on first write and preserved afterwards.
     */
    suspend fun saveEntry(id: EntryId, date: LocalDate, body: EntryBody)

    /** Idempotent. Deleting an absent id is a no-op, not an error. */
    suspend fun deleteEntry(id: EntryId)

    /** Whole-aggregate replace. No per-field setters, so no two fields can drift apart. */
    suspend fun setChallenge(challenge: Challenge)
}

// ---------------------------------------------------------------------------
// :core:domain / the aggregate
// ---------------------------------------------------------------------------

/**
 * Everything the user configured about their run. The date range, how the grid is laid out, and
 * where the days-left badge sits.
 *
 * v1 has exactly one challenge and no id. Entries are keyed by date alone, so re-targeting the
 * window re-scopes what is visible without touching a single entry.
 */
data class Challenge(
    val window: ChallengeWindow,
    val layout: GridLayout,
    val daysLeft: DaysLeftPref,
) {
    companion object {
        /**
         * The shape a first-run user sees before onboarding writes anything. Computed from
         * `today` rather than stored, so the default has no persisted state to migrate.
         */
        fun startingOn(today: LocalDate): Challenge = Challenge(
            window = ChallengeWindow(start = today, length = DayCount.HUNDRED),
            layout = GridLayout.WeekAligned(weekStart = DayOfWeek.MONDAY),
            daysLeft = DaysLeftPref.At(GridCorner.TOP_END),
        )
    }
}

/**
 * An inclusive date range that cannot be built backwards.
 *
 * Constructed from a start plus a length rather than two dates, so `end < start` is not a state
 * this type can hold. Callers holding two picked dates go through [of], which is the one place a
 * raw pair is validated.
 */
data class ChallengeWindow(
    val start: LocalDate,
    val length: DayCount,
) {
    val endInclusive: LocalDate get() = TODO("not implemented: start.plus(length.days - 1, DAY)")

    /** All dates in the window, ascending. Size is exactly `length.days`. */
    fun dates(): List<LocalDate> = TODO("not implemented")

    /** Where a date sits relative to this window. Total, so no caller needs a range check. */
    fun placementOf(date: LocalDate): DayPlacement = TODO("not implemented")

    /**
     * The single source of truth for "where are we in the run". The grid badge, the day card, and
     * any future streak feature read this instead of recomputing day arithmetic.
     */
    fun progressOn(today: LocalDate): ChallengeProgress = TODO(
        """
        not implemented
        TODO: today < start          -> NotStarted(daysUntilStart = start - today)
        TODO: today > endInclusive   -> Complete
        TODO: otherwise              -> Active(
                  dayNumber = (today - start) + 1,
                  daysRemaining = endInclusive - today,
              )
        """
    )

    companion object {
        /** Boundary parse for a date-range picker. Null when the pair is out of order. */
        fun of(start: LocalDate, endInclusive: LocalDate): ChallengeWindow? =
            TODO("not implemented: DayCount.of(daysBetween(start, endInclusive) + 1)?.let { ... }")
    }
}

/** A window length of at least one day. */
@JvmInline
value class DayCount private constructor(val days: Int) {
    companion object {
        val HUNDRED = DayCount(100)

        fun of(days: Int): DayCount? = if (days >= 1) DayCount(days) else null
    }
}

/**
 * How the window's days become a rectangle.
 *
 * Two variants, because they carry genuinely different layout rules. [WeekAligned] needs leading
 * padding so a date lands under its weekday. [Fixed] does not. Both are resolved once, inside
 * [ContributionGrid.project].
 */
sealed interface GridLayout {

    /** Calendar-shaped. Seven columns, weeks as rows, first row padded to the start weekday. */
    data class WeekAligned(val weekStart: DayOfWeek) : GridLayout

    /** Poster-shaped. Ten columns is the 10x10 hundred-day grid. */
    data class Fixed(val columns: ColumnCount) : GridLayout
}

@JvmInline
value class ColumnCount private constructor(val count: Int) {
    companion object {
        val TEN = ColumnCount(10)

        /** Capped so a pathological value cannot produce a one-cell-wide grid on a phone. */
        fun of(count: Int): ColumnCount? = if (count in 2..31) ColumnCount(count) else null
    }
}

/**
 * Where the days-left badge draws, or that it does not.
 *
 * A sealed pair rather than a corner plus a `visible` boolean, so "hidden at the top end" is not
 * a state that compiles.
 */
sealed interface DaysLeftPref {
    data object Hidden : DaysLeftPref
    data class At(val corner: GridCorner) : DaysLeftPref
}

enum class GridCorner { TOP_START, TOP_END, BOTTOM_START, BOTTOM_END }

// ---------------------------------------------------------------------------
// :core:domain / the child fact
// ---------------------------------------------------------------------------

/**
 * One journal entry, keyed by the local date it belongs to.
 *
 * Deliberately carries no challenge reference. Entries outside the current window stay valid and
 * become visible again if the window moves back over them.
 */
data class JournalEntry(
    val id: EntryId,
    val date: LocalDate,
    val body: EntryBody,
    val createdAt: Instant,
    val updatedAt: Instant,
)

/**
 * Caller-minted identity, which is what makes [HundredDaysStore.saveEntry] an idempotent upsert
 * instead of an insert that a double tap duplicates.
 */
@JvmInline
value class EntryId private constructor(val value: String) {
    companion object {
        fun new(): EntryId = TODO("not implemented: EntryId(Uuid.random().toString())")

        /** Boundary parse for a deep link or a restored saved-state bundle. */
        fun parse(raw: String): EntryId? = TODO("not implemented")
    }
}

/** Non-blank entry text. Trimmed once at construction, trusted everywhere after. */
@JvmInline
value class EntryBody private constructor(val text: String) {
    companion object {
        fun of(raw: String): EntryBody? = raw.trim().takeIf { it.isNotEmpty() }?.let(::EntryBody)
    }
}

// ---------------------------------------------------------------------------
// :core:domain / derived vocabulary, never stored
// ---------------------------------------------------------------------------

/**
 * Cell shade, derived from a day's entry count.
 *
 * Not a column, not a cached field, not a widget-side copy. [forCount] is the only place the
 * thresholds exist, and the projector is its only caller.
 */
enum class Intensity {
    NONE, LIGHT, MEDIUM, HEAVY, PEAK;

    companion object {
        fun forCount(count: Int): Intensity = TODO(
            "not implemented: 0 -> NONE, 1 -> LIGHT, 2 -> MEDIUM, 3 -> HEAVY, else PEAK"
        )
    }
}

/** A date's relationship to the window. Replaces every `if (date in window)` at a call site. */
sealed interface DayPlacement {
    /** [dayNumber] is 1-based, so day one of a hundred reads as 1. */
    data class InWindow(val dayNumber: Int) : DayPlacement
    data object Outside : DayPlacement
}

/** A date's relationship to now. Drives dimming of days the user cannot have filled yet. */
enum class DayPhase {
    PAST, TODAY, FUTURE;

    companion object {
        fun on(date: LocalDate, today: LocalDate): DayPhase = TODO("not implemented")
    }
}

/** Progress as three distinct situations rather than a set of counters that can contradict. */
sealed interface ChallengeProgress {
    data class NotStarted(val daysUntilStart: Int) : ChallengeProgress
    data class Active(val dayNumber: Int, val daysRemaining: Int) : ChallengeProgress
    data object Complete : ChallengeProgress
}

// ---------------------------------------------------------------------------
// :core:domain / the projector and its read models
// ---------------------------------------------------------------------------

/**
 * The pure function both the app grid and the widget grid are built from.
 *
 * Pure by construction. `today` is a parameter because the clock is a boundary, and [DayCounts] is
 * a value so a test can pass three literal days. Every rendering decision that is not a colour or
 * a dimension is made here, once.
 */
object ContributionGrid {

    /**
     * Invariant: [counts] must cover [Challenge.window]. Absent days read as zero, which is
     * correct for a day with no entries and wrong for a stale fetch, so the store is the only
     * caller and it derives the fetch range from the same window it passes here.
     *
     * Every returned row has the same cell count. The tail of the last row is padded.
     */
    fun project(
        challenge: Challenge,
        counts: DayCounts,
        today: LocalDate,
    ): ContributionGridModel = TODO(
        """
        not implemented
        TODO: columns = when (challenge.layout) {
                  is WeekAligned -> 7
                  is Fixed       -> layout.columns.count
              }
        TODO: leading = when (challenge.layout) {
                  is WeekAligned -> weekdayOffset(window.start, layout.weekStart)  // 0..6
                  is Fixed       -> 0
              }
        TODO: cells = List(leading) { Padding } +
                  window.dates().mapIndexed { i, date ->
                      val count = counts.countOn(date)
                      Day(
                          date = date,
                          dayNumber = i + 1,
                          count = count,
                          intensity = Intensity.forCount(count),
                          phase = DayPhase.on(date, today),
                      )
                  }
        TODO: pad the tail to a whole row, then chunk(columns) into GridRow
        TODO: badgeCorner = (challenge.daysLeft as? DaysLeftPref.At)?.corner
        """
    )
}

/** Render-ready grid. A consumer iterates rows and reads fields. It computes nothing. */
data class ContributionGridModel(
    val rows: List<GridRow>,
    val progress: ChallengeProgress,
    /** Null when the user hid the badge. */
    val badgeCorner: GridCorner?,
)

data class GridRow(val cells: List<GridCell>)

sealed interface GridCell {
    /** Alignment filler. Carries no date, so it cannot be clicked or mistaken for a zero day. */
    data object Padding : GridCell

    data class Day(
        val date: LocalDate,
        val dayNumber: Int,
        val count: Int,
        val intensity: Intensity,
        val phase: DayPhase,
    ) : GridCell
}

/** Render-ready day card. [intensity] matches the grid cell for the same date by construction. */
data class DayCard(
    val date: LocalDate,
    val placement: DayPlacement,
    val phase: DayPhase,
    val entries: List<JournalEntry>,
    val intensity: Intensity,
)

/**
 * Per-day entry counts for one window.
 *
 * A total lookup, so the projector has no null branch. Structural equality on the backing map is
 * what lets the store drop duplicate emissions.
 */
class DayCounts private constructor(private val byDate: Map<LocalDate, Int>) {

    fun countOn(date: LocalDate): Int = byDate[date] ?: 0

    override fun equals(other: Any?): Boolean = other is DayCounts && other.byDate == byDate
    override fun hashCode(): Int = byDate.hashCode()

    companion object {
        val EMPTY = DayCounts(emptyMap())

        fun of(byDate: Map<LocalDate, Int>): DayCounts = DayCounts(byDate)
    }
}

// ---------------------------------------------------------------------------
// :core:domain / the one deep-link decision
// ---------------------------------------------------------------------------

/**
 * The widget builds these and the nav graph parses them. Both sides live in `:app`, so the format
 * lives here to keep one decision in one file.
 */
object DayDeepLink {
    const val PATTERN = "hundreddays://day/{date}"

    fun uri(date: LocalDate): String = TODO("not implemented: ISO-8601 local date, no zone")

    /** Boundary parse. Null for anything that is not this app's day link. */
    fun parse(raw: String): LocalDate? = TODO("not implemented")
}

// ---------------------------------------------------------------------------
// :core:domain / ports.
//
// Implemented in :core:data. Public only because Kotlin `internal` is module-scoped and the DI
// graph in :app has to name them once to wire them. A Konsist test fails the build if any :app
// file outside `di/` references a port type, which is the enforceable version of "callers use the
// store". Per encode-lessons-in-structure, that check is the rule, not this paragraph.
// ---------------------------------------------------------------------------

/**
 * Persisted challenge, or null before onboarding.
 *
 * Null stops at the store, which substitutes [Challenge.startingOn]. The nullability exists at
 * this one boundary so that no read model or screen carries it.
 */
interface ChallengeSettings {
    val stored: Flow<Challenge?>

    /** Replace-only. Single writer, which is why no merge policy exists. */
    suspend fun replace(challenge: Challenge)
}

/**
 * Entry persistence. Counting is a query, not a fold over loaded rows, because the grid reads
 * counts for a hundred days on every emission.
 */
interface JournalStore {

    /** `SELECT date, COUNT(*) ... WHERE date BETWEEN ? AND ? GROUP BY date`. */
    fun counts(window: ChallengeWindow): Flow<DayCounts>

    fun entriesOn(date: LocalDate): Flow<List<JournalEntry>>

    /** `INSERT ... ON CONFLICT(id) DO UPDATE SET body, updatedAt`, so createdAt survives edits. */
    suspend fun upsert(id: EntryId, date: LocalDate, body: EntryBody, at: Instant)

    suspend fun delete(id: EntryId)
}

/** The clock as a boundary, so the projector stays pure and midnight is testable. */
interface LocalClock {
    fun today(): LocalDate

    fun now(): Instant

    /** Emits the new local date at each midnight and on timezone change. */
    fun days(): Flow<LocalDate>
}

/** Glance's `updateAll`, behind an interface so `:core:domain` stays free of Android. */
interface GridInvalidator {
    suspend fun invalidate()
}

// ---------------------------------------------------------------------------
// :core:domain / construction and implementation
// ---------------------------------------------------------------------------

/**
 * The only constructor the DI graph calls. Keeps the implementation class unexported, so the
 * module's public surface stays at the interface plus this function.
 */
fun HundredDaysStore(
    settings: ChallengeSettings,
    journal: JournalStore,
    clock: LocalClock,
    invalidator: GridInvalidator,
): HundredDaysStore = DefaultHundredDaysStore(settings, journal, clock, invalidator)

/**
 * Pure orchestration over the four ports. This is where the depth sits: flow composition, the
 * counts-follow-the-window rule, the default-challenge substitution, and post-write widget
 * invalidation. Callers get none of it.
 */
internal class DefaultHundredDaysStore(
    private val settings: ChallengeSettings,
    private val journal: JournalStore,
    private val clock: LocalClock,
    private val invalidator: GridInvalidator,
) : HundredDaysStore {

    override val challenge: Flow<Challenge> = TODO(
        "not implemented: settings.stored.map { it ?: Challenge.startingOn(clock.today()) }"
    )

    override val grid: Flow<ContributionGridModel> = TODO(
        """
        not implemented
        TODO: challenge
                  .flatMapLatest { c -> journal.counts(c.window).map { counts -> c to counts } }
                  .combine(clock.days()) { (c, counts), today ->
                      ContributionGrid.project(c, counts, today)
                  }
                  .distinctUntilChanged()
        TODO: flatMapLatest is load-bearing. A new window resubscribes the count query, so the
              window and the counts in one emission always agree.
        """
    )

    override suspend fun gridNow(): ContributionGridModel = TODO("not implemented: grid.first()")

    override fun dayCard(date: LocalDate): Flow<DayCard> = TODO(
        """
        not implemented
        TODO: combine(challenge, journal.entriesOn(date), clock.days()) { c, entries, today ->
                  DayCard(
                      date = date,
                      placement = c.window.placementOf(date),
                      phase = DayPhase.on(date, today),
                      entries = entries,
                      intensity = Intensity.forCount(entries.size),
                  )
              }
        """
    )

    override suspend fun saveEntry(id: EntryId, date: LocalDate, body: EntryBody) = TODO(
        "not implemented: journal.upsert(id, date, body, clock.now()); invalidator.invalidate()"
    )

    override suspend fun deleteEntry(id: EntryId) = TODO(
        "not implemented: journal.delete(id); invalidator.invalidate()"
    )

    override suspend fun setChallenge(challenge: Challenge) = TODO(
        "not implemented: settings.replace(challenge); invalidator.invalidate()"
    )
}
