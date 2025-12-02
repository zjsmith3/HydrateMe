package com.hydrateme.app.viewmodel

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
        viewModelScope.launch {
            repository.ensureDefaultSettings()
        }
    }

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
}
