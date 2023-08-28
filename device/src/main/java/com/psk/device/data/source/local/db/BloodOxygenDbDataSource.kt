package com.psk.device.data.source.local.db

import com.psk.device.data.db.dao.BloodOxygenDao
import com.psk.device.data.model.BloodOxygen
import com.psk.device.data.source.local.IDbDataSource
import kotlinx.coroutines.flow.Flow

class BloodOxygenDbDataSource(
    private val bloodOxygenDao: BloodOxygenDao
) : IDbDataSource<BloodOxygen> {
    override fun listenLatest(startTime: Long): Flow<BloodOxygen?> {
        return bloodOxygenDao.listenLatest(startTime)
    }

    override suspend fun getByMedicalOrderId(medicalOrderId: Long): List<BloodOxygen>? {
        return bloodOxygenDao.getByMedicalOrderId(medicalOrderId)
    }

    override suspend fun save(data: BloodOxygen) {
        bloodOxygenDao.insert(data)
    }

}
