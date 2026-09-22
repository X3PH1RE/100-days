package com.x3phire.hundreddays.domain

@JvmInline
value class EntryText private constructor(val value: String) {
    companion object {
        const val MAX_LENGTH: Int = 4_000

        fun parse(raw: String): EntryTextParse {
            val trimmed = raw.trim()
            return when {
                trimmed.isEmpty() -> EntryTextParse.Blank
                trimmed.length > MAX_LENGTH -> EntryTextParse.TooLong(MAX_LENGTH)
                else -> EntryTextParse.Ok(EntryText(trimmed))
            }
        }
    }
}

sealed interface EntryTextParse {
    data class Ok(val text: EntryText) : EntryTextParse
    data object Blank : EntryTextParse
    data class TooLong(val maximumCharacters: Int) : EntryTextParse
}
