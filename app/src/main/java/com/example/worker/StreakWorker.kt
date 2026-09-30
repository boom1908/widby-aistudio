package com.example.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.db.AppDatabase
import com.example.data.model.StreakType
import com.example.data.repository.StreakRepository
import com.example.notification.NotificationHelper
import java.util.Calendar
import java.util.concurrent.TimeUnit

class StreakWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.getInstance(applicationContext)
        val repository = StreakRepository(applicationContext, db.streakDao())

        // 1. Sync auto counters
        repository.syncAllAutoCounters()

        // 2. Check reminders for manual counters
        val streaks = db.streakDao().getAllStreaksSync()
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()
        val currentHour = cal.get(Calendar.HOUR_OF_DAY)
        val currentMinute = cal.get(Calendar.MINUTE)

        val prefs = applicationContext.getSharedPreferences("widby_reminders", Context.MODE_PRIVATE)

        for (streak in streaks) {
            if (streak.type == StreakType.MANUAL_TAP && streak.reminderEnabled) {
                val todayStr = repository.getLogicalDateString(now, streak.resetHour, streak.resetMinute)
                val todayLog = db.streakDao().getLogForDate(streak.id, todayStr)

                // If not tapped today yet and past reminder time
                if (todayLog == null) {
                    val isPastReminder = currentHour > streak.reminderHour ||
                            (currentHour == streak.reminderHour && currentMinute >= streak.reminderMinute)

                    val key = "notif_sent_${streak.id}_$todayStr"
                    val alreadySent = prefs.getBoolean(key, false)

                    if (isPastReminder && !alreadySent) {
                        NotificationHelper.showStreakReminder(applicationContext, streak)
                        prefs.edit().putBoolean(key, true).apply()
                    }
                }
            }
        }

        // 3. Update widgets
        repository.updateAllWidgets()

        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "widby_streak_periodic_worker"

        fun enqueuePeriodicWork(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<StreakWorker>(
                15, TimeUnit.MINUTES
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }
}
