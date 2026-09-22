package com.x3phire.hundreddays.domain

import java.time.LocalDate

@JvmInline
value class DayKey(val value: LocalDate) : Comparable<DayKey> {
    override fun compareTo(other: DayKey): Int = value.compareTo(other.value)

    override fun toString(): String = value.toString()

    companion object {
        fun from(date: LocalDate): DayKey = DayKey(date)

        fun parse(iso: String): DayKey? =
            runCatching { DayKey(LocalDate.parse(iso)) }.getOrNull()
    }
}
