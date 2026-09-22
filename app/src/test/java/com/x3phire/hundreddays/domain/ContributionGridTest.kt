package com.x3phire.hundreddays.domain

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class ContributionGridTest {
    private val start = DayKey.from(LocalDate.of(2026, 1, 1))
    private val end = DayKey.from(LocalDate.of(2026, 1, 5))
    private val settings = ChallengeSettings(
        window = ChallengeWindow.validated(start, end),
        layout = GridLayout.TEN,
        daysLeftCorner = DaysLeftCorner.TOP_END,
    )

    @Test
    fun project_fillsMissingDaysAndSetsPhase() {
        val today = DayKey.from(LocalDate.of(2026, 1, 3))
        val counts = mapOf(
            DayKey.from(LocalDate.of(2026, 1, 1)) to 1,
            DayKey.from(LocalDate.of(2026, 1, 3)) to 4,
        )

        val model = ContributionGrid.project(settings, counts, today)

        assertEquals(5, model.cells.size)
        assertEquals(Intensity.LOW, model.cells[0].intensity)
        assertEquals(DayPhase.PAST, model.cells[0].phase)
        assertEquals(0, model.cells[1].count)
        assertEquals(Intensity.EMPTY, model.cells[1].intensity)
        assertEquals(DayPhase.PAST, model.cells[1].phase)
        assertEquals(Intensity.MAX, model.cells[2].intensity)
        assertEquals(DayPhase.TODAY, model.cells[2].phase)
        assertEquals(DayPhase.FUTURE, model.cells[3].phase)
        assertEquals(DayPhase.FUTURE, model.cells[4].phase)
        assertEquals(DaysLeftCorner.TOP_END, model.corner)
        assertEquals(settings, model.settings)
    }

    @Test
    fun weekColumns_areMondayAlignedAndSevenTall() {
        val today = DayKey.from(LocalDate.of(2026, 1, 3))
        val model = ContributionGrid.project(settings, emptyMap(), today)
        val weeks = model.asWeekColumns()
        // 2026-01-01 Thursday through 2026-01-05 Monday spans two week columns.
        assertEquals(2, weeks.size)
        assertEquals(7, weeks[0].slots.size)
        assertEquals(WeekSlot.Padding, weeks[0].slots[0])
        assertEquals(WeekSlot.Padding, weeks[0].slots[1])
        assertEquals(WeekSlot.Padding, weeks[0].slots[2])
        val thursday = weeks[0].slots[3] as WeekSlot.Day
        assertEquals(LocalDate.of(2026, 1, 1), thursday.cell.day.value)
        val monday = weeks[1].slots[0] as WeekSlot.Day
        assertEquals(LocalDate.of(2026, 1, 5), monday.cell.day.value)
    }

    @Test
    fun daysLeft_inclusiveAndClamped() {
        val endInclusive = DayKey.from(LocalDate.of(2026, 1, 10))
        assertEquals(
            10,
            ContributionGrid.daysLeft(endInclusive, DayKey.from(LocalDate.of(2026, 1, 1))),
        )
        assertEquals(
            1,
            ContributionGrid.daysLeft(endInclusive, DayKey.from(LocalDate.of(2026, 1, 10))),
        )
        assertEquals(
            0,
            ContributionGrid.daysLeft(endInclusive, DayKey.from(LocalDate.of(2026, 1, 11))),
        )
        assertEquals(
            0,
            ContributionGrid.daysLeft(endInclusive, DayKey.from(LocalDate.of(2026, 2, 1))),
        )
    }
}
