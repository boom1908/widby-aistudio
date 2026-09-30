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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StreakLogEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HeatmapView(
    logs: List<StreakLogEntity>,
    accentColor: Color,
    modifier: Modifier = Modifier,
    weeksToShow: Int = 16
) {
    val logMap = remember(logs) {
        logs.associateBy { it.dateString }
    }

    var selectedCellInfo by remember { mutableStateOf<String?>(null) }

    // Prepare grid dates: weeksToShow * 7 days
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val displayFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.US) }
    val todayCal = Calendar.getInstance()

    // Align to the end of the current week (Sunday)
    val dayOfWeek = todayCal.get(Calendar.DAY_OF_WEEK)
    // In Java Calendar: Sunday=1, Monday=2 ... Saturday=7
    val daysAfterMonday = (dayOfWeek + 5) % 7 // Monday = 0, Sunday = 6

    val totalDays = weeksToShow * 7
    val calendarDays = remember(weeksToShow, dayOfWeek) {
        val list = mutableListOf<CalendarDayInfo>()
        val startCal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -(totalDays - 1 - (6 - daysAfterMonday)))
        }

        for (i in 0 until totalDays) {
            val dateStr = dateFormat.format(startCal.time)
            val dispStr = displayFormat.format(startCal.time)
            val isFuture = startCal.after(todayCal)

            list.add(
                CalendarDayInfo(
                    dateString = dateStr,
                    displayDate = dispStr,
                    isFuture = isFuture
                )
            )
            startCal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    // Split into weeks (columns)
    val weeks = remember(calendarDays) {
        calendarDays.chunked(7)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0F141C))
            .border(1.dp, Color(0xFF232B3A), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ACTIVITY HEATMAP",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF8B949E),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Last $weeksToShow Weeks",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF8B949E),
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Heatmap Grid with horizontal scroll
        val scrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            verticalAlignment = Alignment.Top
        ) {
            // Day of week labels (M, W, F)
            Column(
                modifier = Modifier.padding(end = 6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("M", fontSize = 9.sp, color = Color(0xFF6E7681), modifier = Modifier.height(14.dp))
                Spacer(modifier = Modifier.height(14.dp))
                Text("W", fontSize = 9.sp, color = Color(0xFF6E7681), modifier = Modifier.height(14.dp))
                Spacer(modifier = Modifier.height(14.dp))
                Text("F", fontSize = 9.sp, color = Color(0xFF6E7681), modifier = Modifier.height(14.dp))
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Columns of weeks
            weeks.forEach { weekDays ->
                Column(
                    modifier = Modifier.padding(horizontal = 2.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    weekDays.forEach { dayInfo ->
                        val log = logMap[dayInfo.dateString]
                        val count = log?.count ?: 0
                        val isFreeze = log?.isFreezeUsed == true

                        val cellColor = when {
                            dayInfo.isFuture -> Color(0xFF131822).copy(alpha = 0.4f)
                            isFreeze -> Color(0xFF00E5FF) // Icy Freeze cyan
                            count >= 3 -> accentColor
                            count == 2 -> accentColor.copy(alpha = 0.7f)
                            count == 1 -> accentColor.copy(alpha = 0.4f)
                            else -> Color(0xFF1A2230) // Inactive cell
                        }

                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(cellColor)
                                .clickable(enabled = !dayInfo.isFuture) {
                                    val status = when {
                                        isFreeze -> "Streak Freeze Used ❄️"
                                        count > 0 -> "$count tap${if (count > 1) "s" else ""} logged 🔥"
                                        else -> "No activity"
                                    }
                                    selectedCellInfo = "${dayInfo.displayDate}: $status"
                                }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Selected Cell Tooltip / Info Banner
        if (selectedCellInfo != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF161C26))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = selectedCellInfo!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Legend row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Less", fontSize = 10.sp, color = Color(0xFF6E7681))
            Spacer(modifier = Modifier.width(4.dp))
            Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFF1A2230)))
            Spacer(modifier = Modifier.width(3.dp))
            Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(accentColor.copy(alpha = 0.4f)))
            Spacer(modifier = Modifier.width(3.dp))
            Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(accentColor.copy(alpha = 0.7f)))
            Spacer(modifier = Modifier.width(3.dp))
            Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(accentColor))
            Spacer(modifier = Modifier.width(4.dp))
            Text("More", fontSize = 10.sp, color = Color(0xFF6E7681))

            Spacer(modifier = Modifier.width(12.dp))
            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF00E5FF)))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Freeze", fontSize = 10.sp, color = Color(0xFF00E5FF))
        }
    }
}

private data class CalendarDayInfo(
    val dateString: String,
    val displayDate: String,
    val isFuture: Boolean
)
