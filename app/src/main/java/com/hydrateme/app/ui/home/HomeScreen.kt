package com.hydrateme.app.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.hydrateme.app.R
import com.hydrateme.app.data.database.HydrateDatabase
import com.hydrateme.app.data.model.WaterLogEntity
import com.hydrateme.app.data.repository.HydrateRepository
import com.hydrateme.app.viewmodel.HydrateViewModel
import com.hydrateme.app.viewmodel.HydrateViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// --------------------------------------------------------
// Weekly summary model + helper
// --------------------------------------------------------
data class WeeklySummary(
    val daysLogged: Int,
    val daysGoalMet: Int,
    val currentGoalStreak: Int,
    val bestDayAmount: Int,
    val totalWeekAmount: Int
)

fun computeWeeklySummary(
    logs: List<WaterLogEntity>,
    dailyGoal: Int
): WeeklySummary {
    if (logs.isEmpty()) return WeeklySummary(0, 0, 0, 0, 0)

    val dayMillis = 24L * 60 * 60 * 1000
    val now = System.currentTimeMillis()
    val startOfToday = now - (now % dayMillis)

    val totals = IntArray(7) { 0 }

    for (log in logs) {
        val logDayStart = log.timestamp - (log.timestamp % dayMillis)
        val daysAgo = ((startOfToday - logDayStart) / dayMillis).toInt()
        if (daysAgo in 0..6) {
            val index = 6 - daysAgo // 6 = today
            totals[index] += log.amount
        }
    }

    val totalWeekAmount = totals.sum()
    val daysLogged = totals.count { it > 0 }
    val daysGoalMet = if (dailyGoal > 0) totals.count { it >= dailyGoal } else 0
    val bestDayAmount = totals.maxOrNull() ?: 0

    var currentStreak = 0
    if (dailyGoal > 0) {
        for (i in 6 downTo 0) {
            if (totals[i] >= dailyGoal) currentStreak++ else break
        }
    }

    return WeeklySummary(
        daysLogged = daysLogged,
        daysGoalMet = daysGoalMet,
        currentGoalStreak = currentStreak,
        bestDayAmount = bestDayAmount,
        totalWeekAmount = totalWeekAmount
    )
}

data class MonthlySummary(
    val daysLogged: Int,
    val daysGoalMet: Int,
    val longestGoalStreak: Int,
    val totalMonthAmount: Int,
    val bestDayAmount: Int
)

fun computeMonthlySummary(
    logs: List<WaterLogEntity>,
    dailyGoal: Int
): MonthlySummary {
    if (logs.isEmpty()) return MonthlySummary(0, 0, 0, 0, 0)

    val dayMillis = 24L * 60 * 60 * 1000
    val now = System.currentTimeMillis()
    val startOfToday = now - (now % dayMillis)

    // earliest day we care about (30 days, including today)
    val earliestDayStart = startOfToday - 29L * dayMillis

    // Sum water per day
    val totalsByDay = mutableMapOf<Long, Int>()
    for (log in logs) {
        if (log.timestamp < earliestDayStart) continue

        val dayStart = log.timestamp - (log.timestamp % dayMillis)
        totalsByDay[dayStart] = (totalsByDay[dayStart] ?: 0) + log.amount
    }

    // Build a list of 30 days in order (oldest → newest)
    val dayStarts = (0 until 30).map { offset ->
        earliestDayStart + offset * dayMillis
    }
    val totalsList = dayStarts.map { day -> totalsByDay[day] ?: 0 }

    val totalMonthAmount = totalsList.sum()
    val daysLogged = totalsList.count { it > 0 }
    val daysGoalMet = if (dailyGoal > 0) totalsList.count { it >= dailyGoal } else 0
    val bestDayAmount = totalsList.maxOrNull() ?: 0

    // longest consecutive streak of goal-met days in the last 30 days
    var longestStreak = 0
    var currentStreak = 0
    if (dailyGoal > 0) {
        for (total in totalsList) {
            if (total >= dailyGoal) {
                currentStreak++
                if (currentStreak > longestStreak) longestStreak = currentStreak
            } else {
                currentStreak = 0
            }
        }
    }

    return MonthlySummary(
        daysLogged = daysLogged,
        daysGoalMet = daysGoalMet,
        longestGoalStreak = longestStreak,
        totalMonthAmount = totalMonthAmount,
        bestDayAmount = bestDayAmount
    )
}


// --------------------------------------------------------
// Home screen
// --------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {

    val context = LocalContext.current
    val db = HydrateDatabase.getDatabase(context)
    val repository = HydrateRepository(db.waterLogDao(), db.userSettingsDao())
    val viewModel: HydrateViewModel = viewModel(
        factory = HydrateViewModelFactory(repository)
    )

    val todayLogs by viewModel.todayLogs.observeAsState(emptyList())
    val last7DaysLogs by viewModel.last7DaysLogs.observeAsState(emptyList())
    val settings = viewModel.userSettings.observeAsState().value
    val allLogs by viewModel.allLogs.observeAsState(emptyList())

    if (settings == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val units = settings.units
    val goal = settings.dailyGoal
    val totalToday = todayLogs.sumOf { it.amount }
    val progress =
        if (goal == 0) 0f else (totalToday.toFloat() / goal.toFloat()).coerceIn(0f, 1f)

    val weeklySummary = remember(last7DaysLogs, goal) {
        computeWeeklySummary(last7DaysLogs, goal)
    }

    val monthlySummary = remember(allLogs, goal) {
        computeMonthlySummary(allLogs, goal)
    }


    val formattedDate = remember {
        SimpleDateFormat("EEEE · MMM d", Locale.getDefault()).format(Date())
    }

    var showGoalDialog by remember { mutableStateOf(false) }
    var hasShownGoalDialog by remember { mutableStateOf(false) }

    LaunchedEffect(totalToday, goal) {
        if (totalToday >= goal && !hasShownGoalDialog) {
            showGoalDialog = true
            hasShownGoalDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("HydrateMe") },
                navigationIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.hydrateme_logo),
                        contentDescription = "Hydrate Me logo",
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .size(40.dp),
                        tint = Color.Unspecified
                    )
                }
            )
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (showGoalDialog) {
                AlertDialog(
                    onDismissRequest = { showGoalDialog = false },
                    title = { Text("Hydration High-Five! 💧") },
                    text = {
                        Text(
                            "You just hit your daily goal of $goal $units!\n\n" +
                                    "Your cells are doing a happy little splash dance. 🫧"
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = { showGoalDialog = false }) {
                            Text("Keep Sipping ✨")
                        }
                    }
                )
            }

            AnimatedWaterBackground(
                progress = progress,
                modifier = Modifier.matchParentSize()
            )

            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {

                // --- Today card ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Today's Water Intake",
                            style = MaterialTheme.typography.headlineSmall
                        )

                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$totalToday / $goal $units",
                                style = MaterialTheme.typography.headlineMedium
                            )

                            if (progress >= 1f) {
                                AssistChip(
                                    onClick = { },
                                    enabled = false,
                                    label = { Text("Goal reached 🏆") },
                                    colors = AssistChipDefaults.assistChipColors(
                                        disabledContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        disabledLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(16.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            LinearProgressIndicator(
                                progress = progress,
                                modifier = Modifier.fillMaxSize(),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = Color.Transparent
                            )
                        }
                    }
                }

                // --- Quick Add card ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Quick Add",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = "Tap to log common amounts fast.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(
                                16.dp,
                                Alignment.CenterHorizontally
                            )
                        ) {
                            QuickAddButton("+8 $units") { viewModel.addWater(8) }
                            QuickAddButton("+12 $units") { viewModel.addWater(12) }
                            QuickAddButton("+16 $units") { viewModel.addWater(16) }
                        }
                    }
                }

                // --- Scroll hint arrow (between quick add and nav buttons) ---
                ScrollHintArrow()

                // --- Navigation buttons ---
                Button(
                    onClick = { navController.navigate("history") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.History, contentDescription = "History")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("View History")
                }

                Button(
                    onClick = { navController.navigate("settings") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Settings")
                }

                Spacer(modifier = Modifier.height(16.dp))

                AchievementBadges(
                    progress = progress,
                    totalToday = totalToday,
                    goal = goal
                )

                WeeklyAchievements(
                    summary = weeklySummary,
                    units = units
                )

                MonthlyAchievements(
                    summary = monthlySummary,
                    units = units
                )
            }
        }
    }
}

// --------------------------------------------------------
// Scroll hint arrow
// --------------------------------------------------------
@Composable
fun ScrollHintArrow(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scrollHint")

    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetY"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Scroll for more",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        )

        Icon(
            imageVector = Icons.Filled.ExpandMore,
            contentDescription = "Scroll down",
            tint = MaterialTheme.colorScheme.primary.copy(alpha = alpha),
            modifier = Modifier
                .size(32.dp)
                .offset(y = offsetY.dp)
        )
    }
}

// --------------------------------------------------------
// Weekly achievements section
// --------------------------------------------------------
@Composable
fun WeeklyAchievements(
    summary: WeeklySummary,
    units: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "This Week's Achievements",
            style = MaterialTheme.typography.titleMedium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            WeeklyStatCard(
                title = "Goal Days ✅",
                value = "${summary.daysGoalMet} / 7",
                subtitle = "Days you hit your goal",
                modifier = Modifier.weight(1f)
            )
            WeeklyStatCard(
                title = "Current Streak 🔥",
                value = "${summary.currentGoalStreak} days",
                subtitle = "In a row at goal",
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            WeeklyStatCard(
                title = "Total Week 💧",
                value = "${summary.totalWeekAmount} $units",
                subtitle = "All water this week",
                modifier = Modifier.weight(1f)
            )
            WeeklyStatCard(
                title = "Best Day 🌟",
                value = "${summary.bestDayAmount} $units",
                subtitle = "Highest single day",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun WeeklyStatCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.heightIn(min = 80.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(text = title, style = MaterialTheme.typography.labelLarge)
            Text(text = value, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MonthlyAchievements(
    summary: MonthlySummary,
    units: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "This Month's Achievements",
            style = MaterialTheme.typography.titleMedium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            WeeklyStatCard(
                title = "Goal Days ✅",
                value = "${summary.daysGoalMet} / 30",
                subtitle = "Days you hit your goal",
                modifier = Modifier.weight(1f)
            )
            WeeklyStatCard(
                title = "Longest Streak 🔥",
                value = "${summary.longestGoalStreak} days",
                subtitle = "Best goal streak",
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            WeeklyStatCard(
                title = "Total Month 💧",
                value = "${summary.totalMonthAmount} $units",
                subtitle = "All water this month",
                modifier = Modifier.weight(1f)
            )
            WeeklyStatCard(
                title = "Best Day 🌟",
                value = "${summary.bestDayAmount} $units",
                subtitle = "Highest single day",
                modifier = Modifier.weight(1f)
            )
        }
    }
}


// --------------------------------------------------------
// Buttons, background & badges
// --------------------------------------------------------
@Composable
fun QuickAddButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "quickAddScale"
    )

    Button(
        onClick = onClick,
        modifier = modifier.scale(scale),
        shape = RoundedCornerShape(12.dp),
        interactionSource = interactionSource
    ) {
        Text(text)
    }
}

@Composable
fun AnimatedWaterBackground(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        label = "waterFill"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 4000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val waterTop = height * (1f - animatedProgress)

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF69BDF5),
                    Color(0xFF1E88E5)
                ),
                startY = waterTop,
                endY = height
            ),
            topLeft = Offset(0f, waterTop),
            size = Size(width, height - waterTop)
        )

        val waveHeight = 18f
        val path = Path().apply {
            moveTo(0f, waterTop)
            val step = 8f
            var x = 0f
            while (x <= width) {
                val y = waterTop + kotlin.math.sin(
                    (x / width) * 2f * Math.PI.toFloat() + wavePhase
                ) * waveHeight
                lineTo(x, y)
                x += step
            }
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }

        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0x8069BDF5),
                    Color(0x801E88E5)
                ),
                startY = waterTop - waveHeight,
                endY = height
            )
        )
    }
}

@Composable
fun AchievementBadges(
    progress: Float,
    totalToday: Int,
    goal: Int,
    modifier: Modifier = Modifier
) {
    val firstSipUnlocked = totalToday > 0
    val quarterUnlocked = progress >= 0.25f
    val halfwayUnlocked = progress >= 0.5f
    val almostUnlocked = progress >= 0.75f
    val goalUnlocked = progress >= 1f

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Every sip counts. Stay hydrated 💙",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Today's Achievements",
            style = MaterialTheme.typography.titleMedium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AchievementBadge(
                title = "First Sip 💧",
                description = "Logged water today",
                unlocked = firstSipUnlocked,
                modifier = Modifier.weight(1f)
            )
            AchievementBadge(
                title = "25% Charged ⚡",
                description = "Quarter to your goal",
                unlocked = quarterUnlocked,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AchievementBadge(
                title = "Halfway Hero 🌟",
                description = "50% of your goal",
                unlocked = halfwayUnlocked,
                modifier = Modifier.weight(1f)
            )
            AchievementBadge(
                title = "Almost There 💫",
                description = "75% of your goal",
                unlocked = almostUnlocked,
                modifier = Modifier.weight(1f)
            )
            AchievementBadge(
                title = "Goal Crusher 🏆",
                description = "Goal reached. You Rock!",
                unlocked = goalUnlocked,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun AchievementBadge(
    title: String,
    description: String,
    unlocked: Boolean,
    modifier: Modifier = Modifier
) {
    val targetBg = when {
        unlocked && title.contains("Goal Crusher") -> Color(0xFFFFE082)
        unlocked -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val bgColor by animateColorAsState(
        targetValue = targetBg,
        label = "badgeBg"
    )

    val contentColor = when {
        unlocked && title.contains("Goal Crusher") -> Color(0xFF6D4C00)
        unlocked -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
    }

    val border: BorderStroke? =
        if (unlocked) null
        else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

    val targetScale = if (unlocked) 1.05f else 1f
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "badgeScale"
    )

    Card(
        modifier = modifier
            .heightIn(min = 72.dp)
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale
            ),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = border,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor
            )
            Text(
                text = if (unlocked) description else "Locked",
                style = MaterialTheme.typography.labelSmall,
                color = contentColor
            )
        }
    }
}
