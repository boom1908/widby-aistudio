package com.example.data.repository

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.example.data.db.StreakDao
import com.example.data.model.StreakEntity
import com.example.data.model.StreakLogEntity
import com.example.data.model.StreakType
import com.example.data.model.WidgetConfigEntity
import com.example.widget.MultiStreakGlanceWidget
import com.example.widget.StreakGlanceWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class StreakRepository(
    private val context: Context,
    private val streakDao: StreakDao
) {
    val allStreaks: Flow<List<StreakEntity>> = streakDao.getAllStreaks()

    fun getStreakById(id: Long): Flow<StreakEntity?> = streakDao.getStreakById(id)

    suspend fun getStreakByIdSync(id: Long): StreakEntity? = streakDao.getStreakByIdSync(id)

    fun getLogsForStreak(streakId: Long): Flow<List<StreakLogEntity>> =
        streakDao.getLogsForStreak(streakId)

    suspend fun insertStreak(streak: StreakEntity): Long {
        val id = streakDao.insertStreak(streak)
        updateAllWidgets()
        return id
    }

    suspend fun updateStreak(streak: StreakEntity) {
        streakDao.updateStreak(streak)
        updateAllWidgets()
    }

    suspend fun deleteStreak(id: Long) {
        streakDao.deleteStreak(id)
        streakDao.deleteAllLogsForStreak(id)
        updateAllWidgets()
    }

    suspend fun incrementManualStreak(streakId: Long): StreakEntity? = withContext(Dispatchers.IO) {
        val streak = streakDao.getStreakByIdSync(streakId) ?: return@withContext null
        if (streak.type != StreakType.MANUAL_TAP) return@withContext streak

        val now = System.currentTimeMillis()
        val todayStr = getLogicalDateString(now, streak.resetHour, streak.resetMinute)
        val todayLog = streakDao.getLogForDate(streakId, todayStr)

        val currentCal = Calendar.getInstance()
        val currentWeek = currentCal.get(Calendar.WEEK_OF_YEAR)
        val currentYear = currentCal.get(Calendar.YEAR)

        var freezeUsedThisWeek = streak.freezeUsedThisWeek
        var lastFreezeWeek = streak.lastFreezeWeekOfYear
        var lastFreezeYear = streak.lastFreezeYear

        // Check if freeze reset is needed for new week
        if (lastFreezeYear != currentYear || lastFreezeWeek != currentWeek) {
            freezeUsedThisWeek = false
        }

        var newCount = streak.currentCount + 1

        if (todayLog == null) {
            // First tap for this logical day
            val yesterdayMillis = now - 86400000L
            val yesterdayStr = getLogicalDateString(yesterdayMillis, streak.resetHour, streak.resetMinute)
            val yesterdayLog = streakDao.getLogForDate(streakId, yesterdayStr)

            if (yesterdayLog == null && streak.currentCount > 0) {
                // User missed yesterday!
                if (streak.streakFreezeAllowed && !freezeUsedThisWeek) {
                    // Apply freeze to save yesterday
                    streakDao.insertOrUpdateLog(
                        StreakLogEntity(
                            streakId = streakId,
                            dateString = yesterdayStr,
                            count = 0,
                            isFreezeUsed = true,
                            timestamp = yesterdayMillis
                        )
                    )
                    freezeUsedThisWeek = true
                    lastFreezeWeek = currentWeek
                    lastFreezeYear = currentYear
                } else {
                    // No freeze available, streak resets to 1
                    newCount = 1
                }
            }

            streakDao.insertOrUpdateLog(
                StreakLogEntity(
                    streakId = streakId,
                    dateString = todayStr,
                    count = 1,
                    isFreezeUsed = false,
                    timestamp = now
                )
            )
        } else {
            // Consecutive tap today
            streakDao.insertOrUpdateLog(
                todayLog.copy(count = todayLog.count + 1, timestamp = now)
            )
        }

        val updated = streak.copy(
            currentCount = newCount,
            bestStreak = maxOf(streak.bestStreak, newCount),
            totalTaps = streak.totalTaps + 1,
            lastIncrementMillis = now,
            freezeUsedThisWeek = freezeUsedThisWeek,
            lastFreezeWeekOfYear = lastFreezeWeek,
            lastFreezeYear = lastFreezeYear
        )
        streakDao.updateStreak(updated)
        updateAllWidgets()
        updated
    }

    suspend fun undoLastIncrement(streakId: Long): StreakEntity? = withContext(Dispatchers.IO) {
        val streak = streakDao.getStreakByIdSync(streakId) ?: return@withContext null
        if (streak.type != StreakType.MANUAL_TAP || streak.currentCount <= 0) return@withContext streak

        val now = System.currentTimeMillis()
        val todayStr = getLogicalDateString(now, streak.resetHour, streak.resetMinute)
        val todayLog = streakDao.getLogForDate(streakId, todayStr)

        if (todayLog != null) {
            if (todayLog.count > 1) {
                streakDao.insertOrUpdateLog(todayLog.copy(count = todayLog.count - 1))
            } else {
                streakDao.deleteLogForDate(streakId, todayStr)
            }
        }

        val newCount = maxOf(0, streak.currentCount - 1)
        val updated = streak.copy(
            currentCount = newCount,
            totalTaps = maxOf(0, streak.totalTaps - 1)
        )
        streakDao.updateStreak(updated)
        updateAllWidgets()
        updated
    }

    suspend fun resetStreak(streakId: Long): StreakEntity? = withContext(Dispatchers.IO) {
        val streak = streakDao.getStreakByIdSync(streakId) ?: return@withContext null
        val now = System.currentTimeMillis()

        val updated = streak.copy(
            currentCount = 0,
            lastResetMillis = now
        )
        streakDao.updateStreak(updated)
        updateAllWidgets()
        updated
    }

    suspend fun syncAllAutoCounters() = withContext(Dispatchers.IO) {
        val streaks = streakDao.getAllStreaksSync()
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()
        val currentWeek = cal.get(Calendar.WEEK_OF_YEAR)
        val currentYear = cal.get(Calendar.YEAR)

        for (streak in streaks) {
            if (streak.type == StreakType.AUTO_DAILY) {
                val daysElapsed = getLogicalDaysBetween(
                    streak.lastResetMillis,
                    now,
                    streak.resetHour,
                    streak.resetMinute
                )
                val newCount = maxOf(0, daysElapsed)
                val newBest = maxOf(streak.bestStreak, newCount)

                val todayStr = getLogicalDateString(now, streak.resetHour, streak.resetMinute)
                if (newCount > 0 && streakDao.getLogForDate(streak.id, todayStr) == null) {
                    streakDao.insertOrUpdateLog(
                        StreakLogEntity(
                            streakId = streak.id,
                            dateString = todayStr,
                            count = 1,
                            isFreezeUsed = false,
                            timestamp = now
                        )
                    )
                }

                if (streak.currentCount != newCount || streak.bestStreak != newBest) {
                    streakDao.updateStreak(
                        streak.copy(
                            currentCount = newCount,
                            bestStreak = newBest
                        )
                    )
                }
            } else {
                // Check if freeze needs resetting
                if (streak.freezeUsedThisWeek &&
                    (streak.lastFreezeYear != currentYear || streak.lastFreezeWeekOfYear != currentWeek)
                ) {
                    streakDao.updateStreak(streak.copy(freezeUsedThisWeek = false))
                }
            }
        }
        updateAllWidgets()
    }

    suspend fun getWidgetConfig(appWidgetId: Int): WidgetConfigEntity? {
        return streakDao.getWidgetConfig(appWidgetId)
    }

    suspend fun saveWidgetConfig(appWidgetId: Int, streakId: Long, customTitle: String) {
        streakDao.setWidgetConfig(
            WidgetConfigEntity(
                appWidgetId = appWidgetId,
                streakId = streakId,
                customTitle = customTitle
            )
        )
        updateAllWidgets()
    }

    suspend fun updateAllWidgets() {
        try {
            StreakGlanceWidget().updateAll(context)
            MultiStreakGlanceWidget().updateAll(context)
        } catch (_: Exception) {
            // Glance updates may fail if no active widget instances exist yet
        }
    }

    fun getLogicalDateString(timestamp: Long, resetHour: Int, resetMinute: Int): String {
        val offset = (resetHour * 3600000L) + (resetMinute * 60000L)
        val shifted = timestamp - offset
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date(shifted))
    }

    fun getLogicalDaysBetween(startMillis: Long, endMillis: Long, resetHour: Int, resetMinute: Int): Int {
        if (endMillis <= startMillis) return 0
        val offset = (resetHour * 3600000L) + (resetMinute * 60000L)
        val shiftedStart = startMillis - offset
        val shiftedEnd = endMillis - offset

        val calStart = Calendar.getInstance().apply {
            timeInMillis = shiftedStart
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val calEnd = Calendar.getInstance().apply {
            timeInMillis = shiftedEnd
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val diff = calEnd.timeInMillis - calStart.timeInMillis
        return TimeUnit.MILLISECONDS.toDays(diff).toInt()
    }
}
