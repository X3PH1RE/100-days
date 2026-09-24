package com.x3phire.hundreddays.di

import android.content.Context
import androidx.room.Room
import com.x3phire.hundreddays.data.AppDatabase
import com.x3phire.hundreddays.data.DataStoreChallengeSettingsStore
import com.x3phire.hundreddays.data.GlanceGridInvalidator
import com.x3phire.hundreddays.data.RoomJournalStore
import com.x3phire.hundreddays.data.SystemLocalClock
import com.x3phire.hundreddays.domain.DefaultJournalService
import com.x3phire.hundreddays.domain.JournalService

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    private val database: AppDatabase =
        Room.databaseBuilder(appContext, AppDatabase::class.java, "hundred_days.db")
            .fallbackToDestructiveMigration()
            .build()

    val journalService: JournalService =
        DefaultJournalService(
            journals = RoomJournalStore(database.entryDao()),
            settings = DataStoreChallengeSettingsStore(appContext),
            clock = SystemLocalClock(),
            invalidator = GlanceGridInvalidator(appContext),
        )

    val transcriptCleaner: com.x3phire.hundreddays.domain.TranscriptCleaner =
        com.x3phire.hundreddays.data.OnDeviceTranscriptCleaner()
}
