package com.example.ui

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.example.data.db.AppDatabase
import com.example.data.model.StreakEntity
import com.example.data.model.StreakType
import com.example.data.repository.StreakRepository
import com.example.ui.theme.WidbyTheme
import com.example.widget.StreakGlanceWidget
import kotlinx.coroutines.launch

class WidgetConfigureActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Set result to CANCELED initially so if user backs out, widget is not placed
        setResult(Activity.RESULT_CANCELED)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        setContent {
            WidbyTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WidgetConfigureScreen(
                        appWidgetId = appWidgetId,
                        onComplete = {
                            val resultValue = Intent().apply {
                                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                            }
                            setResult(Activity.RESULT_OK, resultValue)
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun WidgetConfigureScreen(
    appWidgetId: Int,
    onComplete: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val db = remember { AppDatabase.getInstance(context) }
    val repository = remember { StreakRepository(context, db.streakDao()) }

    var streaks by remember { mutableStateOf<List<StreakEntity>>(emptyList()) }
    var selectedStreakId by remember { mutableLongStateOf(0L) }
    var widgetCustomTitle by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val list = db.streakDao().getAllStreaksSync()
        streaks = list
        if (list.isNotEmpty()) {
            selectedStreakId = list.first().id
            widgetCustomTitle = list.first().name
        }
        isLoading = false
    }

    val selectedStreak = streaks.firstOrNull { it.id == selectedStreakId }

    Scaffold(
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            if (selectedStreakId != 0L) {
                                repository.saveWidgetConfig(
                                    appWidgetId = appWidgetId,
                                    streakId = selectedStreakId,
                                    customTitle = widgetCustomTitle.ifBlank { selectedStreak?.name ?: "Streak" }
                                )
                                try {
                                    val glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId)
                                    StreakGlanceWidget().update(context, glanceId)
                                } catch (_: Exception) {}
                            }
                            onComplete()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("save_widget_config_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = selectedStreak?.let {
                            try { Color(android.graphics.Color.parseColor(it.accentColorHex)) } catch (_: Exception) { MaterialTheme.colorScheme.primary }
                        } ?: MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Add Widget to Homescreen",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Widgets,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Configure Widget",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Select a tracker and customize its widget label",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF8B949E)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Widget Custom Label Input
            item {
                Text(
                    text = "WIDGET DISPLAY LABEL",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF8B949E),
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = widgetCustomTitle,
                    onValueChange = { widgetCustomTitle = it },
                    placeholder = { Text("e.g. My Gym Routine, Days Sober") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("widget_title_input"),
                    shape = RoundedCornerShape(14.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Live Preview Card
            item {
                Text(
                    text = "HOMESCREEN PREVIEW",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF8B949E),
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                val previewAccent = selectedStreak?.let {
                    try { Color(android.graphics.Color.parseColor(it.accentColorHex)) } catch (_: Exception) { Color(0xFFFF9100) }
                } ?: Color(0xFFFF9100)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1117))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = (widgetCustomTitle.ifBlank { selectedStreak?.name ?: "STREAK NAME" }).uppercase(),
                            color = previewAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF161B22)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${selectedStreak?.currentCount ?: 7}",
                                    color = Color.White,
                                    fontSize = 38.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (selectedStreak?.type == StreakType.AUTO_DAILY) "DAYS STREAK" else "STREAK TAPS",
                                    color = Color(0xFF8B949E),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Streak Selection List
            item {
                Text(
                    text = "CHOOSE TRACKER TO DISPLAY",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF8B949E),
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(streaks) { streak ->
                val isSelected = streak.id == selectedStreakId
                val streakAccent = try {
                    Color(android.graphics.Color.parseColor(streak.accentColorHex))
                } catch (_: Exception) {
                    Color(0xFFFF9100)
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                            selectedStreakId = streak.id
                            widgetCustomTitle = streak.name
                        }
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) streakAccent else Color(0xFF21262D),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF1C2128) else Color(0xFF161B22)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(streakAccent)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = streak.name,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (streak.type == StreakType.AUTO_DAILY) "Auto Daily Counter" else "Manual Tap Counter",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF8B949E)
                            )
                        }
                        Text(
                            text = "${streak.currentCount}",
                            fontWeight = FontWeight.Bold,
                            color = streakAccent,
                            fontSize = 20.sp
                        )
                        if (isSelected) {
                            Spacer(modifier = Modifier.width(10.dp))
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = streakAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}
