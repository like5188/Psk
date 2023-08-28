package com.psk.device.data.source.local.db

import com.psk.device.data.db.dao.BloodPressureDao
import com.psk.device.data.model.BloodPressure
import com.psk.device.data.source.local.IDbDataSource
import kotlinx.coroutines.flow.Flow

class BloodPressureDbDataSource(
    private val bloodPressureDao: BloodPressureDao
) : IDbDataSource<BloodPressure> {
    override fun listenLatest(startTime: Long): Flow<BloodPressure?> {
        return bloodPressureDao.listenLatest(startTime)
    }

    override suspend fun getByMedicalOrderId(medicalOrderId: Long): List<BloodPressure>? {
        return bloodPressureDao.getByMedicalOrderId(medicalOrderId)
    }

    override suspend fun save(data: BloodPressure) {
        bloodPressureDao.insert(data)
    }

}
