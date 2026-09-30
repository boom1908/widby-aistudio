package com.example.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.AppWidgetId
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
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.MainActivity
import com.example.R
import com.example.data.db.AppDatabase
import com.example.data.model.StreakEntity
import com.example.data.model.StreakType

class StreakGlanceWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appWidgetId = (id as? AppWidgetId)?.appWidgetId ?: -1
        val db = AppDatabase.getInstance(context)
        val streakDao = db.streakDao()

        // Lookup configured streak for this widget, or fallback to first streak
        val config = if (appWidgetId != -1) streakDao.getWidgetConfig(appWidgetId) else null
        val streak = if (config != null) {
            streakDao.getStreakByIdSync(config.streakId)
        } else {
            streakDao.getAllStreaksSync().firstOrNull()
        }

        val displayTitle = config?.customTitle?.takeIf { it.isNotBlank() } ?: streak?.name ?: "WIDBY Streak"

        provideContent {
            StreakWidgetContent(
                streak = streak,
                title = displayTitle,
                appWidgetId = appWidgetId
            )
        }
    }
}

@androidx.compose.runtime.Composable
fun StreakWidgetContent(
    streak: StreakEntity?,
    title: String,
    appWidgetId: Int
) {
    val size = LocalSize.current
    val context = LocalContext.current

    val accentColor = try {
        streak?.let { Color(android.graphics.Color.parseColor(it.accentColorHex)) }
            ?: Color(0xFFFF9100)
    } catch (_: Exception) {
        Color(0xFFFF9100)
    }

    val isAutoDaily = streak?.type == StreakType.AUTO_DAILY
    val count = streak?.currentCount ?: 0
    val streakId = streak?.id ?: -1L

    val isSmall = size.width < 160.dp || size.height < 140.dp
    val isMedium = !isSmall && size.width < 260.dp

    // Dark sleek container
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color(0xFF0D1117))
            .cornerRadius(22.dp)
            .padding(12.dp)
            .clickable(actionStartActivity<MainActivity>()),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Header: Name label & Type Badge
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Streak Name Label (user customizable)
                Text(
                    text = title.uppercase(),
                    maxLines = 1,
                    style = TextStyle(
                        color = ColorProvider(accentColor),
                        fontSize = if (isSmall) 11.sp else 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                )
            }

            Spacer(modifier = GlanceModifier.height(4.dp))

            // Center: Big Number Counter
            // If Manual Counter: big tap area that increments
            // If Auto Daily: counts days automatically, with reset indicator
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .defaultWeight()
                    .background(Color(0xFF161B22))
                    .cornerRadius(16.dp)
                    .clickable(
                        if (isAutoDaily) {
                            actionStartActivity<MainActivity>()
                        } else {
                            actionRunCallback<TapIncrementActionCallback>(
                                actionParametersOf(StreakIdKey to streakId)
                            )
                        }
                    )
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$count",
                        style = TextStyle(
                            color = ColorProvider(Color.White),
                            fontSize = if (isSmall) 38.sp else 50.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    )

                    Text(
                        text = if (isAutoDaily) "DAYS STREAK" else if (count == 1) "STREAK TAP" else "STREAK TAPS",
                        style = TextStyle(
                            color = ColorProvider(Color(0xFF8B949E)),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }

            Spacer(modifier = GlanceModifier.height(6.dp))

            // Bottom Actions / Status row
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isAutoDaily) {
                    // Auto Daily Counter: Reset Action Button with Confirmation
                    Box(
                        modifier = GlanceModifier
                            .background(Color(0xFF21262D))
                            .cornerRadius(12.dp)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .clickable(
                                actionRunCallback<ResetActionCallback>(
                                    actionParametersOf(StreakIdKey to streakId)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "RESET TO 0",
                            style = TextStyle(
                                color = ColorProvider(Color(0xFFF85149)),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                } else {
                    // Manual Counter: Tap indicator + Undo Action Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Undo button
                        Box(
                            modifier = GlanceModifier
                                .background(Color(0xFF21262D))
                                .cornerRadius(10.dp)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .clickable(
                                    actionRunCallback<UndoActionCallback>(
                                        actionParametersOf(StreakIdKey to streakId)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "UNDO -1",
                                style = TextStyle(
                                    color = ColorProvider(Color(0xFF8B949E)),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Spacer(modifier = GlanceModifier.width(8.dp))

                        // Tap button
                        Box(
                            modifier = GlanceModifier
                                .background(accentColor)
                                .cornerRadius(10.dp)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                .clickable(
                                    actionRunCallback<TapIncrementActionCallback>(
                                        actionParametersOf(StreakIdKey to streakId)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+1 TAP",
                                style = TextStyle(
                                    color = ColorProvider(Color.Black),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

class StreakGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = StreakGlanceWidget()
}
