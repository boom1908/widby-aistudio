package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.StreakEntity
import com.example.ui.MainViewModel
import com.example.ui.StreakFilter
import com.example.ui.components.StreakCard
import com.example.ui.components.StreakDetailBottomSheet
import com.example.ui.components.StreakEditDialog
import com.example.ui.components.WidgetGuideDialog
import com.example.ui.theme.WidbyTheme
import com.example.worker.StreakWorker
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Schedule periodic streak and reminder worker
        StreakWorker.enqueuePeriodicWork(this)

        // Handle intent from notification or widget
        val initialStreakId = intent?.getLongExtra("streak_id", -1L)?.takeIf { it != -1L }
            ?: intent?.getLongExtra("pinned_streak_id", -1L)?.takeIf { it != -1L }
        if (initialStreakId != null) {
            viewModel.selectStreak(initialStreakId)
        }

        setContent {
            WidbyTheme(darkTheme = true) {
                MainApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(viewModel: MainViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val allStreaks by viewModel.allStreaks.collectAsStateWithLifecycle()
    val filteredStreaks by viewModel.filteredStreaks.collectAsStateWithLifecycle()
    val activeFilter by viewModel.activeFilter.collectAsStateWithLifecycle()
    val selectedStreak by viewModel.selectedStreak.collectAsStateWithLifecycle()
    val selectedStreakLogs by viewModel.selectedStreakLogs.collectAsStateWithLifecycle()

    var showCreateDialog by remember { mutableStateOf(false) }
    var streakToEdit by remember { mutableStateOf<StreakEntity?>(null) }
    var showWidgetGuide by remember { mutableStateOf(false) }

    val detailSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val editSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val guideSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Request notification permission on Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Notifications disabled: Streak reminders will not be displayed.")
            }
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "WIDBY",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Widget Streak Tracker",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF8B949E),
                                fontSize = 10.sp
                            )
                        }
                    }
                },
                actions = {
                    // Widget Guide Button
                    OutlinedButton(
                        onClick = { showWidgetGuide = true },
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("widget_guide_button"),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Widgets,
                            contentDescription = "Widgets Guide",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Widgets",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    streakToEdit = null
                    showCreateDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.Black,
                shape = CircleShape,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("add_streak_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add New Streak Tracker",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Stats Summary Strip
                StreakSummaryStrip(allStreaks = allStreaks)
            }

            item {
                // Filter Chips Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = activeFilter == StreakFilter.ALL,
                        onClick = { viewModel.setFilter(StreakFilter.ALL) },
                        label = { Text("All (${allStreaks.size})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.Black
                        ),
                        modifier = Modifier.testTag("filter_chip_all")
                    )
                    FilterChip(
                        selected = activeFilter == StreakFilter.MANUAL,
                        onClick = { viewModel.setFilter(StreakFilter.MANUAL) },
                        label = { Text("Manual Tap") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.Black
                        ),
                        modifier = Modifier.testTag("filter_chip_manual")
                    )
                    FilterChip(
                        selected = activeFilter == StreakFilter.AUTO,
                        onClick = { viewModel.setFilter(StreakFilter.AUTO) },
                        label = { Text("Auto Daily") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.Black
                        ),
                        modifier = Modifier.testTag("filter_chip_auto")
                    )
                }
            }

            if (filteredStreaks.isEmpty()) {
                item {
                    EmptyStreakState(
                        filter = activeFilter,
                        onCreateClick = {
                            streakToEdit = null
                            showCreateDialog = true
                        }
                    )
                }
            } else {
                items(
                    items = filteredStreaks,
                    key = { it.id }
                ) { streak ->
                    StreakCard(
                        streak = streak,
                        onTapIncrement = { viewModel.incrementStreak(streak.id) },
                        onUndoIncrement = { viewModel.undoStreak(streak.id) },
                        onResetStreak = { viewModel.resetStreak(streak.id) },
                        onCardClick = { viewModel.selectStreak(streak.id) },
                        onEditStreak = {
                            streakToEdit = streak
                            showCreateDialog = true
                        },
                        onDeleteStreak = { viewModel.deleteStreak(streak.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Streak Detail Bottom Sheet (Heatmap & Stats)
    if (selectedStreak != null) {
        StreakDetailBottomSheet(
            streak = selectedStreak!!,
            logs = selectedStreakLogs,
            sheetState = detailSheetState,
            onDismiss = { viewModel.selectStreak(null) },
            onEdit = {
                streakToEdit = selectedStreak
                viewModel.selectStreak(null)
                showCreateDialog = true
            },
            onDelete = {
                viewModel.deleteStreak(selectedStreak!!.id)
                viewModel.selectStreak(null)
            }
        )
    }

    // Create / Edit Streak Sheet
    if (showCreateDialog) {
        StreakEditDialog(
            initialStreak = streakToEdit,
            sheetState = editSheetState,
            onDismiss = {
                showCreateDialog = false
                streakToEdit = null
            },
            onSave = { updated ->
                viewModel.saveStreak(updated)
                showCreateDialog = false
                streakToEdit = null
            }
        )
    }

    // Widget Guide Sheet
    if (showWidgetGuide) {
        WidgetGuideDialog(
            sheetState = guideSheetState,
            onDismiss = { showWidgetGuide = false }
        )
    }
}

@Composable
fun StreakSummaryStrip(allStreaks: List<StreakEntity>) {
    val totalCount = allStreaks.sumOf { it.currentCount }
    val maxStreak = allStreaks.maxOfOrNull { it.bestStreak } ?: 0
    val activeCount = allStreaks.count { it.currentCount > 0 }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF0F141C))
            .border(1.dp, Color(0xFF232B3A), RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ACTIVE STREAKS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF8B949E),
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "$activeCount / ${allStreaks.size}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(30.dp)
                    .background(Color(0xFF232B3A))
            )

            Column {
                Text(
                    text = "TOTAL PROGRESS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF8B949E),
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "$totalCount Days/Taps",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF00E5FF)
                )
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(30.dp)
                    .background(Color(0xFF232B3A))
            )

            Column {
                Text(
                    text = "BEST RECORD",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF8B949E),
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "$maxStreak Days",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFFD600)
                )
            }
        }
    }
}

@Composable
fun EmptyStreakState(
    filter: StreakFilter,
    onCreateClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF161C26)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Widgets,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = when (filter) {
                    StreakFilter.ALL -> "No Streak Trackers Yet"
                    StreakFilter.MANUAL -> "No Manual Tap Trackers"
                    StreakFilter.AUTO -> "No Auto Daily Trackers"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Add a tracker to place interactive counter widgets on your homescreen!",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF8B949E),
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            OutlinedButton(
                onClick = onCreateClick,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Create First Tracker")
            }
        }
    }
}
