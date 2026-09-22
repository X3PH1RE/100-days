package com.x3phire.hundreddays.domain

enum class Intensity {
    EMPTY,
    LOW,
    MEDIUM,
    HIGH,
    MAX,
    ;

    companion object {
        fun forCount(count: Int): Intensity =
            when {
                count <= 0 -> EMPTY
                count == 1 -> LOW
                count == 2 -> MEDIUM
                count == 3 -> HIGH
                else -> MAX
            }
    }
}
