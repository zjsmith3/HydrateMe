package com.hydrateme.app.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.asLiveData
import com.hydrateme.app.data.model.UserSettingsEntity
import com.hydrateme.app.data.repository.HydrateRepository
import kotlinx.coroutines.launch

class HydrateViewModel(
    private val repository: HydrateRepository
) : ViewModel() {

    init {
        // Ensure the settings row exists BEFORE UI reads it
        viewModelScope.launch {
            repository.ensureDefaultSettings()
        }
    }


    //------------------
    // history hydration data
    //------------------

    @RequiresApi(Build.VERSION_CODES.O)
    val last7DaysIntake = repository.getDailyIntakeLast7Days().asLiveData()
    @RequiresApi(Build.VERSION_CODES.O)
    val last30DaysIntake = repository.getDailyIntakeLast30Days().asLiveData()



    // -----------------------------------------
    // TODAY'S HYDRATION DATA
    // -----------------------------------------

    val todayLogs = repository.getTodayLogs().asLiveData()
    val todayTotal = todayLogs
    val last7DaysLogs = repository.getLast7DaysLogs().asLiveData()

    // -----------------------------------------
    // USER SETTINGS
    // -----------------------------------------

    val userSettings = repository.getUserSettings().asLiveData()

    fun saveUserSettings(settings: UserSettingsEntity) {
        viewModelScope.launch {
            repository.saveUserSettings(settings)
        }
    }

    // -----------------------------------------
    // WATER LOGGING ACTIONS
    // -----------------------------------------

    fun addWater(amount: Int) {
        viewModelScope.launch {
            repository.addWater(amount)
        }
    }


    fun resetToday() {
        viewModelScope.launch {
            repository.resetTodayLogs()
        }
    }


    fun generateFakeHistoryData() {
        viewModelScope.launch {
            // Last 30 days
            val now = System.currentTimeMillis()
            val dayMillis = 24L * 60 * 60 * 1000

            for (i in 1..30) {
                val timestamp = now - (i * dayMillis)
                val fakeAmount = (20..120).random()  // random between 20–120 oz

                repository.addWaterAtTime(fakeAmount, timestamp)
            }
        }
    }


    fun generateFakeData() {
        viewModelScope.launch {
            repository.generateFakeData()
        }
    }


}
