package com.psk.device.data.source.local

import com.psk.device.data.db.dao.BloodPressureDao
import com.psk.device.data.model.BloodPressure
import kotlinx.coroutines.flow.Flow

class BloodPressureDbDataSource(
    private val bloodPressureDao: BloodPressureDao
) {
    fun listenLatest(startTime: Long): Flow<BloodPressure?> {
        return bloodPressureDao.listenLatest(startTime)
    }

    suspend fun getByOrderId(orderId: Long): List<BloodPressure>? {
        return bloodPressureDao.getByOrderId(orderId)
    }

    suspend fun insert(data: BloodPressure) {
        bloodPressureDao.insert(data)
    }

}
