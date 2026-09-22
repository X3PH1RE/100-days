package com.x3phire.hundreddays.domain

@ConsistentCopyVisibility
data class ChallengeWindow private constructor(
    override val start: DayKey,
    override val endInclusive: DayKey,
) : ClosedRange<DayKey> {
    companion object {
        fun validated(start: DayKey, endInclusive: DayKey): ChallengeWindow {
            require(start <= endInclusive) {
                "Challenge end must not be before start"
            }
            return ChallengeWindow(start, endInclusive)
        }

        fun of(start: DayKey, endInclusive: DayKey): ChallengeWindow? =
            if (start <= endInclusive) ChallengeWindow(start, endInclusive) else null
    }

    fun dates(): List<DayKey> {
        val out = ArrayList<DayKey>()
        var cursor = start
        while (cursor <= endInclusive) {
            out.add(cursor)
            cursor = DayKey.from(cursor.value.plusDays(1))
        }
        return out
    }
}

@JvmInline
value class GridLayout private constructor(
    val columns: Int,
) {
    companion object {
        const val MIN_COLUMNS: Int = 2
        const val MAX_COLUMNS: Int = 31
        val TEN: GridLayout = GridLayout(10)

        fun validated(columns: Int): GridLayout {
            require(columns in MIN_COLUMNS..MAX_COLUMNS) {
                "Unsupported column count: $columns"
            }
            return GridLayout(columns)
        }

        fun of(columns: Int): GridLayout? =
            if (columns in MIN_COLUMNS..MAX_COLUMNS) GridLayout(columns) else null
    }
}

enum class DaysLeftCorner {
    TOP_START,
    TOP_END,
}

data class ChallengeSettings(
    val window: ChallengeWindow,
    val layout: GridLayout,
    val daysLeftCorner: DaysLeftCorner,
)
