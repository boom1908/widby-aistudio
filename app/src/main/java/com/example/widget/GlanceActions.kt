package com.example.widget

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.example.data.db.AppDatabase
import com.example.data.repository.StreakRepository

val StreakIdKey = ActionParameters.Key<Long>("streak_id")

class TapIncrementActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val streakId = parameters[StreakIdKey] ?: return
        val db = AppDatabase.getInstance(context)
        val repository = StreakRepository(context, db.streakDao())
        repository.incrementManualStreak(streakId)
        triggerHaptic(context)
    }

    private fun triggerHaptic(context: Context) {
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

class UndoActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val streakId = parameters[StreakIdKey] ?: return
        val db = AppDatabase.getInstance(context)
        val repository = StreakRepository(context, db.streakDao())
        repository.undoLastIncrement(streakId)
    }
}

class ResetActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val streakId = parameters[StreakIdKey] ?: return
        val db = AppDatabase.getInstance(context)
        val repository = StreakRepository(context, db.streakDao())
        repository.resetStreak(streakId)
    }
}
