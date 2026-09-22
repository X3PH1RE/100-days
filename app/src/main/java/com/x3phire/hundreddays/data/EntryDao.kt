package com.x3phire.hundreddays.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: EntryEntity)

    @Update
    suspend fun update(entity: EntryEntity)

    @Query("SELECT * FROM entries WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): EntryEntity?

    @Query("DELETE FROM entries WHERE id = :id")
    suspend fun deleteById(id: String): Int

    @Query("SELECT * FROM entries WHERE date = :date ORDER BY createdAt ASC")
    fun observeByDate(date: String): Flow<List<EntryEntity>>

    @Query(
        """
        SELECT date AS date, COUNT(*) AS count
        FROM entries
        WHERE date BETWEEN :start AND :end
        GROUP BY date
        """,
    )
    fun observeCountsBetween(start: String, end: String): Flow<List<DayCountRow>>
}
