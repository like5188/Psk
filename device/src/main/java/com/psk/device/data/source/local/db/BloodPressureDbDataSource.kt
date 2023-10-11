package com.psk.device.data.source.local.db

import com.psk.device.data.db.dao.BloodPressureDao
import com.psk.device.data.model.BloodPressure
import kotlinx.coroutines.flow.Flow

class BloodPressureDbDataSource(
    private val bloodPressureDao: BloodPressureDao
) {
    fun listenLatest(startTime: Long): Flow<BloodPressure?> {
        return bloodPressureDao.listenLatest(startTime)
    }

    suspend fun getByMedicalOrderId(medicalOrderId: Long): List<BloodPressure>? {
        return bloodPressureDao.getByMedicalOrderId(medicalOrderId)
    }

    suspend fun insert(data: BloodPressure) {
        bloodPressureDao.insert(data)
    }

}
