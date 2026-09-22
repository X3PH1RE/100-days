package com.x3phire.hundreddays.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class IntensityTest {
    @Test
    fun forCount_buckets() {
        assertEquals(Intensity.EMPTY, Intensity.forCount(0))
        assertEquals(Intensity.EMPTY, Intensity.forCount(-1))
        assertEquals(Intensity.LOW, Intensity.forCount(1))
        assertEquals(Intensity.MEDIUM, Intensity.forCount(2))
        assertEquals(Intensity.HIGH, Intensity.forCount(3))
        assertEquals(Intensity.MAX, Intensity.forCount(4))
        assertEquals(Intensity.MAX, Intensity.forCount(99))
    }
}
