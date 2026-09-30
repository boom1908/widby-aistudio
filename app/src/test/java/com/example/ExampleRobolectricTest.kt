package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.model.StreakEntity
import com.example.data.model.StreakType
import com.example.data.repository.StreakRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var repository: StreakRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = StreakRepository(context, database.streakDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `appName string matches WIDBY`() {
        val appName = context.getString(R.string.app_name)
        assertEquals("WIDBY", appName)
    }

    @Test
    fun `manual counter increments and undoes count correctly`() = runBlocking {
        val streak = StreakEntity(
            name = "Pushups Tracker",
            type = StreakType.MANUAL_TAP,
            currentCount = 0,
            bestStreak = 0,
            resetHour = 0,
            resetMinute = 0
        )
        val id = repository.insertStreak(streak)

        // Increment
        val afterInc = repository.incrementManualStreak(id)
        assertNotNull(afterInc)
        assertEquals(1, afterInc?.currentCount)
        assertEquals(1, afterInc?.bestStreak)

        // Increment again
        val afterSecondInc = repository.incrementManualStreak(id)
        assertEquals(2, afterSecondInc?.currentCount)

        // Undo
        val afterUndo = repository.undoLastIncrement(id)
        assertEquals(1, afterUndo?.currentCount)
    }

    @Test
    fun `auto counter resets to 0 correctly`() = runBlocking {
        val autoStreak = StreakEntity(
            name = "No Smoking",
            type = StreakType.AUTO_DAILY,
            currentCount = 10,
            bestStreak = 10,
            resetHour = 0,
            resetMinute = 0
        )
        val id = repository.insertStreak(autoStreak)

        val afterReset = repository.resetStreak(id)
        assertNotNull(afterReset)
        assertEquals(0, afterReset?.currentCount)
    }
}
