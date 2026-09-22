package com.x3phire.hundreddays.domain

enum class DayPhase {
    PAST,
    TODAY,
    FUTURE,
    ;

    companion object {
        fun of(day: DayKey, today: DayKey): DayPhase =
            when {
                day < today -> PAST
                day == today -> TODAY
                else -> FUTURE
            }
    }
}
