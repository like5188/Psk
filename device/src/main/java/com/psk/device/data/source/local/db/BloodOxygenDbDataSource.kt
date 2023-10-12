package com.psk.device.data.source.local.db

import com.psk.device.data.db.dao.BloodOxygenDao
import com.psk.device.data.model.BloodOxygen
import kotlinx.coroutines.flow.Flow

class BloodOxygenDbDataSource(
    private val bloodOxygenDao: BloodOxygenDao
) {
    fun listenLatest(startTime: Long): Flow<BloodOxygen?> {
        return bloodOxygenDao.listenLatest(startTime)
    }

    suspend fun getByMedicalOrderId(medicalOrderId: Long): List<BloodOxygen>? {
        return bloodOxygenDao.getByMedicalOrderId(medicalOrderId)
    }

    suspend fun insert(data: BloodOxygen) {
        bloodOxygenDao.insert(data)
    }

}
