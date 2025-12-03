package com.hydrateme.app.ui.history

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.hydrateme.app.data.database.HydrateDatabase
import com.hydrateme.app.data.repository.HydrateRepository
import com.hydrateme.app.viewmodel.HydrateViewModel
import com.hydrateme.app.viewmodel.HydrateViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HistoryScreen(navController: NavController) {

    val context = LocalContext.current
    val db = HydrateDatabase.getDatabase(context)
    val repository = HydrateRepository(db.waterLogDao(), db.userSettingsDao())
    val viewModel: HydrateViewModel = viewModel(
        factory = HydrateViewModelFactory(repository)
    )

    val userSettings by viewModel.userSettings.observeAsState()
    val last7Days by viewModel.last7DaysIntake.observeAsState(emptyMap())
    val last30Days by viewModel.last30DaysIntake.observeAsState(emptyMap())

    Scaffold(
        topBar = { TopAppBar(title = { Text("History") }) }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // ----------------- LAST 7 DAYS LIST -----------------
            Text("Last 7 Days", style = MaterialTheme.typography.headlineMedium)

// --- Column Titles ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Date",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Amount",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Goal Met",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.weight(1f)
                )
            }

// --- 7-Day Rows ---
            last7Days.forEach { (day, amount) ->
                val goal = userSettings?.dailyGoal ?: 64
                val metGoal = amount >= goal

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "$amount ${userSettings?.units ?: "oz"}",
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = if (metGoal) "✓" else "✗",
                        modifier = Modifier.weight(1f)
                    )
                }
            }


            // ----------------- LAST 30 DAYS GRAPH -----------------
            Text("Last 30 Days Intake", style = MaterialTheme.typography.headlineMedium)

            val sortedEntries = last30Days.toSortedMap()
            val values = sortedEntries.values.toList()

            if (values.isNotEmpty()) {
                val maxAmount = (values.maxOrNull() ?: 1).toFloat()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                ) {

                    // ---- GRAPH AREA ----
                    Canvas(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        val path = Path()
                        val count = values.size
                        val widthStep =
                            if (count > 1) size.width / (count - 1).toFloat() else 0f

                        values.forEachIndexed { index, value ->

                            val x = index * widthStep
                            val y = size.height - (value / maxAmount) * size.height

                            if (index == 0) path.moveTo(x, y)
                            else path.lineTo(x, y)

                            drawCircle(
                                color = Color(0xFF1E88E5),
                                radius = 4.dp.toPx(),
                                center = Offset(x, y)
                            )
                        }

                        drawPath(
                            path = path,
                            color = Color(0xFF42A5F5),
                            style = Stroke(width = 3.dp.toPx())
                        )
                    }

                    // ---- Y-AXIS LABELS ----
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(start = 6.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.End
                    ) {
                        // Top: maxAmount
                        Text("${maxAmount.toInt()}", style = MaterialTheme.typography.bodySmall)

                        // 75%
                        Text("${(maxAmount * 0.75f).toInt()}", style = MaterialTheme.typography.bodySmall)

                        // 50%
                        Text("${(maxAmount * 0.5f).toInt()}", style = MaterialTheme.typography.bodySmall)

                        // 25%
                        Text("${(maxAmount * 0.25f).toInt()}", style = MaterialTheme.typography.bodySmall)

                        // Bottom: 0
                        Text("0", style = MaterialTheme.typography.bodySmall)
                    }
                }

                // ---- X-axis labels (days) ----
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    sortedEntries.keys.toList().forEachIndexed { index, day ->
                        if (index % 5 == 0 || index == sortedEntries.size - 1) {
                            Text(
                                text = day.takeLast(2),
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else {
                            Spacer(Modifier.width(0.dp))
                        }
                    }
                }
            } else {
                Text("No data yet for the last 30 days.")
            }
        }
    }
}
