package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Default WIDBY Dark Palette
val WidbyBackground = Color(0xFF090C10)
val WidbySurface = Color(0xFF0F141C)
val WidbySurfaceElevated = Color(0xFF161C26)
val WidbySurfaceBorder = Color(0xFF232B3A)
val WidbyPrimary = Color(0xFFFF9100) // Flame orange
val WidbySecondary = Color(0xFF00E5FF) // Electric cyan
val WidbyTertiary = Color(0xFF00E676) // Emerald
val WidbyError = Color(0xFFF85149)

val WidbyTextPrimary = Color(0xFFF0F6FC)
val WidbyTextSecondary = Color(0xFF8B949E)
val WidbyTextTertiary = Color(0xFF6E7681)

// Streak Accent Color Options
data class AccentOption(val name: String, val hex: String, val color: Color)

val StreakAccents = listOf(
    AccentOption("Electric Flame", "#FF9100", Color(0xFFFF9100)),
    AccentOption("Sunset Orange", "#FF5722", Color(0xFFFF5722)),
    AccentOption("Cyan Pulse", "#00E5FF", Color(0xFF00E5FF)),
    AccentOption("Emerald Glow", "#00E676", Color(0xFF00E676)),
    AccentOption("Cyber Purple", "#B388FF", Color(0xFFB388FF)),
    AccentOption("Hot Pink", "#FF4081", Color(0xFFFF4081)),
    AccentOption("Solar Gold", "#FFD600", Color(0xFFFFD600)),
    AccentOption("Sky Blue", "#40C4FF", Color(0xFF40C4FF))
)

