package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class StreakType {
    MANUAL_TAP,
    AUTO_DAILY
}

@Entity(tableName = "streaks")
data class StreakEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: StreakType,
    val currentCount: Int = 0,
    val bestStreak: Int = 0,
    val accentColorHex: String = "#FF9100", // Default vibrant flame orange
    val iconName: String = "Flame", // Flame, Zap, Star, Fitness, Book, Water, Code, Heart
    val resetHour: Int = 0, // 0-23 (custom daily reset hour)
    val resetMinute: Int = 0, // 0-59 (custom daily reset minute)
    val reminderEnabled: Boolean = false,
    val reminderHour: Int = 20,
    val reminderMinute: Int = 0,
    val streakFreezeAllowed: Boolean = true,
    val freezeUsedThisWeek: Boolean = false,
    val lastFreezeWeekOfYear: Int = -1,
    val lastFreezeYear: Int = -1,
    val startDateMillis: Long = System.currentTimeMillis(),
    val lastIncrementMillis: Long = 0L,
    val lastResetMillis: Long = System.currentTimeMillis(),
    val totalTaps: Int = 0,
    val isArchived: Boolean = false
)

@Entity(
    tableName = "streak_logs",
    indices = [Index(value = ["streakId", "dateString"], unique = true)]
)
data class StreakLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val streakId: Long,
    val dateString: String, // "YYYY-MM-DD"
    val count: Int = 0, // number of taps that day
    val isFreezeUsed: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "widget_configs")
data class WidgetConfigEntity(
    @PrimaryKey
    val appWidgetId: Int,
    val streakId: Long,
    val customTitle: String = ""
)
