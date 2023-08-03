package com.psk.recovery.data.source.db

import com.psk.recovery.data.db.dao.BloodPressureDao
import com.psk.recovery.data.model.BloodPressure
import kotlinx.coroutines.flow.Flow

class BloodPressureDbDataSource(
    private val bloodPressureDao: BloodPressureDao
) {
    fun listenLatest(startTime: Long): Flow<BloodPressure> {
        return bloodPressureDao.listenLatest(startTime)
    }

    suspend fun getByMedicalOrderId(medicalOrderId: Long): List<BloodPressure>? {
        return bloodPressureDao.getByMedicalOrderId(medicalOrderId)
    }

    suspend fun save(bloodPressure: BloodPressure) {
        bloodPressureDao.insert(bloodPressure)
    }

}
