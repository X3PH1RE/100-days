package com.x3phire.hundreddays.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.x3phire.hundreddays.domain.ChallengeSettings
import com.x3phire.hundreddays.domain.ChallengeSettingsStore
import com.x3phire.hundreddays.domain.ChallengeWindow
import com.x3phire.hundreddays.domain.DayKey
import com.x3phire.hundreddays.domain.DaysLeftCorner
import com.x3phire.hundreddays.domain.GridLayout
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.challengeDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "challenge_settings",
)

internal class DataStoreChallengeSettingsStore(
    private val context: Context,
) : ChallengeSettingsStore {
    private val dataStore = context.challengeDataStore

    override fun observe(): Flow<ChallengeSettings?> =
        dataStore.data.map { prefs -> prefs.toSettings() }

    override suspend fun replace(settings: ChallengeSettings): Boolean {
        val current = dataStore.data.first().toSettings()
        if (current == settings) return false
        dataStore.edit { prefs ->
            prefs[KEY_START] = settings.window.start.value.toString()
            prefs[KEY_END] = settings.window.endInclusive.value.toString()
            prefs[KEY_COLUMNS] = settings.layout.columns
            prefs[KEY_CORNER] = settings.daysLeftCorner.name
        }
        return true
    }

    private fun Preferences.toSettings(): ChallengeSettings? {
        val startRaw = this[KEY_START] ?: return null
        val endRaw = this[KEY_END] ?: return null
        val columns = this[KEY_COLUMNS] ?: return null
        val cornerRaw = this[KEY_CORNER] ?: return null
        val start = DayKey.parse(startRaw) ?: return null
        val end = DayKey.parse(endRaw) ?: return null
        val window = ChallengeWindow.of(start, end) ?: return null
        val layout = GridLayout.of(columns) ?: return null
        val corner = runCatching { DaysLeftCorner.valueOf(cornerRaw) }.getOrNull() ?: return null
        return ChallengeSettings(window, layout, corner)
    }

    private companion object {
        val KEY_START = stringPreferencesKey("start")
        val KEY_END = stringPreferencesKey("end")
        val KEY_COLUMNS = intPreferencesKey("columns")
        val KEY_CORNER = stringPreferencesKey("days_left_corner")
    }
}
