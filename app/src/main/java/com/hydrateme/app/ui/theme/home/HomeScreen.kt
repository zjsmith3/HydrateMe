// HomeScreen.kt
package com.hydrateme.app.ui.home

// --- Compose UI imports ---
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue




// --- Java time imports for formatting timestamps ---
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// --- Your own classes ---
import com.hydrateme.app.data.model.WaterLogEntity
import com.hydrateme.app.viewmodel.WaterViewModel

// Top-level Home screen that shows today's total and a list of today's logs.
@Composable
fun HomeScreen(
    waterViewModel: WaterViewModel,
    modifier: Modifier = Modifier,
    innerPadding: PaddingValues = PaddingValues(0.dp)   // padding from Scaffold
) {
    // Collect today's logs from the ViewModel as state
    val todayLogs by waterViewModel.todayLogs.collectAsState(initial = emptyList())

    // Sum up today's total amount (e.g., in oz) from the logs
    val totalToday = todayLogs.sumOf { it.amount }

    // NEW: State for the custom amount (slider value)
    var customAmount by remember { mutableStateOf(8f) }   // start at 8 oz

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(16.dp)
    ) {
        // --- Header: Today's total ---
        Text(
            text = "Today's intake: $totalToday oz",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        // --- Quick-add buttons (+8, +16, +24, +32) ---
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = { waterViewModel.addWater(8) }) {
                Text(text = "+ 8 oz")
            }

            Button(onClick = { waterViewModel.addWater(16) }) {
                Text(text = "+ 16 oz")
            }

            Button(onClick = { waterViewModel.addWater(24) }) {
                Text(text = "+ 24 oz")
            }

            Button(onClick = { waterViewModel.addWater(32) }) {
                Text(text = "+ 32 oz")
            }
        }


        Spacer(modifier = Modifier.height(16.dp))

        // --- NEW: Custom amount slider (8–64 oz) ---
        Text(
            text = "Custom amount: ${customAmount.toInt()} oz",
            style = MaterialTheme.typography.titleSmall
        )

        Slider(
            value = customAmount,
            onValueChange = { customAmount = it },
            valueRange = 8f..64f,      // min and max in oz
            steps = 56,                // makes it feel more "stepped"; 56 = 64-8
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Button(
            onClick = { waterViewModel.addWater(customAmount.toInt()) },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text(text = "Add ${customAmount.toInt()} oz")
        }

        // --- Today's logs list ---
        Text(
            text = "Today's logs",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .padding(top = 16.dp, bottom = 8.dp)
        )

        TodayLogsList(
            logs = todayLogs,
            modifier = Modifier.fillMaxWidth()
        )
    }
}


// This composable shows a scrollable list of WaterLogEntity items.
@Composable
fun TodayLogsList(
    logs: List<WaterLogEntity>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
    ) {
        // items() will create one row per WaterLogEntity
        items(logs) { log ->
            WaterLogRow(log = log)
        }
    }
}

// Single row representing one drink entry.
// It shows the amount and the time in 12-hour format with AM/PM.
@Composable
fun WaterLogRow(log: WaterLogEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Amount of water for this log
            Text(
                text = "${log.amount} oz",
                style = MaterialTheme.typography.bodyLarge
            )

            // Time formatted as 12-hour with AM/PM (e.g., 03:15 PM)
            Text(
                text = formatTime12Hour(log.timestamp),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

// Helper function that converts a timestamp (millis) into "hh:mm a" format.
// Example output: "03:15 PM"
private fun formatTime12Hour(timestampMillis: Long): String {
    val date = Date(timestampMillis) // turn millis into a Date
    val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return formatter.format(date)
}
