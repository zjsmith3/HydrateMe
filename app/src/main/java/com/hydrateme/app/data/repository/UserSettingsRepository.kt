// UserSettingsRepository.kt
package com.hydrateme.app.data.repository

import com.hydrateme.app.data.dao.UserSettingsDao
import com.hydrateme.app.data.model.UserSettingsEntity
import kotlinx.coroutines.flow.Flow

// This class is the data manager for user settings (goal, units, reminders).
// The ViewModel will talk to this instead of using UserSettingsDao directly.
class UserSettingsRepository(
    private val userSettingsDao: UserSettingsDao   // DAO is injected via constructor
) {

    // Always returns a Flow of the one settings row (id = 1).
    // The UI/ViewModel can collect this and react to changes.
    fun getSettings(): Flow<UserSettingsEntity> {
        return userSettingsDao.getSettings()
    }

    // Save (insert or update) the settings row in the database.
    // We'll call this when the user changes their goal, units, etc.
    suspend fun saveSettings(settings: UserSettingsEntity) {
        userSettingsDao.saveSettings(settings)
    }
}
