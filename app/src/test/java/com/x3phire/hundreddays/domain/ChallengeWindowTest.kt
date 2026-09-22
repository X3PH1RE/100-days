package com.x3phire.hundreddays.domain

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class ChallengeWindowTest {
    @Test
    fun validated_rejectsInvertedRange() {
        val start = DayKey.from(LocalDate.of(2026, 2, 1))
        val end = DayKey.from(LocalDate.of(2026, 1, 1))
        assertThrows(IllegalArgumentException::class.java) {
            ChallengeWindow.validated(start, end)
        }
        assertNull(ChallengeWindow.of(start, end))
    }

    @Test
    fun validated_acceptsInclusiveSingleDay() {
        val day = DayKey.from(LocalDate.of(2026, 3, 15))
        val window = ChallengeWindow.validated(day, day)
        assertEquals(day, window.start)
        assertEquals(day, window.endInclusive)
        assertEquals(listOf(day), window.dates())
    }
}
