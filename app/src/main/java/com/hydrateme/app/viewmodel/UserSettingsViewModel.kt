// UserSettingsViewModel.kt
package com.hydrateme.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.hydrateme.app.data.model.UserSettingsEntity
import com.hydrateme.app.data.repository.UserSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

// ViewModel that holds the user's hydration settings (goal, units, reminders)
class UserSettingsViewModel(
    private val repository: UserSettingsRepository   // injected repository
) : ViewModel() {

    // Flow that always emits the current settings row (id = 1).
    // Later the UI will collect this with collectAsState().
    val settings: Flow<UserSettingsEntity> = repository.getSettings()

    // Called by the UI when the user updates their settings.
    fun saveSettings(newSettings: UserSettingsEntity) {
        // Launch a coroutine so we can call the suspend function safely.
        viewModelScope.launch {
            repository.saveSettings(newSettings)
        }
    }
}

// Factory so we can create UserSettingsViewModel with our repository.
class UserSettingsViewModelFactory(
    private val repository: UserSettingsRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserSettingsViewModel::class.java)) {
            return UserSettingsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}