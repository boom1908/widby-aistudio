package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.StreakEntity
import com.example.data.model.StreakLogEntity
import com.example.data.model.StreakType
import com.example.data.model.WidgetConfigEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Database(
    entities = [
        StreakEntity::class,
        StreakLogEntity::class,
        WidgetConfigEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun streakDao(): StreakDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "widby_streak_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database.streakDao())
                    }
                }
            }
        }

        private suspend fun populateInitialData(dao: StreakDao) {
            val now = System.currentTimeMillis()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val cal = Calendar.getInstance()

            // Streak 1: Manual Tap Counter
            val streak1 = StreakEntity(
                name = "Daily Workout",
                type = StreakType.MANUAL_TAP,
                currentCount = 8,
                bestStreak = 15,
                accentColorHex = "#FF5722",
                iconName = "Fitness",
                resetHour = 4, // 4 AM reset
                resetMinute = 0,
                reminderEnabled = true,
                reminderHour = 20,
                reminderMinute = 0,
                streakFreezeAllowed = true,
                freezeUsedThisWeek = false,
                startDateMillis = now - (20L * 86400000L),
                lastIncrementMillis = now,
                totalTaps = 26
            )
            val id1 = dao.insertStreak(streak1)

            // Populate some activity logs for Workout
            for (i in 0..7) {
                cal.time = Date(now - (i * 86400000L))
                val dateStr = dateFormat.format(cal.time)
                dao.insertOrUpdateLog(
                    StreakLogEntity(
                        streakId = id1,
                        dateString = dateStr,
                        count = if (i % 2 == 0) 2 else 1,
                        isFreezeUsed = false,
                        timestamp = cal.timeInMillis
                    )
                )
            }
            // Freeze day 9 days ago
            cal.time = Date(now - (9 * 86400000L))
            dao.insertOrUpdateLog(
                StreakLogEntity(
                    streakId = id1,
                    dateString = dateFormat.format(cal.time),
                    count = 0,
                    isFreezeUsed = true,
                    timestamp = cal.timeInMillis
                )
            )

            // Streak 2: Auto Daily Counter
            val streak2 = StreakEntity(
                name = "Sober & Clean Days",
                type = StreakType.AUTO_DAILY,
                currentCount = 14,
                bestStreak = 14,
                accentColorHex = "#00E676",
                iconName = "Zap",
                resetHour = 0,
                resetMinute = 0,
                startDateMillis = now - (14L * 86400000L),
                lastResetMillis = now - (14L * 86400000L),
                totalTaps = 0
            )
            val id2 = dao.insertStreak(streak2)
            for (i in 0..13) {
                cal.time = Date(now - (i * 86400000L))
                dao.insertOrUpdateLog(
                    StreakLogEntity(
                        streakId = id2,
                        dateString = dateFormat.format(cal.time),
                        count = 1,
                        isFreezeUsed = false,
                        timestamp = cal.timeInMillis
                    )
                )
            }

            // Streak 3: Manual Counter
            val streak3 = StreakEntity(
                name = "Read 20 Pages",
                type = StreakType.MANUAL_TAP,
                currentCount = 5,
                bestStreak = 24,
                accentColorHex = "#00E5FF",
                iconName = "Book",
                resetHour = 0,
                resetMinute = 0,
                reminderEnabled = true,
                reminderHour = 21,
                reminderMinute = 30,
                startDateMillis = now - (30L * 86400000L),
                lastIncrementMillis = now,
                totalTaps = 42
            )
            val id3 = dao.insertStreak(streak3)
            for (i in 0..4) {
                cal.time = Date(now - (i * 86400000L))
                dao.insertOrUpdateLog(
                    StreakLogEntity(
                        streakId = id3,
                        dateString = dateFormat.format(cal.time),
                        count = 1,
                        isFreezeUsed = false,
                        timestamp = cal.timeInMillis
                    )
                )
            }

            // Streak 4: Auto Daily Counter
            val streak4 = StreakEntity(
                name = "Meditation Habit",
                type = StreakType.AUTO_DAILY,
                currentCount = 21,
                bestStreak = 21,
                accentColorHex = "#B388FF",
                iconName = "Heart",
                resetHour = 5, // 5 AM reset
                resetMinute = 0,
                startDateMillis = now - (21L * 86400000L),
                lastResetMillis = now - (21L * 86400000L),
                totalTaps = 0
            )
            val id4 = dao.insertStreak(streak4)
            for (i in 0..20) {
                cal.time = Date(now - (i * 86400000L))
                dao.insertOrUpdateLog(
                    StreakLogEntity(
                        streakId = id4,
                        dateString = dateFormat.format(cal.time),
                        count = 1,
                        isFreezeUsed = false,
                        timestamp = cal.timeInMillis
                    )
                )
            }
        }
    }
}
