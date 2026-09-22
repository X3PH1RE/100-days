package com.x3phire.hundreddays.refresh

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.x3phire.hundreddays.widget.ContributionGridWidget
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

class MidnightRefreshWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        ContributionGridWidget().updateAll(applicationContext)
        scheduleNext(applicationContext)
        return Result.success()
    }

    companion object {
        const val UNIQUE_NAME: String = "hundred_days_midnight_refresh"

        fun scheduleNext(context: Context) {
            val delay = millisUntilNextLocalMidnight()
            val request = OneTimeWorkRequestBuilder<MidnightRefreshWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build()
            WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
                UNIQUE_NAME,
                ExistingWorkPolicy.REPLACE,
                request,
            )
        }

        fun millisUntilNextLocalMidnight(): Long {
            val zone = ZoneId.systemDefault()
            val now = LocalDateTime.now(zone)
            val nextMidnight = LocalDate.now(zone).plusDays(1).atTime(LocalTime.MIDNIGHT)
            val delay = Duration.between(now, nextMidnight).toMillis()
            return delay.coerceAtLeast(1_000L)
        }
    }
}
