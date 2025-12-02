// WaterRepository.kt
package com.hydrateme.app.data.repository

import com.hydrateme.app.data.dao.WaterLogDao
import com.hydrateme.app.data.model.WaterLogEntity
import kotlinx.coroutines.flow.Flow

// This class is the "data manager" for water logs.
// The ViewModel will talk to this class instead of using WaterLogDao directly.
class WaterRepository(
    private val waterLogDao: WaterLogDao   // DAO is injected through the constructor
) {

    // Add a new drink entry with the given amount (in oz or ml)
    suspend fun addWater(amount: Int) {
        // We create a new WaterLogEntity with the amount.
        // timestamp will automatically be "now" because of the default value
        val log = WaterLogEntity(amount = amount)
        waterLogDao.insertLog(log)
    }

    // Get ALL logs sorted by newest first.
    // The UI/ViewModel can collect this Flow to observe changes.
    fun getAllLogs(): Flow<List<WaterLogEntity>> {
        return waterLogDao.getAllLogs()
    }

    // Get ONLY today's logs (using the DAO's query that filters to today).
    fun getTodayLogs(): Flow<List<WaterLogEntity>> {
        return waterLogDao.getTodayLogs()
    }

    // Get logs from the last 7 days.
    // `sevenDaysAgoMillis` is a timestamp cutoff that the caller passes in.
    fun getLast7Days(sevenDaysAgoMillis: Long): Flow<List<WaterLogEntity>> {
        return waterLogDao.getLast7Days(sevenDaysAgoMillis)
    }
}
