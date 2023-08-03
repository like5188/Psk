package com.psk.recovery.data.source.db

import com.psk.recovery.data.db.dao.BloodOxygenDao
import com.psk.recovery.data.model.BloodOxygen
import kotlinx.coroutines.flow.Flow

class BloodOxygenDbDataSource(
    private val bloodOxygenDao: BloodOxygenDao
) {
    fun listenLatest(startTime: Long): Flow<BloodOxygen> {
        return bloodOxygenDao.listenLatest(startTime)
    }

    suspend fun getByMedicalOrderId(medicalOrderId: Long): List<BloodOxygen>? {
        return bloodOxygenDao.getByMedicalOrderId(medicalOrderId)
    }

    suspend fun save(bloodOxygen: BloodOxygen) {
        bloodOxygenDao.insert(bloodOxygen)
    }

}
