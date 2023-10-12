package com.psk.device.data.source.local.db

import com.psk.device.data.db.dao.HeartRateDao
import com.psk.device.data.model.HeartRate
import kotlinx.coroutines.flow.Flow

class HeartRateDbDataSource(
    private val heartRateDao: HeartRateDao
) {
    fun listenLatest(startTime: Long): Flow<HeartRate?> {
        return heartRateDao.listenLatest(startTime)
    }

    suspend fun getByMedicalOrderId(medicalOrderId: Long): List<HeartRate>? {
        return heartRateDao.getByMedicalOrderId(medicalOrderId)
    }

    suspend fun insert(data: HeartRate) {
        heartRateDao.insert(data)
    }

}
