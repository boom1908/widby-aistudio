package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.ui.graphics.vector.ImageVector

data class StreakIconOption(val id: String, val name: String, val icon: ImageVector)

val AvailableStreakIcons = listOf(
    StreakIconOption("Flame", "Flame", Icons.Default.LocalFireDepartment),
    StreakIconOption("Zap", "Energy", Icons.Default.Bolt),
    StreakIconOption("Fitness", "Fitness", Icons.Default.FitnessCenter),
    StreakIconOption("Book", "Reading", Icons.Default.Book),
    StreakIconOption("Water", "Hydration", Icons.Default.Opacity),
    StreakIconOption("Heart", "Wellness", Icons.Default.Favorite),
    StreakIconOption("Star", "Goals", Icons.Default.Star),
    StreakIconOption("Code", "Coding", Icons.Default.Code),
    StreakIconOption("Trophy", "Milestone", Icons.Default.WorkspacePremium)
)

fun getStreakIcon(iconName: String): ImageVector {
    return AvailableStreakIcons.firstOrNull { it.id.equals(iconName, ignoreCase = true) }?.icon
        ?: Icons.Default.LocalFireDepartment
}
