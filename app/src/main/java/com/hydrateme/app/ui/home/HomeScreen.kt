package com.hydrateme.app.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hydrateme.app.data.database.HydrateDatabase
import com.hydrateme.app.data.repository.HydrateRepository
import com.hydrateme.app.viewmodel.HydrateViewModel
import com.hydrateme.app.viewmodel.HydrateViewModelFactory
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.hydrateme.app.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {

    // Build ViewModel
    val context = LocalContext.current
    val db = HydrateDatabase.getDatabase(context)
    val repository = HydrateRepository(db.waterLogDao(), db.userSettingsDao())
    val viewModel: HydrateViewModel = viewModel(
        factory = HydrateViewModelFactory(repository)
    )

    // Observe today's logs
    val todayLogs by viewModel.todayLogs.observeAsState(emptyList())

    // Observe settings
    val settings = viewModel.userSettings.observeAsState().value

    // If settings aren't loaded yet, show loading
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

    val progress = if (goal == 0) 0f else (totalToday.toFloat() / goal.toFloat()).coerceIn(0f, 1f)

    var showGoalDialog by remember { mutableStateOf(false) }
    var hasShownGoalDialog by remember { mutableStateOf(false) }

// When user first crosses their goal, show the dialog once
    LaunchedEffect(totalToday, goal) {
        if (totalToday >= goal && !hasShownGoalDialog) {
            showGoalDialog = true
            hasShownGoalDialog = true
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hydrate Me") }
            )
        }
    ) { padding ->

        val animatedProgress by animateFloatAsState(
            targetValue = progress,
            label = "waterFill"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            // ------------------------------------------
            // Water Background (fills from bottom)
            // ------------------------------------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(animatedProgress)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF69BDF5),  // top color
                                Color(0xFF1E88E5)   // bottom color
                            )
                        )
                    )
            )

            // ------------------------------------------
            // Foreground UI (everything else on top)
            // ------------------------------------------
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Your existing UI stays the same here ↓↓↓

                // ---- Today's intake card ----
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
                    text = "$totalToday / $goal $units",
                    style = MaterialTheme.typography.headlineMedium
                )

                            if (progress >= 1f) {
                                AssistChip(
                                    onClick = { /* decorative only */ },
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
                                .clip(RoundedCornerShape(999.dp)) // pill shape
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            LinearProgressIndicator(
                                progress = progress,
                                modifier = Modifier.fillMaxSize(),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = Color.Transparent
                            )
                        }

                Text(
                    text = "Quick Add",
                    style = MaterialTheme.typography.titleMedium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
                ) {
                    QuickAddButton("+8 $units") { viewModel.addWater(8) }
                    QuickAddButton("+12 $units") { viewModel.addWater(12) }
                    QuickAddButton("+16 $units") { viewModel.addWater(16) }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { navController.navigate("history") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("View History")


        Column(
            modifier = Modifier
                .padding(padding)
                .padding(20.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // ------------------------------------------
            // Today's Progress Section
            // ------------------------------------------
            Text(
                text = "Today's Water Intake",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = "$totalToday / $goal $units",
                style = MaterialTheme.typography.headlineMedium
            )

            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                color = MaterialTheme.colorScheme.primary
            )

            // ------------------------------------------
            // Quick Add Buttons
            // ------------------------------------------
            Text(
                text = "Quick Add",
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                QuickAddButton("+8 $units") { viewModel.addWater(8) }
                QuickAddButton("+12 $units") { viewModel.addWater(12) }
                QuickAddButton("+16 $units") { viewModel.addWater(16) }
            }

            // ------------------------------------------
            // Navigation Buttons
            // ------------------------------------------
            Spacer(modifier = Modifier.height(16.dp))

                // Navigation buttons
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

                // Push achievements toward the bottom
                Spacer(modifier = Modifier.weight(1f))

            // HydrateMe logo at the bottom center
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.hydrateme_logo),
                    contentDescription = "HydrateMe Logo",
                    modifier = Modifier
                        .fillMaxWidth(0.55f)   // ~55% of screen width so it fits nicely
                        .aspectRatio(1f)       // keep proportions, no stretching
                )
            }

        }
    }
}

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
        modifier = modifier,
        shape = RoundedCornerShape(12.dp)
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
            animation = tween<Float>(
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

        // Water fill
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

        // Wave
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
            text = "Today's Achievements",
            style = MaterialTheme.typography.titleMedium
        )

        // Row 1
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

        // Row 2
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
                description = "Goal reached",
                unlocked = goalUnlocked,
                modifier = Modifier.weight(1f)
            )
        }
        Text(
            text = "Every sip counts. Stay hydrated 💙",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AchievementBadge(
    title: String,
    description: String,
    unlocked: Boolean,
    modifier: Modifier = Modifier
) {
    // Animated scale so it “pops” when it becomes unlocked
    val targetScale = if (unlocked) 1.05f else 1f
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "badgeScale"
    )

    // Animated background color between locked & unlocked
    val bgColor by animateColorAsState(
        targetValue = if (unlocked)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surfaceVariant,
        label = "badgeBg"
    )

    val contentColor =
        if (unlocked) MaterialTheme.colorScheme.onPrimaryContainer
        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)

    val border: BorderStroke? =
        if (unlocked) null
        else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

    Card(
        modifier = modifier.scale(scale),
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
