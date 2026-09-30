package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.StreakEntity
import com.example.data.model.StreakLogEntity
import com.example.data.model.WidgetConfigEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StreakDao {
    @Query("SELECT * FROM streaks WHERE isArchived = 0 ORDER BY id DESC")
    fun getAllStreaks(): Flow<List<StreakEntity>>

    @Query("SELECT * FROM streaks WHERE isArchived = 0 ORDER BY id DESC")
    suspend fun getAllStreaksSync(): List<StreakEntity>

    @Query("SELECT * FROM streaks WHERE id = :id LIMIT 1")
    fun getStreakById(id: Long): Flow<StreakEntity?>

    @Query("SELECT * FROM streaks WHERE id = :id LIMIT 1")
    suspend fun getStreakByIdSync(id: Long): StreakEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStreak(streak: StreakEntity): Long

    @Update
    suspend fun updateStreak(streak: StreakEntity)

    @Query("DELETE FROM streaks WHERE id = :id")
    suspend fun deleteStreak(id: Long)

    // Activity Logs for Heatmap
    @Query("SELECT * FROM streak_logs WHERE streakId = :streakId ORDER BY dateString ASC")
    fun getLogsForStreak(streakId: Long): Flow<List<StreakLogEntity>>

    @Query("SELECT * FROM streak_logs WHERE streakId = :streakId ORDER BY dateString ASC")
    suspend fun getLogsForStreakSync(streakId: Long): List<StreakLogEntity>

    @Query("SELECT * FROM streak_logs WHERE streakId = :streakId AND dateString = :dateString LIMIT 1")
    suspend fun getLogForDate(streakId: Long, dateString: String): StreakLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLog(log: StreakLogEntity): Long

    @Query("DELETE FROM streak_logs WHERE streakId = :streakId AND dateString = :dateString")
    suspend fun deleteLogForDate(streakId: Long, dateString: String)

    @Query("DELETE FROM streak_logs WHERE streakId = :streakId")
    suspend fun deleteAllLogsForStreak(streakId: Long)

    // Widget Configurations
    @Query("SELECT * FROM widget_configs WHERE appWidgetId = :appWidgetId LIMIT 1")
    suspend fun getWidgetConfig(appWidgetId: Int): WidgetConfigEntity?

    @Query("SELECT * FROM widget_configs")
    suspend fun getAllWidgetConfigs(): List<WidgetConfigEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setWidgetConfig(config: WidgetConfigEntity)

    @Query("DELETE FROM widget_configs WHERE appWidgetId = :appWidgetId")
    suspend fun deleteWidgetConfig(appWidgetId: Int)
}
