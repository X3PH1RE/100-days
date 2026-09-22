package com.x3phire.hundreddays.domain

import java.util.UUID

@JvmInline
value class EntryId(val value: String) {
    companion object {
        fun new(): EntryId = EntryId(UUID.randomUUID().toString())

        fun parse(raw: String): EntryId? =
            raw.takeIf { it.isNotBlank() }?.let(::EntryId)
    }
}
