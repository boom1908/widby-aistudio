package com.example.ui

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.StreakEntity
import com.example.data.model.StreakLogEntity
import com.example.data.model.StreakType
import com.example.data.repository.StreakRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class StreakFilter {
    ALL,
    MANUAL,
    AUTO
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = StreakRepository(application, db.streakDao())

    val allStreaks: StateFlow<List<StreakEntity>> = repository.allStreaks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val activeFilter = MutableStateFlow(StreakFilter.ALL)

    val filteredStreaks: StateFlow<List<StreakEntity>> = combine(allStreaks, activeFilter) { streaks, filter ->
        when (filter) {
            StreakFilter.ALL -> streaks
            StreakFilter.MANUAL -> streaks.filter { it.type == StreakType.MANUAL_TAP }
            StreakFilter.AUTO -> streaks.filter { it.type == StreakType.AUTO_DAILY }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val selectedStreakId = MutableStateFlow<Long?>(null)

    val selectedStreak: StateFlow<StreakEntity?> = selectedStreakId.flatMapLatest { id ->
        if (id != null) repository.getStreakById(id) else flowOf(null)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val selectedStreakLogs: StateFlow<List<StreakLogEntity>> = selectedStreakId.flatMapLatest { id ->
        if (id != null) repository.getLogsForStreak(id) else flowOf(emptyList())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            repository.syncAllAutoCounters()
        }
    }

    fun setFilter(filter: StreakFilter) {
        activeFilter.value = filter
    }

    fun selectStreak(id: Long?) {
        selectedStreakId.value = id
    }

    fun incrementStreak(streakId: Long) {
        viewModelScope.launch {
            repository.incrementManualStreak(streakId)
            triggerHaptic()
        }
    }

    fun undoStreak(streakId: Long) {
        viewModelScope.launch {
            repository.undoLastIncrement(streakId)
        }
    }

    fun resetStreak(streakId: Long) {
        viewModelScope.launch {
            repository.resetStreak(streakId)
        }
    }

    fun saveStreak(streak: StreakEntity) {
        viewModelScope.launch {
            if (streak.id == 0L) {
                repository.insertStreak(streak)
            } else {
                repository.updateStreak(streak)
            }
        }
    }

    fun deleteStreak(streakId: Long) {
        viewModelScope.launch {
            if (selectedStreakId.value == streakId) {
                selectedStreakId.value = null
            }
            repository.deleteStreak(streakId)
        }
    }

    private fun triggerHaptic() {
        val context = getApplication<Application>()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(40)
            }
        } catch (_: Exception) {}
    }
}
