package com.x3phire.hundreddays.data

import com.x3phire.hundreddays.domain.DayKey
import com.x3phire.hundreddays.domain.LocalClock
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class SystemLocalClock(
    private val zone: ZoneId = ZoneId.systemDefault(),
    private val clock: Clock = Clock.system(zone),
) : LocalClock {
    override fun today(): DayKey = DayKey.from(LocalDate.now(clock))

    override fun now(): Instant = Instant.now(clock)
}
