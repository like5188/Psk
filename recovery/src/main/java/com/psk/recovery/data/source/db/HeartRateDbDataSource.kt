package com.psk.recovery.data.source.db

import com.psk.recovery.data.db.dao.HeartRateDao
import com.psk.recovery.data.model.HeartRate
import kotlinx.coroutines.flow.Flow

class HeartRateDbDataSource(
    private val heartRateDao: HeartRateDao
) {
    fun listenLatest(startTime: Long): Flow<HeartRate> {
        return heartRateDao.listenLatest(startTime)
    }

    suspend fun getByMedicalOrderId(medicalOrderId: Long): List<HeartRate>? {
        return heartRateDao.getByMedicalOrderId(medicalOrderId)
    }

    suspend fun save(heartRate: HeartRate) {
        heartRateDao.insert(heartRate)
    }

}
