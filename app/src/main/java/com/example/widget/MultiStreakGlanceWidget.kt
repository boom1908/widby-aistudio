package com.example.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.MainActivity
import com.example.data.db.AppDatabase
import com.example.data.model.StreakEntity
import com.example.data.model.StreakType

class MultiStreakGlanceWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db = AppDatabase.getInstance(context)
        val streaks = db.streakDao().getAllStreaksSync().take(4)

        provideContent {
            MultiStreakContent(streaks = streaks)
        }
    }
}

@androidx.compose.runtime.Composable
fun MultiStreakContent(streaks: List<StreakEntity>) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
            .cornerRadius(24.dp)
            .padding(12.dp)
            .clickable(actionStartActivity<MainActivity>())
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize()
        ) {
            // Header Row
            Row(
                modifier = GlanceModifier.fillMaxWidth().padding(bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "WIDBY STREAKS",
                    modifier = GlanceModifier.defaultWeight(),
                    style = TextStyle(
                        color = ColorProvider(Color(0xFFFF9100)),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "${streaks.size} ACTIVE",
                    style = TextStyle(
                        color = ColorProvider(Color(0xFF8B949E)),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            if (streaks.isEmpty()) {
                Box(
                    modifier = GlanceModifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tap to create your first streak",
                        style = TextStyle(color = ColorProvider(Color(0xFF8B949E)), fontSize = 12.sp)
                    )
                }
            } else {
                Column(
                    modifier = GlanceModifier.fillMaxSize()
                ) {
                    streaks.forEachIndexed { index, streak ->
                        val streakAccent = try {
                            Color(android.graphics.Color.parseColor(streak.accentColorHex))
                        } catch (_: Exception) {
                            Color(0xFFFF9100)
                        }
                        val isAuto = streak.type == StreakType.AUTO_DAILY

                        Row(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .defaultWeight()
                                .background(Color(0xFF161B22))
                                .cornerRadius(12.dp)
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left: Name and type
                            Column(
                                modifier = GlanceModifier.defaultWeight()
                            ) {
                                Text(
                                    text = streak.name,
                                    maxLines = 1,
                                    style = TextStyle(
                                        color = ColorProvider(Color.White),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = if (isAuto) "Auto Daily" else "Manual Tap",
                                    style = TextStyle(
                                        color = ColorProvider(Color(0xFF8B949E)),
                                        fontSize = 9.sp
                                    )
                                )
                            }

                            // Middle: Count
                            Text(
                                text = "${streak.currentCount}",
                                modifier = GlanceModifier.padding(horizontal = 8.dp),
                                style = TextStyle(
                                    color = ColorProvider(streakAccent),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )

                            // Right: Action button
                            if (isAuto) {
                                Box(
                                    modifier = GlanceModifier
                                        .background(Color(0xFF21262D))
                                        .cornerRadius(8.dp)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                        .clickable(
                                            actionRunCallback<ResetActionCallback>(
                                                actionParametersOf(StreakIdKey to streak.id)
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "RESET",
                                        style = TextStyle(
                                            color = ColorProvider(Color(0xFFF85149)),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            } else {
                                Box(
                                    modifier = GlanceModifier
                                        .background(streakAccent)
                                        .cornerRadius(8.dp)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                        .clickable(
                                            actionRunCallback<TapIncrementActionCallback>(
                                                actionParametersOf(StreakIdKey to streak.id)
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "+1",
                                        style = TextStyle(
                                            color = ColorProvider(Color.Black),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }

                        if (index < streaks.size - 1) {
                            Spacer(modifier = GlanceModifier.height(4.dp))
                        }
                    }
                }
            }
        }
    }
}

class MultiStreakGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MultiStreakGlanceWidget()
}
