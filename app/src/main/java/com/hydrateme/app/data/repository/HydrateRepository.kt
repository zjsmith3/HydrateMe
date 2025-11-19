package com.hydrateme.app.data.repository

import com.hydrateme.app.data.dao.UserSettingsDao
import com.hydrateme.app.data.dao.WaterLogDao
import com.hydrateme.app.data.model.UserSettingsEntity
import com.hydrateme.app.data.model.WaterLogEntity
import kotlinx.coroutines.flow.Flow

class HydrateRepository(
    private val waterLogDao: WaterLogDao,
    private val userSettingsDao: UserSettingsDao
) {

    // --------------------------
    // WATER LOG FUNCTIONS
    // --------------------------

    // Add a new drink event
    suspend fun addWater(amount: Int) {
        val log = WaterLogEntity(
            amount = amount,
            timestamp = System.currentTimeMillis()
        )
        waterLogDao.insertLog(log)
    }

    // Stream of today's logs
    fun getTodayLogs(): Flow<List<WaterLogEntity>> {
        return waterLogDao.getTodayLogs()
    }

    // Stream of last 7 days of logs
    fun getLast7DaysLogs(): Flow<List<WaterLogEntity>> {
        val sevenDaysAgo = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
        return waterLogDao.getLast7Days(sevenDaysAgo)
    }

    // All logs (for history screen later if you want)
    fun getAllLogs(): Flow<List<WaterLogEntity>> {
        return waterLogDao.getAllLogs()
    }

    // --------------------------
    // USER SETTINGS FUNCTIONS
    // --------------------------

    fun getUserSettings(): Flow<UserSettingsEntity> {
        return userSettingsDao.getSettings()
    }

    suspend fun saveUserSettings(settings: UserSettingsEntity) {
        userSettingsDao.saveSettings(settings)
    }
}
