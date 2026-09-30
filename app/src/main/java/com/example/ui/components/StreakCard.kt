package com.example.ui.components

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainActivity
import com.example.data.model.StreakEntity
import com.example.data.model.StreakType
import com.example.widget.StreakGlanceWidgetReceiver

@Composable
fun StreakCard(
    streak: StreakEntity,
    onTapIncrement: () -> Unit,
    onUndoIncrement: () -> Unit,
    onResetStreak: () -> Unit,
    onCardClick: () -> Unit,
    onEditStreak: () -> Unit,
    onDeleteStreak: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showResetDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    val accentColor = remember(streak.accentColorHex) {
        try {
            Color(android.graphics.Color.parseColor(streak.accentColorHex))
        } catch (_: Exception) {
            Color(0xFFFF9100)
        }
    }

    val isAuto = streak.type == StreakType.AUTO_DAILY

    // Progress percentage towards best streak or benchmark
    val progressRatio = if (streak.bestStreak > 0) {
        (streak.currentCount.toFloat() / streak.bestStreak.toFloat()).coerceIn(0f, 1f)
    } else if (streak.currentCount > 0) 1f else 0.05f

    val animatedProgress by animateFloatAsState(
        targetValue = progressRatio,
        animationSpec = spring(),
        label = "progress_anim"
    )

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Streak to 0?") },
            text = {
                Text("This will reset your current count for \"${streak.name}\" to zero. Your best streak record (${streak.bestStreak} days) will remain saved.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResetDialog = false
                        onResetStreak()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Reset to 0")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onCardClick() }
            .border(1.dp, Color(0xFF222A38), RoundedCornerShape(20.dp))
            .testTag("streak_card_${streak.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F141C)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Icon + Title + Reset Time + Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon badge
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accentColor.copy(alpha = 0.15f))
                        .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getStreakIcon(streak.iconName),
                        contentDescription = streak.iconName,
                        tint = accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = streak.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isAuto) "Auto Daily Counter" else "Manual Tap Counter",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF8B949E),
                            fontSize = 11.sp
                        )
                        Text(
                            text = " • ",
                            color = Color(0xFF6E7681),
                            fontSize = 11.sp
                        )
                        val resetTimeStr = String.format("%02d:%02d", streak.resetHour, streak.resetMinute)
                        Text(
                            text = "Reset $resetTimeStr",
                            color = Color(0xFF8B949E),
                            fontSize = 11.sp
                        )
                    }
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = Color(0xFF8B949E)
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("View Heatmap & Details") },
                            onClick = {
                                showMenu = false
                                onCardClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Pin Widget to Homescreen") },
                            onClick = {
                                showMenu = false
                                pinWidgetToHomescreen(context, streak.id)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit Streak") },
                            onClick = {
                                showMenu = false
                                onEditStreak()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Reset to 0", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                showResetDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Streak", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                onDeleteStreak()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Center Row: Progress Ring with Count and Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Flame / Progress Ring
                Box(
                    modifier = Modifier.size(76.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(76.dp)) {
                        val strokeWidth = 5.dp.toPx()
                        // Track ring
                        drawCircle(
                            color = Color(0xFF1E2636),
                            style = Stroke(width = strokeWidth)
                        )
                        // Progress arc
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(accentColor.copy(alpha = 0.6f), accentColor)
                            ),
                            startAngle = -90f,
                            sweepAngle = 360f * animatedProgress,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${streak.currentCount}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            fontSize = 26.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Stats & Freeze Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isAuto) "DAYS RUNNING" else "CURRENT STREAK",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF8B949E),
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Best: ${streak.bestStreak} ${if (streak.bestStreak == 1) "day" else "days"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE6EDF3),
                            fontWeight = FontWeight.Medium
                        )

                        if (!isAuto && streak.streakFreezeAllowed) {
                            Spacer(modifier = Modifier.width(8.dp))
                            // Freeze status badge
                            val freezeColor = if (streak.freezeUsedThisWeek) Color(0xFF8B949E) else Color(0xFF00E5FF)
                            val freezeText = if (streak.freezeUsedThisWeek) "Used" else "Ready"
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(freezeColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AcUnit,
                                    contentDescription = null,
                                    tint = freezeColor,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = freezeText,
                                    fontSize = 10.sp,
                                    color = freezeColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (streak.reminderEnabled) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "🔔 Reminder at %02d:%02d".format(streak.reminderHour, streak.reminderMinute),
                            fontSize = 10.sp,
                            color = Color(0xFF8B949E)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quick Pin Widget affordance
                OutlinedButton(
                    onClick = { pinWidgetToHomescreen(context, streak.id) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF8B949E)
                    ),
                    modifier = Modifier.height(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Widgets,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Pin Widget", fontSize = 12.sp)
                }

                if (isAuto) {
                    // Auto Daily Counter: Reset Action with confirmation
                    Button(
                        onClick = { showResetDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF21262D)
                        ),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = null,
                            tint = Color(0xFFF85149),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset", color = Color(0xFFF85149), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    // Manual Counter: Undo and Increment buttons
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Undo button
                        IconButton(
                            onClick = onUndoIncrement,
                            enabled = streak.currentCount > 0,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF161C26))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Undo Tap",
                                tint = if (streak.currentCount > 0) Color.White else Color(0xFF484F58),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // +1 Tap Button
                        Button(
                            onClick = onTapIncrement,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accentColor
                            ),
                            modifier = Modifier
                                .height(38.dp)
                                .testTag("tap_increment_button_${streak.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "+1 TAP",
                                color = Color.Black,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

fun pinWidgetToHomescreen(context: Context, streakId: Long) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val appWidgetManager = context.getSystemService(AppWidgetManager::class.java)
        if (appWidgetManager.isRequestPinAppWidgetSupported) {
            val provider = ComponentName(context, StreakGlanceWidgetReceiver::class.java)
            val intent = Intent(context, MainActivity::class.java).apply {
                putExtra("pinned_streak_id", streakId)
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                streakId.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            appWidgetManager.requestPinAppWidget(provider, null, pendingIntent)
        }
    }
}
