package com.x3phire.hundreddays.domain

import java.time.temporal.ChronoUnit
import kotlin.math.max

data class GridCell(
    val day: DayKey,
    val count: Int,
    val intensity: Intensity,
    val phase: DayPhase,
)

data class ContributionGridModel(
    val settings: ChallengeSettings,
    val cells: List<GridCell>,
    val daysLeft: Int,
    val corner: DaysLeftCorner,
)

object ContributionGrid {
    fun project(
        settings: ChallengeSettings,
        countsByDay: Map<DayKey, Int>,
        today: DayKey,
    ): ContributionGridModel {
        val cells = settings.window.dates().map { day ->
            val count = countsByDay[day] ?: 0
            GridCell(
                day = day,
                count = count,
                intensity = Intensity.forCount(count),
                phase = DayPhase.of(day, today),
            )
        }
        return ContributionGridModel(
            settings = settings,
            cells = cells,
            daysLeft = daysLeft(settings.window.endInclusive, today),
            corner = settings.daysLeftCorner,
        )
    }

    fun daysLeft(endInclusive: DayKey, today: DayKey): Int {
        val remaining = ChronoUnit.DAYS.between(today.value, endInclusive.value).toInt() + 1
        return max(0, remaining)
    }
}

sealed interface ChallengeState {
    data object NotConfigured : ChallengeState
    data class Active(val overview: ContributionGridModel) : ChallengeState
}
