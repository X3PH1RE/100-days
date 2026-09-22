package com.x3phire.hundreddays.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "entries")
data class EntryEntity(
    @PrimaryKey val id: String,
    val date: String,
    val text: String,
    val createdAt: Long,
    val updatedAt: Long,
)

data class DayCountRow(
    val date: String,
    val count: Int,
)
