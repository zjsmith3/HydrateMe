// WaterViewModel.kt
package com.hydrateme.app.viewmodel

// ViewModel + coroutine imports
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hydrateme.app.data.model.WaterLogEntity
import com.hydrateme.app.data.repository.WaterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

// This ViewModel holds all logic related to water logs.
// The UI (Composable) will use this to:
//  - observe logs (today, all, last 7 days)
//  - add new water amounts
class WaterViewModel(
    private val repository: WaterRepository    // repository is injected
) : ViewModel() {

    // Flow of ALL logs, newest first.
    // Later, the UI will collect this using collectAsState().
    val allLogs: Flow<List<WaterLogEntity>> = repository.getAllLogs()

    // Flow of ONLY today's logs.
    val todayLogs: Flow<List<WaterLogEntity>> = repository.getTodayLogs()

    // Helper method to get logs from last 7 days.
    // We'll call this from UI when we build a weekly chart or history screen.
    fun getLast7DaysLogs(sevenDaysAgoMillis: Long): Flow<List<WaterLogEntity>> {
        return repository.getLast7Days(sevenDaysAgoMillis)
    }

    // Called by the UI when the user logs water (e.g., presses "+8 oz" button).
    fun addWater(amount: Int) {
        // viewModelScope.launch starts a coroutine tied to the ViewModel's lifecycle.
        viewModelScope.launch {
            repository.addWater(amount)
        }
    }
}

// Factory class so we can create WaterViewModel with our custom constructor.
// We'll use this in MainActivity when we request the ViewModel instance.
class WaterViewModelFactory(
    private val repository: WaterRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WaterViewModel::class.java)) {
            return WaterViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
