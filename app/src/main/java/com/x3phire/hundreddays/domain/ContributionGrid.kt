package com.x3phire.hundreddays.domain

import java.time.DayOfWeek
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
) {
    fun asWeekColumns(weekStartsOn: DayOfWeek = DayOfWeek.MONDAY): List<WeekColumn> =
        ContributionGrid.toWeekColumns(cells, weekStartsOn)
}

sealed interface WeekSlot {
    data object Padding : WeekSlot
    data class Day(val cell: GridCell) : WeekSlot
}

data class WeekColumn(val slots: List<WeekSlot>) {
    init {
        require(slots.size == 7) { "A week column must hold exactly 7 slots" }
    }
}

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

    fun toWeekColumns(
        cells: List<GridCell>,
        weekStartsOn: DayOfWeek = DayOfWeek.MONDAY,
    ): List<WeekColumn> {
        if (cells.isEmpty()) return emptyList()
        val byDay = cells.associateBy { it.day }
        val first = cells.first().day.value
        val last = cells.last().day.value
        val padStart = ((first.dayOfWeek.value - weekStartsOn.value + 7) % 7)
        var cursor = first.minusDays(padStart.toLong())
        val weeks = ArrayList<WeekColumn>()
        while (!cursor.isAfter(last)) {
            val slots = (0 until 7).map { offset ->
                val date = cursor.plusDays(offset.toLong())
                val key = DayKey.from(date)
                val cell = byDay[key]
                if (cell == null) WeekSlot.Padding else WeekSlot.Day(cell)
            }
            weeks.add(WeekColumn(slots))
            cursor = cursor.plusWeeks(1)
        }
        return weeks
    }
}

sealed interface ChallengeState {
    data object NotConfigured : ChallengeState
    data class Active(val overview: ContributionGridModel) : ChallengeState
}
