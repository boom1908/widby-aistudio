package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StreakEntity
import com.example.data.model.StreakType
import com.example.ui.theme.StreakAccents

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreakEditDialog(
    initialStreak: StreakEntity?,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onSave: (StreakEntity) -> Unit
) {
    var name by remember { mutableStateOf(initialStreak?.name ?: "") }
    var type by remember { mutableStateOf(initialStreak?.type ?: StreakType.MANUAL_TAP) }
    var selectedAccentHex by remember { mutableStateOf(initialStreak?.accentColorHex ?: "#FF9100") }
    var selectedIconName by remember { mutableStateOf(initialStreak?.iconName ?: "Flame") }
    var resetHour by remember { mutableIntStateOf(initialStreak?.resetHour ?: 0) }
    var resetMinute by remember { mutableIntStateOf(initialStreak?.resetMinute ?: 0) }
    var freezeAllowed by remember { mutableStateOf(initialStreak?.streakFreezeAllowed ?: true) }
    var reminderEnabled by remember { mutableStateOf(initialStreak?.reminderEnabled ?: false) }
    var reminderHour by remember { mutableIntStateOf(initialStreak?.reminderHour ?: 20) }
    var reminderMinute by remember { mutableIntStateOf(initialStreak?.reminderMinute ?: 0) }

    val accentColor = remember(selectedAccentHex) {
        try {
            Color(android.graphics.Color.parseColor(selectedAccentHex))
        } catch (_: Exception) {
            Color(0xFFFF9100)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0F141C),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (initialStreak == null) "New Streak Tracker" else "Edit Streak",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF8B949E))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Name Field
            Text(
                text = "TRACKER NAME",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF8B949E),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("e.g. Cold Shower, Reading, Sober Days") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("streak_name_input"),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Counter Type Selector
            Text(
                text = "COUNTER TYPE",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF8B949E),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TypeSelectCard(
                    title = "Manual Tap Counter",
                    description = "Big number widget. Tapping increments count by 1 with haptic feedback.",
                    isSelected = type == StreakType.MANUAL_TAP,
                    accentColor = accentColor,
                    onClick = { type = StreakType.MANUAL_TAP },
                    modifier = Modifier.weight(1f)
                )

                TypeSelectCard(
                    title = "Auto Daily Counter",
                    description = "Automatically increments once per day on its own. Long-press resets.",
                    isSelected = type == StreakType.AUTO_DAILY,
                    accentColor = accentColor,
                    onClick = { type = StreakType.AUTO_DAILY },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Accent Color Selector
            Text(
                text = "ACCENT COLOR",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF8B949E),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StreakAccents.forEach { opt ->
                    val isSelected = opt.hex.equals(selectedAccentHex, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(opt.color)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) Color.White else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { selectedAccentHex = opt.hex },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Icon Picker
            Text(
                text = "ICON",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF8B949E),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AvailableStreakIcons.forEach { opt ->
                    val isSelected = opt.id.equals(selectedIconName, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) accentColor.copy(alpha = 0.2f) else Color(0xFF161C26))
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) accentColor else Color(0xFF232B3A),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedIconName = opt.id },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = opt.icon,
                            contentDescription = opt.name,
                            tint = if (isSelected) accentColor else Color(0xFF8B949E),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Custom Daily Reset Time Picker
            Text(
                text = "CUSTOM DAILY RESET TIME",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF8B949E),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Choose when a new streak day starts (e.g. 04:00 AM for night owls)",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF8B949E),
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF161C26))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Daily Reset At",
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TimePickerChips(
                        selectedHour = resetHour,
                        onSelectHour = { resetHour = it }
                    )
                }
            }

            // Streak Freeze Setting (for Manual Tap Counter)
            if (type == StreakType.MANUAL_TAP) {
                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF161C26))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Streak Freeze Protection",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Allows 1 missed day skip per week without breaking your streak chain.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF8B949E),
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = freezeAllowed,
                        onCheckedChange = { freezeAllowed = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF00E5FF),
                            checkedTrackColor = Color(0xFF00E5FF).copy(alpha = 0.4f)
                        )
                    )
                }

                // Reminder Notification
                Spacer(modifier = Modifier.height(14.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF161C26))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Daily Reminder",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Remind if tracker hasn't been tapped by set time",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF8B949E),
                                fontSize = 11.sp
                            )
                        }
                        Switch(
                            checked = reminderEnabled,
                            onCheckedChange = { reminderEnabled = it }
                        )
                    }

                    if (reminderEnabled) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Reminder Time", color = Color(0xFF8B949E), fontSize = 12.sp)
                            TimePickerChips(
                                selectedHour = reminderHour,
                                onSelectHour = { reminderHour = it }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save Button
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val streakToSave = (initialStreak ?: StreakEntity(
                            name = name,
                            type = type
                        )).copy(
                            name = name.trim(),
                            type = type,
                            accentColorHex = selectedAccentHex,
                            iconName = selectedIconName,
                            resetHour = resetHour,
                            resetMinute = resetMinute,
                            streakFreezeAllowed = freezeAllowed,
                            reminderEnabled = reminderEnabled,
                            reminderHour = reminderHour,
                            reminderMinute = reminderMinute
                        )
                        onSave(streakToSave)
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_streak_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text(
                    text = if (initialStreak == null) "Create Tracker" else "Save Changes",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun TypeSelectCard(
    title: String,
    description: String,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) accentColor else Color(0xFF232B3A),
                shape = RoundedCornerShape(14.dp)
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF1B2332) else Color(0xFF161C26)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) accentColor else Color.White,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF8B949E),
                fontSize = 11.sp,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
private fun TimePickerChips(
    selectedHour: Int,
    onSelectHour: (Int) -> Unit
) {
    val commonHours = listOf(0, 4, 6, 8, 12, 18, 20, 21, 22)
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        commonHours.forEach { hr ->
            val isSelected = hr == selectedHour
            val label = String.format("%02d:00", hr)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF232B3A))
                    .clickable { onSelectHour(hr) }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.Black else Color.White
                )
            }
        }
    }
}
