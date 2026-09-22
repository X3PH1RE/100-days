package com.x3phire.hundreddays.refresh

import org.junit.Assert.assertTrue
import org.junit.Test

class MidnightRefreshWorkerTest {
    @Test
    fun millisUntilNextLocalMidnight_isPositiveAndUnderOneDay() {
        val delay = MidnightRefreshWorker.millisUntilNextLocalMidnight()
        assertTrue(delay >= 1_000L)
        assertTrue(delay <= 24 * 60 * 60 * 1_000L)
    }
}
